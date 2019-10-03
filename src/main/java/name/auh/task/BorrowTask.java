package name.auh.task;

import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.CrawlerModel;
import cn.wanghaomiao.seimi.struct.Request;
import lombok.extern.slf4j.Slf4j;
import name.auh.config.URLConfig;
import name.auh.srawlers.BorrowCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static name.auh.consts.Consts.initialDelay;

@ConditionalOnProperty(prefix = "task", name = "borrow", havingValue = "open", matchIfMissing = false)
@Slf4j
@Component
public class BorrowTask {

    @Autowired
    private URLConfig urlConfig;

    /**
     * 监控标的列表
     */
    @Scheduled(initialDelay = initialDelay, fixedDelay = 3000)
    public void WebMonitorBorrowList() {
        CrawlerModel crawlerModel = CrawlerCache.getCrawlerModel(BorrowCrawler.class.getSimpleName());
        crawlerModel.sendRequest(Request.build(urlConfig.getLoans(), "parseBorrowList"));
    }


}
