package name.auh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("transfer")
@Data
public class InvestTransferConfig {

    private boolean notify;

}
