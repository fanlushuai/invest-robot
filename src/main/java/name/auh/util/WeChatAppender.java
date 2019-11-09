package name.auh.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.WxCpMessage;
import name.auh.config.WeChatConfig;

import java.util.concurrent.TimeUnit;

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
            if (LimitUtil.overRate(event.getMessage(), 1, 5, TimeUnit.MINUTES)) {
                return;
            }

            String fatLog = parseFatLogForQYWX(event);

            wxCpService.messageSend(WxCpMessage.TEXT().agentId(0).toUser("@all").content(fatLog).build());

        } catch (WxErrorException e) {
            e.printStackTrace();
        }
    }

    private String parseFatLogForQYWX(ILoggingEvent event) {
        if (event == null) {
            return "";
        }

        //日志事件的最终消息，需要格式化一下。位置：org.slf4j.helpers.MessageFormatter.arrayFormat(java.lang.String, java.lang.Object[], java.lang.Throwable)
        //所以使用的时候 event.getMessage() 简单不完整的消息;  event.getFormattedMessage() 带参数的完整消息
        //如果需要打印具体的栈信息，也是可以的
        StringBuilder fatLog = new StringBuilder("\uD83D\uDE2D[MSG]\uD83D\uDE2D\n").append(event.getFormattedMessage());
        if (event.getThrowableProxy() == null) {
            return fatLog.toString();
        }

        //小坑，本来想发送markdown消息。但是微信企业版app才支持这种消息格式。使用普通微信接受企业号消息是不支持的。尴尬。
        //无奈，只能使用emoji来替代格式了。
        fatLog.append("\n\n").append("\uD83D\uDE2C[MSG_CLASS]\uD83D\uDE2C\n").append(event.getThrowableProxy().getClassName());
        fatLog.append("\n\n").append("\uD83D\uDC40[Stack-Top10]\uD83D\uDC40");
        for (int i = 0, forLength = (Math.min(event.getThrowableProxy().getStackTraceElementProxyArray().length, 10)); i < forLength; i++) {
            fatLog.append("\n").append(event.getThrowableProxy().getStackTraceElementProxyArray()[i].getSTEAsString());
        }
        return fatLog.toString();
    }

}
