package compass.context;



/**
 * 记录会话上下文
 * @author GuXueZheng
 *
 */
public class Context {
	
	/**
	 * 进程
	 */
	private Process process;
	
	/**
	 * 是否已经执行
	 */
	private boolean executed;
	/**
	 * 文件
	 */
	private String filePath;
	/**
	 * 组件名
	 */
	private String component;
	
	public Context(Process process, boolean executed, String filePath, String component) {
		this.process = process;
		this.executed = executed;
		this.filePath = filePath;
		this.component = component;
	}
	public Process getProcess() {
		return process;
	}
	public void setProcess(Process process) {
		this.process = process;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getComponent() {
		return component;
	}
	public void setComponent(String component) {
		this.component = component;
	}
	public boolean isExecuted() {
		return executed;
	}
	public void setExecuted(boolean executed) {
		this.executed = executed;
	}
}
