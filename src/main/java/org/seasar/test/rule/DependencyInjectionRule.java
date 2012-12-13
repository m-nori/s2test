package org.seasar.test.rule;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.log.Logger;
import org.seasar.framework.util.FieldUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.test.context.TestContext;

public class DependencyInjectionRule extends S2PrepareInstanceRule {

    private static final Logger logger =
        Logger.getLogger(DependencyInjectionRule.class);

    private List<Field> boundFieldsCache;

    @Override
    public void apply(TestContext testContext) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Performing dependency injection for test context ["
                + testContext
                + "].");
        }
        injectDependencies(testContext);
    }

    /**
     * インジェクションを行う。
     *
     * @param testContext
     *            対象のテストコンテキスト
     * @throws Exception
     *             すべての例外発生時
     */
    protected void injectDependencies(final TestContext testContext)
            throws Exception {
        List<Field> boundFields = getBindFields(testContext.getTargetClass());
        for (Field field : boundFields) {
            bindField(testContext.getContainer(),
                    testContext.getTestInstance(),
                    field);
        }
    }

    /**
     * テスト対象クラスの保持しているフィールドを親クラス階層をたどって取得する。<br>
     * 取得結果はキャッシュしておく。
     *
     * @param testClass
     *            テスト対象のクラス
     * @return テスト対象クラスの保持しているフィールド
     * @throws Exception
     *             すべての例外発生時
     */
    protected List<Field> getBindFields(Class<?> testClass) throws Exception {
        if (boundFieldsCache == null) {
            boundFieldsCache = new ArrayList<Field>();
            for (Class<?> clazz = testClass; clazz != null; clazz =
                clazz.getSuperclass()) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (!boundFieldsCache.contains(field)) {
                        boundFieldsCache.add(field);
                    }
                }
            }
        }
        return boundFieldsCache;
    }

    /**
     * フィールドに対してインジェクションを行う。
     *
     * @param container
     *            コンテナ
     * @param targetInstance
     *            インジェクション対象のインスタンス
     * @param field
     *            インジェクション対象のフィールド
     */
    protected void bindField(S2Container container, Object targetInstance,
            Field field) {
        if (isAutoBindable(field)) {
            field.setAccessible(true);
            if (FieldUtil.get(field, targetInstance) != null) {
                return;
            }
            String name = normalizeName(field.getName());
            Object component = null;
            if (container.hasComponentDef(name)) {
                Class<?> componentClass =
                    container.getComponentDef(name).getComponentClass();
                if (componentClass == null) {
                    component = container.getComponent(name);
                    if (component != null) {
                        componentClass = component.getClass();
                    }
                }
                if (componentClass != null
                    && field.getType().isAssignableFrom(componentClass)) {
                    if (component == null) {
                        component = container.getComponent(name);
                    }
                } else {
                    component = null;
                }
            }
            if (component == null && container.hasComponentDef(field.getType())) {
                component = container.getComponent(field.getType());
            }
            if (component != null) {
                FieldUtil.set(field, targetInstance, component);
            }
        }
    }

    /**
     * 自動バインディング可能かどうか返却する。
     *
     * @param field
     *            フィールド
     * @return 自動バインディング可能かどうか
     */
    private boolean isAutoBindable(Field field) {
        int modifiers = field.getModifiers();
        return !Modifier.isStatic(modifiers)
            && !Modifier.isFinal(modifiers)
            && !field.getType().isPrimitive();
    }

    /**
     * 名前を正規化する。
     *
     * @param name
     *            名前
     * @return 正規化された名前
     */
    private String normalizeName(String name) {
        return StringUtil.replace(name, "_", "");
    }
}
