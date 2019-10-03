package name.auh.annotation;

import java.lang.annotation.*;

/**
 * 标识被墙检测的位置，统一执行被墙逻辑
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Forbidden {

}
