package name.auh.task;

import cn.wanghaomiao.seimi.spring.common.CrawlerCache;
import cn.wanghaomiao.seimi.struct.Request;
import lombok.extern.slf4j.Slf4j;
import name.auh.config.URLConfig;
import name.auh.srawlers.TransferCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static name.auh.consts.Consts.initialDelay;

@ConditionalOnProperty(prefix = "task", name = "transfer", havingValue = "open", matchIfMissing = false)
@Slf4j
@Component
public class TransferTask {

    @Autowired
    private URLConfig urlConfig;

    @Scheduled(initialDelay = initialDelay, fixedDelay = 3000)
    public void monitorTransferListPage() {
        CrawlerCache.getCrawlerModel(TransferCrawler.class.getSimpleName()).sendRequest(Request.build(urlConfig.getTransfers(), "parseTransferList"));
    }

}
