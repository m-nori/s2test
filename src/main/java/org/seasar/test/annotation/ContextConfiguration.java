package org.seasar.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 対象のテストで使用するDiconファイルを指定する。
 * <p>
 * テストクラスに付与することでテスト全体で使用するdiconファイルを指定できる。
 * <p>
 * TODO:メソッドごとに指定できるようにする必要があるか確認。
 * 
 * @author m_nori
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextConfiguration {
	/**
	 * 使用するdiconファイルへのパス。
	 * 
	 * @return diconファイルへのパス
	 */
	String path();
}
