package org.apache.commons.jxpath.ext.jaxb;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ext.jaxb.model.JAXBNodePointerFactory;
import org.apache.commons.jxpath.ext.jaxb.model.JXPathJAXBBeanInfo;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.lang.StringUtils;

/**
 * Common function library {@link org.apache.commons.jxpath.FunctionLibrary} that helps with some JAXB specific beans querying.
 */
public class CommonFunctions {
    public static final boolean isXsiType(final ExpressionContext context, final String namespace, final String localName){
        JXPathJAXBBeanInfo beanInfo = JAXBNodePointerFactory.getBeanInfo(context.getContextNodePointer().getNode());
        if(beanInfo != null){
            boolean namespaceMatch = StringUtils.equals(namespace, beanInfo.getNamespaceURI());
            boolean nameMatch = StringUtils.equals(localName, beanInfo.getTypeName());
            return nameMatch && namespaceMatch;
        }
        return false;
    }

    public static final boolean isClass(final ExpressionContext context, final String className){
        Class clazz = context.getContextNodePointer().getNode().getClass();
        Class matchAgainstClass = null;
        try {
            matchAgainstClass = Class.forName(className);
        } catch (ClassNotFoundException cne) {
            throw new IllegalArgumentException("Cannot find class on classpath " + className, cne);
        }
        return clazz.equals(matchAgainstClass);
    }

    public static final boolean isNull(final ExpressionContext context){
        Pointer nodePointer = context.getContextNodePointer();
        return nodePointer instanceof NullPointer || nodePointer.getValue() == null;
    }

    public static final boolean isNotNull(final ExpressionContext context){
        return !isNull(context);
    }
}
