package example;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.seasar.framework.log.Logger;
import org.seasar.test.S2JUnit4ClassRunner;
import org.seasar.test.annotation.ContextConfiguration;

import example.logic.DummyLogic;

@RunWith(Enclosed.class)
public class S2JUnit4ClassRunnerExample {
    private static final Logger logger =
        Logger.getLogger(S2JUnit4ClassRunnerExample.class);

    @RunWith(S2JUnit4ClassRunner.class)
    public static class ContextConfigurationを指定しない場合 {

        public DummyLogic dummyLogic;

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
        public void dummyLogicにインジェクションが行われてる() {
            assertThat(dummyLogic, is(notNullValue()));
            String actual = dummyLogic.execute();
            assertThat(actual, is("example1"));
        }

        @Test
        public void コンテナが再読み込みされない() {
            logger.debug("called");
        }
    }

    @RunWith(S2JUnit4ClassRunner.class)
    @ContextConfiguration(path = "example/ContextConfigurationを指定する場合_.dicon")
    public static class ContextConfigurationを指定する場合 {
        public DummyLogic dummyLogic;

        @Test
        public void dummyLogicにインジェクションが行われてる() {
            assertThat(dummyLogic, is(notNullValue()));
            String actual = dummyLogic.execute();
            assertThat(actual, is("example2"));
        }
    }
}
