package compass.dao;

public interface IComponentDao {
	
	
	Integer getComponentStatus(String clusterId,String component);
	
	Integer getComponentStatus(String componentKey);
	
	boolean setComponentStatus(String clusterId,String component,Integer status);
//	/**
//	 * @describe: 根据组件和集群id,获取运行状态
//	 * @param clusterId
//	 * @param type
//	 * @return
//	 */
//	@Override
//	public Integer getTaskStatus(String clusterId, String type) {
//		String cluterMapName = clusterId + "-" + type;
//		return getTaskStatus(cluterMapName);
//	}
//
//	/**
//	 * @describe: 根据组件和集群id,获取运行状态
//	 * @param clusterId
//	 * @param type
//	 * @return
//	 */
//	@Override
//	public Integer getTaskStatus(String mapKey) {
//		if (!db.getDb().exists(mapKey)) {
//			return TaskStatus.noExist;
//		}
//		return db.getDb().atomicInteger(mapKey).get();
//	}

}
