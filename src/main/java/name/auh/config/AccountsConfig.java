package name.auh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties("accounts")
@Data
public class AccountsConfig {

    List<AccountConfig> account;

}
