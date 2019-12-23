package compass.dao;

import org.mapdb.DB;

public interface IDBClient {
	
	DB getDb();
	
	boolean isClose();
}
