package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * {@link JXPathBeanInfo} that returns only the xsd:elements. For attributes, see {@link #getAttributeBeanInfo()}.
 */
public class JXPathJAXBBeanInfo implements JXPathBeanInfo {
    private Class clazz;
    private JXPathJAXBBeanAttributeInfo attributeInfo;
    private transient Map<Field, XmlElement> classFieldToXmlElement = null;
    private JAXBPropertyDescriptor[] propertyDescriptors = null;

    public JXPathJAXBBeanInfo(Class clazz) {
        this.clazz = clazz;
    }

    public synchronized JXPathJAXBBeanAttributeInfo getAttributeBeanInfo(){
        if(attributeInfo == null){
            attributeInfo = new JXPathJAXBBeanAttributeInfo(this);
        }
        return attributeInfo;
    }

    public Class getBeanClass(){return clazz;}

    @Override
    public Class getDynamicPropertyHandlerClass() {
        return null;
    }

    @Override
    public boolean isAtomic() {
        return false;
    }

    public boolean isXmlValueType(){
        for(final JAXBPropertyDescriptor descriptor : getPropertyDescriptors()){
            if(descriptor.getXmlValueProperty() != null)
                return true;
        }
        return false;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    public String getTypeName(){
        if(clazz.getAnnotation(XmlType.class) != null){
            XmlType xmlType = (XmlType) clazz.getAnnotation(XmlType.class);
            return xmlType.name();
        }
        return null;
    }

    public String getElementName(){
        if(clazz.getAnnotation(XmlRootElement.class) != null){
            XmlRootElement xmlElement = (XmlRootElement) clazz.getAnnotation(XmlRootElement.class);
            return xmlElement.name();
        }
        return null;
    }

    public String getNamespaceURI(){
        return getNamespaceURI(clazz);
    }

    //Not fully correct as the XmlElement could also specify it, but is rare
    public static String getNamespaceURI(Class clazz){
        if(clazz.getAnnotation(XmlType.class) != null && !"##default".equals(((XmlType)clazz.getAnnotation(XmlType.class)).namespace())){
            return ((XmlType)clazz.getAnnotation(XmlType.class)).namespace();
        }
        else if(clazz.getAnnotation(XmlRootElement.class) != null && !"##default".equals(((XmlRootElement)clazz.getAnnotation(XmlRootElement.class)).namespace())){
            return ((XmlRootElement)clazz.getAnnotation(XmlRootElement.class)).namespace();
        }
        else if(getXmlSchema(clazz) != null){
            return getXmlSchema(clazz).namespace();
        }
        return null;
    }

    public static boolean isQualified(Class clazz, final XmlElement element) {
        if(element != null && !"##default".equals(element.namespace()) && StringUtils.isNotEmpty(element.namespace())){
           return true;
        }
        else{
            return getXmlSchema(clazz) != null ? getXmlSchema(clazz).elementFormDefault() == XmlNsForm.QUALIFIED : false;
        }
    }

    public static boolean isQualified(Class clazz, final XmlAttribute attribute) {
        if(attribute != null && !"##default".equals(attribute.namespace()) && StringUtils.isNotEmpty(attribute.namespace())){
            return true;
        }
        else{
            return getXmlSchema(clazz) != null ? getXmlSchema(clazz).attributeFormDefault() == XmlNsForm.QUALIFIED : false;
        }
    }

    @Override
    public JAXBPropertyDescriptor getPropertyDescriptor(final String name) {
        if(name == null)
            return null;
        for(final JAXBPropertyDescriptor propertyDescriptor : getPropertyDescriptors()){
            if(name.equals(propertyDescriptor.getName())){
                return propertyDescriptor;
            }
        }
        return null;
    }

    @Override
    public synchronized JAXBPropertyDescriptor[] getPropertyDescriptors() {
        if(propertyDescriptors == null){
            Map<Field, XmlElement> elementsForClass = getElementsForClass();
            propertyDescriptors = new JAXBPropertyDescriptor[elementsForClass.size()];
            int index = 0;
            for(final Field field : elementsForClass.keySet()){
                XmlElement element = elementsForClass.get(field);
                JAXBPropertyDescriptor propertyDescriptor;
                try {
                    propertyDescriptor = getPropertyDescriptorForField(clazz, getBeanInfoForClass(), element != null ? element.name() : field.getName(), field);
                }
                catch (IntrospectionException ie){
                    propertyDescriptors = null;
                    throw new RuntimeException("Unexpected error while trying to create a property descriptor for " + field, ie);
                }

                propertyDescriptors[index] = propertyDescriptor;
                index++;
            }
        }
        return propertyDescriptors;
    }

    public BeanInfo getBeanInfoForClass(){
        return getBeanInfo(clazz);
    }

    public static BeanInfo getBeanInfo(Class clazz){
        try {
            return Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException ie) {
            throw new RuntimeException("Unexpected error while trying to determine standard java BeanInfo " + clazz, ie);
        }
    }

    public static JAXBPropertyDescriptor getPropertyDescriptorForField(final Class clazz, final BeanInfo beanInfo, final String xmlName, final Field field) throws IntrospectionException{
        PropertyDescriptor matchingBeanPropertyDescriptor = null;
        for(final PropertyDescriptor beanPropertyDescriptor : beanInfo.getPropertyDescriptors()){
            if(field.getName().equals(beanPropertyDescriptor.getName()) ||
               xmlName.equals(beanPropertyDescriptor.getName())     ){
                matchingBeanPropertyDescriptor = beanPropertyDescriptor;
                break;
            }
        }
        if(matchingBeanPropertyDescriptor == null){
            throw new RuntimeException("Unable to match any bean properties for xmlName "+ xmlName + " in field " + field);
        }
        QName qname = new QName(getNamespaceURI(clazz), xmlName);
        boolean isQualified;
        if(field.getAnnotation(XmlAttribute.class) != null)
            isQualified = isQualified(clazz, field.getAnnotation(XmlAttribute.class));
        else
            isQualified = isQualified(clazz, field.getAnnotation(XmlElement.class));
        PropertyDescriptor simpleContent = getSimpleContent(field.getType());

        if(simpleContent == null)
            return new JAXBPropertyDescriptor(qname, isQualified, matchingBeanPropertyDescriptor.getReadMethod(), matchingBeanPropertyDescriptor.getWriteMethod());
        else
            return new JAXBPropertyDescriptor(qname, isQualified, matchingBeanPropertyDescriptor.getReadMethod(), matchingBeanPropertyDescriptor.getWriteMethod(), simpleContent);
    }

    public static PropertyDescriptor getSimpleContent(final Class clazz) {
        Field simpleContentField = null;
        for (final Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(XmlValue.class) != null) {
                simpleContentField = field;
                break;
            }
        }
        if (simpleContentField != null) {
            BeanInfo beanInfo = getBeanInfo(clazz);
            for (final PropertyDescriptor beanPropertyDescriptor : beanInfo.getPropertyDescriptors()) {
                if (simpleContentField.getName().equals(beanPropertyDescriptor.getName())) {
                    return beanPropertyDescriptor;
                }
            }
        }
    return null;
    }

    /** Return a map representing all fields that are annotated with {@link XmlElement}, i.e. fields directly mapped to xml. This includes any inherited fields.
     * @return
     */
    protected synchronized Map<Field, XmlElement> getElementsForClass(){
            if (classFieldToXmlElement == null) {
                if (clazz.getAnnotation(XmlType.class) == null) {
                    classFieldToXmlElement = Collections.emptyMap();
                    return classFieldToXmlElement;
                }
                classFieldToXmlElement = new LinkedHashMap<>();
                List<Map<Field, XmlElement>> reverseFieldsMapList = new ArrayList<>();

                Class currentClazz = clazz;
                do{
                    reverseFieldsMapList.add(getElementsForClass(currentClazz));
                    currentClazz = currentClazz.getSuperclass();
                }while(currentClazz != null && currentClazz.getAnnotation(XmlType.class) != null);

                Collections.reverse(reverseFieldsMapList);
                for(final Map<Field, XmlElement> map : reverseFieldsMapList){
                    classFieldToXmlElement.putAll(map);
                }

            }

        return classFieldToXmlElement;
    }

    protected static synchronized Map<Field, XmlElement> getElementsForClass(final Class inputClass){
        if(inputClass.getAnnotation(XmlType.class) == null)
            return Collections.emptyMap();

        Map<Field, XmlElement> result = new LinkedHashMap<>();
        for(final String prop : ((XmlType)inputClass.getAnnotation(XmlType.class)).propOrder()){
            try{
                if(!"".equals(prop)) {
                    Field classField = inputClass.getDeclaredField(prop);
                    result.put(classField, classField.getAnnotation(XmlElement.class));
                }
            }
            catch (NoSuchFieldException nse){
                throw new RuntimeException("Unexpected exception while trying to retrieve an xml property " + prop + " from class" + result, nse);
            }
        }
        for (final Field classField : inputClass.getDeclaredFields()) {
            if (classField.getAnnotation(XmlElement.class) != null) {
                result.put(classField, classField.getAnnotation(XmlElement.class));
            }
        }

        return result;
    }

    public XmlSchema getXmlSchema(){
        return getXmlSchema(clazz);
    }

    public static XmlSchema getXmlSchema(Class clazz){
        return getPackageClass(clazz).getAnnotation(XmlSchema.class);
    }

    public Class<?> getPackageClass(){
        return getPackageClass(clazz);
    }

    public static Class<?> getPackageClass(Class clazz){
        try {
            return Class.forName(clazz.getPackage().getName() + "." + "package-info" );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Package info is not present, check your JAXB generated code", e);
        }
    }
}
