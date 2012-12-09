package org.seasar.test.context;

public interface TestExecutionListener {

	/**
	 * Pre-processes a test class <em>before</em> execution of all tests within
	 * the class.
	 * 
	 * @param testContext
	 *            the test context for the test; never <code>null</code>
	 * @throws Exception
	 *             allows any exception to propagate
	 */
	void beforeTestClass(TestContext testContext) throws Exception;

	/**
	 * 
	 * @param testContext
	 *            the test context for the test; never <code>null</code>
	 * @throws Exception
	 *             allows any exception to propagate
	 */
	void prepareTestInstance(TestContext testContext) throws Exception;

	/**
	 * 
	 * @param testContext
	 *            the test context in which the test method will be executed;
	 *            never <code>null</code>
	 * @throws Exception
	 *             allows any exception to propagate
	 */
	void beforeTestMethod(TestContext testContext) throws Exception;

	/**
	 * 
	 * @param testContext
	 *            the test context in which the test method was executed; never
	 *            <code>null</code>
	 * @throws Exception
	 *             allows any exception to propagate
	 */
	void afterTestMethod(TestContext testContext) throws Exception;

	/**
	 * 
	 * @param testContext
	 *            the test context for the test; never <code>null</code>
	 * @throws Exception
	 *             allows any exception to propagate
	 */
	void afterTestClass(TestContext testContext) throws Exception;
}
