package compass.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import compass.dao.IComponentDao;
import compass.status.TaskStatus;


@Service
public class ComponentDao implements IComponentDao{
	
	@Autowired
	DBClient db;

	@Override
	public Integer getComponentStatus(String clusterId, String component) {
		String componentName = clusterId + "-" + component;
		if (!db.getDb().exists(componentName)) {
			return TaskStatus.noExist;
		}
		return db.getDb().atomicInteger(componentName).get();
	}

	@Override
	public Integer getComponentStatus(String componentKey) {
		if (!db.getDb().exists(componentKey)) {
			return TaskStatus.noExist;
		}
		return db.getDb().atomicInteger(componentKey).get();
	}

	@Override
	public boolean setComponentStatus(String clusterId, String component,Integer status) {
		try {
			org.mapdb.Atomic.Integer dbStep = db.getDb().atomicInteger(clusterId + "-" + component);
			dbStep.set(status);
			db.getDb().commit();
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	

}
