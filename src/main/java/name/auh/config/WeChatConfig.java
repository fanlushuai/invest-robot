package name.auh.config;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.WxCpInMemoryConfigStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class WeChatConfig {

    @Bean
    public WxCpServiceImpl wxCpInMemoryConfigStorage(@Value("${qywx.assistantSecret}")
                                                             String assistantSecret,
                                                     @Value("${qywx.corpId}")
                                                             String corpId) {
        WxCpInMemoryConfigStorage config = new WxCpInMemoryConfigStorage();
        config.setCorpId(corpId);      // 设置微信企业号的appid
        config.setCorpSecret(assistantSecret);  // 设置微信企业号的app corpSecret
        config.setAgentId(0);//企业小助手

        WxCpServiceImpl wxCpService = new WxCpServiceImpl();
        wxCpService.setWxCpConfigStorage(config);
        return wxCpService;
    }

}
