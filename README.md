# OXOFramework
OXOFramework is an MVC Web Framework that uses XML and XSLT to build web content automatically from objects.

The framework is built on top of Java Servlet Technology. In OXOFramework a web module is a set of one or more pages
that work or belong together (e.g. a search page and a seach results page). To use the framework, you simply extend
OXOServlet and implement processRequest(request, response) to set a data object with response.setData(). That object
is then automatically converted to XML using XStream and the XML is then automatically transformed using an XSLT
template to whichever output format you want ((X)HTML, XML, JSON, Text, PDF, etc.).
