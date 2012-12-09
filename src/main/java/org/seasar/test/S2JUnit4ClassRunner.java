package org.seasar.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.seasar.framework.log.Logger;
import org.seasar.test.context.TestContextManager;
import org.seasar.test.context.junit4.statements.RunAfterTestClassCallbacks;
import org.seasar.test.context.junit4.statements.RunAfterTestMethodCallbacks;
import org.seasar.test.context.junit4.statements.RunBeforeTestClassCallbacks;
import org.seasar.test.context.junit4.statements.RunBeforeTestMethodCallbacks;

/**
 * S2JUnitを実行するためのRunnerクラス。
 * <p>
 * TODO:withBeforesとwithAftersが非推奨になっているのでRulesを使うように変える必要があるみたい。
 * 
 * @author m_nori
 */
public class S2JUnit4ClassRunner extends BlockJUnit4ClassRunner {

	private static final Logger logger = Logger.getLogger(S2JUnit4ClassRunner.class);

	private final TestContextManager testContextManager;

	/**
	 * テスト起動時に呼び出されるコンストラクタ。
	 * 
	 * @param clazz
	 *            テスト対象クラス
	 * @throws InitializationError
	 *             コンストラクタ初期化例外
	 */
	public S2JUnit4ClassRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
		if (logger.isDebugEnabled()) {
			logger.debug("constructor called");
		}
		this.testContextManager = createTestContextManager(clazz);
		if (logger.isDebugEnabled()) {
			logger.debug("testContextManager created");
		}
	}

	/**
	 * TestContextManagerを生成する。
	 * 
	 * @param clazz
	 *            テスト対象クラス
	 * @return TestContextManager
	 */
	protected TestContextManager createTestContextManager(Class<?> clazz) {
		return new TestContextManager(clazz);
	}

	/**
	 * TestContextManagerを返却する。
	 * 
	 * @return testContextManager
	 */
	protected final TestContextManager getTestContextManager() {
		return testContextManager;
	}

	/**
	 * BeforeClassのStatementにTestContextManagerのフックを設定する。
	 * 
	 * @see RunBeforeTestClassCallbacks
	 */
	@Override
	protected Statement withBeforeClasses(Statement statement) {
		final Statement junitBeforeClasses = super.withBeforeClasses(statement);
		return new RunBeforeTestClassCallbacks(junitBeforeClasses, getTestContextManager());
	}

	/**
	 * AfterClassのStatementにTestContextManagerのフックを設定する。
	 * 
	 * @see RunAfterTestClassCallbacks
	 */
	@Override
	protected Statement withAfterClasses(Statement statement) {
		final Statement junitAfterClasses = super.withAfterClasses(statement);
		return new RunAfterTestClassCallbacks(junitAfterClasses, getTestContextManager());
	}

	/**
	 * テストインスタンス生成処理にTestContextManagerのprepareTestInstanceのフックを設定する。
	 */
	@Override
	protected Object createTest() throws Exception {
		Object testInstance = super.createTest();
		testContextManager.prepareTestInstance(testInstance);
		return testInstance;
	}

	/**
	 * BeforeのStatementにTestContextManagerのフックを設定する。
	 * 
	 * @see RunBeforeTestMethodCallbacks
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance,
	        Statement statement) {
		Statement junitBefores = super.withBefores(frameworkMethod, testInstance, statement);
		return new RunBeforeTestMethodCallbacks(junitBefores, testInstance,
		        frameworkMethod.getMethod(), getTestContextManager());
	}

	/**
	 * AfterのStatementにTestContextManagerのフックを設定する。
	 * 
	 * @see RunAfterTestMethodCallbacks
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance,
	        Statement statement) {
		Statement junitAfters = super.withAfters(frameworkMethod, testInstance, statement);
		return new RunAfterTestMethodCallbacks(junitAfters, testInstance,
		        frameworkMethod.getMethod(), getTestContextManager());
	}
}
