package org.seasar.test.rule;

import org.seasar.test.context.TestContext;

public abstract class S2InstanceRule {

    /**
     * 対象のテストコンテキストを準備する。
     *
     * @param testContext
     * @throws Exception
     */
    public abstract void apply(final TestContext testContext) throws Exception;

}
