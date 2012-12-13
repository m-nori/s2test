package org.seasar.test.context;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.junit.runners.model.TestClass;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.test.annotation.RootDicon;

/**
 * テストのクラス・メソッドとコンテナを管理する。
 *
 * @author m_nori
 */
public class TestContext {

    private static final Logger logger = Logger.getLogger(TestContext.class);

    private String rootDicon;

    private ContainerHolder containerHolder;

    TestClass testClass;

    private Object testInstance;

    /**
     * TestContextを初期化する。
     * <p>
     * この段階でベースとするdiconを取得し、保持しておく。
     *
     * @param testClass
     */
    public TestContext(TestClass testClass, ContainerHolder containerHolder) {
        this.testClass = testClass;
        this.containerHolder = containerHolder;
        this.rootDicon = retrieveRootDicon();
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
        containerHolder.resetContainer(rootDicon);
    }

    /**
     * コンテナを返却する。
     *
     * @return
     */
    public S2Container getContainer() {
        return containerHolder.getContainer(rootDicon);
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
     * テストクラスを返却する。
     *
     * @return テストクラス
     */
    public TestClass getTestClass() {
        return testClass;
    }

    /**
     * テスト対象のクラスを返却する。
     *
     * @return テスト対象のクラス
     */
    public Class<?> getTargetClass() {
        return testClass.getJavaClass();
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
     * テストインスタンスを設定する。
     *
     * @param testInstance テストインスタンス
     */
    public void setTestInstance(Object testInstance) {
        this.testInstance = testInstance;
    }

    /**
     * rootとなるDiconファイルを見つける。
     * <p>
     * {@link RootDicon} が付与されている場合、そこからdiconファイルのパスを取得する。<br>
     * それ以外の場合は「テストクラス名 + .dicon」がrootのdiconファイルとなる。<br>
     * ContextConfigurationへの指定はパスを指定して記載すること。
     *
     * @return rootとなるDiconファイルパス
     */
    protected String retrieveRootDicon() {
        RootDicon contextConfiguration =
            getTargetClass().getAnnotation(RootDicon.class);
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
            rootDicon =
                ResourceUtil.convertPath(getTargetClass().getSimpleName()
                    + ".dicon", getTargetClass());
        }
        return rootDicon;
    }

    /**
     * TestContextを文字列化する。
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).toString();
    }
}
