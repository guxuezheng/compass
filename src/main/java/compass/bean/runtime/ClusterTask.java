package compass.bean.runtime;

import java.util.List;

/**
 * 
 * @author GuXueZheng
 * @describe 集群全局任务类
 */
public class ClusterTask {
	
	private String taskSize;
	
	private String current;
	
	private List<Task> tasks;
	
	private String status;
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTaskSize() {
		return taskSize;
	}

	public void setTaskSize(String taskSize) {
		this.taskSize = taskSize;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public ClusterTask(String taskSize, String current, List<Task> tasks) {
		this.taskSize = taskSize;
		this.current = current;
		this.tasks = tasks;
	}

	public ClusterTask() {
	}
}
