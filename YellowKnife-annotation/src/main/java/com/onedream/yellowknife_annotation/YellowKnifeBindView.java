package com.onedream.yellowknife_annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jdallen
 * @since 2020/9/2
 */

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
public @interface YellowKnifeBindView {
    int viewId() default 0;
}

