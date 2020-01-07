package compass.dao;

import java.util.List;
import java.util.Map;

import compass.bean.ClusterTask;
import compass.bean.Task;

public interface IClusterDao {
	
	Map<String,String> createClusterTaskMap(String clusterId,String current,List<Task> tasks);
	
	Map<String,String> getGlobalTaskMap(String clusterId);
	
	String getClusterCurrentTask(String clusterId);
	
	String nextTask(String clusterId);
	
	ClusterTask getClusterTaskStatus(String clusterId);
	
	boolean isExist(String clusterId);
	
	List<String> getAllClusterSet();
	
	boolean hasNext(String clusterId);
	
	void clusterComplete(String clusterId);
	
	void setClusterStatus(String clusterId,Integer status);
	
	String getClusterCurrentStatus(String clusterId);
}
