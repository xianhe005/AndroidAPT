package com.hxh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by HXH at 2019/5/22
 * test
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Test {
}
