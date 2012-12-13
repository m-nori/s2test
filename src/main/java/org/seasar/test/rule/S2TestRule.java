package org.seasar.test.rule;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.seasar.test.context.TestContext;

/**
 * S2Testにて管理を行うルール。<br>
 * 実行時にtestContextをセットする。
 *
 * @author m_nori
 */
public abstract class S2TestRule implements TestRule {

    private TestContext testContext;

    /**
     * ステートメントを作成して返却する。
     */
    public Statement apply(Statement base, Description description) {
        return statement(base, getTestContext());
    }

    /**
     * 受け取った情報を元にStatementを作成する。
     * @param base 元となるステートメント
     * @param testContext ベースとなるコンテキスト
     * @return 前後処理を組み込んだステートメント
     */
    private Statement statement(final Statement base,
            final TestContext testContext) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before(testContext);
                base.evaluate();
                after(testContext);
            }
        };
    }

    /**
     * 前処理を行う。
     * @throws Throwable
     */
    protected void before(TestContext testContext) throws Throwable {
        // do nothing
    }

    /**
     * 後処理を行う。<br>
     * before、evaluateにて例外が発生してた場合は処理を行わない。
     * @throws Throwable
     */
    protected void after(TestContext testContext) throws Throwable {
        // do nothing
    }

    /**
     * TestContextを取得する。
     * @return
     */
    public TestContext getTestContext() {
        return testContext;
    }

    /**
     * TestContextを設定する。
     * @param testContext
     */
    public void setTestContext(TestContext testContext) {
        this.testContext = testContext;
    }
}
