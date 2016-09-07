package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPropertyPointer;
import org.apache.commons.jxpath.util.ValueUtils;
import org.apache.commons.lang.reflect.ConstructorUtils;

/**
 * Created by Mike on 31/08/2016.
 */
public class JAXBPropertyPointer extends BeanPropertyPointer {

    private JXPathBeanInfo beanInfo;
    public JAXBPropertyPointer(NodePointer parent, JXPathBeanInfo beanInfo) {
        super(parent, beanInfo);
        this.beanInfo = beanInfo;
    }


    @Override
    public void setPropertyName(String propertyName) {
        JAXBPropertyDescriptor propertyDescriptor = JAXBPointer.getNodeName(parent, beanInfo, new QName(propertyName));
        if(propertyDescriptor != null)
            super.setPropertyName(propertyDescriptor.getName());
        else
            super.setPropertyName(propertyName);
    }


    /**
     * //Once the {@link javax.xml.bind.annotation.XmlValue} stuff is figured out, this can be enabled. Otherwise this hides the true objects without good reason.
     *
     * @Override
    public NodePointer getImmediateValuePointer() {
        NodePointer wrapperPointer = super.getImmediateValuePointer();
        JAXBPropertyDescriptor descriptor = JAXBPointer.getNodeName(this, beanInfo, new QName(getPropertyName()));
        if(descriptor != null && descriptor.getXmlValueProperty() != null && getBaseValue() != null){
            //JAXBXmlValuePropertyEditor propEditor = new JAXBXmlValuePropertyEditor(getBaseValue());
            return newChildNodePointer(
                    (NodePointer) wrapperPointer.clone(),
                    new QName(descriptor.getXmlValueProperty().getName()),
                    ValueUtils.getValue(getBaseValue(), descriptor.getXmlValueProperty()));
        }
        return wrapperPointer;
    }

    @Override
    public void setValue(Object value) {
        JAXBPropertyDescriptor descriptor = JAXBPointer.getNodeName(this, beanInfo, new QName(getPropertyName()));
        if(descriptor != null && descriptor.getXmlValueProperty() != null){
            Object wrapperObject = getBaseValue();
            if(wrapperObject == null){
                try {
                    wrapperObject = ConstructorUtils.invokeConstructor(descriptor.getPropertyType(), null);
                }
                catch (ReflectiveOperationException e){
                    throw new JXPathException("Unable to create a XmlValue including JAXB object with no-arg constructor: " + descriptor ,e);
                }
            }
            ValueUtils.setValue(wrapperObject, descriptor.getXmlValueProperty(), value);
            super.setValue(wrapperObject);
        }
        else
            super.setValue(value);
    }
*/
    @Override
    public NodePointer createPath(JXPathContext context) {
        return super.createPath(context);
    }
}
