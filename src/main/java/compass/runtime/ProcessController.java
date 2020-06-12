package compass.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import compass.bean.init.ComponentName;
import compass.context.Context;
import compass.context.IContextController;
import compass.dao.IClusterDao;
import compass.dao.IComponentDao;
import compass.dao.IDBClient;
import compass.status.TaskStatus;

@Service
public class ProcessController implements IProcessController {

	Logger log = LogManager.getLogger(ProcessController.class);

	@Autowired
	IDBClient db;
	@Autowired
	IClusterDao clusterTaskDao;
	@Autowired
	IComponentDao componentDao;
	@Autowired
	IContextController contextController;

	@Override
	public void startProcessTask() {
		// 集群列表
		List<String> clusters = clusterTaskDao.getAllClusterSet();
		for (String clusterId : clusters) {
			if (!clusterIsBuilding(clusterId)) {
				continue;
			}
			// 1. 获取当前任务
			String component = clusterTaskDao.getClusterCurrentTask(clusterId);
			// 2. 过滤不需要执行的任务
			if(componentIsEnvironment(component)) {
				break;
			}
			// 3. 查看当前任务是否已经被标记为异常
			Integer taskStatus = componentDao.getComponentStatus(component);
			if (taskStatus == TaskStatus.fail) {
				log.info(component + " 任务状态码为 " + taskStatus + " 不进行处理...");
				break;
			}
			// 3. 查看任务进程是否执行过
			boolean processIsExecuted = contextController.processIsExecuted(clusterId, component);
			if (!processIsExecuted) {
				log.info(component + " 任务首次执行,拉起任务进程.");
				startStep(clusterId, component);
			}
		}
	}
	
	public synchronized boolean clusterIsBuilding(String clusterId) {
		String status = clusterTaskDao.getClusterCurrentStatus(clusterId);
		if (status.equals(TaskStatus.wait + "")) {
			//首次执行 复制数据到临时位置
//			AnsibleCommondUtils.cpAnsibleFileToTmp(clusterId);
			clusterTaskDao.setClusterStatus(clusterId, TaskStatus.running);
			return true;
		} else if(status.equals(TaskStatus.running + "") ){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @describe 查看当前步骤的执行结果
	 * @param clusterId
	 * @param component
	 * @return
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private Integer checkComponentStatus(String clusterId, String component) {
		try {
			String checkAnsibleplaybookCmd = AnsibleCommondUtils.getCheckAnsibleplaybookCmd(clusterId, component);
			Process process = Runtime.getRuntime().exec(checkAnsibleplaybookCmd);
			new WriteLogToDBThread(process.getInputStream(), clusterId + "-" +component,db).start();
			while(true) {
				if(process.isAlive()) {
					Thread.sleep(1000);
					continue;
				}
				break;
			}
			return getComponentStatus(clusterId);
		}catch(Exception e) {
			e.printStackTrace();
			return TaskStatus.fail;
		}
	}

	private void setComponentStatus(String clusterId, Integer status, String component) {
		switch (status) {
		case TaskStatus.wait:
			break;
		case TaskStatus.running:
			break;
		case TaskStatus.success:
			runningToSuccess(clusterId, component);
			if (clusterTaskDao.hasNext(clusterId)) {
				clusterTaskDao.nextTask(clusterId);
			} else {
				clusterTaskDao.clusterComplete(clusterId);
			}
			break;
		case TaskStatus.fail:
			runningToFail(clusterId, component);
			clusterTaskDao.setClusterStatus(clusterId, status);
			break;
		case TaskStatus.noExist:
			break;
		default:
			break;
		}
	}

	private void runningToSuccess(String clusterId, String component) {
		log.error("集群:" + clusterId + " 过程中, 组件 : " + component + "执行成功");
		componentDao.setComponentStatus(clusterId, component, TaskStatus.success);
	}

	private void runningToFail(String clusterId, String component) {
		log.error("部署集群: " + clusterId + " 过程中, 组件 : " + component + "执行失败");
		componentDao.setComponentStatus(clusterId, component, TaskStatus.fail);;
	}

	public Context startStep(String clusterId, String component) {
		try {
			String cmd = AnsibleCommondUtils.getStartAnsiblePlaybookCMD(clusterId, component);
			Process process = Runtime.getRuntime().exec(cmd);
			log.info(cmd);
//			Process process = Runtime.getRuntime().exec("java -jar d://taskTest.jar " + component);
			Context context = new Context(process, true, cmd, component);
			// 异步推送日志到数据库
			new WriteLogToDBThread(process.getInputStream(), clusterId + "-" +component,db).start();
			componentDao.setComponentStatus(clusterId, component, TaskStatus.running);
			// 加入上下文
			contextController.addContext(clusterId, component, context);
			return context;
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void checkProcessResult() {
		List<String> clusters = clusterTaskDao.getAllClusterSet();
		for (String clusterId : clusters) {
			if (!clusterIsBuilding(clusterId)) {
				continue;
			}
			String component = clusterTaskDao.getClusterCurrentTask(clusterId);
			//判断任务是否属于基础环境检查
			if(componentIsEnvironment(component)) {
				Integer componentStatus = checkComponentStatus(clusterId, component);
				setComponentStatus(clusterId, componentStatus, component);
				return;
			}
			//获取当前组件上下文
			Context context = contextController.getContext(clusterId, component);
			//空表示未执行过,等待start任务拉起
			if(context == null) {
				break;
			}
			if (contextController.processIsRunning(context)) {
				log.info(component + " 进程处于执行中");
				break;
			}
			Integer componentStatus = checkComponentStatus(clusterId, component);
			setComponentStatus(clusterId, componentStatus, component);
		}
	}
	
	private boolean componentIsEnvironment(String component) {
		if(ComponentName.environment.equals(component)) {
			return true;
		}else {
			return false;
		}
	}
	
	private Integer getComponentStatus(String clusterid) {
		try {
			String path = AnsibleCommondUtils.getComponentStatusFilePath(clusterid);
			log.info("cat " + path);
			File file = new File(path);
			FileReader reader = new FileReader(file);
			BufferedReader bReader = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder();
			String s = "";
			while ((s =bReader.readLine()) != null) {
				sb.append(s);
				System.out.println(s);
			}
			bReader.close();
			String status = sb.toString();
			if(status.equals("0")) {
				return TaskStatus.success;
			}else {
				return TaskStatus.fail;
			}
		}catch(Exception e) {
			e.printStackTrace();
			return TaskStatus.fail;
		}
	}
}
