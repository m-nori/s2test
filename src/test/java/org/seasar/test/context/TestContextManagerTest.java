package org.seasar.test.context;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runners.model.TestClass;
import org.seasar.test.rule.S2TestRule;

public class TestContextManagerTest {

	private TestContextManager testContextManager;

	private TestClass testClass;

	@Before
	public void before() throws Exception {
		testClass = spy(new TestClass(this.getClass()));
		testContextManager = new TestContextManager(testClass);
	}

	@Test
	public void prepareTestInstance_ClassRuleが1個設定されている場合() {
		List<S2TestRule> rules = setS2ClassRule(new SampleS2TestRule());
		testContextManager.prepareTestClass();
		for (S2TestRule rule : rules) {
			assertThat(rule.getTestContext(), is(notNullValue()));
		}
	}

	@Test
	public void prepareTestInstance_ClassRuleが複数設定されている場合() {
		List<S2TestRule> rules = setS2ClassRule(new SampleS2TestRule(), new SampleS2TestRule(),
		        new SampleS2TestRule());
		testContextManager.prepareTestClass();
		for (S2TestRule rule : rules) {
			assertThat(rule.getTestContext(), is(notNullValue()));
		}
	}

	private List<S2TestRule> setS2ClassRule(S2TestRule... rules) {
		List<S2TestRule> ruleList = new ArrayList<S2TestRule>();
		for (S2TestRule rule : rules) {
			ruleList.add(rule);
		}
		when(testClass.getAnnotatedFieldValues(null, ClassRule.class, S2TestRule.class))
		        .thenReturn(ruleList);
		return ruleList;
	}

	public static class SampleS2TestRule extends S2TestRule {
	}
}
