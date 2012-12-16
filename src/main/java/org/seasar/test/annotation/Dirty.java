package org.seasar.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.seasar.test.rule.DirtyContainerRule;

/**
 * コンテナが汚れたことを表す。
 * 
 * @see DirtyContainerRule
 * @author m_nori
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Dirty {
}
