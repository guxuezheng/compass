package compass.bean.ansiblevar;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author GuXueZheng
 *
 */
public class Docker {
	
	private String dataDir = "/data/docker";

	private List<String> registryMirrors = new ArrayList<String>();
	
	private List<String> insecureRegistries = new ArrayList<String>();

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public List<String> getRegistryMirrors() {
		return registryMirrors;
	}

	public void setRegistryMirrors(List<String> registryMirrors) {
		this.registryMirrors = registryMirrors;
	}

	public List<String> getInsecureRegistries() {
		return insecureRegistries;
	}

	public void setInsecureRegistries(List<String> insecureRegistries) {
		this.insecureRegistries = insecureRegistries;
	}

	public Docker(String dataDir, List<String> registryMirrors, List<String> insecureRegistries) {
		this.dataDir = dataDir;
		this.registryMirrors = registryMirrors;
		this.insecureRegistries = insecureRegistries;
	}
	
	public Docker() {
	}
}
