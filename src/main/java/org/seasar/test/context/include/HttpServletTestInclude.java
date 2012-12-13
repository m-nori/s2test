package org.seasar.test.context.include;

import javax.servlet.Servlet;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.deployer.ComponentDeployerFactory;
import org.seasar.framework.container.deployer.ExternalComponentDeployerProvider;
import org.seasar.framework.container.external.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.container.servlet.S2ContainerServlet;
import org.seasar.framework.log.Logger;
import org.seasar.framework.mock.servlet.MockHttpServletRequest;
import org.seasar.framework.mock.servlet.MockHttpServletResponse;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletConfig;
import org.seasar.framework.mock.servlet.MockServletConfigImpl;
import org.seasar.framework.mock.servlet.MockServletContext;
import org.seasar.framework.mock.servlet.MockServletContextImpl;

/**
 * コンテナに対してHTTP Servlet関連のテストを行うための情報を追加する。
 *
 * @author m_nori
 */
public class HttpServletTestInclude implements ContainerInclude {
    private static final Logger logger =
        Logger.getLogger(HttpServletTestInclude.class);

    public void execute(S2Container container) throws Exception {
        logger.debug("HttpServletTestInclude.execute()");
        setMockContext(container);
        container.setExternalContextComponentDefRegister(new HttpServletExternalContextComponentDefRegister());
        ComponentDeployerFactory.setProvider(new ExternalComponentDeployerProvider());
    }

    /**
     * Servlet等のモックを作成する。
     * @return
     * @throws Exception
     */
    private void setMockContext(S2Container container) throws Exception {
        MockServletContext servletContext =
            new MockServletContextImpl("s2-example");
        MockHttpServletRequest request =
            servletContext.createRequest("/hello.html");
        MockHttpServletResponse response =
            new MockHttpServletResponseImpl(request);
        MockServletConfig servletConfig = new MockServletConfigImpl();
        servletConfig.setServletContext(servletContext);
        Servlet servlet = new S2ContainerServlet();
        servlet.init(servletConfig);
        container.register(servletConfig);
        container.register(servletContext);
        container.register(servlet);
        container.register(request);
        container.register(response);
    }
}
