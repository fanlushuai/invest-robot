package name.auh.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Table(name = "t_coupon_rate")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponRate {

    @Id
    public String id;

    public String summary;

    public String note;

    public String useTimeStr;

    public String expirTime;

    public String expirTimeStr;

    public String createTime;

    public String useTime;

    public String labelStr;

    public String label;

    public String type;

    public String status;

    public BigDecimal labelStrBigDecimal;

}
