package compass.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.mapdb.HTreeMap;

import compass.dao.IDBClient;

public class WriteLogThread extends Thread{
	
	private InputStream inputStream;
	
	private String taskID;
	
	private IDBClient db;

	public WriteLogThread(InputStream inputStream,String taskID,IDBClient db) {
		this.inputStream = inputStream;
		this.taskID = taskID;
		this.db = db;
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
