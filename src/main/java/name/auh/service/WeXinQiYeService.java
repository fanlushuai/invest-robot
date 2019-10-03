package name.auh.service;

import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.bean.WxCpMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class WeXinQiYeService {

    @Autowired
    WxCpServiceImpl wxCpService;

    @Value("${env}")
    private String profile;

    @Value("${qywx.notify}")
    private boolean notify;

    public boolean sendMessage(String pattern, Object... params) {
        String message = StrFormatUtil.info(pattern, params);

        if (!notify) {
            return Boolean.FALSE;
        }

        if (!StringUtils.hasLength(message)) {
            log.warn("企业小助手消息没有内容什么鬼?");
            return Boolean.FALSE;
        }

        try {
            message = "【" + profile + "】" + message;
            wxCpService.messageSend(WxCpMessage.TEXT().agentId(0).toUser("@all").content(message).build());
        } catch (WxErrorException e) {
            //注意不能使用error级别。会导致死循环
            log.warn("企业小助手信息发送异常{}", e);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    public boolean sendMessageTo(String WeChatUser, String pattern, Object... params) {
        if (StringUtils.isEmpty(WeChatUser)) {
            log.warn("未配置微信号");
            return false;
        }
        String message = StrFormatUtil.info(pattern, params);

        if (!notify) {
            return Boolean.FALSE;
        }

        if (!StringUtils.hasLength(message)) {
            log.warn("企业小助手消息没有内容什么鬼?");
            return Boolean.FALSE;
        }

        try {
            message = "【" + profile + "】" + message;
            wxCpService.messageSend(WxCpMessage.TEXT().agentId(0).toUser(WeChatUser).content(message).build());
        } catch (WxErrorException e) {
            //注意不能使用error级别。会导致死循环
            log.warn("企业小助手信息发送异常{}", e);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }


}
