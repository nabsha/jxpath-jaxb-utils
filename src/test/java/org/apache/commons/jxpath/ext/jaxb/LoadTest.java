package org.apache.commons.jxpath.ext.jaxb;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mikehan.testschema.RepeatingSequenceSubType;
import org.mikehan.testschema.RepeatingSequenceSubType2;
import org.mikehan.testschema.RepeatingSequenceType;
import org.mikehan.testschema.RootElement;

import javax.xml.xpath.XPathExpressionException;
import java.util.*;

/**
 * Slowly grows the array/values based on parameters to test the performance impact of {@link org.apache.commons.jxpath.ext.jaxb.model.JAXBNodePointerFactory#getBeanInfo(Object)}.
 * Idea is that the parsed JAXB annotations are cached so only need to be done once.
 */
@RunWith(Parameterized.class)
public class LoadTest {

    private static final Map<String, String> namespaces = new HashMap<>();
    static{
        namespaces.put("targetNs", "urn:jxpath-jaxb-utils");
    }
    private static final JaxbUtils UTIL = JaxbUtils.instance(namespaces);

    @Parameterized.Parameter(0)
    public  RootElement rootElement;

    @Parameterized.Parameter(1)
    public  int repeatingElements;

    @Parameterized.Parameter(2)
    public  int indexPrefix;

//    public LoadTest(RootElement rootElement, int repeatingElements, int indexPrefix){
//        this.rootElement = rootElement;
//        this.repeatingElements = repeatingElements;
//        this.indexPrefix = indexPrefix;
//    }

    @Parameterized.Parameters
    public static List<Object[]> data() {
        List<Object[]> result = new ArrayList<>();
        for(int i = 1; i < 151; i++){
          int repeatingTypeSize = (int)Math.ceil(i / 3);
            if(repeatingTypeSize < 1){
                repeatingTypeSize = 1;
            }
            RootElement generatedElement = createValidRootElement(repeatingTypeSize, i);
            result.add(new Object[]{generatedElement, repeatingTypeSize, i});
        }
        return result;
    }

    @Test
    public void testXPathQuery() throws XPathExpressionException{
        Iterator<RepeatingSequenceType> recurringElementsIt = UTIL.getValues(rootElement, "targetNs:recurringElement", RepeatingSequenceType.class);
        Assert.assertEquals(repeatingElements, IteratorUtils.toArray(recurringElementsIt).length);

        Iterator<String> prefixedValues = UTIL.getValues(rootElement, "descendant::targetNs:nonSequencedTypeElement1[starts-with(., '" + indexPrefix + "')]", String.class);
        Assert.assertEquals(repeatingElements, IteratorUtils.toArray(prefixedValues).length);
    }

    /**
     * @return RootElement with repeatingElements with matching {@link RootElement#getRecurringElement()} and one of the values prefixed with {@link #indexPrefix}
     *
     */
    public static RootElement createValidRootElement(int repeatingElements, int indexPrefix){
        RootElement rootElement = new RootElement();
        rootElement.setQualifiedAttribute(indexPrefix + "_qualifiedAttributeValue");

        for(int i = 1; i <= repeatingElements; i++){
            int typeOfRepeatingSequence = i % 3;
            RepeatingSequenceType repeatingElement = null;
            switch(typeOfRepeatingSequence){
                case 0: repeatingElement = new RepeatingSequenceType();break;
                case 1: repeatingElement = new RepeatingSequenceSubType();break;
                case 2: repeatingElement = new RepeatingSequenceSubType2();break;
            }

            repeatingElement.setNonSequencedTypeElement1(indexPrefix + "element2");

            rootElement.getRecurringElement().add(repeatingElement);
        }

        return rootElement;
    }
}
