package name.auh.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Table(name = "t_transfer")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transfer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean notify;

    private Boolean onSale;

    @Id
    private String id;

    private String title;

    private String detailUrl;

    private String count;

    private Integer day;

    private BigDecimal rate;

    private Long updateTime;

    private Long createTime;

}
