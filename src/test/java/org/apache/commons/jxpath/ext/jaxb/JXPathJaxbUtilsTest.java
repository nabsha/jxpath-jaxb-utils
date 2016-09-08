package org.apache.commons.jxpath.ext.jaxb;

import junit.framework.Assert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mikehan.testschema.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlType;
import javax.xml.xpath.XPathExpressionException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.*;


/**
 * Test cases for most common use cases using TestCaseSchemas.xsd ({@link RootElement}
 */
@RunWith(Parameterized.class)
public class JXPathJaxbUtilsTest {

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[1][0]);
    }
    private static final Map<String, String> namespaces = new HashMap<>();
    static{
        namespaces.put("targetNs", "urn:jxpath-jaxb-utils");
    }
    private static final JaxbUtils UTIL = JaxbUtils.instance(namespaces);


    /**
     * Tests {@link CommonFunctions#isXsiType(ExpressionContext, String, String)}
     *
     * @throws XPathExpressionException
     */
    @Test
    public void whenXSITypeFunctionIsCalledClassAnnotationIsSuccessfullyEvaluated() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();
        Iterator<String> validResult = UTIL.getValues(validRootElement, "targetNs:recurringElement[xsi:isXsiType('urn:jxpath-jaxb-utils', 'RepeatingSequenceType')]/targetNs:nonSequencedTypeElement1", String.class);
        Iterator<String> noResult = UTIL.getValues(validRootElement, "targetNs:recurringElement[xsi:isXsiType('urn:jxpath-jaxb-utils', 'RepeatingSequenceSubType2')]/targetNs:nonSequencedTypeElement1", String.class);

        Assert.assertTrue("Result should be returned when xsi type matches class", validResult.hasNext());
        Assert.assertFalse("No result should be returned with invalid predicate", noResult.hasNext());
    }

    /**
     * Tests {@link CommonFunctions#isClass(ExpressionContext, String)}
     *
     * @throws XPathExpressionException
     */
    @Test
    public void whenClassFunctionIsCalledClassAnnotationIsSuccessfullyEvaluated() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();
        Iterator<String> validResult = UTIL.getValues(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceType')]/targetNs:nonSequencedTypeElement1", String.class);
        Iterator<String> noResult = UTIL.getValues(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType2')]/targetNs:nonSequencedTypeElement1", String.class);

        Assert.assertTrue("Result should be returned when xsi type matches class", validResult.hasNext());
        Assert.assertFalse("No result should be returned with invalid predicate", noResult.hasNext());
    }

    @Test
    public void whenMultipleItemsInJAXBArraysIteratorReturnsAll() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();
        Iterator<RepeatingSequenceType> result = UTIL.getValues(validRootElement, "targetNs:recurringElement", RepeatingSequenceType.class);

        Assert.assertTrue(validRootElement.getRecurringElement().size() > 1);
        Assert.assertEquals(validRootElement.getRecurringElement().size(), IteratorUtils.toArray(result).length);
    }

    /**
     * See {@link RepeatingSequenceType} {@link XmlType#propOrder()} which defines xsd:sequence.
     * @throws XPathExpressionException
     */
    @Test
    public void whenPropertyOrderIsDefinedPropertiesAreReturnedInOrder() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();
        Iterator<Object> result = UTIL.getValues(validRootElement, "targetNs:recurringElement[1]/node()[xsi:isNotNull()]", Object.class);
        Object[] resultArray = IteratorUtils.toArray(result);

        Assert.assertEquals(2, resultArray.length);
        Assert.assertEquals(resultArray[0], validRootElement.getRecurringElement().get(0).getNonSequencedTypeElement1());
        Assert.assertEquals(resultArray[1], validRootElement.getRecurringElement().get(0).getNonSequencedTypeElement2());
    }

    @Test
    public void whenAttributeIsSetItsOnlyReturnedViaAttributeExpression() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();

        Iterator<String> unqualifiedAttributeResult = UTIL.getValues(validRootElement, "targetNs:recurringElement[1]/@unqualifiedAttribute[xsi:isNotNull()]", String.class);
        Object[] unqualifiedAttributeResultArray = IteratorUtils.toArray(unqualifiedAttributeResult);

        Assert.assertEquals(1, unqualifiedAttributeResultArray.length);
        Assert.assertEquals(unqualifiedAttributeResultArray[0], validRootElement.getRecurringElement().get(0).getUnqualifiedAttribute());

        Iterator<String> qualifiedAttributeResult = UTIL.getValues(validRootElement, "@targetNs:qualifiedAttribute[xsi:isNotNull()]", String.class);
        Object[] qualifiedAttributeResultArray = IteratorUtils.toArray(qualifiedAttributeResult);

        Assert.assertEquals(1, qualifiedAttributeResultArray.length);
        Assert.assertEquals(qualifiedAttributeResultArray[0], validRootElement.getQualifiedAttribute());

        Assert.assertNull(UTIL.getValue(validRootElement, "targetNs:qualifiedAttribute[xsi:isNotNull()]", String.class));

        Iterator<Pointer> nodeResult = UTIL.getPointers(validRootElement, "node()[xsi:isNotNull()]");
        Object[] nodeResultArray = IteratorUtils.toArray(nodeResult);

        Assert.assertEquals(validRootElement.getRecurringElement().size(), nodeResultArray.length);
        Assert.assertFalse("No attributes should be returned with node() call", CollectionUtils.exists(Arrays.asList(nodeResultArray), new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((NodePointer)object).isAttribute();
            }
        }));
    }

    @Test
    public void whenAttributeAxisIsSpecifiedAllAttributeFieldsAreReturned() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();

        Iterator<Pointer> attributeResult = UTIL.getPointers(validRootElement.getRecurringElement().get(0), "@*");
        Object[] attributeResultArray = IteratorUtils.toArray(attributeResult);

        Assert.assertEquals("/@unqualifiedAttribute", ((Pointer)attributeResultArray[0]).asPath());
        Assert.assertEquals("/@unqualifiedAttribute1", ((Pointer)attributeResultArray[1]).asPath());
        Assert.assertEquals(2, attributeResultArray.length);
    }

    @Test
    public void whenCreatePathAndSetValueCalledThenLastNodeIsUpdated() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();
        String originalNonSequencedTypeElement1 = validRootElement.getRecurringElement().get(0).getNonSequencedTypeElement1();
        UTIL.newContext(validRootElement).createPathAndSetValue("targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceType')]/targetNs:nonSequencedTypeElement1", "myNewValue");

        Assert.assertNotSame(originalNonSequencedTypeElement1, validRootElement.getRecurringElement().get(0).getNonSequencedTypeElement1());
        Assert.assertEquals("myNewValue", validRootElement.getRecurringElement().get(0).getNonSequencedTypeElement1());
    }

    @Test
    public void whenQualifiedElementIsQueriedOnlySpecificNamespaceOrWildcardNSReturnsResult() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();

        Assert.assertFalse("Unqualified XPath should not return anything", UTIL.getValues(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceType')]/nonSequencedTypeElement1").hasNext());
        Assert.assertEquals("Qualified and wildcard prefix should return the same result", UTIL.getValue(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceType')]/targetNs:nonSequencedTypeElement1"), UTIL.getValue(validRootElement, "*:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceType')]/*:nonSequencedTypeElement1"));
        Assert.assertFalse("Qualified XPath should not return anything", UTIL.getValues(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/nonQualifiedElement").hasNext());
        Assert.assertEquals("Qualified and wildcard prefix should return the same result", UTIL.getValue(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/nonQualifiedElement"), UTIL.getValue(validRootElement, "*:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/*:nonQualifiedElement"));
        Assert.assertEquals("Qualified and wildcard prefix should return the same result", UTIL.getValue(validRootElement, "*:recurringElement[namespace-uri() = 'urn:jxpath-jaxb-utils' and xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/nonQualifiedElement[namespace-uri() = '']"), UTIL.getValue(validRootElement, "*:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/*:nonQualifiedElement"));
    }

    @Test(expected = JXPathException.class)
    public void whenUnknownPrefixIsSuppliedExceptionIsExpected() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();

        UTIL.getValues(validRootElement, "badPrefix:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceType')]/nonSequencedTypeElement1");
    }

    @Test
    @Ignore("SimpleContent stuff is not implemented properly as of yet")
    public void whenXmlValueIsPresentItsTreatedLikeSimpleType() throws XPathExpressionException {
        RootElement validRootElement = createValidRootElement();
        Assert.assertEquals("10", UTIL.getValue(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/targetNs:xmlValue", String.class));
        Assert.assertEquals(((RepeatingSequenceSubType)validRootElement.getRecurringElement().get(1)).getXmlValue(), ((NodePointer)UTIL.getPointer(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/targetNs:xmlValue")).getBaseValue());

        UTIL.newContext(validRootElement).setValue("targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/targetNs:xmlValue", new BigInteger("11"));
        Assert.assertEquals("11", UTIL.getValue(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/targetNs:xmlValue", String.class));

        UTIL.newContext(validRootElement).createPathAndSetValue("targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/targetNs:xmlValue", "12");
        Assert.assertEquals("12", UTIL.getValue(validRootElement, "targetNs:recurringElement[xsi:isClass('org.mikehan.testschema.RepeatingSequenceSubType')]/targetNs:xmlValue", String.class));
    }


    /**
     * @return
     *
     */
    public static RootElement createValidRootElement(){
        RootElement rootElement = new RootElement();
        rootElement.setQualifiedAttribute("qualifiedAttributeValue");

        RepeatingSequenceType repeatingSequenceType = new RepeatingSequenceType();
        repeatingSequenceType.setUnqualifiedAttribute("unqualifiedAttributeValue");
        //repeatingSequenceType.setUnqualifiedAttribute1("unqualifiedAttributeValue1");
        repeatingSequenceType.setNonSequencedTypeElement1("element1");
        repeatingSequenceType.setNonSequencedTypeElement2("element2");

        //Ordered properties
        RepeatingSequenceSubType repeatingSequenceSubType = new RepeatingSequenceSubType();
        repeatingSequenceSubType.setUnqualifiedAttribute("unqualifiedAttributeValue01");
        repeatingSequenceSubType.setNonSequencedTypeElement1("element11");
        repeatingSequenceSubType.setNonSequencedTypeElement2("element22");
        repeatingSequenceSubType.setNonSequencedSubTypeElement1("subElement1");
        repeatingSequenceSubType.setXmlValue(new XmlValueType());
        repeatingSequenceSubType.getXmlValue().setValue(BigInteger.TEN);
        repeatingSequenceSubType.setNonQualifiedElement(new RepeatingSequenceSubType2());

        //Multiple items in array with subtypes
        rootElement.getRecurringElement().add(repeatingSequenceType);
        rootElement.getRecurringElement().add(repeatingSequenceSubType);

        return rootElement;
    }

    @BeforeClass
    public static void dumpTestXml(){
        try{
            Object validRootElement = createValidRootElement();
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = JAXBContext.newInstance(validRootElement.getClass()).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(validRootElement, stringWriter);
            System.out.println("Test data: ");
            System.out.println(stringWriter.toString());}
        catch (JAXBException e){throw new RuntimeException(e);}
    }
}
