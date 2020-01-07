package compass.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.mapdb.HTreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import compass.dao.impl.DBClient;

@Service
public class WriteLogToDBThread extends Thread{
	
	private InputStream inputStream;
	
	private String taskID;
	
	@Autowired
	DBClient db;

	public WriteLogToDBThread(InputStream inputStream,String taskID) {
		this.inputStream = inputStream;
		this.taskID = taskID;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public String getTaskID() {
		return taskID;
	}
	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}
	@Override
	public void run() {
		writeLog();
	}
	
	private void writeLog() {
		try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
            	HTreeMap<String, String> hashMap = db.getDb().hashMap(taskID + "-logout");
            	hashMap.put(index + "", line);
            	index ++;
            	db.getDb().commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
	}

}
