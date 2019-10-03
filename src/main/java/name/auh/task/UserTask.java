package name.auh.task;

import cn.wanghaomiao.seimi.http.HttpMethod;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import lombok.extern.slf4j.Slf4j;
import name.auh.bean.Event;
import name.auh.bean.MyMap;
import name.auh.config.URLConfig;
import name.auh.data.entity.User;
import name.auh.data.repository.UserRepository;
import name.auh.srawlers.UserCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

import static name.auh.consts.Consts.initialDelay;

@ConditionalOnProperty(prefix = "task", name = "user", havingValue = "open", matchIfMissing = false)
@Slf4j
@Component
public class UserTask {

    @Autowired
    private URLConfig urlConfig;

    @Autowired
    private UserRepository userRepository;

    /**
     * 登录请求
     */
    public Request buildLoginRequest(User user) {
        return Request.build(urlConfig.getLogin(),
                "parseLogin",
                HttpMethod.POST,
                new MyMap<String, String>().kv("cellPhone", user.getAccount()).kv("password", user.getPassword()).map,
                null).setUseCookieOfAccount(user.getAccount());
    }

    /**
     * 每20分钟登录一次 并且 进行一次余额刷新
     */
    @Scheduled(initialDelay = initialDelay, fixedDelay = 1000 * 60 * 20)
    public void loginWebMonitor() throws InterruptedException {
        List<User> investOpenTrueUsers = userRepository.findByInvestOpenTrue();
        if (CollectionUtils.isEmpty(investOpenTrueUsers)) {
            log.info("没有打开投资的用户！！！");
            return;
        }

        CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(UserCrawler.class.getSimpleName());

        for (User user : investOpenTrueUsers) {
            try {
                crawlerModel.sendRequest(Event.addSourceId(buildLoginRequest(user), "定时登录"));
            } catch (NullPointerException n) {
                Thread.sleep(3000);
                crawlerModel.sendRequest(Event.addSourceId(buildLoginRequest(user), "定时登录"));
            }
        }
    }

    /**
     * 每3分钟刷新一下余额
     */
    @Scheduled(initialDelay = initialDelay * 2 + 1000 * 60 * 3, fixedRate = 1000 * 60 * 3)
    public void userAmountWebMonitor() {
        CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(UserCrawler.class.getSimpleName());

        List<User> users = userRepository.findByLoginIsTrue();
        for (User user : users) {
            Map<String, Object> meta = new MyMap<String, Object>().kv("account", user.getAccount()).map;
            crawlerModel.sendRequest(Event.addSourceId(
                    new Request(urlConfig.getOverview(), "parseAmount").setMeta(meta), "定时刷余额{}", user.getAccount())
                    .setUseCookieOfAccount(user.getAccount())
            );
        }
    }

    /**
     * 每15分钟刷新一下券包
     */
    @Scheduled(initialDelay = initialDelay * 2 + 1000 * 60 * 15, fixedRate = 1000 * 60 * 15)
    public void userCouponWebMonitor() {
        CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(UserCrawler.class.getSimpleName());

        List<User> users = userRepository.findByLoginIsTrue();
        for (User user : users) {
            Map<String, Object> meta = new MyMap<String, Object>().kv("account", user.getAccount()).map;

            crawlerModel.sendRequest(
                    Event.addSourceId(
                            new Request(urlConfig.getCoupon(), "parseCouponRate").setUseCookieOfAccount(user.getAccount()).setMeta(meta),
                            "定时刷新加息券 账号{}", user.getAccount())
            );
            crawlerModel.sendRequest(
                    Event.addSourceId(
                            new Request(urlConfig.getRedReward(), "parseRedReward").setUseCookieOfAccount(user.getAccount()).setMeta(meta),
                            "定时刷新红包 账号{}", user.getAccount()
                    ));
        }
    }

}
