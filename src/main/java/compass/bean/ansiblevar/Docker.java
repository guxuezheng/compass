package compass.bean.ansiblevar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author GuXueZheng
 *
 */
public class Docker extends AnsibleVar{
	
	private String dataDir = "/data/docker";

	private List<String> registryMirrors = new ArrayList<String>();
	
	private List<String> insecureRegistries = new ArrayList<String>();
	
	public Map<String,String> keyAnnoations = new HashMap<String, String>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -3415479635564007515L;
		{
			put("registryMirrors", "默认仓库");
			put("insecureRegistries", "可信仓库");
			put("dataDir", "存储位置");
		}
	};
		
	public Map<String, String> getKeyAnnoations() {
		return keyAnnoations;
	}

	public void setKeyAnnoations(Map<String, String> keyAnnoations) {
		this.keyAnnoations = keyAnnoations;
	}

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
