package com.centropoly.oxo;

import com.centropoly.oxo.CachedResponseManager.CachedResponse;
import com.centropoly.oxo.converters.OXOResponseConverter;
import com.centropoly.oxo.converters.OXORequestConverter;
import com.centropoly.oxo.converters.ClientConverter;
import com.centropoly.oxo.converters.DateTimeConverter;
import com.centropoly.oxo.converters.LocaleConverter;
import com.centropoly.oxo.converters.NullWrappingCollectionConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.TraxSource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * To enable us to localize the templates that are used to transform the XML representation of the response into output, we will use entities.
 * Every entity that is to be localized should be an external entity using the special scheme outlined below.
 * A custom entity resolver (OXOEntityResolver) will look up the value for the (localisable) entities.
 * 
 * An opaque URI with a custom scheme appropriately named 'property' will tell the entity resolver where to find a value for the requested entity.
 * The scheme specific part tells the entity resolver the name of the resource bundle, and the fragment represents the key to the desired data.
 * This method has the benefit of using an XML-native technology. XML entities are well-known and this also allows us to use any SAX2 Core compatible parser
 * to do the work. We'll be using XML entities and URI's in a legal way, without breaking any protocols.
 * 
 * @author Paul van der Maas
 */
public abstract class OXOServlet extends HttpServlet
{
    private final static Logger logger = LogManager.getLogger(OXOServlet.class);

    protected final CachedResponseManager cachedResponseManager = new CachedResponseManager();
    
    /**
     * This method is called only once when a servlet is being put into service 
     * and can be used to perform 'global' servlet initialization tasks.
     * 
     * Overriding implementations of this method must call the super implementation
     * first.
     * 
     * This is a good place for (abstract) sub implementations of OXOServlet
     * to set defaults that do not depend on request parameters.
     */
    @Override
    public void init()
    {
        // For the OXO Framework to function properly a few parameters need
        // to be set in the deployment descriptor (web.xml). The following
        // logic will load these parameters into OXOContext and fail if they cannot be located.

        String templatesPackage = this.getServletContext().getInitParameter("templatesPackage");
        if (templatesPackage == null)
        {
            throw new NullPointerException("The templatesPackage context parameter is undefined! This context parameter needs to be defined in the deployment descriptor for the OXO Framework to function properly.");
        }
        else
        {
            OXOContext.setTemplatesPackage(templatesPackage);
        }

        String propertiesPackage = this.getServletContext().getInitParameter("propertiesPackage");
        if (propertiesPackage == null)
        {
            throw new NullPointerException("The propertiesPackage context parameter is undefined! This context parameter needs to be defined in the deployment descriptor for the OXO Framework to function properly.");
        }
        else
        {
            OXOContext.setPropertiesPackage(propertiesPackage);
        }

        String servletsPackage = this.getServletContext().getInitParameter("servletsPackage");
        if (servletsPackage == null)
        {
            throw new NullPointerException("The servletsPackage context parameter is undefined! This context parameter needs to be defined in the deployment descriptor for the OXO Framework to function properly.");
        }
        else
        {
            OXOContext.setServletsPackage(servletsPackage);
        }
        
        // These are optional, but can be set as environment variables or in the deployment descriptor.

        // Turns debugging on or off globally. This does not control the log level.
        // Essentially, this controls wether (potentially sensitive) debugging data may be sent as part of the (error) response.
        String debug = System.getProperty("DEBUG", this.getServletContext().getInitParameter("debug"));
        if (debug != null) {
            OXOContext.debug(Boolean.parseBoolean(debug));
        }

        // Turns caching on or off globally.
        String cache = System.getProperty("CACHE", this.getServletContext().getInitParameter("cache"));
        if (cache != null) {
            OXOContext.cache(Boolean.parseBoolean(cache));
        }

        logger.debug("OXOServlet.init()");
    }

