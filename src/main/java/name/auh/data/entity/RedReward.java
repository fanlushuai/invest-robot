package name.auh.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "t_red_reward")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedReward {

    @Id
    public String id;

    public String hasUseAmount;

    public String rewardRate;

    public String isHasUse;

    public String userId;

    public String fullAmount;

    public String rewardName;

    public String useTimeStr;

    public String rewardId;

    public String fundPlanId;

    public String createTime;

    public String rewardRateStr;

    public String investId;

    public String useTime;

    public String borrowId;

    public String rewardAmount;

    public String endTime;

    public String enTimeByDay;


}
