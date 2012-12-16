package org.seasar.test.context;

import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runners.model.TestClass;
import org.seasar.framework.log.Logger;
import org.seasar.test.annotation.ContextConfiguration;
import org.seasar.test.annotation.InstanceRule;
import org.seasar.test.context.include.ContainerInclude;
import org.seasar.test.rule.S2InstanceRule;
import org.seasar.test.rule.S2TestRule;

/**
 * テストの実行管理を行う。
 * <p>
 * 以下の処理を行う。
 * <ul>
 * <li>テストの状態をtestContextに設定する。</li>
 * <li>テストの流れに合わせてTestExecutionListenerを呼び出す。</li>
 * </ul>
 * <p>
 * 
 * @author m_nori
 */
public class TestContextManager {
	/** デフォルトのインスタンス生成ルール */
	private static final String[] DEFAULT_PREPARE_INSTANCE_RULES_CLASS_NAMES = new String[] { "org.seasar.test.rule.DependencyInjectionRule" };

	private static final Logger logger = Logger.getLogger(TestContextManager.class);

	private final TestContext testContext;

	private final ContainerHolder containerHolder = new ContainerHolder();

	/** InjectionRuleはデフォルトのPrepareInstanceRulesとして使用する。 */
	private List<S2InstanceRule> defaultPrepareInstanceRules;

	/**
	 * TestContextManagerを初期化する。
	 * 
	 * @param clazz
	 *            テストクラス
	 */
	public TestContextManager(TestClass testClass) throws Exception {
		this.testContext = new TestContext(testClass, containerHolder);
		initByContextConfiguration();
	}

	/**
	 * testContextを返却する。
	 * 
	 * @return testContext
	 */
	public final TestContext getTestContext() {
		return testContext;
	}

	/**
	 * テストクラス生成処理のフック処理を行う。
	 * 
	 */
	public void prepareTestClass() {
		if (logger.isDebugEnabled()) {
			logger.debug("prepareTestClass()");
		}
		prepareClassS2TestRules();
	}

	/**
	 * テストインスタンス生成処理のフック処理を行う。
	 * 
	 * @param testInstance
	 *            ベースとなるテストインスタンス
	 * @throws Exception
	 *             すべての例外発生時
	 */
	public void prepareTestInstance(Object testInstance) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("prepareTestInstance(): instance [" + testInstance + "]");
		}
		getTestContext().setTestInstance(testInstance);
		applyPrepareInstanceRules();
		prepareMethodS2TestRules();
	}

	/**
	 * {@link ContextConfiguration}に設定された情報でContextを初期化する。
	 * 
	 * @throws すべての例外発生時
	 */
	protected void initByContextConfiguration() throws Exception {
		ContextConfiguration configuration = getTestContext().getTargetClass().getAnnotation(
		        ContextConfiguration.class);
		if (configuration != null) {
			Class<? extends ContainerInclude>[] classes = configuration.includes();
			for (Class<? extends ContainerInclude> clazz : classes) {
				ContainerInclude include = clazz.newInstance();
				include.execute(getTestContext().getContainer());
			}
		}
	}

	/**
	 * {@link ClassRule}が設定されたS2TestRuleにTestContextを設定する。
	 * 
	 * @throws Exception
	 *             すべての例外発生時
	 */
	protected void prepareClassS2TestRules() {
		List<S2TestRule> testRules = retrieveClassS2TestRules();
		for (S2TestRule testRule : testRules) {
			testRule.setTestContext(getTestContext());
		}
	}

	/**
	 * テストインスタンスに対してPrepareInstanceRuleを適用する。
	 * 
	 * @throws Exception
	 *             すべての例外発生時
	 */
	protected void applyPrepareInstanceRules() throws Exception {
		List<S2InstanceRule> prepareRules = createPrepareInstanceRules(getTestContext()
		        .getTestInstance());
		for (S2InstanceRule rule : prepareRules) {
			try {
				rule.apply(getTestContext());
			} catch (Exception e) {
				logger.warn("error TestExecutionListener:" + rule);
				throw e;
			}
		}
	}

	/**
	 * {@link Rule}が設定されたS2TestRuleにTestContextを設定する。
	 * 
	 * @throws Exception
	 *             すべての例外発生時
	 */
	protected void prepareMethodS2TestRules() {
		List<S2TestRule> testRules = retrieveMethodS2TestRules(getTestContext().getTestInstance());
		for (S2TestRule testRule : testRules) {
			testRule.setTestContext(getTestContext());
		}
	}

	/**
	 * PrepareInstanceRuleを取得する。
	 * 
	 * @param target
	 *            対象オブジェクト
	 * @return 取得したルール
	 */
	private List<S2InstanceRule> createPrepareInstanceRules(Object target) {
		List<S2InstanceRule> result = new ArrayList<S2InstanceRule>();
		result.addAll(getDefaultPrepareInstanceRules());
		result.addAll(retrievePrepareInstanceRules(target));
		return result;
	}

	/**
	 * デフォルトのPrepareInstanceRuleを返却する。
	 * 
	 * @return デフォルトのS2PrepareInstanceRule
	 */
	private List<S2InstanceRule> getDefaultPrepareInstanceRules() {
		if (defaultPrepareInstanceRules == null) {
			defaultPrepareInstanceRules = new ArrayList<S2InstanceRule>();
			for (String className : DEFAULT_PREPARE_INSTANCE_RULES_CLASS_NAMES) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends S2InstanceRule> clazz = (Class<? extends S2InstanceRule>) getClass()
					        .getClassLoader().loadClass(className);
					defaultPrepareInstanceRules.add(clazz.newInstance());
				} catch (Throwable e) {
					logger.warn("Could not load default PrepareInstanceRule class [" + className
					        + "] ");
				}
			}
		}
		return defaultPrepareInstanceRules;
	}

	/**
	 * インスタンスに付与されているPrepareInstanceRuleを返却する。
	 * 
	 * @param target
	 *            対象インスタンス
	 * @return 対象インスタンスに付与されているPrepareInstanceRule
	 */
	private List<S2InstanceRule> retrievePrepareInstanceRules(Object target) {
		return getTestContext().getTestClass().getAnnotatedFieldValues(target, InstanceRule.class,
		        S2InstanceRule.class);
	}

	/**
	 * インスタンスに設定されているS2TestRuleを抽出する。
	 * 
	 * @param target
	 * @return
	 */
	private List<S2TestRule> retrieveMethodS2TestRules(Object target) {
		return getTestContext().getTestClass().getAnnotatedFieldValues(target, Rule.class,
		        S2TestRule.class);
	}

	/**
	 * クラスに設定されているS2TestRuleを抽出する。
	 * 
	 * @param target
	 * @return
	 */
	private List<S2TestRule> retrieveClassS2TestRules() {
		return getTestContext().getTestClass().getAnnotatedFieldValues(null, ClassRule.class,
		        S2TestRule.class);
	}
}
