package name.auh.srawlers;

import cn.wanghaomiao.seimi.def.BaseSeimiCrawler;
import cn.wanghaomiao.seimi.struct.Response;

public class BaseCrawler extends BaseSeimiCrawler {

    @Override
    public String[] startUrls() {
        return new String[0];
    }

    @Override
    public void start(Response response) {

    }
}
