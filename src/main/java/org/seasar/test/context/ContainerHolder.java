package org.seasar.test.context;

import java.util.HashMap;
import java.util.Map;

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

public class ContainerHolder {
    /** 環境が設定されているファイルのパス。 */
    protected static final String ENV_PATH = "env_ut.txt";

    /** 環境が設定されていない場合のデフォルト値。 */
    protected static final String ENV_VALUE = "ut";

    private static final Logger logger =
        Logger.getLogger(ContainerHolder.class);

    private Map<String, S2Container> containerMap =
        new HashMap<String, S2Container>();

    private boolean warmDeploy = true;

    private boolean registerNamingConvention = true;

    public ContainerHolder() {
        initEnv();
    }

    /**
     * コンテナを初期化する。
     * <p>
     * 次にgetContanierを行った場合再読み込みが行われる。
     *
     * @param rootDicon 対象となるdiconファイル
     */
    public void resetContainer(String rootDicon) {
        if (logger.isDebugEnabled()) {
            logger.debug("destroy container");
        }
        S2Container container = containerMap.get(rootDicon);
        if (container != null) {
            container.destroy();
            containerMap.remove(rootDicon);
        }
    }

    /**
     * コンテナを返却する。
     *
     * @param rootDicon 対象となるdiconファイル
     * @return コンテナ
     */
    public S2Container getContainer(String rootDicon) {
        S2Container container = containerMap.get(rootDicon);
        if (container == null) {
            container = loadContainer(rootDicon);
            containerMap.put(rootDicon, container);
        }
        return container;
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
     * 環境設定を行う。
     */
    private void initEnv() {
        Env.setFilePath(ENV_PATH);
        Env.setValueIfAbsent(ENV_VALUE);
    }

    /**
     * コンテナを読み込みを返却する。
     *
     * @param rootDicon rootとなるDiconファイル
     * @return 読み込んだコンテナ
     */
    protected S2Container loadContainer(String rootDicon) {
        if (logger.isDebugEnabled()) {
            logger.debug("load contaner dicon:" + rootDicon);
        }
        ClassLoader originalClassLoader = getOriginalClassLoader();
        UnitClassLoader unitClassLoader =
            new UnitClassLoader(originalClassLoader);
        Thread.currentThread().setContextClassLoader(unitClassLoader);
        if (isWarmDeploy()) {
            S2ContainerFactory.configure("warmdeploy.dicon");
        }
        S2Container container =
            StringUtil.isEmpty(rootDicon)
                ? S2ContainerFactory.create()
                : S2ContainerFactory.create(rootDicon);
        SingletonS2ContainerFactory.setContainer(container);
        container.setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
        ComponentDeployerFactory.setProvider(new ExternalComponentDeployerProvider());
        if (!container.hasComponentDef(NamingConvention.class)
            && isRegisterNamingConvention()) {
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
        S2Container configurationContainer =
            S2ContainerFactory.getConfigurationContainer();
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
        return warmDeploy
            && !ResourceUtil.isExist("s2container.dicon")
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
}
