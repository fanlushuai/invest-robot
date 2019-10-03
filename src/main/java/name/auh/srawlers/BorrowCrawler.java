package name.auh.srawlers;

import cn.wanghaomiao.seimi.annotation.Crawler;
import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.struct.Response;
import lombok.extern.slf4j.Slf4j;
import name.auh.annotation.Forbidden;
import name.auh.bean.Event;
import name.auh.bean.MyMap;
import name.auh.config.URLConfig;
import name.auh.data.entity.Borrow;
import name.auh.data.repository.BorrowRepository;
import name.auh.enums.BorrowTypeEnum;
import name.auh.service.NotifyService;
import name.auh.task.UpSpeedTask;
import name.auh.util.DateUtil;
import name.auh.util.ParseUtil;
import name.auh.util.Utils;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;
import org.seimicrawler.xpath.exception.XpathSyntaxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Crawler(useUnrepeated = false, httpTimeOut = 1000 * 20)
@Slf4j
public class BorrowCrawler extends BaseCrawler {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private URLConfig urlConfig;

    @Autowired
    private BorrowRepository borrowRepository;

    private static long count = 0L;

    @Autowired
    private UpSpeedTask upSpeedTask;

    @Forbidden
    public void parseBorrowList(Response response) {
        log.info("============监控散标列表(" + (++count) + ")============");
        JXDocument doc = Utils.getJXDocument(response);
        try {
            //倒计时单元处理
            List<JXNode> borrowList = doc.selN(ParseUtil.borrowUnitCountDown.getXpath());
            //标的单元处理
            borrowList.addAll(doc.selN(ParseUtil.borrowUnit.getXpath()));
            for (JXNode jxNode : borrowList) {
                parseBorrow(jxNode);
            }
            //推送新标
            notifyService.notifyNewBorrowPublish();
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
    }

    private void parseBorrow(JXNode node) throws XpathSyntaxErrorException {

        JXDocument doc = JXDocument.create(new Elements(node.asElement()));

        String detailUrl = doc.selOne(ParseUtil.borrowDetailDataUrl.getXpath()).toString();
        String title = doc.selOne(ParseUtil.borrowTitle.getXpath()).toString();
        String amount = doc.selOne(ParseUtil.amount.getXpath()).toString();
        String amountLeft = doc.selOne(ParseUtil.amountLeft.getXpath()).toString();
        String rateStr = doc.selOne(ParseUtil.rate.getXpath()).toString();
        String saleTime = doc.selOne(ParseUtil.borrowDetailSaletime.getXpath()).toString();
        //日期统一转化为天
        String day = doc.selOne(ParseUtil.time.getXpath()).toString();
        String timeUnit = doc.selOne(ParseUtil.timeUnit.getXpath()).toString();
        if (StringUtils.hasLength(timeUnit) && timeUnit.contains("月")) {
            Integer d = (Integer.valueOf(day) * 30);
            day = d.toString();
        }

        BigDecimal rate = new BigDecimal(rateStr);

        boolean saleTimeEmpty = StringUtils.isEmpty(saleTime);

        //jpa 真的好垃圾。
        String id = Utils.getIdFromDetailPath(detailUrl);
        if (borrowRepository.existsById(id)) {
            Borrow borrow = borrowRepository.findById(id).get();
            borrow.setLeftAmount(new BigDecimal(Utils.covertAmount(amountLeft.substring(5, amountLeft.length() - 1))));
            borrow.setOnSaleTime(!saleTimeEmpty ? DateUtil.toDate(DateUtil.parseTime(saleTime.substring(0, 19), "yyyy.MM.dd HH:mm:ss")) : null);
            borrow.setOnSale(saleTimeEmpty);
            borrowRepository.save(borrow);
        } else {
            Borrow borrowFromPage = Borrow.builder()
                    .rate(rate)
                    .day(new Integer(day))
                    .title(title)
                    .type(BorrowTypeEnum.BORROW)
                    .amount(new BigDecimal(Utils.covertAmount(amount)))
                    .id(id)
                    .detailUrl(detailUrl)
                    .leftAmount(new BigDecimal(Utils.covertAmount(amountLeft.substring(5, amountLeft.length() - 1))))
                    .onSaleTime(!saleTimeEmpty ? DateUtil.toDate(DateUtil.parseTime(saleTime.substring(0, 19), "yyyy.MM.dd HH:mm:ss")) : null)
                    .onSale(saleTimeEmpty).build();

            //更新标的金额，是否开售 或者 添加标的信息
            borrowRepository.save(borrowFromPage);
        }

        //如果发现存在倒计时，就进行定点刷新设置。onle set once
        Borrow borrowFromDB = borrowRepository.findById(id).get();
        upSpeedTask.autoSolve(borrowFromDB.getId());

        if (borrowFromDB != null && borrowFromDB.getRealId() != null) {
            return;
        }
        //刷新详情。获取realId
        Map<String, Object> meta = new MyMap<String, Object>().kv("borrow", borrowFromDB).map;
        push(Event.addSourceId(new Request(urlConfig.getHost() + borrowFromDB.getDetailUrl(), "parseBorrowDetail").setMeta(meta), "刷线借款列表"));
    }

    @Forbidden
    public void parseBorrowDetail(Response response) {
        try {
            JXDocument doc = Utils.getJXDocument(response);

            Borrow borrowMete = (Borrow) response.getMeta().get("borrow");
            Borrow borrowFromDB = borrowRepository.findById(borrowMete.getId()).get();
            if (borrowFromDB != null && borrowFromDB.getRealId() == null) {
                String borrowRealId = Utils.getRelativeUrlParams(doc.selOne("//div[@class='detail-content-con']/div/a/@href").toString()).get("borrowId");
                borrowRepository.updateRealIdById(borrowRealId, borrowFromDB.getId());
                log.info("获取借款realId {} 通过id {}", borrowRealId, borrowMete.getId());
                List<Object> objects = doc.sel("//div[@class='clearfix m-t-10']//span[@class='left m-l-5 second-text']/text()");
                for (Object o : objects) {
                    if (o != null && o.toString().contains("不可")) {
                        borrowRepository.updateCanTransfer(borrowFromDB.getId(), false);
                        log.info("不可转让 {}", borrowFromDB.getId());
                        return;
                    }
                }
                borrowRepository.updateCanTransfer(borrowFromDB.getId(), true);
            }

            List<Object> objects = doc.sel("//div[@class='left font-24 m-l-5 m-t_10 last-amount']/text()");
            if (!CollectionUtils.isEmpty(objects) && objects.size() == 1) {
                BigDecimal leftBorrowAmount = new BigDecimal(Utils.covertAmount(objects.get(0).toString()));

                borrowRepository.updateLeftAmountById(borrowFromDB.getId(), leftBorrowAmount, System.currentTimeMillis());
                //一旦进来详情意味着，可以投资了。可以刷请求了
                borrowRepository.updateOnsaleById(borrowFromDB.getId(), true);

                if (leftBorrowAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    upSpeedTask.stopBorrowOnTimeFLushSchedule(borrowFromDB);
                }
                log.info("刷新详情 {}", response.getMeta().get("sourceId"));
            } else {
                List<Object> objects1 = doc.sel("//div[@class='detail-content-con']/div/div[@class='left m-l-20']/text()");
                for (Object o : objects1) {
                    if (o != null && o.toString().contains("满标用时")) {
                        borrowRepository.updateLeftAmountById(borrowFromDB.getId(), BigDecimal.ZERO, System.currentTimeMillis());
                        log.info("刷新详情已满标 {}", borrowFromDB.getId());
                        return;
                    }
                }
                log.warn("刷新详情未解析到金额{} {}", response.getMeta().get("sourceId"), borrowMete.getId());
            }
        } catch (XpathSyntaxErrorException e) {
            e.printStackTrace();
        }
    }


}
