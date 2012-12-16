package org.seasar.test.rule;

import org.junit.runner.Description;
import org.seasar.framework.log.Logger;
import org.seasar.test.annotation.Dirty;
import org.seasar.test.context.TestContext;

/**
 * コンテナを初期化するためのルール。
 * <p>
 * 後処理にてコンテナを初期化する。
 * 
 * @see Dirty
 * @author m_nori
 */
public class DirtyContainerRule extends S2TestRule {

	private static final Logger logger = Logger.getLogger(DirtyContainerRule.class);

	/**
	 * 初期化するモード
	 * 
	 * @author m_nori
	 */
	public enum Mode {
		/**
		 * すべてのメソッドの後初期化する。
		 */
		ALL,
		/**
		 * 対象メソッドに{@link Dirty}が付与されていた場合に初期化する。
		 */
		DIRTY
	}

	private Mode mode;

	public DirtyContainerRule() {
		this(Mode.ALL);
	}

	public DirtyContainerRule(Mode mode) {
		super();
		this.mode = mode;
	}

	@Override
	protected void after(Description description, TestContext testContext) throws Throwable {
		if (mode == Mode.DIRTY) {
			Dirty dirty = description.getAnnotation(Dirty.class);
			if (dirty == null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Target method is not Dirty!");
				}
				return;
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Reset Container!");
		}
		testContext.resetContainer();
	}
}
