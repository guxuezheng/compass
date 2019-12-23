package compass.context;

import java.util.List;

public interface IContextController {

	
	Context getContext(String clusterId,String componentId);
	
	List<Context> getContextsWithCluster(String clusterId);
	
	boolean processIsExecuted(String clusterId,String componentId);
	
	boolean addContext(String clusterId,String componentId,Context context);
	
	boolean processIsRunning(Context context);
}
