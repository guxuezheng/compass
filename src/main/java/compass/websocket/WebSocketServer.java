package compass.websocket;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mapdb.HTreeMap;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import compass.dao.impl.DBClient;

@ServerEndpoint("/websocket/{clusterId}/{component}")
@Component
@Service
public class WebSocketServer {

	static Log log = LogFactory.getLog(WebSocketServer.class);
	// concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
	private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

	// 与某个客户端的连接会话，需要通过它来给客户端发送数据
	private Session session;

	// 接收cid
	private String clusterId = "";

	// 接收gid
	private String component = "";

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("clusterId") String clusterId,
			@PathParam("component") String component) {
		this.session = session;
		webSocketSet.add(this); // 加入set中
		this.clusterId = clusterId;
		this.component = component;
		try {
			log.info("新增链接:" + clusterId + "-" + component);
			sendComponentLog();
		} catch (IOException e) {
			log.error("websocket IO异常");
		}
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		webSocketSet.remove(this); // 从set中删除
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message
	 *            客户端发送过来的消息
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		// 群发消息
		for (WebSocketServer item : webSocketSet) {
			try {
				item.sendComponentLog();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("发生错误");
		error.printStackTrace();
	}

	/**
	 * 实现服务器主动推送
	 */
	public void sendComponentLog() throws IOException {
		String taskId = clusterId + "-" + component + "-" + "logout";
		int index = 0;
		while (true) {
			HTreeMap<String, String> hashMap = new DBClient().getDb().hashMap(taskId);
			for (int i = index; i < hashMap.size(); i++) {
				this.session.getBasicRemote().sendText(hashMap.get(i + "") + "<br/>");
				index++;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
