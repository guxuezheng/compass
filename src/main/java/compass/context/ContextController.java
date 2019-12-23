package compass.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;


@Service
public class ContextController implements IContextController{
	
	private Map<String,Context> contextMap = new ConcurrentHashMap<String,Context>();
	
	@Override
	public Context getContext(String clusterId,String componentId) {
		return contextMap.get(clusterId + "-" + componentId);
	}
	
	/**
	 * 进程是否执行过
	 * @param clusterId
	 * @param componentId
	 * @return
	 */
	@Override
	public boolean processIsExecuted(String clusterId,String componentId) {
		Context context = contextMap.get(clusterId + "-" + componentId);
		//不存在上下文
		if(context == null) {
			return false;
		}
		return context.isExecuted();
	}
	
	/**
	 * 进程是否执行过
	 * @param clusterId
	 * @param componentId
	 * @return
	 */
	@Override
	public boolean processIsRunning(Context context) {
		Process process = context.getProcess();
		if(process == null) {
			return false;
		}
		return process.isAlive();
	}
	
	
	/**
	 * 添加上下文到进程
	 * @param clusterId
	 * @param componentId
	 * @return
	 */
	@Override
	public boolean addContext(String clusterId,String componentId,Context context) {
		try {
			this.contextMap.put(clusterId+ "-" + componentId, context);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public List<Context> getContextsWithCluster(String clusterId) {
		List<Context> contexts = new ArrayList<Context>();
		Set<String> keySet = contextMap.keySet();
		for (String key : keySet) {
			if(key.startsWith(clusterId + "-")) {
				contexts.add(contextMap.get(key));
			}
		}
		return contexts;
	}
}
