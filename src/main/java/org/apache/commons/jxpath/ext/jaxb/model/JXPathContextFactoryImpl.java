package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextFactoryReferenceImpl;

/**
 * Added to extend reference implementation {@link org.apache.commons.jxpath.ri.JXPathContextReferenceImpl with {@link JXPathContextImpl}
 */
public class JXPathContextFactoryImpl extends JXPathContextFactoryReferenceImpl {

    public JXPathContext newContext(
            JXPathContext parentContext,
            Object contextBean) {
        return new JXPathContextImpl(parentContext, contextBean);
    }
}
