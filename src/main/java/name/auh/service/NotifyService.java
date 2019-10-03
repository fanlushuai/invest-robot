package name.auh.service;

import lombok.extern.slf4j.Slf4j;
import name.auh.config.AccountConfig;
import name.auh.config.AccountsConfig;
import name.auh.data.entity.Borrow;
import name.auh.data.entity.Transfer;
import name.auh.data.repository.BorrowRepository;
import name.auh.data.repository.TransferRepository;
import name.auh.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class NotifyService {

    @Autowired
    private WeXinQiYeService weXinQiYeService;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountsConfig accountsConfig;

    public void notifyNewBorrowPublish() {
        Iterable<Borrow> borrowList = borrowRepository.findAll();
        borrowList.forEach(borrow -> {
                    if (Boolean.TRUE.equals(borrow.getNotify()) || borrow.getLeftAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return;
                    }
                    log.info("微信推送可投的标的{}", borrow);
                    borrowRepository.updateNotify(borrow.getId(), Boolean.TRUE);
                    for (AccountConfig accountConfig : accountsConfig.getAccount()) {
                        if (!accountConfig.getInvest().getBorrow().isNotify()) {
                            log.info("通知关闭【发现可投标的】{},{}", DateUtil.getCurrentDatetime(), borrow.toString());
                            return;
                        }
                        weXinQiYeService.sendMessageTo(accountConfig.getWeChat(), "【发现可投标的】{},{}", DateUtil.getCurrentDatetime(), borrow.toString());
                    }
                }
        );
    }

    public void notifyUserNewTransferPublish() {
        Iterable<Transfer> transferList = transferRepository.findAll();
        transferList.forEach(transfer -> {
                    if (Boolean.TRUE.equals(transfer.getNotify()) || !Boolean.TRUE.equals(transfer.getOnSale())) {
                        return;
                    }
                    log.info("微信推送可投的债转{}", transfer);
                    transferRepository.updateNotify(transfer.getId(), Boolean.TRUE);
                    for (AccountConfig accountConfig : accountsConfig.getAccount()) {
                        if (!accountConfig.getInvest().getTransfer().isNotify()) {
                            log.info("通知关闭【发现可投债权】{},{}", DateUtil.getCurrentDatetime(), transfer.toString());
                            return;
                        }
                        weXinQiYeService.sendMessageTo(accountConfig.getWeChat(), "【发现可投债权】{},{}", DateUtil.getCurrentDatetime(), transfer.toString());
                    }
                }
        );
    }


}
