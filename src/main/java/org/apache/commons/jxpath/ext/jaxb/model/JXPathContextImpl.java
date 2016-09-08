package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.ext.jaxb.CommonFunctions;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.QName;

import java.util.HashMap;
import java.util.Map;


/**
 * Fixing JXPath bug where the function name prefix is treated as namespaceURI for some reason.
 */
public class JXPathContextImpl extends JXPathContextReferenceImpl {

    private static final Map<String, String> DEFAULT_NAMESPACES = new HashMap<>();
    static{
        addNodePointerFactory(new JAXBNodePointerFactory());

        DEFAULT_NAMESPACES.put("xmlns", "http://www.w3.org/2000/xmlns/");
        DEFAULT_NAMESPACES.put("xml", "http://www.w3.org/XML/1998/namespace");
        DEFAULT_NAMESPACES.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");

        //This namespace is used to qualify unqualified element names
        DEFAULT_NAMESPACES.put("noNS", "");
    }

    protected JXPathContextImpl(JXPathContext parentContext,
                                         Object contextBean) {
        super(parentContext, contextBean);
        if(parentContext == null){
            setupJAXBNamespacesAndFunctions(this, DEFAULT_NAMESPACES);
        }
    }

    public void registerNamespaces(final Map<String, String> namespaces) {
        if(namespaces != null) {
            for (final Map.Entry<String, String> namespaceEntry : namespaces.entrySet()) {
                registerNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
            }
        }
    }

    @Override
    public Function getFunction(QName functionName, Object[] parameters) {
        if(functionName.getPrefix() != null)
            functionName = new QName(getNamespaceURI(functionName.getPrefix()), functionName.getName()) ;
       return super.getFunction(functionName, parameters);
    }

    /**
     * Sets up a parentContext for {@link JXPathContext} that will contain:
     * *JAXB {@link CommonFunctions} at xsi: prefix
     * *Default namespace prefixes {@link #DEFAULT_NAMESPACES}
     * *Supplied namespace prefixes
     * @param namespaces
     * @return
     */
    public static final void setupJAXBNamespacesAndFunctions(final JXPathContext rootContext, final Map<String, String> namespaces){

        //Default namespaces and supplied namespaces
//        for (final Map.Entry<String, String> namespaceEntry : DEFAULT_NAMESPACES.entrySet()) {
//            rootContext.registerNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
//        }
        if(namespaces != null) {
            for (final Map.Entry<String, String> namespaceEntry : namespaces.entrySet()) {
                rootContext.registerNamespace(namespaceEntry.getKey(), namespaceEntry.getValue());
            }
        }

        //Dont throw exceptions on getValue, just return null
        rootContext.setLenient(true);

        //Register CommonFunctions
        FunctionLibrary funcLibrary = new FunctionLibrary();
        funcLibrary.addFunctions(new ClassFunctions(CommonFunctions.class, rootContext.getNamespaceURI("xsi")));
        funcLibrary.addFunctions(new PackageFunctions("", (String)null));
        rootContext.setFunctions(funcLibrary);

    }
}
