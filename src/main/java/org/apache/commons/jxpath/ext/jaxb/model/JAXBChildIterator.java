package org.apache.commons.jxpath.ext.jaxb.model;

import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPropertyPointer;
import org.apache.commons.jxpath.ri.model.beans.PropertyIterator;
import org.apache.commons.lang.StringUtils;

/**
 * QNamed Element/Attribute iterator based on a flag.
 */
public class JAXBChildIterator extends PropertyIterator {

    private final BeanPointer parent;
    private final JXPathJAXBBeanInfo beanInfo;
    private final boolean isAttribute;
    private final boolean reverse;
    public JAXBChildIterator(final BeanPointer parent, final QName qName, final JXPathJAXBBeanInfo beanInfo){
        super(parent, deriveName(qName), false, null);
        this.beanInfo = beanInfo;
        this.parent = parent;
        this.isAttribute = true;
        this.reverse = false;
        ((BeanPropertyPointer)getPropertyPointer()).setAttribute(isAttribute);
    }

    public JAXBChildIterator(final BeanPointer parent, final QName qName, final JXPathJAXBBeanInfo beanInfo, final boolean reverse, final NodePointer startWith){
        super(parent, deriveName(qName), reverse, startWith);
        this.beanInfo = beanInfo;
        this.parent = parent;
        this.isAttribute = false;
        this.reverse = reverse;
        ((BeanPropertyPointer)getPropertyPointer()).setAttribute(isAttribute);
    }

    public static String deriveName(final QName qname){
        if(qname == null)
            return null;
        if(StringUtils.isBlank(qname.getPrefix()) && (StringUtils.isBlank(qname.getName())
           || "*".equals(qname.getName())    )){
            return null;
        }
        return qname.toString();
    }

    @Override
    protected void prepareForIndividualProperty(final String name) {
        QName qName = new QName(name);
        String uri = null;
        String localName = qName.getName();
        if("*".equals(qName.getPrefix()))
            uri = "*";
        else if(qName.getPrefix() != null){
            uri = parent.getNamespaceURI(qName.getPrefix());
        }
        int index = Integer.MIN_VALUE;
        JAXBPropertyDescriptor matchingPropertyDescriptor = null;
        JAXBPropertyDescriptor[] descriptors = isAttribute ? beanInfo.getAttributeBeanInfo().getPropertyDescriptors() : beanInfo.getPropertyDescriptors();

        if(reverse){
            for(int i = descriptors.length - 1; i >= 0; i--){
                JAXBPropertyDescriptor descriptor = descriptors[i];
                boolean isMatch = JAXBPointer.isPropertyNameMatch(uri, localName, descriptor);
                if (isMatch){
                    matchingPropertyDescriptor = descriptor;
                    index = i;
                    break;
                }
            }
        }
        else{
            for(int i = 0; i < descriptors.length; i++){
                JAXBPropertyDescriptor descriptor = descriptors[i];
                boolean isMatch = JAXBPointer.isPropertyNameMatch(uri, localName, descriptor);
                if (isMatch){
                    matchingPropertyDescriptor = descriptor;
                    index = i;
                    break;
                }
            }
        }


        if(matchingPropertyDescriptor != null)
            ((BeanPropertyPointer)getPropertyPointer()).setPropertyIndex(index);
        else if(uri == null)
            super.prepareForIndividualProperty("noNS:" + localName);
    }

}
