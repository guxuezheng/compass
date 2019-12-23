package compass.bean;

public class Task {

	
	private String step;
	
	private String component;
	
	private String status;

	public String getStep() {
		return step;
	}

	public void setStep(String step) {
		this.step = step;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Task(String step, String component, String status) {
		this.step = step;
		this.component = component;
		this.status = status;
	}

	public Task() {
	}
}
