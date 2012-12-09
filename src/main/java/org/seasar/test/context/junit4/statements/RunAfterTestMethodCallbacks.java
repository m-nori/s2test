package org.seasar.test.context.junit4.statements;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.seasar.test.context.TestContextManager;

/**
 * Afterに{@link TestContextManager}の処理をフックさせる。
 * 
 * @author m_nori
 * @see Statement
 */
public class RunAfterTestMethodCallbacks extends Statement {

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
	public RunAfterTestMethodCallbacks(Statement next, Object testInstance, Method testMethod,
	        TestContextManager testContextManager) {
		this.next = next;
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.testContextManager = testContextManager;
	}

	/**
	 * 本来の処理の後に
	 * {@link TestContextManager #afterTestMethod(Object, Method, Throwable)}
	 * を呼び出す。
	 */
	@Override
	public void evaluate() throws Throwable {
		Throwable testException = null;
		List<Throwable> errors = new ArrayList<Throwable>();
		try {
			this.next.evaluate();
		} catch (Throwable e) {
			testException = e;
			errors.add(e);
		}

		try {
			this.testContextManager.afterTestMethod(this.testInstance, this.testMethod,
			        testException);
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
