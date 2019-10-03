package name.auh.util;

import name.auh.data.entity.Borrow;
import name.auh.data.entity.User;
import name.auh.enums.InvestStyleEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class InvestUtil {

    public static boolean canInvest(Borrow borrow) {
        return borrow.getOnSale() && borrow.getLeftAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 投资金额决策
     * @param borrow      标的
     * @param user        用户
     * @param investStyle 投资决策级别
     */
    public static BigDecimal decideInvestAmount(Borrow borrow, User user, InvestStyleEnum investStyle) {
        if (user.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal investAmount;
        if (borrow.getLeftAmount().compareTo(user.getAmount()) > 0) {
            investAmount = user.getAmount().divide(new BigDecimal("50")).setScale(0, RoundingMode.DOWN).multiply(new BigDecimal("50"));
            if (investStyle.equals(InvestStyleEnum.RIGHT_NOW)) {
                return investAmount;
            }

            if (investStyle.equals(InvestStyleEnum.LAST_ONE) || investAmount.equals(InvestStyleEnum.MAX_LAST_ONE)) {
                return BigDecimal.ZERO;
            }

            //todo check
            if (investStyle.equals(InvestStyleEnum.MAX_ONE)) {
                BigDecimal maxRateMinInvestAmount = getMaxRateMinInvestAmount(borrow);
                if (maxRateMinInvestAmount.compareTo(user.getAmount()) <= 0) {
                    return user.getAmount();
                }
            }
        } else {
            investAmount = borrow.getLeftAmount();

            if (investStyle.equals(InvestStyleEnum.RIGHT_NOW) || investStyle.equals(InvestStyleEnum.LAST_ONE)) {
                return investAmount;
            }

            if (investStyle.equals(InvestStyleEnum.MAX_ONE)) {
                BigDecimal maxRateMinInvestAmount = getMaxRateMinInvestAmount(borrow);
                if (maxRateMinInvestAmount.compareTo(investAmount) <= 0) {
                    return investAmount;
                }
            }

            if (investStyle.equals(InvestStyleEnum.MAX_LAST_ONE)) {
                BigDecimal maxRateMinInvestAmount = getMaxRateMinInvestAmount(borrow);
                if (maxRateMinInvestAmount.compareTo(investAmount) <= 0) {
                    return investAmount;
                }
            }
        }

        return BigDecimal.ZERO;
    }

    private static BigDecimal getMaxRateMinInvestAmount(Borrow borrow) {
        //刷新一下更加精准
//        borrow = Data.flushBorrow(borrow);

        //富甲一方最大返利金额
        BigDecimal maxInvestAmount = borrow.getAmount().divide(new BigDecimal("4")).multiply(new BigDecimal("1").setScale(0, RoundingMode.DOWN));

        //进行10分之一的容错
        maxInvestAmount = maxInvestAmount.add(maxInvestAmount.divide(new BigDecimal("10")).setScale(0, RoundingMode.DOWN));

        return maxInvestAmount;
    }

    /**
     * 获得返利=出借金额×返利系数（返利上限为标的金额×25%×返利系数）
     * 0-29天标的0.05%返利系数
     * 30-69天标的0.08%返利系数
     * 70天以上标的0.1%返利系数
     * @param investAmount 投资金额
     */
    public BigDecimal calculateLastInvestExtraAmount(BigDecimal amount, int day, BigDecimal investAmount) {
        BigDecimal returnRate = getLastInvestExtraRate(day);
        BigDecimal userMaxReturnAmount = investAmount.multiply(returnRate);
        BigDecimal borrowMaxReturnAmount = amount.multiply(new BigDecimal("0.25")).multiply(returnRate);
        return borrowMaxReturnAmount.compareTo(userMaxReturnAmount) == 1 ? userMaxReturnAmount : borrowMaxReturnAmount;
    }

    private BigDecimal getLastInvestExtraRate(Integer day) {
        if (day > 70) {
            return new BigDecimal("0.001");
        } else if (day > 30) {
            return new BigDecimal("0.0008");
        } else {
            return new BigDecimal("0.0005");
        }
    }

    /**
     * 富甲一方奖
     * 获得返利=出借金额×返利系数（返利上限为标的金额×25%×返利系数）
     * 0-29天标的%0.1%返利系数
     * 30-69天标的%0.15%返利系数
     * 70天以上标的%0.2%返利系数
     * @param investAmount 投资金额
     * @return 估计能够得到的返利
     */
    public BigDecimal calculateMaxInvestExtraAmount(BigDecimal amount, int day, BigDecimal investAmount) {
        BigDecimal returnRate = getMaxInvestExtraRate(day);
        BigDecimal userMaxReturnAmount = investAmount.multiply(returnRate);
        BigDecimal borrowMaxReturnAmount = amount.multiply(new BigDecimal("0.25")).multiply(returnRate);
        return borrowMaxReturnAmount.compareTo(userMaxReturnAmount) == 1 ? userMaxReturnAmount : borrowMaxReturnAmount;
    }

    private BigDecimal getMaxInvestExtraRate(Integer day) {
        if (day > 70) {
            return new BigDecimal("0.002");
        } else if (day > 30) {
            return new BigDecimal("0.0015");
        } else {
            return new BigDecimal("0.001");
        }
    }

    public static List<Borrow> listMaxRateBorrow(List<Borrow> borrowList) {
        List<Borrow> maxRateBorrowList = new ArrayList<>();
        BigDecimal maxRate = BigDecimal.ZERO;
        Borrow maxRateBorrow = null;
        for (Borrow borrow : borrowList) {
            if (maxRate.compareTo(borrow.getRate()) < 0) {
                maxRateBorrow = borrow;
            }
        }

        if (maxRateBorrow != null) {
            maxRateBorrowList.add(maxRateBorrow);
            for (Borrow borrow : borrowList) {
                if (maxRate.compareTo(borrow.getRate()) == 0) {
                    maxRateBorrowList.add(borrow);
                }
            }
        }

        return maxRateBorrowList;
    }

}
