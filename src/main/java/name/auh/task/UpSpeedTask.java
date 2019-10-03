package name.auh.task;

import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.Request;
import lombok.extern.slf4j.Slf4j;
import name.auh.bean.Event;
import name.auh.bean.MyMap;
import name.auh.config.URLConfig;
import name.auh.data.entity.Borrow;
import name.auh.data.entity.User;
import name.auh.data.repository.BorrowRepository;
import name.auh.data.repository.UserRepository;
import name.auh.srawlers.BorrowCrawler;
import name.auh.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

//默认单例
@Component
@Slf4j
public class UpSpeedTask {

    @Autowired
    private URLConfig urlConfig;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskScheduler taskScheduler;

    /**
     * 存储开启的定时任务。用于关闭
     */
    Map<String, ScheduledFuture> borrowOnTimeFLushScheduledStorage = new HashMap<>();

    public void stopBorrowOnTimeFLushSchedule(Borrow borrow) {
        try {
            ScheduledFuture scheduledFuture = borrowOnTimeFLushScheduledStorage.get(borrow.getId());
            if (scheduledFuture == null || scheduledFuture.isCancelled()) {
                return;
            }
            scheduledFuture.cancel(Boolean.TRUE);
            log.info("停止加速刷新成功{} {}", borrow.getId(), borrow.getTitle());
        } catch (Exception e) {
            log.error("停止加速刷新失败{} {} {}", borrow.getId(), borrow.getTitle(), e.getMessage());
        }
    }

    public void autoSolve(String borrowId) {
        Optional<Borrow> borrowOptional = borrowRepository.findById(borrowId);
        if (!borrowOptional.isPresent()) {
            return;
        }
        Borrow borrowFromDB = borrowOptional.get();

        if (borrowFromDB.getOnSaleTime() != null || borrowFromDB.getLeftAmount().compareTo(BigDecimal.ZERO) > 0) {
            //存在倒计时。或者已经开卖剩余标的金额大于0。
            // todo 并且符合用户要求规则
            List<User> userList = userRepository.findByLoginIsTrue();
            boolean star = false;
            for (User user : userList) {
                if (user.getInvestMinRate().compareTo(borrowFromDB.getRate()) >= 0) {
                    star = true;
                    break;
                }
            }
            if (star) {
                startBorrowOnTimeFLushSchedule(borrowFromDB);
            }
        } else {
            //如果发现剩余金额为0，并且定时器存在，就进行stop
            if (borrowFromDB.getLeftAmount().compareTo(BigDecimal.ZERO) == 0) {
                stopBorrowOnTimeFLushSchedule(borrowFromDB);
            }
        }
    }

    public void startBorrowOnTimeFLushSchedule(Borrow borrow) {
        //只能进来设置一次
        int updateUpSpeedFlush = borrowRepository.updateUpSpeedFlush(borrow.getId(), true);
        if (updateUpSpeedFlush != 1 || borrowOnTimeFLushScheduledStorage.get(borrow.getId()) != null) {
            return;
        }

        //开启一个定时器，刷新详情。提前3s开始。或者立马开始.持续到标的卖完
        Date runDate = DateUtil.toDate(DateUtil.toLocalDateTime(borrow.getOnSaleTime() == null ? new Date() : borrow.getOnSaleTime()).plusSeconds(-3));
        ScheduledFuture scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> {

            if (borrowRepository.findById(borrow.getId()).get() == null) {
                log.warn("标的不可投{}", borrow);
                return;
            }
            Map<String, Object> meta = new MyMap<String, Object>().kv("borrow", borrow).map;
            CrawlerCache.getCrawlerModel(BorrowCrawler.class.getSimpleName()).sendRequest(
                    Event.addSourceId(new Request(urlConfig.getHost() + borrow.getDetailUrl(), "parseBorrowDetail").setMeta(meta), "加速刷新")
            );

        }, runDate, 100);

        log.info("开启加速刷新成功{} {}", borrow.getId(), borrow.getTitle());
        //存储定时任务，后期判断停止
        borrowOnTimeFLushScheduledStorage.put(borrow.getId(), scheduledFuture);
    }

}
