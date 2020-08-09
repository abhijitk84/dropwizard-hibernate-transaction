package com.dropwizard.hibernate.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface Transactional {

  boolean readOnly() default false;

  boolean transactional() default true;

  CacheMode cacheMode() default CacheMode.NORMAL;

  FlushMode flushMode() default FlushMode.AUTO;
}