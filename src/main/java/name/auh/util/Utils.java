package name.auh.util;

import cn.wanghaomiao.seimi.struct.BodyType;
import cn.wanghaomiao.seimi.struct.Response;
import org.seimicrawler.xpath.JXDocument;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private Utils() {
        //私有构造器
    }

    public static String info(String pattern, Object... params) {
        Object[] var2 = params;
        int var3 = params.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Object p = var2[var4];
            pattern = org.apache.commons.lang3.StringUtils.replaceOnce(pattern, "{}", p == null ? "NULL" : p.toString());
        }

        return pattern;
    }

    public static String getIdFromDetailPath(String path) {
        return path.split("/")[2];
    }

    /**
     * /borrow/protocol/templateV2?type=borrow&borrowWay=4&borrowId=18892567
     */
    public static Integer getIdFromDetailPathProtocol(String path) {
        return new Integer(path.split("/")[2]);
    }

    public static String covertAmount(String amount) {
        return amount.replace(",", "").replace("。", ".").replace(" ", "");
    }

    public static String covertNumber(String numberStr) {
        if (StringUtils.isEmpty(numberStr)) {
            return numberStr;
        }
        return numberStr.replace(",", "");
    }

    public static JXDocument getJXDocument(Response response) {
        return transfer(response.getBodyType(), response.getContent());
    }

    /**
     * 升级xpath解析版本。兼容代码
     */
    public static JXDocument transfer(BodyType bodyType, String content) {
        return BodyType.TEXT.equals(bodyType) && content != null ? JXDocument.create(content) : null;
    }

    /**
     * 解析url中的参数 url必须符合规则 //xx?fdfd=fdfd&ffdfd&
     */
    public static Map<String, String> getUrlParams(String url) {
        Map<String, String> paramsMap = new HashMap();
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return paramsMap;
        }

        String[] urlParams = url.split("\\?");
        String params = urlParams[1];
        String[] keyValueStr = params.split("&");

        for (String s : keyValueStr) {
            String[] keyValue = s.split("=");
            if (StringUtils.isEmpty(keyValue[0]) || StringUtils.isEmpty(keyValue[1])) {
                continue;
            }
            paramsMap.put(keyValue[0], keyValue[1]);
        }
        return paramsMap;
    }

    public static Map<String, String> getRelativeUrlParams(String url) {
        Map<String, String> paramsMap = new HashMap();
        String[] urlParams = url.split("\\?");
        String params = urlParams[1];
        String[] keyValueStr = params.split("&");

        for (String s : keyValueStr) {
            String[] keyValue = s.split("=");
            if (StringUtils.isEmpty(keyValue[0]) || StringUtils.isEmpty(keyValue[1])) {
                continue;
            }
            paramsMap.put(keyValue[0], keyValue[1]);
        }
        return paramsMap;
    }

    public static String filterUnNumber(String str) {
        // 只允数字 和小数点
        String regEx = "[^0-9 \\.]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

}
