package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.servlet.Servlet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;
import org.seasar.framework.mock.servlet.MockHttpServletRequest;
import org.seasar.framework.mock.servlet.MockHttpServletResponse;
import org.seasar.framework.mock.servlet.MockServletConfig;
import org.seasar.framework.mock.servlet.MockServletContext;
import org.seasar.test.S2JUnit4ClassRunner;
import org.seasar.test.annotation.ContextConfiguration;
import org.seasar.test.annotation.Dirty;
import org.seasar.test.annotation.InstanceRule;
import org.seasar.test.annotation.RootDicon;
import org.seasar.test.context.TestContext;
import org.seasar.test.context.include.HttpServletTestInclude;
import org.seasar.test.rule.DirtyContainerRule;
import org.seasar.test.rule.DirtyContainerRule.Mode;
import org.seasar.test.rule.S2InstanceRule;
import org.seasar.test.rule.S2TestRule;

import example.logic.DummyLogic;

@RunWith(Enclosed.class)
public class S2JUnit4ClassRunnerExample {
	private static final Logger logger = Logger.getLogger(S2JUnit4ClassRunnerExample.class);

	@RunWith(S2JUnit4ClassRunner.class)
	public static class RootDiconを指定しない場合 {

		public DummyLogic dummyLogic;

		@Test
		public void dummyLogicにインジェクションが行われてる() {
			assertThat(dummyLogic.dummyService, is(notNullValue()));
			String actual = dummyLogic.execute();
			assertThat(actual, is("example1"));
		}

		@Test
		public void コンテナが再読み込みされない() {
			logger.debug("called");
		}
	}

	@RunWith(S2JUnit4ClassRunner.class)
	@RootDicon(path = "example/RootDiconを指定する場合_.dicon")
	public static class RootDiconを指定する場合 {
		public DummyLogic dummyLogic;

		@Test
		public void dummyLogicにインジェクションが行われてる() {
			assertThat(dummyLogic.dummyService, is(notNullValue()));
			String actual = dummyLogic.execute();
			assertThat(actual, is("example2"));
		}
	}

	@RunWith(S2JUnit4ClassRunner.class)
	@RootDicon()
	public static class RootDiconを空にする場合 {
		public DummyLogic dummyLogic;

		@Test
		public void dummyLogicにインジェクションが行われていないこと() {
			assertThat(dummyLogic.dummyService, is(nullValue()));
		}
	}

	@RunWith(S2JUnit4ClassRunner.class)
	@RootDicon()
	public static class 各ルールが設定されている場合 {
		public static class S2PrepareInstanceRuleSapmle extends S2InstanceRule {
			@Override
			public void apply(TestContext testContext) throws Exception {
				logger.debug("S2PrepareInstanceRuleSapmle.apply()");
			}
		}

		public static class S2MethodRuleSample extends S2TestRule {
			@Override
			protected void before(Description description, TestContext testContext)
			        throws Throwable {
				logger.debug("S2MethodRuleSample.before()");
				logger.debug(testContext.getTestClass().getName());
				System.out.println(testContext.getTestInstance());
			}

			@Override
			protected void after(Description description, TestContext testContext) throws Throwable {
				logger.debug("S2MethodRuleSample.after()");
				System.out.println(testContext.getTestInstance());
			}
		}

		public static class S2ClassRuleSample extends S2TestRule {
			@Override
			protected void before(Description description, TestContext testContext)
			        throws Throwable {
				logger.debug("S2ClassRuleSample.before()");
				logger.debug(testContext.getTestClass().getName());
				System.out.println(testContext.getTestInstance());
			}

			@Override
			protected void after(Description description, TestContext testContext) throws Throwable {
				logger.debug("S2ClassRuleSample.after()");
				System.out.println(testContext.getTestInstance());
			}
		}

		@ClassRule
		public static TestRule s2ClassRule = new S2ClassRuleSample();

		@Rule
		public TestRule s2MethodRule = new S2MethodRuleSample();

		@InstanceRule
		public S2InstanceRule s2InstanceRule = new S2PrepareInstanceRuleSapmle();

		@BeforeClass
		public static void beforeClass() throws Exception {
			logger.debug("beforeClass()");
		}

		@AfterClass
		public static void afterClass() throws Exception {
			logger.debug("afterClass()");
		}

		@Before
		public void before() throws Exception {
			logger.debug("before()");
		}

		@After
		public void after() throws Exception {
			logger.debug("after()");
		}

		@Test
		public void test1() throws Exception {
			logger.debug("test1()");
		}

		@Test
		public void test2() throws Exception {
			logger.debug("test2()");
		}
	}

	@RunWith(S2JUnit4ClassRunner.class)
	@RootDicon()
	@ContextConfiguration(includes = { HttpServletTestInclude.class })
	public static class ContextIncludeが設定されている場合 {
		public S2Container container;

		protected MockServletContext servletContext;

		protected Servlet servlet;

		protected MockServletConfig servletConfig;

		protected MockHttpServletRequest request;

		protected MockHttpServletResponse response;

		@Test
		public void test() {
			assertThat(container, is(notNullValue()));
			assertThat(servletContext, is(notNullValue()));
			assertThat(servlet, is(notNullValue()));
			assertThat(servletConfig, is(notNullValue()));
			assertThat(request, is(notNullValue()));
			assertThat(response, is(notNullValue()));
		}
	}

	@RunWith(S2JUnit4ClassRunner.class)
	@RootDicon(path = "example/RootDiconを指定する場合_.dicon")
	public static class DirtyContainerRuleが付与されている場合 {
		@Rule
		public TestRule dirtyContainerRule = new DirtyContainerRule();

		@Test
		public void test1() {
			logger.debug("test1()");
		}

		@Test
		public void test2() throws Exception {
			logger.debug("test2()");
		}
	}

	@RunWith(S2JUnit4ClassRunner.class)
	@RootDicon(path = "example/RootDiconを指定する場合_.dicon")
	public static class DirtyContainerRuleがDirtyModeで付与されている場合 {
		@Rule
		public TestRule dirtyContainerRule = new DirtyContainerRule(Mode.DIRTY);

		@Test
		@Dirty
		public void test1() {
			logger.debug("test1()");
		}

		@Test
		public void test2() throws Exception {
			logger.debug("test2()");
		}

		@Test
		public void test3() throws Exception {
			logger.debug("test3()");
		}
	}
}
