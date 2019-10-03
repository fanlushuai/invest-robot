package name.auh.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import name.auh.service.WeXinQiYeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 日志用微信的方式输出。
 * 配置error日志使用
 */
@Component
@ConditionalOnBean({WeXinQiYeService.class})
public class WeChatAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    @Autowired
    WeXinQiYeService weXinQiYeService;

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        System.out.println("*************");
        weXinQiYeService.sendMessage(event.getMessage());
    }
}
