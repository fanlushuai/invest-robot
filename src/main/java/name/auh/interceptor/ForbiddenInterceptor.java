package name.auh.interceptor;

import cn.wanghaomiao.seimi.annotation.Interceptor;
import cn.wanghaomiao.seimi.core.SeimiInterceptor;
import cn.wanghaomiao.seimi.struct.Response;
import lombok.extern.slf4j.Slf4j;
import name.auh.annotation.Forbidden;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Interceptor(everyMethod = true)
@Slf4j
public class ForbiddenInterceptor implements SeimiInterceptor {

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
            //使用lambda的无法拦截  获取方法签名
            return;
        }

        if (method.getAnnotation(Forbidden.class) != null && response.getContent().contains("403 Forbidden")) {
            log.error("被墙了，请降低频率！！！{}", response.getRequest().getUrl());
        }
    }

    @Override
    public void after(Method method, Response response) {

    }
}
