**Introduction** 
This extension on top of JXPath intends to allow querying of JAXB beans with XPath most closely related to the raw XML (rather than bean-path). 
It adds support for qualified names as-defined by xml schemas, elements and attributes and aligns bean access names to the XPath.
It also adds some functions that might help with some common xml processing things (can be found at org.apache.commons.jxpath.ext.jaxb and registered under xsi: prefix).

**Usage** 
Most use-cases covered by this library can be found in org.apache.commons.jxpath.ext.jaxb.JXPathJaxbUtilsTest, including
* Qualified names
* Null filtering using common functions
* xsiType (sub-class filtering) on predicates

To use this library:
```java
//create new instance of utils
org.apache.commons.jxpath.ext.jaxb.JaxbUtils UTIL = new org.apache.commons.jxpath.ext.jaxb.JaxbUtils(NamespacesMap);
//Get context and query
Object result = UTIL.newContext($myBean).getValue("abc:def");
```

For more information of limitations of JXPath (as compared to XPath) and functions supported can be found at https://commons.apache.org/proper/commons-jxpath/users-guide.html#Object_Graph_Traversal 