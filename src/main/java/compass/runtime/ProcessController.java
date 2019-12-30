package compass.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import compass.context.Context;
import compass.context.IContextController;
import compass.dao.IClusterTaskDao;
import compass.dao.impl.DBClient;
import compass.status.TaskStatus;

@Service
public class ProcessController implements IProcessController {

	Logger log = LogManager.getLogger(ProcessController.class);

	@Autowired
	DBClient db;
	@Autowired
	IClusterTaskDao clusterTaskDao;
	@Autowired
	IContextController contextController;

	@Override
	public void startProcessTask() {
		// 集群列表
		Set<String> clusters = db.getDb().hashSet("clusters");
		for (String clusterId : clusters) {
			if (!clusterIsBuilding(clusterId)) {
				continue;
			}
			// 1. 获取当前任务
			String taskid = clusterTaskDao.getClusterCurrentTask(clusterId);
			// 2. 查看当前任务是否已经被标记为异常
			Integer taskStatus = clusterTaskDao.getTaskStatus(taskid);
			if (taskStatus == TaskStatus.fail) {
				log.info(taskid + " 任务状态码为 " + taskStatus + " 不进行处理...");
				break;
			}
			// 3. 查看任务进程是否执行过
			boolean processIsExecuted = contextController.processIsExecuted(clusterId, taskid);
			if (!processIsExecuted) {
				log.info(taskid + " 任务首次执行,拉起任务进程.");
				startStep(clusterId, taskid);
			}
		}
	}
	
	public synchronized boolean clusterIsBuilding(String clusterId) {
		HTreeMap<String, String> hashMap = db.getDb().hashMap(clusterId);
		String status = hashMap.get("status");
		if (status.equals(TaskStatus.wait + "")) {
			//首次执行 复制数据到临时位置
			AnsibleCommondUtils.cpAnsibleFileToTmp(clusterId);
			hashMap.put("status", TaskStatus.running + "");
			db.getDb().commit();
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
			while(true) {
				if(process.isAlive()) {
					Thread.sleep(1000);
					continue;
				}
				break;
			}
			return getComponentStatus(clusterId, component);
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
			setClusterStatus(clusterId, status);
			break;
		case TaskStatus.noExist:
			break;
		default:
			break;
		}
	}

	private void setClusterStatus(String clusterId, Integer status) {
		HTreeMap<String, String> hashMap = db.getDb().hashMap(clusterId);
		hashMap.put("status", status + "");
		db.getDb().commit();
	}

	private void runningToSuccess(String clusterId, String component) {
		System.err.println("集群:" + clusterId + ", 任务:" + component + "执行成功");
		org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(clusterId + "-" + component);
		dbStep.set(TaskStatus.success);
		db.getDb().commit();
	}

	private void runningToFail(String clusterId, String component) {
		System.err.println("拉起集群:" + clusterId + ", 任务:" + component + "执行失败");
		org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(clusterId + "-" + component);
		dbStep.set(TaskStatus.fail);
		db.getDb().commit();
	}

	public Context startStep(String clusterId, String component) {
		try {
			String cmd = AnsibleCommondUtils.getStartAnsiblePlaybookCMD(clusterId, component);
			Process process = Runtime.getRuntime().exec(cmd);
			log.info(cmd);
//			Process process = Runtime.getRuntime().exec("java -jar d://taskTest.jar " + component);
			Context context = new Context(process, true, cmd, component);
			// 异步推送日志到数据库
			new WriteLogToDBThread(process.getInputStream(), clusterId + "-" +component, db).start();
			org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(clusterId + "-" +component);
			dbStep.set(TaskStatus.running);
			db.getDb().commit();
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
		Set<String> clusters = db.getDb().hashSet("clusters");
		for (String clusterId : clusters) {
			if (!clusterIsBuilding(clusterId)) {
				continue;
			}
			// 1. 获取当前任务
			String taskid = clusterTaskDao.getClusterCurrentTask(clusterId);
			//是否执行过
			Context context = contextController.getContext(clusterId, taskid);
			//未执行过不检查,等待start任务拉起
			if(context == null) {
				break;
			}
			if (contextController.processIsRunning(context)) {
				log.info(taskid + " 进程处于执行中");
				break;
			}
			Integer componentStatus = checkComponentStatus(clusterId, taskid);
			setComponentStatus(clusterId, componentStatus, taskid);
		}
	}
	
	
	private Integer getComponentStatus(String clusterid,String component) {
		try {
			String path = AnsibleCommondUtils.getComponentStatusFilePath(clusterid, component);
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
