package org.apache.commons.jxpath.ext.jaxb.model;

import javax.xml.namespace.QName;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * {@link PropertyDescriptor} that adds namespace and whether it is qualified. i.e. abc:element or just element
 */
public class JAXBPropertyDescriptor extends PropertyDescriptor{

    private QName qname;
    private boolean isQualified;
    private PropertyDescriptor xmlValueProperty;
    public JAXBPropertyDescriptor(final QName qname, final boolean isQualified, final Method readMethod, final Method writeMethod) throws IntrospectionException {
        this(qname, isQualified, readMethod, writeMethod, null);
    }

    public JAXBPropertyDescriptor(final QName qname, final boolean isQualified, final Method readMethod, final Method writeMethod, final PropertyDescriptor xmlValueProperty) throws IntrospectionException {
        super(qname.getLocalPart(), readMethod, writeMethod);
        this.qname = qname;
        this.isQualified = isQualified;
        this.xmlValueProperty = xmlValueProperty;
    }

    public QName getQName(){
        return qname;
    }

    public boolean isQualified(){
        return isQualified;
    }

    public PropertyDescriptor getXmlValueProperty(){return xmlValueProperty;}

}
