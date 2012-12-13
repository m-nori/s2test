package org.seasar.test;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.seasar.framework.log.Logger;
import org.seasar.test.context.TestContextManager;

/**
 * S2JUnitを実行するためのRunnerクラス。
 * <p>
 * TODO:withBeforesとwithAftersが非推奨になっているのでRulesを使うように変える必要があるみたい。
 *
 * @author m_nori
 */
public class S2JUnit4ClassRunner extends BlockJUnit4ClassRunner {

    private static final Logger logger =
        Logger.getLogger(S2JUnit4ClassRunner.class);

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
    protected TestContextManager createTestContextManager(Class<?> clazz)
            throws InitializationError {
        try {
            return new TestContextManager(getTestClass());
        } catch (Exception e) {
            logger.error(e);
            throw new InitializationError(e);
        }
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
     * テストクラスの生成処理に{@link TestContextManager #prepareTestClass()}をフックさせる。
     */
    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        testContextManager.prepareTestClass();
        return super.classBlock(notifier);
    }

    /**
     * テストインスタンスの生成処理に{@link TestContextManager #prepareTestInstance(Object)}をフックさせる。
     */
    @Override
    protected Object createTest() throws Exception {
        Object testInstance = super.createTest();
        testContextManager.prepareTestInstance(testInstance);
        return testInstance;
    }

}
