package name.auh.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import name.auh.enums.BorrowTypeEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Table(name = "t_borrow")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Borrow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否通知微信标志位
     */
    private Boolean notify;

    @Builder.Default
    private Boolean canTransfer = false;

    /**
     * 定点刷新
     */
    @Column(name = "upSpeedFlush", columnDefinition = "boolean default 0")
    @Builder.Default
    private Boolean upSpeedFlush = false;

    /**
     * 开卖时间
     */
    @Temporal(TemporalType.DATE)
    private Date onSaleTime;

    /**
     * 开卖
     */
    @Column(name = "onSale", columnDefinition = "boolean default 0")
    @Builder.Default
    private Boolean onSale = false;

    @Id
    private String id;

    private String realId;

    private BorrowTypeEnum type;

    private String title;

    private String detailUrl;

    private BigDecimal amount;

    private BigDecimal leftAmount;

    private Integer day;

    private BigDecimal rate;

    private Long updateTime;

    private Long createTime;

}
