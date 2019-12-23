package compass.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import compass.bean.ClusterTask;
import compass.bean.Task;
import compass.dao.IClusterTaskDao;
import compass.runtime.Crontab;
import compass.status.TaskStatus;

/**
 * @author GuXueZheng
 */
@Service
public class ClusterTaskDao implements IClusterTaskDao {

	
	Logger log = LogManager.getLogger(Crontab.class);
	/**
	 * 当前位置
	 */
	private String currentKey = "current";
	
	/**
	 * 集群状态
	 */
	private String clusterStatusKey = "status";

	/**
	 * 任务数量
	 */
	private String taskSizeKey = "taskSize";

	/**
	 * 单一集群内的数据格式包含如下: 
	 * 	1. 全局任务,key值为 $clusterId-cluster 记录包含执行任务步骤,集群所有状态列表
	 *  # $clusterId-etcd: etcd组件 
	 *  # $clusterId-docker: docker组件 
	 *  # $clusterId-harbor: harbor组件 
	 *  # $clusterId-kubernetes: k8s组件 
	 *  # $clusterId-gfs: gfs组件 
	 *  # $clusterId-ceph: ceph组件 
	 *  # current: 当前位置 
	 *  # taskSize: 任务长度 
	 *  数据格式为: 
	 * 	# $clusterId-etcd: 0 or 1 or 2 or 3 表示步骤
	 * 	2. 单个任务状态,key值为$clusterId-$componentId
	 * 	$componentId根据全局的集群key获取 map中的value格式为0/1/2/3/4的数字, 
	 * 	0: 任务未执行 1： 任务执行中 2： 任务执行成功 3: 任务执行失败 4: 不存在
	 */

	@Autowired
	DBClient db;

	/**
	 * 根据集群id创建集群任务
	 * 
	 */
	@Override
	public Map<String, String> createClusterTaskMap(String clusterId, String current, List<Task> tasks) {
		String cluterMapName = clusterId + "-cluster";
		boolean insertSuccess = insertCluserToClusterSet(clusterId);
		if(!insertSuccess) {
			return null;
		}
		if (db.getDb().exists(cluterMapName)) {
			return db.getDb().hashMap(cluterMapName);
		}
		HTreeMap<String, String> clusterMap = db.getDb().hashMap(cluterMapName);
		clusterMap.put(currentKey, current);
		clusterMap.put(taskSizeKey, tasks.size() + "");
		clusterMap.put(clusterStatusKey, TaskStatus.running + "");
		for (Task task : tasks) {
			String component = task.getComponent();
			String step = task.getStep();
			clusterMap.put(clusterId + "-" + component, step);
			createComponentTask(clusterId, task.getComponent());
		}
		db.getDb().commit();
		return clusterMap;
	}
	
	
	private void createComponentTask(String clusterId,String component) {
		org.mapdb.Atomic.Integer componentTask = db.getDb().atomicInteger(clusterId + "-" + component);
		componentTask.addAndGet(TaskStatus.wait);
		db.getDb().commit();
	}
	
