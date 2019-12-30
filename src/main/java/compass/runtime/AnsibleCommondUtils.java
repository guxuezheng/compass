package compass.runtime;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AnsibleCommondUtils {

	static Logger log = LogManager.getLogger(AnsibleCommondUtils.class);
	
	private static String tmpRootDir = "/data";
	
	public static String getCheckAnsibleplaybookCmd(String clusterid,String component) {
		String playbookPath = tmpRootDir + "/" + clusterid + "/" + "check_" + component + ".yml";
		String playbookVarPath = tmpRootDir + "/" + clusterid + "/inventory/hosts";
		String cmd = "ansible-playbook " + playbookPath + " -i " + playbookVarPath;
		log.info(cmd);
		return cmd;
	}
	
	
	private static String getStartAnsibleplaybookPath(String clusterid,String component) {
		return tmpRootDir + "/" + clusterid + "/" + component + ".yml";
	}
	
	private static String getStartAnsibleplaybookVarPath(String clusterid,String component) {
		return tmpRootDir + "/" + clusterid + "/inventory/hosts";
	}
	
	public static String getStartAnsiblePlaybookCMD(String clusterid,String component) {
		String playbookPath = getStartAnsibleplaybookPath(clusterid, component);
		String playbookVarPath = getStartAnsibleplaybookVarPath(clusterid, component);
		return "ansible-playbook " + playbookPath + " -i " + playbookVarPath;
	}
	
	public static String getComponentStatusFilePath(String clusterid,String component) {
		String path = tmpRootDir + "/" + clusterid + "/roles/check/" + component + "/result/" + component + "_status";
		return path;
	}
	
	public static boolean cpAnsibleFileToTmp(String clusterid) {
		File file = new File("");
		String filePath;
		try {
			filePath = file.getCanonicalPath();
			String cmd = "cp -r " + filePath + "/* " + tmpRootDir + "/" + clusterid;
			String[] cmds = {"/bin/sh","-c",cmd};
			log.info(cmd);
			Process process = Runtime.getRuntime().exec(cmds);
			while(true) {
				if(process.isAlive()) {
					Thread.sleep(1000);
					continue;
				}
				break;
			}
			return true;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	}
}
