package org.seasar.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.test.context.include.ContainerInclude;

/**
 * Context読み込み時に設定するインジェクト対象の情報を定義する。
 *
 * @author m_nori
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextConfiguration {
    Class<? extends ContainerInclude>[] includes();
}
