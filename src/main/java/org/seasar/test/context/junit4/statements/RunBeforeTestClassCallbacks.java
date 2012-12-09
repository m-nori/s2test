package org.seasar.test.context.junit4.statements;

import org.junit.runners.model.Statement;
import org.seasar.test.context.TestContextManager;

/**
 * BeforeClassに{@link TestContextManager}の処理をフックさせる。
 * 
 * @author m_nori
 * @see Statement
 */
public class RunBeforeTestClassCallbacks extends Statement {

	private final Statement next;

	private final TestContextManager testContextManager;

	/**
	 * フックに使う情報を受け取る。
	 * 
	 * @param next
	 * @param testContextManager
	 */
	public RunBeforeTestClassCallbacks(Statement next, TestContextManager testContextManager) {
		this.next = next;
		this.testContextManager = testContextManager;
	}

	/**
	 * 本来の処理の前に{@link TestContextManager #beforeTestClass()}を呼び出す。
	 */
	@Override
	public void evaluate() throws Throwable {
		this.testContextManager.beforeTestClass();
		this.next.evaluate();
	}

}
