package name.auh.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于guava的缓存失效，来实现的一个访问频率控制
 */
public class LimitUtil {

    private volatile static LoadingCache<String, Cache> cacheContext = CacheBuilder.newBuilder()
            .maximumSize(100)
            .build(new CacheLoader<String, Cache>() {
                @Override
                public Cache load(String key) throws Exception {

                    return CacheBuilder.newBuilder()
                            .maximumSize(1000)
                            .expireAfterWrite(Integer.valueOf(key.split(",")[1]),
                                    TimeUnit.valueOf(key.split(",")[2]))
                            .build();
                }
            });

    /**
     * 限制频率
     * 注意失效策略。上次操作的时间和这次比较。中间的间隔时间。如果大于失效时间，就失效。这样key就会归零。
     * 主要应用。就是微信告警。相同的微信告警，不能太频繁。
     */
    public static boolean overRate(String event, int times, int time, TimeUnit timeUnit) {
        String cacheTypeKey = String.format("%d,%d,%s", times, time, timeUnit.name());
        Cache<String, AtomicInteger> counter = cacheContext.getUnchecked(cacheTypeKey);
        try {
            AtomicInteger count = counter.get(event, AtomicInteger::new);
            int ct = count.getAndIncrement();
            if (ct == 0) {
                System.out.println("重新计数");
            }
            return ct + 1 > times;
        } catch (ExecutionException e) {
            System.out.println("inc error");
            return true;
        }
    }


}
