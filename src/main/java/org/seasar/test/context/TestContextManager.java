package org.seasar.test.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.seasar.framework.log.Logger;
import org.seasar.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * テストの実行管理を行う。
 * <p>
 * 以下の処理を行う。
 * <ul>
 * <li>テストの状態をtestContextに設定する。</li>
 * <li>テストの流れに合わせてTestExecutionListenerを呼び出す。</li>
 * </ul>
 * 
 * @author m_nori
 */
public class TestContextManager {
	private static final Logger logger = Logger.getLogger(TestContextManager.class);

	private final TestContext testContext;

	private final List<TestExecutionListener> testExecutionListeners = new ArrayList<TestExecutionListener>();

	/**
	 * TestContextManagerを初期化する。
	 * 
	 * @param clazz
	 *            テストクラス
	 */
	public TestContextManager(Class<?> clazz) {
		this.testContext = new TestContext(clazz);
		registerTestExecutionListeners(retrieveTestExecutionListeners(clazz));
	}

	/**
	 * testContextを返却する。
	 * 
	 * @return testContext
	 */
	public final TestContext getTestContext() {
		return testContext;
	}

	/**
	 * 登録されているtestExecutionListenerを返却する。
	 * 
	 * @return testExecutionListeners
	 */
	public List<TestExecutionListener> getTestExecutionListeners() {
		return testExecutionListeners;
	}

	/**
	 * 登録されているtestExecutionListenersを逆順にして返却する。<br>
	 * 後処理は逆順で行う必要があるのでこれを使用する。
	 * 
	 * @return testExecutionListenersの逆順
	 */
	private List<TestExecutionListener> getReversedTestExecutionListeners() {
		List<TestExecutionListener> listenersReversed = new ArrayList<TestExecutionListener>(
		        getTestExecutionListeners());
		Collections.reverse(listenersReversed);
		return listenersReversed;
	}

	/**
	 * {@link TestExecutionListener}の登録を行う。
	 * 
	 * @param testExecutionListeners
	 */
	public void registerTestExecutionListeners(TestExecutionListener... testExecutionListeners) {
		for (TestExecutionListener listener : testExecutionListeners) {
			if (logger.isDebugEnabled()) {
				logger.debug("Registering TestExecutionListener: " + listener);
			}
			this.testExecutionListeners.add(listener);
		}
	}

	/**
	 * 登録対象となる{@link TestExecutionListener}を返却する。
	 * <P>
	 * デフォルトでは以下を登録する。
	 * <ul>
	 * <li>{@link DependencyInjectionTestExecutionListener} : 依存関係の注入</li>
	 * </ul>
	 * 登録順番は意味があるので、変更する場合は注意すること。
	 * <p>
	 * TODO:アノテーションからTestExecutionListenerを取得できるようにする。
	 * 
	 * @param clazz
	 *            テスト対象クラス
	 * @return 登録対象となるTestExecutionListener
	 */
	private TestExecutionListener[] retrieveTestExecutionListeners(Class<?> clazz) {
		List<TestExecutionListener> listeners = new ArrayList<TestExecutionListener>();
		listeners.add(new DependencyInjectionTestExecutionListener());
		return listeners.toArray(new TestExecutionListener[listeners.size()]);
	}

	/**
	 * BeforeClassの処理をフックして登録されている{@link TestExecutionListener}に処理を委譲する。
	 * 
	 * @throws Exception
	 *             すべての例外発生時
	 */
	public void beforeTestClass() throws Exception {
		final Class<?> testClass = getTestContext().getTestClass();
		if (logger.isDebugEnabled()) {
			logger.debug("beforeTestClass(): class [" + testClass + "]");
		}
		getTestContext().updateState(null, null, null);
		for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.beforeTestClass(getTestContext());
			} catch (Exception e) {
				logger.warn("error TestExecutionListener:" + testExecutionListener);
				throw e;
			}
		}
	}

	/**
	 * テストインスタンスの生成処理をフックして登録されている{@link TestExecutionListener}に処理を委譲する。<br>
	 * インスタンスの生成はテストメソッドごとに行われる。<br>
	 * Beforeより前に処理をされる。
	 * 
	 * @param testInstance
	 *            ベースとなるテストインスタンス
	 * @throws Exception
	 *             すべての例外発生時
	 */
	public void prepareTestInstance(Object testInstance) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("prepareTestInstance(): instance [" + testInstance + "]");
		}
		getTestContext().updateState(testInstance, null, null);
		for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.prepareTestInstance(getTestContext());
			} catch (Exception e) {
				logger.warn("error TestExecutionListener:" + testExecutionListener);
				throw e;
			}
		}
	}

	/**
	 * Beforeの処理をフックして登録されている{@link TestExecutionListener}に処理を委譲する。
	 * 
	 * @param testInstance
	 *            テストインスタンス
	 * @param testMethod
	 *            テストメソッド
	 * @throws Exception
	 *             すべての例外発生時
	 */
	public void beforeTestMethod(Object testInstance, Method testMethod) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("beforeTestMethod(): instance [" + testInstance + "], method ["
			        + testMethod + "]");
		}
		getTestContext().updateState(testInstance, testMethod, null);
		for (TestExecutionListener testExecutionListener : getTestExecutionListeners()) {
			try {
				testExecutionListener.beforeTestMethod(getTestContext());
			} catch (Exception e) {
				logger.warn("error TestExecutionListener:" + testExecutionListener);
				throw e;
			}
		}
	}

	/**
	 * Afterの処理をフックして登録されている{@link TestExecutionListener}に処理を委譲する。<br>
	 * TestExecutionListenerは登録順番とは逆の順番で呼び出しを行う。<br>
	 * TODO:例外をそのままスローして問題ないか確認。
	 * 
	 * @param testInstance
	 *            テストインスタンス
	 * @param testMethod
	 *            テストメソッド
	 * @param exception
	 *            テストが期待している例外
	 * @throws Exception
	 *             すべての例外発生時
	 */
	public void afterTestMethod(Object testInstance, Method testMethod, Throwable exception)
	        throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("afterTestMethod(): instance [" + testInstance + "], method ["
			        + testMethod + "], exception [" + exception + "]");
		}
		getTestContext().updateState(testInstance, testMethod, exception);
		for (TestExecutionListener testExecutionListener : getReversedTestExecutionListeners()) {
			try {
				testExecutionListener.afterTestMethod(getTestContext());
			} catch (Exception e) {
				logger.warn("error TestExecutionListener:" + testExecutionListener);
				throw e;
			}
		}
	}

	/**
	 * AfterClassの処理をフックして登録されている{@link TestExecutionListener}に処理を委譲する。<br>
	 * TestExecutionListenerは登録順番とは逆の順番で呼び出しを行う。<br>
	 * 
	 * @throws Exception
	 *             すべての例外発生時
	 */
	public void afterTestClass() throws Exception {
		final Class<?> testClass = getTestContext().getTestClass();
		if (logger.isDebugEnabled()) {
			logger.debug("afterTestClass(): class [" + testClass + "]");
		}
		getTestContext().updateState(null, null, null);

		for (TestExecutionListener testExecutionListener : getReversedTestExecutionListeners()) {
			try {
				testExecutionListener.afterTestClass(getTestContext());
			} catch (Exception e) {
				logger.warn("error TestExecutionListener:" + testExecutionListener);
				throw e;
			}
		}
	}
}
