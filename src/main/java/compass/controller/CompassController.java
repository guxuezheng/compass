package compass.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import compass.bean.ClusterTask;
import compass.bean.Task;
import compass.dao.IClusterTaskDao;
import compass.dao.IDBClient;
import compass.dao.impl.DBClient;
import compass.runtime.Crontab;

@RestController
@RequestMapping("/compass")
public class CompassController {
	
	@Autowired
	IDBClient dbClient;
	
	@Autowired
	private IClusterTaskDao clusterTaskDao;
	Logger log = LogManager.getLogger(Crontab.class);
	
	 @GetMapping("/createCluster")
	 public JSONObject createCluster(@RequestParam String clusterId){
		 Map<String, String> clusterTaskMap = clusterTaskDao.createClusterTaskMap(clusterId, 1+"", getTestTask());
		 JSONObject json = (JSONObject)JSONObject.toJSON(clusterTaskMap);
		 log.info("集群任务:" + clusterId + " 创建成功");
		 return json;
	 }
	 
	 @GetMapping("/getClusterTaskStatus")
	 public JSONObject getClusterTaskStatus(@RequestParam String clusterId){
		 ClusterTask clusterTaskStatus = clusterTaskDao.getClusterTaskStatus(clusterId);
		 JSONObject json = (JSONObject)JSONObject.toJSON(clusterTaskStatus);
		 return json;
	 }
	 
	 @GetMapping("/getAllClusterSet")
	 public JSONArray getAllClusterSet(){
		 List<String> allCluster = clusterTaskDao.getAllClusterSet();
		 JSONArray json = (JSONArray) JSONArray.toJSON(allCluster);
		 return json;
	 }
	 
	 @GetMapping("/getLog")
	 public void getLog(@RequestParam String clusterId,@RequestParam String component) {
		 String taskId = clusterId + "-" + component + "-" + "logout";
		 int index = 0;
		 while(true) {
			 HTreeMap<String, String> hashMap = dbClient.getDb().hashMap(taskId);
			 for (int i = index; i < hashMap.size(); i++) {
				 System.err.println(hashMap.get(i+""));
				 index++;
		        }
			 try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {e.printStackTrace();}
		 }
	 }
	 
	 /**
	  * 测试方法,后续删除
	  * @return
	  */
	 private List<Task> getTestTask(){
		 List<Task> list = new ArrayList<Task>();
		 Task task1 = new Task("1", "harbor", null);
		 Task task2 = new Task("2", "docker", null);
		 Task task3 = new Task("3", "kubernetes", null);
		 Task task4 = new Task("4", "addons", null);
		 list.add(task1);
		 list.add(task2);
		 list.add(task3);
		 list.add(task4);
		 return list;
	 }
}
