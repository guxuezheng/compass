package compass.runtime;

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

	Logger log = LogManager.getLogger(Crontab.class);
	
	@Autowired
	DBClient db;
	@Autowired
	IClusterTaskDao clusterTaskDao;
	@Autowired
	IContextController contextController;
	
	@Override
	public void startProcessTask() {
		//获取集群列表
		Set<String> clusters = db.getDb().hashSet("clusters");
		for (String clusterId : clusters) {
			if(!clusterIsBuilding(clusterId)) {
				continue;
			}
			log.info("监控到集群 : " + clusterId + " 状态处于构建中......");
			//1.	获取当前任务
			String taskid = clusterTaskDao.getClusterCurrentTask(clusterId);
			log.info("获取到当前任务 : " + taskid);
			//2.	查看当前任务是否已经被标记为异常
			Integer taskStatus = clusterTaskDao.getTaskStatus(taskid);
			log.info(taskid + " 任务状态码为 " + taskStatus);
			//异常状态,不进行处理
			if(taskStatus == TaskStatus.fail) {
				log.info(taskid + " 任务状态已被标记为异常,不进行处理.");
				break;
			}
			//3.	查看任务进程是否执行过
			boolean processIsExecuted = contextController.processIsExecuted(clusterId, taskid);
			Context context;
			if(!processIsExecuted) {
				log.info(taskid + " 任务首次执行,拉起任务进程.");
				context = startStep(clusterId, taskid);
			}else {
				context = contextController.getContext(clusterId, taskid);
			}
			//4.	进程是否存在，存在的情况下,不进行处理
			if(contextController.processIsRunning(context)) {
				log.info(taskid + " 任务进程处于执行中,不进行处理.");
				break;
			}
			Integer componentStatus = checkComponentStatus(clusterId, taskid);
			setStepStatus(clusterId, componentStatus,taskid);
		}
	}
	
	/**
	 * 根据集群id和任务id获取playbook-path
	 * @param clusterId
	 * @param taskid
	 * @return
	 */
	private String getPlayBookPath(String clusterId,String taskid) {
		return null;
	}
	
	
	
	public boolean clusterIsBuilding(String clusterId) {
		HTreeMap<String, String> hashMap = db.getDb().hashMap(clusterId);
		String status = hashMap.get("status");
		if(status.equals(TaskStatus.running + "")) {
			return true;
		}else {
			return false;
		}
	}
	
	
	/**
	 * @describe 查看当前步骤的执行结果
	 * @param clusterId
	 * @param step
	 * @return
	 */
	private Integer checkComponentStatus(String clusterId,String step) {
		
		return TaskStatus.success;
	}
	
	private void setStepStatus(String clusterId,Integer status,String step) {
		switch (status) {
		case TaskStatus.wait:
			break;
		case TaskStatus.running:
			break;
		case TaskStatus.success:
			runningToSuccess(clusterId,step);
			if(clusterTaskDao.hasNext(clusterId)) {
				clusterTaskDao.nextTask(clusterId);
			} else {
				clusterTaskDao.clusterComplete(clusterId);
			}
			break;
		case TaskStatus.fail:
			runningToFail(clusterId, step);
			setClusterStatus(clusterId, status);
			break;
		case TaskStatus.noExist:
			break;
		default:
			break;
		}
	}
	
	private void setClusterStatus(String clusterId,Integer status) {
		HTreeMap<String, String> hashMap = db.getDb().hashMap(clusterId);
		hashMap.put("status", status+"");
		db.getDb().commit();
	}
	
	
	/**
	 * 启动任务
	 * @param clusterId
	 * @param step
	 * @return
	 */
	private void startCommand(String clusterId,String step) {
		//触发命令
		System.err.println("集群:" + clusterId + ", 启动任务:" + step);
		org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(step);
		dbStep.set(TaskStatus.running);
		db.getDb().commit();
	}
	
	private void runningToSuccess(String clusterId,String step) {
		System.err.println("集群:" + clusterId + ", 任务:" + step + "执行成功");
		org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(step);
		dbStep.set(TaskStatus.success);
		db.getDb().commit();
	}
	
	private void runningToFail(String clusterId,String step) {
		System.err.println("拉起集群:" + clusterId + ", 任务:" + step + "执行失败");
		org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(step);
		dbStep.set(TaskStatus.fail);
		db.getDb().commit();
	}
	
	public Context startStep(String clusterId, String taskid) {
		try {
			String playBookPath = getPlayBookPath(clusterId, taskid);
			String[] split = taskid.split("-");
//			Process process = Runtime.getRuntime().exec("ansible-playbook " + playBookPath + "-i " + playBookPath);
			Process process = Runtime.getRuntime().exec("java -jar d://taskTest.jar" + " " + split[1]);
			Context context = new Context(process, true, playBookPath, taskid);
			//推送数据到数据库
			new WriteLogThread(process.getInputStream(), taskid,db).start();
			org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(taskid);
			dbStep.set(TaskStatus.running);
			db.getDb().commit();
			//加入上下文
			contextController.addContext(clusterId, taskid, context);
			return context;
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
