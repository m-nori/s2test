package org.seasar.test.context.junit4.statements;

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.seasar.test.context.TestContextManager;

/**
 * AfterClassに{@link TestContextManager}の処理をフックさせる。
 * 
 * @author m_nori
 * @see Statement
 */
public class RunAfterTestClassCallbacks extends Statement {

	private final Statement next;

	private final TestContextManager testContextManager;

	/**
	 * フックに使う情報を受け取る。
	 * 
	 * @param next
	 * @param testContextManager
	 */
	public RunAfterTestClassCallbacks(Statement next, TestContextManager testContextManager) {
		this.next = next;
		this.testContextManager = testContextManager;
	}

	/**
	 * 本来の処理の後に{@link TestContextManager #afterTestClass()}を呼び出す。
	 */
	@Override
	public void evaluate() throws Throwable {
		List<Throwable> errors = new ArrayList<Throwable>();
		try {
			this.next.evaluate();
		} catch (Throwable e) {
			errors.add(e);
		}

		try {
			this.testContextManager.afterTestClass();
		} catch (Exception e) {
			errors.add(e);
		}

		if (errors.isEmpty()) {
			return;
		}
		if (errors.size() == 1) {
			throw errors.get(0);
		}
		throw new MultipleFailureException(errors);
	}
}
