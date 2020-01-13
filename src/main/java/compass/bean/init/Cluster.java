package compass.bean.init;

import java.util.List;

public class Cluster {

	
	/**
	 * 默认镜像地址
	 */
	private String registryMirrors;
	
	/**
	 * 可信地址
	 */
	private String insecureRegistries;
	
	/**
	 * 所有主机地址
	 */
	private List<String> allHost;
	
	private Component docker;
	
	private Component harbor;
	
	private Component kubeMaster;
	
	private Component kubeNode;
	
	/**
	 * 组件列表
	 */
	private List<Component> components;
	
	public Component getDocker() {
		return docker;
	}

	public void setDocker(Component docker) {
		this.docker = docker;
	}

	public Component getHarbor() {
		return harbor;
	}

	public void setHarbor(Component harbor) {
		this.harbor = harbor;
	}

	public Component getKubeMaster() {
		return kubeMaster;
	}

	public void setKubeMaster(Component kubeMaster) {
		this.kubeMaster = kubeMaster;
	}

	public Component getKubeNode() {
		return kubeNode;
	}

	public void setKubeNode(Component kubeNode) {
		this.kubeNode = kubeNode;
	}

	public List<Component> getComponents() {
		return components;
	}

	public void setComponents(List<Component> components) {
		this.components = components;
	}

	public String getRegistryMirrors() {
		return registryMirrors;
	}

	public void setRegistryMirrors(String registryMirrors) {
		this.registryMirrors = registryMirrors;
	}

	public String getInsecureRegistries() {
		return insecureRegistries;
	}

	public void setInsecureRegistries(String insecureRegistries) {
		this.insecureRegistries = insecureRegistries;
	}

	public List<String> getAllHost() {
		return allHost;
	}

	public void setAllHost(List<String> allHost) {
		this.allHost = allHost;
	}
}
