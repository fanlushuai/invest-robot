package name.auh.srawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Response;
import lombok.extern.slf4j.Slf4j;
import name.auh.data.entity.Transfer;
import name.auh.data.repository.TransferRepository;
import name.auh.service.NotifyService;
import name.auh.util.ParseUtil;
import name.auh.util.Utils;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;
import org.seimicrawler.xpath.exception.XpathSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Crawler(useUnrepeated = false, httpTimeOut = 1000 * 20)
public class TransferCrawler extends BaseCrawler {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private TransferRepository transferRepository;

    private static long count = 0L;

    public void parseTransferList(Response response) {
        log.info("============监控债转列表(" + (++count) + ")============");
        JXDocument doc = Utils.getJXDocument(response);
        try {
            List<JXNode> transferList = doc.selN(ParseUtil.transferUnit.getXpath());
            for (JXNode jxNode : transferList) {
                parseTransfer(jxNode);
            }
            //推送新的债券
            notifyService.notifyUserNewTransferPublish();
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
    }

    public void parseTransfer(JXNode jxNode) {
        JXDocument doc = JXDocument.create(new Elements(jxNode.asElement()));
        String detailUrl = doc.selOne(ParseUtil.transferDetailUrl.getXpath()).toString();
        String title = doc.selOne(ParseUtil.transferTitle.getXpath()).toString();
        String rateStr = Utils.covertNumber(doc.selOne(ParseUtil.transferRate.getXpath()).toString());
        String id = Utils.getIdFromDetailPath(detailUrl);
        String dayStr = doc.selN(ParseUtil.transferInfo.getXpath()).get(1).toString();

        Object onSaleStr = doc.selOne(ParseUtil.transferOnsale.getXpath());
        boolean onSale = false;
        if (onSaleStr != null && onSaleStr.toString().contains("购买")) {
            onSale = true;
        }

        Integer day = Integer.valueOf(dayStr);
        if (!transferRepository.existsById(id)) {
            Transfer transfer = Transfer.builder()
                    .id(id).day(day).rate(new BigDecimal(rateStr)).onSale(onSale)
                    .detailUrl(detailUrl).title(title).createTime(System.currentTimeMillis()).build();
            transferRepository.save(transfer);
        } else {
            transferRepository.updateOnSaleById(id, onSale);
        }
    }


}
