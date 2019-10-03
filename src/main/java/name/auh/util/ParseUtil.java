package name.auh.util;

import name.auh.bean.Node;

public class ParseUtil {

    public static Node borrowUnit = new Node("//div[@class='list-li']", "标的单元");

    public static Node borrowUnitCountDown = new Node("//div[@class='list-li countDownHour']", "倒计时标的单元");

    public static Node borrowTitle = new Node("//div[@class='title left m-r-10']/text()", "标的标题").setSub(borrowUnit);

    public static Node borrowDetailDataUrl = new Node("//div[@class*='list-li']/@data-url", "标的id").setSub(borrowUnit);

    public static Node borrowDetailSaletime = new Node("//div[@class*='list-li']/@data-saletime", "标的id").setSub(borrowUnit);

    public static Node rate = new Node("//div[@class='common-title orange-title']/text()", "利率").setSub(borrowUnit);

    public static Node time = new Node("//div[@class='left m-l-50 w67']/div[@class='common-title']/text()", "期限").setSub(borrowUnit);

    public static Node timeUnit = new Node("//div[@class='left m-l-50 w67']/div[@class='common-title']/text()", "期限单位").setSub(borrowUnit);

    public static Node amount = new Node("//div[@class='left m-l-50 w165']/div[@class='common-title']/text()", "金额").setSub(borrowUnit);

    public static Node amountUnit = new Node("//div[@class='left m-l-50 w165']/div[@class='common-title']/text()", "金额单位").setSub(borrowUnit);

    public static Node amountLeft = new Node("//div[@class='left m-l-50 percent-con']//div[@class='light-text']/text()", "剩余金额").setSub(borrowUnit);

    public static Node invest = new Node("//span[@class='waves-effect btn2 list-btn']/text()", "马上出借").setSub(borrowUnit);

    public static Node borrowLeftBorrowDetail = new Node("//div[@class='left font-24 m-l-5 last-amount']", "标的详情");

    public static Node transferUnit = new Node("//div[@class='list-li']", "债权单元");

    public static Node transferTitle = new Node("//div[@class='title left']/text()", "债权标题").setSub(borrowUnit);

    public static Node transferDetailUrl = new Node("//div[@class*='list-li']/@data-url", "债权详情路径").setSub(borrowUnit);

    public static Node transferRate = new Node("//div[@class*='common-title orange-title font-20']/text()", "转让后预期年化").setSub(borrowUnit);

    public static Node transferInfo = new Node("//div[@class='list-li']/div[2]/div/div[1]/text()", "债权数值").setSub(borrowUnit);

    public static Node transferOnsale = new Node("//div[@class='list-li']//span[@class='waves-effect btn2 list-btn']/text()", "马上购买").setSub(borrowUnit);


}
