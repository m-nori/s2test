package org.seasar.test.context.junit4.statements;

import java.lang.reflect.Method;

import org.junit.runners.model.Statement;
import org.seasar.test.context.TestContextManager;

/**
 * Beforeに{@link TestContextManager}の処理をフックさせる。
 * 
 * @author m_nori
 * @see Statement
 */
public class RunBeforeTestMethodCallbacks extends Statement {

	private final Statement next;

	private final Object testInstance;

	private final Method testMethod;

	private final TestContextManager testContextManager;

	/**
	 * フックに使う情報を受け取る。
	 * 
	 * @param next
	 * @param testInstance
	 * @param testMethod
	 * @param testContextManager
	 */
	public RunBeforeTestMethodCallbacks(Statement next, Object testInstance, Method testMethod,
	        TestContextManager testContextManager) {
		this.next = next;
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.testContextManager = testContextManager;
	}

	/**
	 * 本来の処理の前に{@link TestContextManager #beforeTestMethod(Object, Method)}
	 * を呼び出す。
	 */
	@Override
	public void evaluate() throws Throwable {
		this.testContextManager.beforeTestMethod(this.testInstance, this.testMethod);
		this.next.evaluate();
	}

}
