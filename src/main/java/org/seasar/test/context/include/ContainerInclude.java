package org.seasar.test.context.include;

import org.seasar.framework.container.S2Container;

/**
 * コンテナに含むオブジェクトを追加する。
 *
 * @author m_nori
 */
public interface ContainerInclude {

    /**
     * 引数のコンテナに依存したいクラスを追加する。
     * @param container コンテナ
     * @exception Exception すべての例外発生時
     */
    void execute(S2Container container) throws Exception;
}
