package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;


/**
 * Fixing JXPath bug where the function name prefix is treated as namespaceURI for some reason.
 */
public class JXPathContextImpl extends JXPathContextReferenceImpl {

    protected JXPathContextImpl(JXPathContext parentContext,
                                         Object contextBean) {
        super(parentContext, contextBean);
    }

    @Override
    public Function getFunction(QName functionName, Object[] parameters) {
        if(functionName.getPrefix() != null)
            functionName = new QName(getNamespaceURI(functionName.getPrefix()), functionName.getName()) ;
       return super.getFunction(functionName, parameters);
    }
}
