package org.seasar.test.context;

import java.lang.reflect.Method;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.external.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.env.Env;
import org.seasar.framework.log.Logger;
import org.seasar.framework.unit.UnitClassLoader;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.test.annotation.ContextConfiguration;

/**
 * テストのクラス・メソッドとコンテナを管理する。
 * <p>
 * TODO:コンテナのキャッシュを出来るようにする。<br>
 * TODO:ちょっとクラスが大きすぎるので分割を考える。
 * 
 * @author m_nori
 */
public class TestContext {
	/** 環境が設定されているファイルのパス。 */
	protected static final String ENV_PATH = "env_ut.txt";

	/** 環境が設定されていない場合のデフォルト値。 */
	protected static final String ENV_VALUE = "ut";

	private static final Logger logger = Logger.getLogger(TestContext.class);

	private boolean warmDeploy = true;

	private boolean registerNamingConvention = true;

	private S2Container container;

	private String rootDicon;

	private Class<?> testClass;

	private Object testInstance;

	private Method testMethod;

	private Throwable testException;

	/**
	 * TestContextを初期化する。
	 * <p>
	 * この段階でベースとするdiconを取得し、保持しておく。
	 * 
	 * @param testClass
	 */
	public TestContext(Class<?> testClass) {
		this.testClass = testClass;
		this.rootDicon = retrieveRootDicon();
		initEnv();
	}

	/**
	 * warmデプロイを行うかどうかを設定する。
	 * <p>
	 * デフォルトではwarmデプロイを行う。
	 * 
	 * @param warmDeploy
	 *            warmデプロイをおこなうか
	 */
	public void setWarmDeploy(boolean warmDeploy) {
		this.warmDeploy = warmDeploy;
	}

	/**
	 * NamingConventionを登録するかどうかを設定する。
	 * <p>
	 * デフォルトでは登録を行う。
	 * 
	 * @param registerNamingConvention
	 *            NamingConventionを登録するか
	 */
	public void setRegisterNamingConvention(boolean registerNamingConvention) {
		this.registerNamingConvention = registerNamingConvention;
	}

	/**
	 * コンテナを初期化する。
	 * <p>
	 * 次にgetContanierを行った場合再読み込みが行われる。
	 */
	public void resetContainer() {
		if (logger.isDebugEnabled()) {
			logger.debug("destroy container");
		}
		if (container != null) {
			container.destroy();
			container = null;
		}
	}

	/**
	 * コンテナを返却する。
	 * 
	 * @return
	 */
	public S2Container getContainer() {
		if (logger.isDebugEnabled()) {
			logger.debug("return contaner dicon:" + rootDicon);
		}
		if (container == null) {
			container = loadContainer();
		}
		return container;
	}

	/**
	 * 現在使用しているrootDiconのファイル名を返却する。
	 * 
	 * @return rootDiconのファイル名
	 */
	public String getRootDicon() {
		return rootDicon;
	}

	/**
	 * テストの状態を更新する。
	 * 
	 * @param testInstance
	 *            テストインスタンス (<code>null</code>を許可する)
	 * @param testMethod
	 *            テストメソッド (<code>null</code>を許可する)
	 * @param testException
	 *            テストメソッドのexceptionに設定された例外。設定されていない場合<code>null</code>
	 */
	void updateState(Object testInstance, Method testMethod, Throwable testException) {
		this.testInstance = testInstance;
		this.testMethod = testMethod;
		this.testException = testException;
	}

	/**
	 * テストクラスを返却する。
	 * 
	 * @return テストクラス
	 */
	public Class<?> getTestClass() {
		return testClass;
	}

	/**
	 * テストインスタンスを返却する。
	 * 
	 * @return テストインスタンス
	 */
	public Object getTestInstance() {
		return testInstance;
	}

	/**
	 * テストメソッドを返却する。
	 * 
	 * @return テストメソッド
	 */
	public Method getTestMethod() {
		return testMethod;
	}

