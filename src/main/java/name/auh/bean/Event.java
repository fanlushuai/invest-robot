package name.auh.bean;

import cn.wanghaomiao.seimi.struct.Request;
import cn.wanghaomiao.seimi.utils.StrFormatUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import name.auh.consts.Consts;
import org.springframework.context.ApplicationEvent;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
public class Event<T> extends ApplicationEvent {

    private static final long serialVersionUID = 3039313222160544111L;

    /**
     * 事件类型
     */
    int eventTypeId;

    /**
     * json对象
     */
    String json;

    /**
     * 对象类型
     */
    Class<T> type;

    /**
     * traceId
     */
    String traceId;

    public Event(Object source) {
        super(source);
    }

    public Event(Object source, int eventTypeId, String json, Class type, String traceId) {
        super(source);
        this.json = json;
        this.type = type;
        this.traceId = System.currentTimeMillis() + "|" + traceId;
    }

    public static Request addSourceId(Request request, String sourceId, Object... params) {
        //如果多个request共用一个metaMap，将他们分离
        if (request.getMeta().get(Consts.SOURCE_ID) != null) {
            Map<String, Object> meteCopy = new HashMap<>(request.getMeta());
            meteCopy.put(Consts.SOURCE_ID, StrFormatUtil.info(sourceId, params));
            request.setMeta(meteCopy);
        } else {
            request.getMeta().put(Consts.SOURCE_ID, StrFormatUtil.info(sourceId, params));
        }

        return request;
    }


}