	/**
	 * @describe 插入全局集群任务列表
	 * @param clusterId
	 * @return
	 */
	private boolean insertCluserToClusterSet(String clusterId) {
		try {
			String clustersName = "clusters";
			String cluterName = clusterId + "-cluster";
			Set<String> hashSet = db.getDb().hashSet(clustersName);
			hashSet.add(cluterName);
			db.getDb().commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 根据集群id 获取集群全局任务
	 */
	@Override
	public Map<String, String> getGlobalTaskMap(String clusterId) {
		String cluterMapName = clusterId + "-cluster";
		HTreeMap<String, String> clusterMap = db.getDb().hashMap(cluterMapName);
		return clusterMap;
	}

	/**
	 * 获取当前正在执行的任务
	 */
	@Override
	public String getClusterCurrentTask(String clusterId) {
		HTreeMap<String, String> clusterMap = db.getDb().hashMap(clusterId);
		String current = clusterMap.get(currentKey);
		Set<String> keySet = clusterMap.keySet();
		for (String key : keySet) {
			if(key.equals(currentKey) || key.equals(clusterStatusKey) || key.equals(taskSizeKey)) {
				continue;
			}
			if(clusterMap.get(key).equals(current)) {
				return key;
			}
		}
		return null;
	}
	
	/**
	 * 获取集群当前状态 0: 等待  1：执行中  2:完成 对应taskstatus
	 */
	@Override
	public String getClusterCurrentStatus(String clusterId) {
		HTreeMap<String, String> clusterMap = db.getDb().hashMap(clusterId);
		String status = clusterMap.get(clusterStatusKey);
		return status;
	}

	/**
	 * @describe: 根据组件和集群id,获取运行状态
	 * @param clusterId
	 * @param type
	 * @return
	 */
	@Override
	public Integer getTaskStatus(String clusterId, String type) {
		String cluterMapName = clusterId + "-" + type;
		return getTaskStatus(cluterMapName);
	}

	/**
	 * @describe: 根据组件和集群id,获取运行状态
	 * @param clusterId
	 * @param type
	 * @return
	 */
	@Override
	public Integer getTaskStatus(String mapKey) {
		if (!db.getDb().exists(mapKey)) {
			return TaskStatus.noExist;
		}
		return db.getDb().atomicInteger(mapKey).get();
	}

	/**
	 * @describe: 获取下一次任务,同时主流程向下执行,如果没有任务,获取为null
	 * @return noexists 为null,
	 */
	@Override
	public String nextTask(String clusterId) {
		if (!db.getDb().exists(clusterId)) {
			return null;
		}
		HTreeMap<String, String> hashMap = db.getDb().hashMap(clusterId);
		String currentStr = hashMap.get(currentKey);
		String taskSizeStr = hashMap.get(taskSizeKey);

		int current = Integer.parseInt(currentStr);
		int taskSize = Integer.parseInt(taskSizeStr);
		if (current < taskSize) {
			current++;
		} else {
			return null;
		}
		String nextComponent = null;
		Set<String> keySet = hashMap.keySet();
		for (String key : keySet) {
			Integer step = Integer.parseInt(hashMap.get(key));
			if (step == current) {
				key = key.replace(clusterId + "-", "");
				nextComponent = key;
			}
		}
		hashMap.put(currentKey, current + "");
		db.getDb().commit();
		return nextComponent;
	}
	
	/**
	 * @describe: 判断是否存在向下执行任务
	 * @return 
	 */
	@Override
	public boolean hasNext(String clusterId) {
		if (!db.getDb().exists(clusterId)) {
			return false;
		}
		HTreeMap<String, String> hashMap = db.getDb().hashMap(clusterId);
		String currentStr = hashMap.get(currentKey);
		String taskSizeStr = hashMap.get(taskSizeKey);

		int current = Integer.parseInt(currentStr);
		int taskSize = Integer.parseInt(taskSizeStr);

		if (current == taskSize) {
			return false;
		} else {
			return true;
		}
	}
	

	/**
	 * @describe: 获取主流程的流程状态
	 * @param clusterId
	 * @return
	 */
	public ClusterTask getClusterTaskStatus(String clusterId) {
		ClusterTask clusterTask = new ClusterTask();
		String cluterMapName = clusterId + "-cluster";
		if (!db.getDb().exists(cluterMapName)) {
			return null;
		}
		HTreeMap<String, String> hashMap = db.getDb().hashMap(cluterMapName);
		Set<String> keySet = hashMap.keySet();
		clusterTask.setTaskSize(hashMap.get(taskSizeKey));
		clusterTask.setCurrent(hashMap.get(currentKey));
		List<Task> tasks = new ArrayList<Task>();
		for (String key : keySet) {
			if (key.equals(taskSizeKey) || key.equals(currentKey) || key.equals(clusterStatusKey)) {
				continue;
			}
			Integer status = getTaskStatus(key);
			Task task = new Task(hashMap.get(key), key, status + "");
			tasks.add(task);
		}
		clusterTask.setTasks(tasks);
		return clusterTask;
	}

	@Override
	public boolean isExist(String clusterId) {
		String cluterMapName = clusterId + "-cluster";
		return db.getDb().exists(cluterMapName);
	}
	
	@Override
	public List<String> getAllClusterSet() {
		Set<String> hashSet = db.getDb().hashSet("clusters");
		List<String> list = new ArrayList<String>();
		for (String clusterId : hashSet) {
			list.add(clusterId.replace("-cluster", ""));
		}
		return list;
	}
	
	@Override
	public void clusterComplete(String clusterId){
		HTreeMap<Object, Object> hashMap = db.getDb().hashMap(clusterId);
		hashMap.put(clusterStatusKey, TaskStatus.success + "");
		db.getDb().commit();
		log.info("恭喜您, 集群: " + clusterId + "部署完毕,请开始使用吧!");
	}
}
