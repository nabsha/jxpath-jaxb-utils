package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.JXPathBeanInfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Returns only the xsd:attribute information related to the bean.
 */
public class JXPathJAXBBeanAttributeInfo implements JXPathBeanInfo {

    private JXPathJAXBBeanInfo parentBeanInfo;

    private transient Map<Field, XmlAttribute> classFieldToXmlAttribute = null;

    private JAXBPropertyDescriptor[] attributeDescriptors = null;

    public JXPathJAXBBeanAttributeInfo(JXPathJAXBBeanInfo parentBeanInfo){
        this.parentBeanInfo = parentBeanInfo;
    }

    @Override
    public JAXBPropertyDescriptor[] getPropertyDescriptors() {
        return getAttributeDescriptors();
    }

    @Override
    public Class getDynamicPropertyHandlerClass() {
        return parentBeanInfo.getDynamicPropertyHandlerClass();
    }

    @Override
    public PropertyDescriptor getPropertyDescriptor(final String attributeName) {
        return getAttributeDescriptor(attributeName);
    }

    @Override
    public boolean isDynamic() {
        return parentBeanInfo.isDynamic();
    }

    @Override
    public boolean isAtomic() {
        return parentBeanInfo.isAtomic();
    }

    public JXPathJAXBBeanInfo getParentBeanInfo() {
        return parentBeanInfo;
    }



    public JAXBPropertyDescriptor getAttributeDescriptor(final String name) {
        for(final JAXBPropertyDescriptor attributeDescriptor : getAttributeDescriptors()){
            if(name.equals(attributeDescriptor.getName())){
                return attributeDescriptor;
            }
        }
        return null;
    }

    public synchronized JAXBPropertyDescriptor[] getAttributeDescriptors() {
        if(attributeDescriptors == null){
            Map<Field, XmlAttribute> attributeForClass = getAttributeForClass();
            attributeDescriptors = new JAXBPropertyDescriptor[attributeForClass.size()];
            int index = 0;
            for(final Field field : attributeForClass.keySet()){
                XmlAttribute attribute = attributeForClass.get(field);
                JAXBPropertyDescriptor attributeDescriptor;
                try {
                    attributeDescriptor = JXPathJAXBBeanInfo.getPropertyDescriptorForField(parentBeanInfo.getBeanClass(), parentBeanInfo.getBeanInfoForClass(), attribute.name(), field);
                } catch (IntrospectionException ie) {
                    attributeDescriptors = null;
                    throw new RuntimeException("Unexpected error while trying to create a (attribute) property descriptor for " + field, ie);
                }

                this.attributeDescriptors[index] = attributeDescriptor;
                index++;
            }
        }
        return attributeDescriptors;
    }

    /** Return a map representing all fields that are annotated with {@link XmlAttribute}, i.e. fields directly mapped to xml. This includes any inherited fields.
     * @return
     */
    protected synchronized Map<Field, XmlAttribute> getAttributeForClass()
    {
        if (classFieldToXmlAttribute == null) {
            if (parentBeanInfo.getBeanClass().getAnnotation(XmlType.class) == null) {
                classFieldToXmlAttribute = Collections.emptyMap();
                return classFieldToXmlAttribute;
            }
            classFieldToXmlAttribute = new LinkedHashMap<>();
            Class currentClazz = parentBeanInfo.getBeanClass();
            do{
                for (final Field classField : currentClazz.getDeclaredFields()) {
                    if (classField.getAnnotation(XmlAttribute.class) != null) {
                        classFieldToXmlAttribute.put(classField, classField.getAnnotation(XmlAttribute.class));
                    }
                }
                currentClazz = currentClazz.getSuperclass();
            }while(currentClazz != null);
        }
        return classFieldToXmlAttribute;
    }
}