	/**
	 * テストメソッドが期待する例外を返却する。
	 * 
	 * @return テストメソッド期待する例外
	 */
	public Throwable getTestException() {
		return testException;
	}

	/**
	 * rootとなるDiconファイルを見つける。
	 * <p>
	 * {@link ContextConfiguration} が付与されている場合、そこからdiconファイルのパスを取得する。<br>
	 * それ以外の場合は「テストクラス名 + .dicon」がrootのdiconファイルとなる。<br>
	 * ContextConfigurationへの指定はパスを指定して記載すること。
	 * 
	 * @return rootとなるDiconファイルパス
	 */
	protected String retrieveRootDicon() {
		ContextConfiguration contextConfiguration = testClass
		        .getAnnotation(ContextConfiguration.class);
		String rootDicon;
		if (contextConfiguration != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Retrieved @ContextConfiguration");
			}
			rootDicon = contextConfiguration.path();
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("@ContextConfiguration not found");
			}
			rootDicon = ResourceUtil.convertPath(testClass.getSimpleName() + ".dicon", testClass);
		}
		return rootDicon;
	}

	/**
	 * 環境設定を行う。
	 */
	protected void initEnv() {
		Env.setFilePath(ENV_PATH);
		Env.setValueIfAbsent(ENV_VALUE);
	}

	/**
	 * コンテナを読み込みを返却する。
	 * <p>
	 * 使用するdiconはrootDiconに設定されているものを使用する。
	 * 
	 * @return 読み込んだコンテナ
	 */
	protected S2Container loadContainer() {
		if (logger.isDebugEnabled()) {
			logger.debug("load contaner dicon:" + rootDicon);
		}
		ClassLoader originalClassLoader = getOriginalClassLoader();
		UnitClassLoader unitClassLoader = new UnitClassLoader(originalClassLoader);
		Thread.currentThread().setContextClassLoader(unitClassLoader);
		if (isWarmDeploy()) {
			S2ContainerFactory.configure("warmdeploy.dicon");
		}
		S2Container container = StringUtil.isEmpty(rootDicon) ? S2ContainerFactory.create()
		        : S2ContainerFactory.create(rootDicon);
		SingletonS2ContainerFactory.setContainer(container);
		container
		        .setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
		ComponentDeployerFactory.setProvider(new ExternalComponentDeployerProvider());
		if (!container.hasComponentDef(NamingConvention.class) && isRegisterNamingConvention()) {
			NamingConvention namingConvention = new NamingConventionImpl();
			container.register(namingConvention);
		}
		return container;
	}

	/**
	 * オリジナルのクラスローダを返却する。
	 * 
	 * @return オリジナルのクラスローダ
	 */
	protected ClassLoader getOriginalClassLoader() {
		S2Container configurationContainer = S2ContainerFactory.getConfigurationContainer();
		if (configurationContainer != null
		        && configurationContainer.hasComponentDef(ClassLoader.class)) {
			return (ClassLoader) configurationContainer.getComponent(ClassLoader.class);
		}
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * WARM deployかどうかを返却する。
	 * 
	 * @return WARM deployかどうか
	 */
	protected boolean isWarmDeploy() {
		return warmDeploy && !ResourceUtil.isExist("s2container.dicon")
		        && ResourceUtil.isExist("convention.dicon")
		        && ResourceUtil.isExist("creator.dicon")
		        && ResourceUtil.isExist("customizer.dicon");
	}

	/**
	 * テスト用のS2コンテナを作成する際に{@link NamingConvention}を登録する場合は<code>true</code>を返却する。
	 * 
	 * @return テスト用のS2コンテナを作成する際に{@link NamingConvention}を登録する場合は
	 *         <code>true</code>
	 */
	protected boolean isRegisterNamingConvention() {
		return registerNamingConvention;
	}

	/**
	 * TestContextを文字列化する。
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).toString();
	}
}
