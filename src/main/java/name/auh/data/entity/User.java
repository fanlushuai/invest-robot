package name.auh.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.auh.config.AccountConfig;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;

@Table(name = "t_user")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    private String account;

    private String password;

    private BigDecimal amount;

    private Long createTime;

    private Long updateTime;

    /**
     * 投资配置
     */
    @Builder.Default
    private boolean login = false;

    private long loginTime;

    private Boolean investOpen;

    private BigDecimal investMinRate;

    private BigDecimal investMinMoney;

    @Transient
    AccountConfig accountConfig;

}
