package name.auh.task;

import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.Request;
import lombok.extern.slf4j.Slf4j;
import name.auh.bean.Event;
import name.auh.bean.MyMap;
import name.auh.config.AccountConfig;
import name.auh.config.AccountsConfig;
import name.auh.config.URLConfig;
import name.auh.data.entity.Borrow;
import name.auh.data.entity.CouponRate;
import name.auh.data.entity.User;
import name.auh.data.repository.BorrowRepository;
import name.auh.data.repository.CouponRateRepository;
import name.auh.data.repository.RedRewardRepository;
import name.auh.data.repository.UserRepository;
import name.auh.srawlers.UserCrawler;
import name.auh.util.InvestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static name.auh.consts.Consts.initialDelay;

@ConditionalOnProperty(prefix = "task", name = "invest", havingValue = "open", matchIfMissing = false)
@Slf4j
@Component
public class InvestTask {

    @Autowired
    private URLConfig urlConfig;

    @Autowired
    private AccountsConfig accountsConfig;

    @Autowired
    private CouponRateRepository couponRateRepository;

    @Autowired
    private RedRewardRepository redRewardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    /**
     * 监控本地数据是否符合投资条件
     */
    @Scheduled(initialDelay = initialDelay, fixedRate = 100)
    public void investDbMonitor() {
        //todo 投标最低利率限制
        Iterable<Borrow> borrows = borrowRepository.findAll();
        List<User> users = userRepository.findByLoginIsTrue();
        List<Borrow> canInvestBorrowList = new ArrayList();
        List<User> canInvestUserList = new ArrayList();

        for (User user : users) {
            for (AccountConfig accountConfig : accountsConfig.getAccount()) {
                if (accountConfig.getUsername().equals(user.getAccount())) {
                    user.setAccountConfig(accountConfig);
                    break;
                }
            }
        }

        //大于0的标就都会去发起投资。在真正发起投资的地方，会决策投资多少钱。
        for (Borrow borrow : borrows) {
            if (InvestUtil.canInvest(borrow) && borrow.getCanTransfer()) {
                //只投能转让的标
                canInvestBorrowList.add(borrow);
            }
        }

        for (User user : users) {
            if (user.getAmount() != null && user.getAmount().compareTo(new BigDecimal("50")) > 0 && user.getInvestOpen()) {
                canInvestUserList.add(user);
            }
        }

        if (canInvestBorrowList.size() > 0 && canInvestUserList.size() > 0) {
            log.info("符合投资条件标的数量{} 用户数量{}", canInvestBorrowList.size(), canInvestUserList.size());

            for (User user : canInvestUserList) {
                if (StringUtils.isEmpty(user.getAccountConfig().getInvest().getBorrow().getId()) ||
                        user.getAccountConfig().getInvest().getBorrow().getId().equals("0")) {
                    //未指定投资的标的
                    List<Borrow> maxRateBorrows = InvestUtil.listMaxRateBorrow(canInvestBorrowList);
                    for (Borrow maxRateBorrow : maxRateBorrows) {
                        invest(maxRateBorrow, user);
                    }
                } else {
                    for (Borrow borrow : canInvestBorrowList) {
                        if (borrow.getId().equals(user.getAccountConfig().getInvest().getBorrow().getId())) {
                            invest(borrow, user);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void invest(Borrow borrow, User user) {
        BigDecimal decideInvestAmount = InvestUtil.decideInvestAmount(borrow, user, user.getAccountConfig().getInvest().getBorrow().getLevel());
        BigDecimal minInvestAmount = user.getAccountConfig().getInvest().getBorrow().getMinInvestAmount() == null ? BigDecimal.ZERO : user.getAccountConfig().getInvest().getBorrow().getMinInvestAmount();
        if (decideInvestAmount.compareTo(BigDecimal.ZERO) > 0 && decideInvestAmount.compareTo(minInvestAmount) >= 0) {
            if (user.getAccountConfig().getInvest().getBorrow().isOpen()) {
                CrawlerCache.getCrawlerModel(UserCrawler.class.getSimpleName())
                        .sendRequest(
                                Event.addSourceId(
                                        buildInvestRequest(borrow, decideInvestAmount, user)
                                                .setSkipDuplicateFilter(true)
                                                .setMaxReqCount(1),
                                        "发起投资{} {} {}", user.getAccount(), borrow.getTitle(), decideInvestAmount)
                        );
            } else {
                log.info("投资关闭。发起投资{} {} {}", user.getAccount(), borrow.getTitle(), decideInvestAmount);
            }
        }

    }

    /**
     * 投资请求
     */
    public Request buildInvestRequest(Borrow borrow, BigDecimal investAmount, User user) {

        Map<String, String> params = new MyMap<String, String>()
                .kv("id", borrow.getRealId())
                .kv("amount", investAmount.toString())
                .kv("isRebateUsable", "1")//使用返利
                .kv("isRedRewardUsable", "0")//1使用
                .kv("redRewardsIds", "")//,分割的字符1593427，1593427
                .map;

        //优先使用加息券。
        if (user.getAccountConfig().getInvest().getBorrow().isUseCoupon()) {
            //加息券使用逻辑
            CouponRate couponRate = couponRateRepository.findMaxRate();
            if (couponRate != null && couponRate.getId() != null) {
                params.put("rateCouponId", couponRate.getId() + "");
            }
        } else {
            //红包使用逻辑
            if (user.getAccountConfig().getInvest().getBorrow().isUseRedReward()) {
                //todo 完善
//                params.put("redRewardsIds", "");
            }
        }

        //todo 智能判断收益大小。来抉择使用

        log.info("账号{} 投资参数{}", user.getAccount(), params);
        Map<String, Object> meta = new MyMap<String, Object>().kv("account", user.getAccount())
                .kv("id", borrow.getId()).map;
        return Request.build(urlConfig.getBankInvest(), "parseInvest", HttpMethod.POST, params, meta)
                .setUseCookieOfAccount(user.getAccount());
    }

}