    /**
     * This method is called once per request/response cycle and can be used to
     * perform 'local' servlet initialization tasks (initialize context).
     * 
     * Overriding implementations of this method must call the super implementation
     * first.
     * 
     * This is a good place for (abstract) sub implementations of OXOServlet
     * to set defaults that depend on request parameters.
     * 
     * @param request
     * @param response
     */
    protected void init(OXORequest request, OXOResponse response)
    {
        logger.debug("OXOServlet.init(request, response) " + request.getServletPath());

        // Set all contextual information needed by the
        // OXO Framework into OXOContext.
        OXOContext.setRequest(request);
        OXOContext.setResponse(response);
        OXOContext.setUser(new User(new Client(request, response)));
    }

    /**
     * Override service method to handle requests for both HTTP <code>GET</code>
     * and <code>POST</code> methods.
     * 
     * This falls back on the super implementation for all other request methods.
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        logger.debug("OXOServlet.service() " + request.getServletPath());

        String method = request.getMethod();
        if (method.equals("GET") || method.equals("POST"))
        {
            // Wrap the request and response.
            OXORequest oxoRequest = new OXORequest(request);
            OXOResponse oxoResponse = new OXOResponse(response);

            // Perform request specific initialization.
            init(oxoRequest, oxoResponse);

            handleRequest(oxoRequest, oxoResponse);
        }
        else
        {
            // Fall back on super implementation for other request methods.
            super.service(request, response);
        }
    }

    /**
     * Handle requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request
     * @param response
     * @throws java.io.IOException
     */
    protected final void handleRequest(OXORequest request, OXOResponse response) throws IOException
    {
        logger.debug("OXOServlet.handleRequest() " + request.getServletPath());

        try
        {
            // Process request/initialize response.
            logger.debug("OXOServlet.processRequest() " + request.getServletPath());
            processRequest(request, response);
            
            // Output the (transformed) response.
            logger.debug("OXOServlet.outputResponse() " + request.getServletPath());
            outputResponse(request, response);
        }
        catch (Exception exception)
        {
            outputException(response, exception);
        }
    }
    
    /**
     * Executes operation(s) determined by the properties of the request object.
     * 
     * Input that may change which resource is generated and (how it is) returned should be handled at this level.
     * Input that may change the resource internally is typically handled in the build method
     * of the data object representing the resource.
     * 
     * Either way, this method should (normally) tell the response what resources to generate and, possibly, how to return them.
     *
     * @param request Contains information about the request which should be used to tell the servlet how to generate and return responses.
     * @param response Should be informed on how to respond to the request by calling the hooks provided.
     * @throws Exception An exception thrown here could not be handled internally. A general error page should be shown.
     */
    protected abstract void processRequest(OXORequest request, OXOResponse response) throws Exception;

    /**
     * Generate data for the response, then send the (transformed) result back to the client.
     *
     * @param request
     * @param response
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     * @throws ReflectiveOperationException
     */
    protected final void outputResponse(OXORequest request, OXOResponse response) throws IOException, SAXException, TransformerException, ReflectiveOperationException
    {
        // Check if the response is not committed yet.
        // A committed response indicates that we should not try to generate the response output.
        // It may have already been 'written' by #processRequest(), for instance.
        // One case of this would be if the required action is a redirect or if output has been written
        // directly to the output stream for debugging or other purposes.
        if (!response.isCommitted())
        {
            OutputStream outputStream;
            
            if (useCache(request, response))
            {
                logger.debug("OXOServlet.outputResponse() -> use cache " + request.getServletPath());
                if (useClientCache(request, response))
                {
                    logger.debug("OXOServlet.outputResponse() -> use client cache " + request.getServletPath());
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    
                    // No further action needed.
                    return;
                }
                else if (useServerCache(request, response))
                {
                    logger.debug("OXOServlet.outputResponse() -> use server cache " + request.getServletPath());
                    outputCachedResponse(request, response);

                    // No further action needed, #outputCachedResponse() 'commits' the response.
                    return;
                }
                else // Prime server cache.
                {
                    logger.debug("OXOServlet.outputResponse() -> prime/use server cache " + request.getServletPath());
                    outputStream = createCachedOutputStreamForResponse(request, response);
                }
            }
            else
            {
                logger.debug("OXOServlet.outputResponse() -> do not use cache " + request.getServletPath());
                outputStream = response.getOutputStream();
            }

            Data data = response.getData();
            if (data != null)
            {
                // Build out the data object.
                try
                {
                    // Data should call #response.isCommitted(true) if it produces the output directly.
                    data.build(request, response);
                }
                catch(Exception exception)
                {
                    outputException(response, new Exception("An unhandled exception occurred while building the response data.", exception));
                }
            }            

            if (!response.isCommitted())
            {
                // Write output to the given outputstream transformed or untransformed.
                if (response.getTransformationOutputType() != null)
                {
                    writeTransformedXMLToOutputStream(request, response, outputStream);
                }
                else
                {
                    writeXMLToOutputStream(request, response, outputStream);
                }
            }
        }
    }

