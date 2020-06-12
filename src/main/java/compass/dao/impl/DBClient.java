package compass.dao.impl;

import java.io.File;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.springframework.stereotype.Service;

import compass.dao.IDBClient;


@Service
public class DBClient implements IDBClient{
	
	
	private static DB db;
	
	private final String defaultDBDir = "/data/mapdb";
	
//	private final String defaultDBDir = "F:\\mapdb\\mapdb";

	@Override
	public DB getDb() {
		if(db == null) {
			String dbDir = System.getenv("mapDBDir");
			if(dbDir == null) {
				dbDir = defaultDBDir;
			}
			db = DBMaker.fileDB(new File(dbDir)).make();
		}
		return db;
	}

	@Override
	public boolean isClose() {
		if(db ==null) {
			return true;
		}
		return db.isClosed();
	}
}
