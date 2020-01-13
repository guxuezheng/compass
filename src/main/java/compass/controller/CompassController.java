package compass.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import compass.bean.runtime.ClusterTask;
import compass.bean.runtime.Task;
import compass.dao.IClusterDao;
import compass.dao.IDBClient;
import compass.runtime.Crontab;

@RestController
@RequestMapping("/compass")
public class CompassController {

	@Autowired
	IDBClient dbClient;

	@Autowired
	private IClusterDao clusterTaskDao;
	Logger log = LogManager.getLogger(Crontab.class);

	@GetMapping("/createCluster")
	public JSONObject createCluster(@RequestParam String clusterId, @RequestParam String components) {
		System.out.println(components);
		Map<String, String> clusterTaskMap = clusterTaskDao.createClusterTaskMap(clusterId, 1 + "", getTestTask());
		JSONObject json = (JSONObject) JSONObject.toJSON(clusterTaskMap);
		log.info("集群任务:" + clusterId + " 创建成功");
		return json;
	}

	@GetMapping("/getClusterTaskStatus")
	public JSONObject getClusterTaskStatus(@RequestParam String clusterId) {
		ClusterTask clusterTaskStatus = clusterTaskDao.getClusterTaskStatus(clusterId);
		JSONObject json = (JSONObject) JSONObject.toJSON(clusterTaskStatus);
		return json;
	}

	@GetMapping("/getAllClusterSet")
	public JSONArray getAllClusterSet() {
		List<String> allCluster = clusterTaskDao.getAllClusterSet();
		JSONArray json = (JSONArray) JSONArray.toJSON(allCluster);
		return json;
	}

	// @ResponseBody
	// @RequestMapping(value = "/ansiblevar/docker", method = RequestMethod.POST,
	// produces = "application/json;charset=UTF-8")
	// public JSONObject getByJSON(@RequestBody JSONObject jsonParam) {
	//
	//
	//
	// return null;
	// }

	/**
	 * 获取所有componentbean的代码格式
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 * @throws IOException 
	 */
	@GetMapping("/getComponentAnsibleBean")
	public JSONObject getComponentAnsibleVar() throws ClassNotFoundException, IOException {
		// List<String> allCluster = clusterTaskDao.getAllClusterSet();
		// JSONArray json = (JSONArray) JSONArray.toJSON(allCluster);
		
		JSONObject json = (JSONObject) JSONObject.toJSON(new Object());
		return json;
	}

	/**
	 * 测试方法,后续删除
	 * 
	 * @return
	 */
	private List<Task> getTestTask() {
		List<Task> list = new ArrayList<Task>();
		Task task1 = new Task("1", "docker", null);
//		Task task2 = new Task("2", "harbor", null);
//		Task task3 = new Task("3", "kubernetes", null);
//		Task task4 = new Task("4", "addons", null);
		list.add(task1);
//		list.add(task2);
//		list.add(task3);
//		list.add(task4);
		return list;
	}
}
