package name.auh.srawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import name.auh.bean.Event;
import name.auh.bean.MyMap;
import name.auh.config.URLConfig;
import name.auh.data.entity.Borrow;
import name.auh.data.entity.CouponRate;
import name.auh.data.entity.RedReward;
import name.auh.data.entity.User;
import name.auh.data.repository.BorrowRepository;
import name.auh.data.repository.CouponRateRepository;
import name.auh.data.repository.RedRewardRepository;
import name.auh.data.repository.UserRepository;
import name.auh.service.WeXinQiYeService;
import name.auh.util.GsonUtils;
import name.auh.util.Utils;
import org.seimicrawler.xpath.exception.XpathSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

@Crawler(useUnrepeated = false, useCookie = true, httpTimeOut = 5000)
@Slf4j
@NoArgsConstructor
public class UserCrawler extends BaseCrawler {

    @Autowired
    private WeXinQiYeService weXinQiYeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private CouponRateRepository couponRateRepository;

    @Autowired
    private RedRewardRepository redRewardRepository;

    @Autowired
    private URLConfig urlConfig;

    public void parseLogin(Response response) {
        String account = response.getRequest().getParams().get("cellPhone");
        try {
            if ("200".equals(JSONObject.parseObject(response.getContent()).getString("code"))) {
                log.info("{} 登录成功!!!", account);

                userRepository.updateLogin(account, true, System.currentTimeMillis());

                Map<String, Object> meta = new MyMap<String, Object>().kv("account", account).map;

                //查询红包、加息券、资产
                push(Event.addSourceId(Request.build(urlConfig.getOverview(), "parseAmount").setMeta(meta).setUseCookieOfAccount(account), "登录成功"));
                push(Event.addSourceId(Request.build(urlConfig.getCoupon(), "parseCouponRate").setMeta(meta).setUseCookieOfAccount(account), "登录成功"));
                push(Event.addSourceId(Request.build(urlConfig.getRedReward(), "parseRedReward").setMeta(meta).setUseCookieOfAccount(account), "登录成功"));
            } else {
                log.error("登录失败 {} {}", account, response.getContent());
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
    }

    public void parseAmount(Response response) {
        try {
            BigDecimal usableAmount = BigDecimal.ZERO;
            Object userAmountDetail = Utils.getJXDocument(response).selOne("//div[@class='right-top']//div[@class='amount-info clearfix m-t-15']/div[@class='right light-text font-16']/span/text()");
            if (userAmountDetail != null) {
                usableAmount = new BigDecimal(userAmountDetail.toString());
            }

            User user = new User();
            user.setAccount(response.getRequest().getMeta().get("account").toString());
            user.setAmount(usableAmount);
            userRepository.updateAmount(user.getAccount(), user.getAmount());
            log.info("更新用户余额 {} {}", user.getAccount(), usableAmount);
        } catch (XpathSyntaxErrorException e) {
            log.error("解析用户余额错误 {}", e.getMessage());
        }
    }

    public void parseCouponRate(Response response) {
        JSONObject jsonObject = JSONObject.parseObject(response.getContent());
        if (!"200".equals(jsonObject.getString("code"))) {
            log.error("券包返回异常{}", response.getContent());
            return;
        }
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
        if (jsonArray == null) {
            log.info("没有券");
            return;
        }
        ArrayList<CouponRate> couponRates = new ArrayList<>();
        for (Object o : jsonArray) {
            CouponRate couponRate = GsonUtils.toObject(o.toString(), CouponRate.class);
            //不要提现券   2是加息券
            if (!"2".equals(couponRate.getType())) {
                continue;
            }

            couponRate.setLabelStrBigDecimal(new BigDecimal(couponRate.getLabelStr()));
            couponRates.add(couponRate);
        }
        couponRateRepository.deleteAll();
        couponRateRepository.saveAll(couponRates);
        log.info("刷新加息券完成 {}", GsonUtils.toJson(couponRates));
    }

    public void parseRedReward(Response response) {
        JSONObject jsonObject = JSONObject.parseObject(response.getContent());
        if (!"200".equals(jsonObject.getString("code"))) {
            log.error("红包返回异常{}", response.getContent());
            return;
        }
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");
        if (jsonArray == null) {
            log.info("没有红包");
            return;
        }
        ArrayList<RedReward> redRewards = new ArrayList<>();
        for (Object o : jsonArray) {
            RedReward re = GsonUtils.toObject(o.toString(), RedReward.class);
            redRewards.add(re);
        }

        redRewardRepository.deleteAll();
        redRewardRepository.saveAll(redRewards);
        log.info("刷新紅包完成 {}", GsonUtils.toJson(redRewards));
    }

    /**
     * 投资响应
     */
    public void parseInvest(Response response) {
        log.info("投资响应{}", response.getContent());
        JSONObject jsonObject = JSONObject.parseObject(response.getContent());
        String account = response.getMeta().get("account").toString();
        String borrowId = response.getMeta().get("id").toString();

        Object borrowRealIdObj = response.getRequest().getParams().get("id");
        String borrowRealId = String.valueOf(borrowRealIdObj);

        if ("200".equals(jsonObject.getString("code"))) {
            log.info("抢标成功");

            try {
                Object obj = response.getRequest().getParams().get("rateCouponId");
                if (obj != null) {
                    couponRateRepository.deleteById(obj.toString());
                }
            } catch (Exception e) {
                log.error("移除已经使用的加息券失败{}", e.getMessage());
            }

            Map params = response.getRequest().getParams();
            log.info(Utils.info("抢标成功,账号[{}]标的id[{}]金额[{}]", account, borrowId, params.get("amount")));
            log.info("投资完成刷新用户余额");
            Map<String, Object> meta = new MyMap<String, Object>().kv("account", account).map;
            push(Event.addSourceId(new Request(urlConfig.getOverview(), "parseAmount")
                    .setSkipDuplicateFilter(true).setMeta(meta).setUseCookieOfAccount(account), "抢标成功"));

            log.info("投资完成刷新标的余额");
            Borrow borrow = borrowRepository.findById(borrowId).get();
            meta = new MyMap<String, Object>().kv("borrow", borrow).map;
            CrawlerCache.getCrawlerModel(BorrowCrawler.class.getSimpleName()).sendRequest(
                    Event.addSourceId(new Request(urlConfig.getHost() + borrow.getDetailUrl(), "parseBorrowDetail").setMeta(meta), "抢标成功")
            );

            log.info("抢标成功微信推送");
            weXinQiYeService.sendMessage("喜大普奔，抢标成功！！！", account, response.getRequest().getParams(), response.getContent());
        } else if ("403".equals(jsonObject.getString("code"))) {
            log.warn("{}", response.getContent());
        } else {
            String message = jsonObject.get("message").toString();

            if ("标的剩余金额不足".equals(message)) {
                log.info("投资完成刷新标的余额");
                Borrow borrow = borrowRepository.findById(borrowId).get();
                Map<String, Object> meta = new MyMap<String, Object>().kv("borrow", borrow).map;
                CrawlerCache.getCrawlerModel(BorrowCrawler.class.getSimpleName()).sendRequest(
                        Event.addSourceId(new Request(urlConfig.getHost() + borrow.getDetailUrl(), "parseBorrowDetail").setMeta(meta), "剩余标的金额不足加速刷新")
                );
            } else if ("该借款已满标".equals(message)) {
                borrowRepository.updateLeftAmountById(borrowId, BigDecimal.ZERO, System.currentTimeMillis());
                log.info("满标更新标的余额为0");
            } else if (message.contains("加息券")) {
                Object obj = response.getRequest().getParams().get("rateCouponId");
                if (obj != null) {
                    couponRateRepository.deleteById(obj.toString());
                }
            } else if (message.contains("请充值")) {
                CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(UserCrawler.class.getSimpleName());
                Map<String, Object> meta = new MyMap<String, Object>().kv("account", account).map;
                crawlerModel.sendRequest(Event.addSourceId(
                        new Request(urlConfig.getOverview(), "parseAmount").setMeta(meta).setUseCookieOfAccount(account), "请充值，刷新余额"));
            }
        }
    }


}
