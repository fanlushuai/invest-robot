package name.auh.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("url")
@Getter
@Setter
public class URLConfig {

    private String host;

    private String login;

    private String overview;

    private String coupon;

    private String redReward;

    private String bankInvest;

    private String loans;

    private String loanConfirm;

    private String transfers;

}
