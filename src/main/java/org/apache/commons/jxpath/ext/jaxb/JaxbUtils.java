package org.apache.commons.jxpath.ext.jaxb;

import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ext.jaxb.model.JAXBNodePointerFactory;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlType;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TLDR:
 * Registers {@link CommonFunctions} that are used in predicates.
 * Registers xmlns, xml, xsi, noNS namespaces in the parent context (JAXB has no context of prefixes)
 * Adds user provided namespaces.
 *
 * Potential TODO: Move to {@link org.apache.commons.jxpath.ext.jaxb.model.JXPathContextFactoryImpl} ?
 */
public class JaxbUtils {
    private static Logger LOG = LoggerFactory.getLogger(JaxbUtils.class);
    private static final JaxbUtils DEFAULT_INSTANCE ;
    private Map<String, String> namespaces = null;
    private JXPathContext PARENT_CONTEXT = JXPathContext.newContext(null);
    private static final Map<String, String> DEFAULT_NAMESPACES = new HashMap<>();
    static
    {
        JXPathContextReferenceImpl.addNodePointerFactory(new JAXBNodePointerFactory());
        DEFAULT_NAMESPACES.put("xmlns", "http://www.w3.org/2000/xmlns/");
        DEFAULT_NAMESPACES.put("xml", "http://www.w3.org/XML/1998/namespace");
        DEFAULT_NAMESPACES.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        //This namespace is used to qualify unqualified element names
        DEFAULT_NAMESPACES.put("noNS", "");

        DEFAULT_INSTANCE = new JaxbUtils((Map)null);
    }

    private JaxbUtils(Map<String, String> namespaces) {
        this.namespaces = new HashMap<>(DEFAULT_NAMESPACES);
        if(namespaces != null)
            this.namespaces.putAll(namespaces);
        init();
    }

    private void init(){
        if(namespaces != null) {
            for (final Map.Entry<String, String> namespaceEntry : namespaces.entrySet()) {
                PARENT_CONTEXT.registerNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
            }
        }

        //Dont throw exceptions on getValue, just return null
        PARENT_CONTEXT.setLenient(true);

        //Register CommonFunctions
        FunctionLibrary funcLibrary = new FunctionLibrary();
        funcLibrary.addFunctions(new ClassFunctions(CommonFunctions.class, PARENT_CONTEXT.getNamespaceURI("xsi")));
        funcLibrary.addFunctions(new PackageFunctions("", (String)null));
        PARENT_CONTEXT.setFunctions(funcLibrary);
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
        JXPathContext context = JXPathContext.newContext(PARENT_CONTEXT, sourceNode);

        return context;
    }

}
