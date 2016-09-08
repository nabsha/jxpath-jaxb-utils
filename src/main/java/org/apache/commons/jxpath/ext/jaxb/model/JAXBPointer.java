package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyPointer;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * Created by Mike on 24/08/2016.
 */
public class JAXBPointer extends BeanPointer {

    private Object obj;
    private transient QName qname;
    private transient JXPathJAXBBeanInfo beanInfo;
    //true = attributes only, false = elements only
    private boolean attributeView;

    public JAXBPointer(QName name, Object obj, JXPathJAXBBeanInfo beanInfo, Locale locale)
    {
        this(name, obj, beanInfo, locale, false, null);
    }

    public JAXBPointer(QName name, Object obj, JXPathJAXBBeanInfo beanInfo, Locale locale, boolean attributeView, final NamespaceResolver nsResolver){
        super(name, obj, attributeView ? beanInfo.getAttributeBeanInfo() : beanInfo, locale);
        this.obj = obj;
        this.beanInfo = beanInfo;
        this.qname = name;
        this.attributeView = attributeView;
        setNamespaceResolver(nsResolver);
    }

    public JAXBPointer(NodePointer parent, QName name, Object obj, JXPathJAXBBeanInfo beanInfo)
    {
        this(parent, name, obj, beanInfo, false, parent == null ? null : parent.getNamespaceResolver());
    }

    public JAXBPointer(NodePointer parent, QName name, Object obj, JXPathJAXBBeanInfo beanInfo, final boolean attributeView, final NamespaceResolver nsResolver)
    {
        super(parent, name, obj, attributeView ? beanInfo.getAttributeBeanInfo() : beanInfo);
        this.obj = obj;
        this.beanInfo = beanInfo;
        this.qname = name;
        this.attributeView = attributeView;
        setNamespaceResolver(nsResolver);
    }

    public synchronized NamespaceResolver getNamespaceResolver() {
        NamespaceResolver result = super.getNamespaceResolver();
        if(result != null)
            result.setNamespaceContextPointer(null);
        return result;
    }

    @Override
    public String getNamespaceURI() {
//        if(parent != null){
//           return getNamespaceURI(qname.getPrefix());
//        }
        return beanInfo.getNamespaceURI();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        String uri = getNamespaceResolver().getNamespaceURI(prefix);
        if(uri == null && prefix != null && !"*".equals(prefix)){//Prefix not defined
            throw new JXPathException("Prefix {" + prefix + "} is not valid in this context.");
            }
        return uri;
    }

    @Override
    public PropertyPointer getPropertyPointer() {
        return new JAXBPropertyPointer(this, attributeView ? beanInfo.getAttributeBeanInfo() : beanInfo);
    }

    @Override
    public boolean isContainer() {
        return beanInfo.isXmlValueType();
    }

    /**
     * This will return attributes no matter which view the pointer has taken.
     */
    @Override
    public NodeIterator attributeIterator(final QName name) {
        return new JAXBChildIterator(withAttributeView(), name, beanInfo);
    }

    /**
     * This will return attributes or elements based on attributeView due to the way JXPath iterates over properties.
     */
    @Override
    public NodeIterator createNodeIterator(final String property, final boolean reverse, NodePointer startWith) {
        return new JAXBChildIterator(this, property == null ? null : new QName(property), beanInfo, reverse, startWith);
    }

    public JAXBPointer withAttributeView(){
        return attributeView ? this : new JAXBPointer(parent, qname, obj, beanInfo, true, getNamespaceResolver());
    }

    public JAXBPointer withElementView(){
        return !attributeView ? this : new JAXBPointer(parent, qname, obj, beanInfo, false, getNamespaceResolver());
    }

    public boolean isValidProperty(QName name) {
        return hasNodeName(this, attributeView ? beanInfo.getAttributeBeanInfo() : beanInfo, name);
    }

    public static final boolean hasNodeName(NodePointer parent, JXPathBeanInfo beanInfo, QName qName){
        return getNodeName(parent, beanInfo, qName) != null;
    }

    public static final JAXBPropertyDescriptor getNodeName(NodePointer parent, JXPathBeanInfo beanInfo, QName qName){
        JAXBPropertyDescriptor[] propertyDescriptors = null;
        if(beanInfo instanceof JXPathJAXBBeanAttributeInfo)
            propertyDescriptors = ((JXPathJAXBBeanAttributeInfo)beanInfo).getPropertyDescriptors();
        if(beanInfo instanceof  JXPathJAXBBeanInfo)
            propertyDescriptors = ((JXPathJAXBBeanInfo)beanInfo).getPropertyDescriptors();
        if(propertyDescriptors != null){
            String uri = null;
            String localName = qName.getName();
            if(qName.getPrefix() != null && !"*".equals(qName.getPrefix()))
                uri = parent.getNamespaceURI(qName.getPrefix());
            else
                uri = "*";
            for(final JAXBPropertyDescriptor propertyDescriptor : propertyDescriptors){
                if(isPropertyNameMatch(uri, localName, propertyDescriptor)){
                    return propertyDescriptor;
                }
            }
        }
        return null;
    }

    public static boolean isPropertyNameMatch(String uri, String localName, JAXBPropertyDescriptor descriptor){
        boolean localNameMatch = StringUtils.equals(localName, descriptor.getQName().getLocalPart());
        boolean uriMatch = "*".equals(uri) || StringUtils.equals(uri, descriptor.getQName().getNamespaceURI());
        if(descriptor.isQualified() && localNameMatch && uriMatch) {
            return true;
        }
        else if(!descriptor.isQualified() && localNameMatch && (uri == null || "*".equals(uri))) {
            return true;
        }
        return false;
    }
}
