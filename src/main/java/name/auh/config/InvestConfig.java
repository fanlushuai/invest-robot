package name.auh.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("invest")
@Data
public class InvestConfig {

    private InvestBorrowConfig borrow;

    private InvestTransferConfig transfer;

}
