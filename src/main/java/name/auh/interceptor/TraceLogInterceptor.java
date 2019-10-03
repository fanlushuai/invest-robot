package name.auh.interceptor;

import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.core.SeimiInterceptor;
import cn.wanghaomiao.seimi.struct.Response;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * 如果存在traceId，就进行打印日志
 */
@Interceptor(everyMethod = true)
@Slf4j
public class TraceLogInterceptor implements SeimiInterceptor {

    @Override
    public Class<? extends Annotation> getTargetAnnotationClass() {
        return null;
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public void before(Method method, Response response) {
        if (method == null) {
            //lambda表达式的回调函数拦截不到
            return;
        }
        String sourceId = (String) response.getRequest().getMeta().get("sourceId");

        if (sourceId != null) {
            log.info("[{}] -> [{}]", sourceId, method.getName());
        }
    }

    @Override
    public void after(Method method, Response response) {

    }
}
