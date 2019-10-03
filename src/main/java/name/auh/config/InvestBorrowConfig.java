package name.auh.config;

import lombok.Data;
import name.auh.enums.InvestStyleEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
@ConfigurationProperties("borrow")
@Data
public class InvestBorrowConfig {

    private boolean open;

    private boolean notify;

    private InvestStyleEnum level;

    private boolean useCoupon;

    private boolean useRedReward;

    private BigDecimal minInvestAmount;

    private String id;

    private BigDecimal minRate;

    private BigDecimal maxRate;

    private Integer maxDay;

    private Integer minDay;

    private boolean canTransfer;

}
