package name.auh.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.WxCpMessage;
import name.auh.config.WeChatConfig;

/**
 * 日志用微信的方式输出。
 * 配置error日志使用
 */
//@Component
//@ConditionalOnBean({WeXinQiYeService.class})
public class WeChatAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    public static final WxCpServiceImpl wxCpService = new WeChatConfig().wxCpInMemoryConfigStorage("WOrKJqkKdYTOYVAUxcd6tqsGHkHRWQCDvHivJzfBFgc", "wx7250b3b504d9d590");

    @Override
    protected void append(ILoggingEvent event) {
        if (!isStarted()) {
            return;
        }
        try {
            wxCpService.messageSend(WxCpMessage.TEXT().agentId(0).toUser("@all").content(event.getMessage()).build());
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }
}
