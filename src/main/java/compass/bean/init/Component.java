package compass.bean.init;

import java.util.List;

/**
 * @author GuXueZheng
 * @2020年1月8日
 * @Description 
 */
public class Component {
	
	/**
	 * 
	 */
	
	private List<String> ips;
	
	private String dockerDir;
	/**
	 * kubelet 存储位置
	 */
	private String kubeletDir;
	
	private String SvcCIDR;
	
	private String PodCIDR;

	public List<String> getIps() {
		return ips;
	}

	public void setIps(List<String> ips) {
		this.ips = ips;
	}

	public String getDockerDir() {
		return dockerDir;
	}

	public void setDockerDir(String dockerDir) {
		this.dockerDir = dockerDir;
	}

	public String getKubeletDir() {
		return kubeletDir;
	}

	public void setKubeletDir(String kubeletDir) {
		this.kubeletDir = kubeletDir;
	}

	public String getSvcCIDR() {
		return SvcCIDR;
	}

	public void setSvcCIDR(String svcCIDR) {
		SvcCIDR = svcCIDR;
	}

	public String getPodCIDR() {
		return PodCIDR;
	}

	public void setPodCIDR(String podCIDR) {
		PodCIDR = podCIDR;
	}
}
