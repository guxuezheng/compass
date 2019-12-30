package compass.runtime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@EnableScheduling
public class Crontab {

	
	Logger log = LogManager.getLogger(Crontab.class);
	@Autowired
	IProcessController processController;
	
	
	/**
	 * 每分钟检测一次
	 */
	@Scheduled(cron = "0 */1 * * * ?")
    private void runTask() {
		processController.startProcessTask();
    }
	
	/**
	 * 每20秒检测一次
	 */
	@Scheduled(cron = "0/20 * * * * ? ")
    private void checkTask() {
		processController.checkProcessResult();
    }
	
	
}
