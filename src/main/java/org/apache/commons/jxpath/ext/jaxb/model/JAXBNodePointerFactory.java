package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointerFactory;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Mike on 24/08/2016.
 */
public class JAXBNodePointerFactory extends BeanPointerFactory {

    private static final transient Map<Class, JXPathJAXBBeanInfo> CLASS_INFO = new LinkedHashMap();


    public int getOrder() {
        return 1;
    }

    public NodePointer createNodePointer(QName name, Object bean, Locale locale) {

        if(bean.getClass().getAnnotation(XmlType.class) != null ){
            JXPathJAXBBeanInfo bi = getBeanInfo(bean);
            return new JAXBPointer(name, bean, bi, locale);
        }
        else if(bean.getClass().getAnnotation(XmlRootElement.class) != null){
            JXPathJAXBBeanInfo bi = getBeanInfo(bean);
            return new JAXBPointer(name, bean, bi, locale);
        }
        return super.createNodePointer(name, bean, locale);
    }

    public NodePointer createNodePointer(NodePointer parent, QName name, Object bean) {
        if(bean == null) {
            return new NullPointer(parent, name);
        } else {
            JXPathJAXBBeanInfo bi = getBeanInfo(bean);
            if(bi != null)
                return new JAXBPointer(parent, name, bean, bi);
        }
        return super.createNodePointer(parent, name, bean);
    }

    public static final JXPathJAXBBeanInfo getBeanInfo(Object bean){
        if(CLASS_INFO.containsKey(bean.getClass()))
        {
            return CLASS_INFO.get(bean.getClass());
        }
        JXPathJAXBBeanInfo bi = populateJAXBClassInfo(bean.getClass());
        CLASS_INFO.put(bean.getClass(), bi);
        return bi;
    }

    public static JXPathJAXBBeanInfo populateJAXBClassInfo(Class beanClass){
        if(beanClass != null && (beanClass.getAnnotation(XmlType.class) != null || beanClass.getAnnotation(XmlRootElement.class) != null)){
            return new JXPathJAXBBeanInfo(beanClass);
        }
        return null;
    }
}