    // TODO
    // If the passed exception implements OXOException (need to be created) it should have a HTTP status code.
    // otherwise, set it as an internal server error. 
    protected void outputException(OXOResponse response, Exception exception)
    {
        // Only set it if it hasn't already been set...
        if (response.getStatus() == 0 || response.getStatus() == HttpServletResponse.SC_OK)
        {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        try
        {
            logger.debug("An exception was encountered and is sent back as the response.", exception);

            // Must wrap the output stream here instead of getting the print
            // writer directly since data may have already been written to it.
            try (PrintWriter out = new PrintWriter(response.getOutputStream()))
            {
                out.println("An exception was encountered:");
                out.println(exception.getMessage());
                out.println();
                if (OXOContext.debug()) exception.printStackTrace(out);
            }
        }
        catch(IOException unhandledException)
        {
            logger.debug("While generating an error response another unhandled exception occurred.", unhandledException, exception);
        }
    }

    /**
     * This method takes the XML from XStream, transforms it using XSLT and then writes it to the output stream.
     * 
     * @param request
     * @param response
     * @param outputStream
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     * @throws javax.xml.transform.TransformerException
     */
    protected void writeTransformedXMLToOutputStream(OXORequest request, OXOResponse response, OutputStream outputStream) throws IOException, SAXException, TransformerException
    {
        Data data = response.getData();
        XStream xStream = getXStreamFromData(data);

        // Create an entity resolver which will be used to resolve the root XSL template
        // as well as external entities referenced in the template itself.
        OXOEntityResolver entityResolver = new OXOEntityResolver(OXOContext.getTemplatesPackage(), OXOContext.getPropertiesPackage());

        String canonicalClassName = response.getData().getClass().getCanonicalName().toLowerCase();
        int dataIndex = canonicalClassName.lastIndexOf("data");
        canonicalClassName = (dataIndex == -1) ? canonicalClassName : canonicalClassName.substring(0, dataIndex);

        // Determine the XSL template to use in the transformation.
        StringBuilder stringBuilder = new StringBuilder("template:");
        stringBuilder.append(canonicalClassName.replaceFirst(OXOContext.getServletsPackage(), "").replace(".", "/"));
        stringBuilder.append(".");
        stringBuilder.append(response.getTransformationOutputType());
        stringBuilder.append(".xsl");

        String xslTemplate = stringBuilder.toString();

        // Retrieve the root XSL template.
        InputSource inputSource = entityResolver.resolveEntity(null, xslTemplate);

        // Create an XML reader which will be used to process the XSL template.
        // We assign our own custom entity resolver so that it can resolve
        // entities that are to be located using our customized template scheme.
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        xmlReader.setEntityResolver(entityResolver);

        // Create the SAXSource objects.
        SAXSource xslSource = new SAXSource(xmlReader, inputSource);
        SAXSource xmlSource = new TraxSource(response, xStream);

        // Create a transformer.
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setURIResolver(new OXOURIResolver(entityResolver));
        transformerFactory.setErrorListener(new OXOErrorListener());

        try
        {
            Transformer transformer = transformerFactory.newTransformer(xslSource);

            // Only set the content headers if we are outputting the result
            // of the tranformation to a servlet output stream.
            // TODO: We should set these as early as possible so that overriding methods can potentially overwrite them.
            if (outputStream instanceof ServletOutputStream)
            {
                String mediaType = transformer.getOutputProperty("media-type");
                if (mediaType != null)
                {
                    response.setContentType(mediaType);
                }
                String encoding = transformer.getOutputProperty("encoding");
                if (encoding != null)
                {
                    response.setCharacterEncoding(encoding);
                }
            }

            transformer.transform(xmlSource, new StreamResult(outputStream));
        }
        catch (TransformerException exception)
        {
            // Wrap the exception to add additional information.
            throw new TransformerException(exception.getMessage() + " This stylesheet may contain an error: " + xslTemplate + ".", exception);
        }
    }

    /**
     * This method writes the XML from XStream to the output stream.
     * 
     * Normally, the output stream will be the response's output stream, but this method can be overridden to intercept calls to it
     * and pipe the 'intercepted' output stream through another process to, for instance, reformat it to another output format.
     * 
     * @param request
     * @param response
     * @param outputStream
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     * @throws ReflectiveOperationException 
     */
    protected void writeXMLToOutputStream(OXORequest request, OXOResponse response, OutputStream outputStream) throws IOException, SAXException, TransformerException, ReflectiveOperationException
    {
        Data data = response.getData();
        XStream xStream = getXStreamFromData(data);

        // Print the XML generated for debugging purposes.
        // logger.debug(xStream.toXML(response));

        // Only set the content headers if we are outputting to a servlet output stream.
        // TODO: We should set these as early as possible sothat overriding methods can potentially overwrite them.
        if (outputStream instanceof ServletOutputStream)
        {
            response.setContentType("text/xml");
            response.setCharacterEncoding("UTF-8");
        }
        xStream.toXML(response, outputStream);
    }

    /**
     * This method converts the data object into XML using XStream.
     * 
     * @param data
     * @return 
     */
    protected final XStream getXStreamFromData(Data data)
    {
        // Convert the response object into XML using XStream.
        XStream xStream = new XStream(new DomDriver());
        xStream.setMode(XStream.NO_REFERENCES);

        // TODO
        // For the converters I'm registering here, I should not have to annotate anywhere I believe.
        // Test and remove annotations that are redundant.
        xStream.registerConverter(new OXOResponseConverter());
        xStream.registerConverter(new OXORequestConverter());
        xStream.registerConverter(new ClientConverter(), XStream.PRIORITY_LOW);
        xStream.registerConverter(new LocaleConverter());
        xStream.registerConverter(new DateTimeConverter());
        // TODO
        // This is a WIP to deal with null values in collections. Right now, it's fine in XML,
        // but when converted to JSON, it breaks the list structure.
        // We can fix it by wrapping the <null/> values in the same XML element as it's siblings,
        // maybe NamedCollectionConverter fixes it (test), or maybe just remove this
        // and deal with list's containing nulls in another way.
        xStream.registerConverter(new NullWrappingCollectionConverter(xStream.getMapper()));
        xStream.aliasType("response", OXOResponse.class);

        if (data != null)
        {
            // In case the data object is a non-static inner class, this will
            // omit the reference to the outer class.
            xStream.omitField(data.getClass(), "this$0");

            xStream.processAnnotations(data.getClass());
        }

        return xStream;
    }

    protected final boolean useCache(OXORequest request, OXOResponse response)
    {
        // Cache must be enabled.
        if (OXOContext.cache())
        {
            // Using any sort of cache only makes sense for get requests.
            if (request.getMethod().equals("GET"))
            {
                // We also require that there are no exceptions or notifications set on the response.
                if (!response.hasExceptions() && !response.hasNotifications())
                {
                    Data data = response.getData();

                    Duration dataExpirationDuration = data.getExpirationDuration();
                    DateTime dataLastModifiedDateTime = data.getLastModifiedDateTime();

                    // Data must have an expiration duration or last modified date/time. 
                    if (dataExpirationDuration != null || dataLastModifiedDateTime != null)
                    {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    protected final boolean useClientCache(OXORequest request, OXOResponse response)
    {
        Data data = response.getData();

        Duration dataExpirationDuration = data.getExpirationDuration();
        DateTime dataLastModifiedDateTime = data.getLastModifiedDateTime();
        DateTime cachedDateTime = new DateTime(request.getDateHeader("If-Modified-Since"));

        // Was the "If-Modified-Since" header present on the request?
        if (cachedDateTime.isAfter(new DateTime(0)))
        {
            // Has the client's cache entry expired?
            if (dataExpirationDuration == null || cachedDateTime.plus(dataExpirationDuration).isAfterNow())
            {
                // Has the data been modified since it was cached on the client?
                if (dataLastModifiedDateTime == null || dataLastModifiedDateTime.isBefore(cachedDateTime))
                {
                    return true; // Not expired or modified.
                }
                else
                {
                    return false; // Modified.
                }
            }
            else
            {
                return false; // Expired.
            }
        }
        else
        {
            return  false; // Non-existent.
        }
    }

    protected final boolean useServerCache(OXORequest request, OXOResponse response)
    {
        Data data = response.getData();

        Duration dataExpirationDuration = data.getExpirationDuration();
        DateTime dataLastModifiedDateTime = data.getLastModifiedDateTime();
        DateTime cachedDateTime = cachedResponseManager.getCachedResponseDateTime(request);

        // Is there an entry in the cache?
        if (cachedDateTime != null)
        {
            // Has the cache entry expired?
            if (dataExpirationDuration == null || cachedDateTime.plus(dataExpirationDuration).isAfterNow())
            {
                // Has the data been modified since it was cached?
                if (dataLastModifiedDateTime == null || dataLastModifiedDateTime.isBefore(cachedDateTime))
                {
                    return true; // Not expired or modified.
                }
                else
                {
                    return false; // Modified.
                }
            }
            else
            {
                return false; // Expired.
            }
        }
        else
        {
            return  false; // Non-existent.
        }
    }

    // No check is done. The cache MUST contain a valid cached response for the request.
    protected void outputCachedResponse(OXORequest request, OXOResponse response) throws IOException
    {
        CachedResponse cachedResponse = cachedResponseManager.getCachedResponse(request);
        
        setResponseCacheHeaders(request, response);

        cachedResponse.outputStream.getBuffer().writeTo(response.getOutputStream());
    }
    
    protected CachedOutputStream createCachedOutputStreamForResponse(OXORequest request, OXOResponse response) throws IOException, SAXException, TransformerException, ReflectiveOperationException
    {
        CachedResponse cachedResponse = cachedResponseManager.createCachedResponse(request, response);
        
        setResponseCacheHeaders(request, response);

        return cachedResponse.outputStream;
    }
    
    protected void setResponseCacheHeaders(OXORequest request, OXOResponse response)
    {
        DateTime cachedDateTime = cachedResponseManager.getCachedResponseDateTime(request);
        response.setDateHeader("Last-Modified", cachedDateTime.getMillis());

        Duration dataExpirationDuration = response.getData().getExpirationDuration();
        if (dataExpirationDuration != null)
        {
            response.setDateHeader("Expires", cachedDateTime.plus(dataExpirationDuration).getMillis());
        }
    }
}