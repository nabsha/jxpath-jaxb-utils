package org.apache.commons.jxpath.ext.jaxb;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ext.jaxb.model.JXPathContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlType;
import javax.xml.xpath.XPathExpressionException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TLDR:
 * Adds user provided namespaces to the context.
 *
 */
public class JaxbUtils {
    private static Logger LOG = LoggerFactory.getLogger(JaxbUtils.class);
    private static final JaxbUtils DEFAULT_INSTANCE;
    private Map<String, String> namespaces = null;
    static
    {
        DEFAULT_INSTANCE = new JaxbUtils((Map)null);
    }

    private JaxbUtils(Map<String, String> namespaces) {
        this.namespaces = namespaces == null ? Collections.EMPTY_MAP : new HashMap<>(namespaces);
    }


    public static JaxbUtils instance(){
        return DEFAULT_INSTANCE;
    }
    public static JaxbUtils instance(Map<String, String> namespaces){
        return new JaxbUtils(namespaces);
    }

    public Object getValue(Object sourceNode, String expression) throws XPathExpressionException {
        return getValue(sourceNode, expression, Object.class);
    }

    public <T> T getValue(Object sourceNode, String expression, Class<T> type) throws XPathExpressionException {
        return (T) newContext(sourceNode).getValue(expression, type);
    }

    public Iterator<? extends Object> getValues(Object sourceNode, String expression) throws XPathExpressionException {
        return getValues(sourceNode, expression, Object.class);
    }

    public <T> Iterator<T> getValues(Object sourceNode, String expression, Class<T> type) throws XPathExpressionException {
        return (Iterator<T>) newContext(sourceNode).iterate(expression);
    }

    public Pointer getPointer(Object sourceNode, String expression) throws XPathExpressionException {
        return newContext(sourceNode).getPointer(expression);
    }

    public Iterator<Pointer> getPointers(Object sourceNode, String expression) throws XPathExpressionException {
        return newContext(sourceNode).iteratePointers(expression);
    }


    public JXPathContext newContext(final Object sourceNode){
        assert sourceNode.getClass().getAnnotation(XmlType.class) != null : "Non-JAXB type has been passed in";
        JXPathContext context = JXPathContext.newContext(sourceNode);
        ((JXPathContextImpl) context).registerNamespaces(namespaces);

        return context;
    }

}
