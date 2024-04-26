package com.centropoly.oxo;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A web service has the special ability to transform XML to JSON if so requested.
 * 
 * TODO
 * Move the processRequest code from CentropolyService and TestToolService here.
 * Add a bit of security to it by requiring each endpoint to have a special annotation.
 * We could even make those annotations actually (optionally) define the endpoint's name.
 * In addition, add the ability to throw exceptions or return an error state. Currently this
 * is implemented as response.addException. Might be fine... just take a look.
 * 
 * NOTE
 * Why are we doing all this here? Can't we just use the logic in OXOServlet instead of this piping stuff...
 * It seems like all we would really need to do is move the xslt filename lookup logic into an overrideable method and override that here...
 * TODO
 * Set up a test where we utilize what's in OXOServlet and see if we can get the same result. If not, make sure we document it here
 * properly so we don't doubt this logic here again...
 * 
 * @author Paul van der Maas
 */
public abstract class WebService extends OXOServlet
{
    private final static Logger logger = LogManager.getLogger(WebService.class);

    private boolean responseAsJSON = false;
    private JsonOutputType jsonOutputType = JsonOutputType.JSON;

    private String jsonXslTemplate = "template:/xmltojson.xsl"; // Requires an XSLT 2.0 Processor. Saxon is hard-coded into this class and thus is a dependency if this stylesheet is used (jsonOutputType = JSON).
    private String jsonmlXslTemplate = "template:/xmltojsonml.xsl";

    @Override
    protected void init(OXORequest request, OXOResponse response)
    {
        super.init(request, response);

        // By default, for a web service, the response is not transformed.
        // It is a plain object to XML conversion. However, the output may
        // still be converted to JSON.
        response.setTransformationOutputType(null);
    }

    @Override
    protected void processRequest(OXORequest request, OXOResponse response) throws Exception
    {
        // NOTE: Response headers need to be set before anything is written to the response outputstream.
        setAccessControlHeaders(response);

        String serviceIdentifier = request.getParameter("serviceIdentifier");
        String responseAs = request.getParameter("responseAs");

        // If given, the responseAs parameter's value should either be XML or JSON.
        if (responseAs != null && responseAs.equalsIgnoreCase("JSON"))
        {
            this.setResponseAsJSON(true);
        }
        else
        {
            this.setResponseAsJSON(false);
        }

        if (serviceIdentifier != null) {
            try {
                response.setData((Data) this.getClass().getMethod(serviceIdentifier).invoke(this));
            } catch(ReflectiveOperationException exception) {
                // TODO
                // It would be nicer if we make a custom OXOServletException which would also take a HTTP status code.
                // Then we don't have to explicitly set the state here and can leave it to the OXOServlet.outputException to set it.
                // We might even create subclasses of Exceptions where status (code) and message are already set...
                response.setStatus(404);
                logger.info("The service identifier (" + serviceIdentifier +") is invalid.", exception);
                throw new ServletException("The service identifier (" + serviceIdentifier +") is invalid.", exception);
            }
        }
        else
        {
            response.setStatus(404);
            throw new ServletException("A valid service identifier is required.");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        setAccessControlHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setAccessControlHeaders(HttpServletResponse response)
    {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    @Override
    protected void writeXMLToOutputStream(OXORequest request, OXOResponse response, OutputStream outputStream) throws IOException, SAXException, TransformerException, ReflectiveOperationException
    {
        if (this.responseAsJSON)
        {
            writePipedXMLToOutputStreamAsJSON(request, response, outputStream);
        }
        else
        {
            super.writeXMLToOutputStream(request, response, outputStream);
        }
    }
    
    /**
     * This needs to be in a call accessible from the runner.
     * 
     * @param request
     * @param response
     * @param outputStream
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     * @throws ReflectiveOperationException 
     */
    protected void pipeWriteResponseDataToOutputStreamAsXML(OXORequest request, OXOResponse response, PipedOutputStream outputStream) throws IOException, SAXException, TransformerException, ReflectiveOperationException
    {
        super.writeXMLToOutputStream(request, response, outputStream);
    }

    protected void writePipedXMLToOutputStreamAsJSON(final OXORequest request, final OXOResponse response, OutputStream outputStream) throws IOException, SAXException, TransformerException
    {
        // We can only transform transformed XML or untransformed XML to JSON, hence the following requirement.
        if (response.getTransformationOutputType() == null || response.getTransformationOutputType() == OXOResponse.TransformationOutputType.XML)
        {
            PipedInputStream pipedInputStream = new PipedInputStream();
            final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);

            // To prevent deadlock, we have to perform the piping in another thread.
            Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        pipeWriteResponseDataToOutputStreamAsXML(request, response, pipedOutputStream);
                    }
                    catch (IOException | SAXException | TransformerException | ReflectiveOperationException exception)
                    {
                        throw new RuntimeException(exception);
                    }
                    finally
                    {
                        try
                        {
                            pipedOutputStream.close();
                        }
                        catch (IOException exception)
                        {
                            throw new RuntimeException(exception);
                        }
                    }
                }
            };
            
            UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler()
            {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable)
                {
                    outputException(response, new Exception(throwable));
                }
            };

            Thread thread = new Thread(runner);
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            thread.start();

            // Create an entity resolver which will be used to resolve the XSL template.
            OXOEntityResolver entityResolver = new OXOEntityResolver("com.centropoly.oxo.templates", null);

            String xslTemplate = this.jsonXslTemplate;
            if (this.jsonOutputType == JsonOutputType.JSONML)
            {
                xslTemplate = this.jsonmlXslTemplate;
            }

            // Retrieve the Json XSL template.
            InputSource inputSource = entityResolver.resolveEntity(null, xslTemplate);

            // Create an XML reader which will be used to process the XSL template.
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setEntityResolver(entityResolver);

            // Create the SAXSource objects.
            SAXSource xslSource = new SAXSource(xmlReader, inputSource);
            SAXSource xmlSource = new SAXSource(xmlReader, new InputSource(pipedInputStream));

            // Create a transformer.
            TransformerFactory transformerFactory;
            if (this.jsonOutputType == JsonOutputType.JSON)
            {
                // The Saxon (HE) library by Saxonica must be included as a project dependency when outputting JSON.
                // It is required to make the transformation from XML to JSON.
                try
                {
                    transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
                }
                catch(TransformerFactoryConfigurationError error)
                {
                    throw new TransformerException("The Saxon (HE) library is required to transform XML to JSON. Please make sure it is included as a project dependency.", error);
                }
            }
            else
            {
                transformerFactory = TransformerFactory.newInstance();
            }
            transformerFactory.setErrorListener(new OXOErrorListener());

            try
            {
                Transformer transformer = transformerFactory.newTransformer(xslSource);

                // Set important response headers if not already set...
                if (response.getContentType() == null) {
                    String mediaType = transformer.getOutputProperty("media-type");
                    if (mediaType == null)
                    {
                        // Set the default.
                        mediaType = "application/json";
                    }
                    response.setContentType(mediaType);
                }
                if (response.getCharacterEncoding() == null)
                {
                    String encoding = transformer.getOutputProperty("encoding");
                    if (encoding == null)
                    {
                        // Set the default.
                        encoding = "UTF-8";
                    }
                    response.setCharacterEncoding(encoding);
                }

                // Transform the XML to JSON.
                transformer.transform(xmlSource, new StreamResult(outputStream));
            }
            catch (TransformerException exception)
            {
                // Wrap the exception to add additional information.
                throw new TransformerException(exception.getMessage() + " The JSON stylesheet may contain an error: " + xslTemplate + ".", exception);
            }
        }
        else
        {
            throw new TransformerException("Only XML can be transformed to JSON. The response's output type was set to " + response.getTransformationOutputType());
        }
    }
    
    public boolean responseAsJSON()
    {
        return this.responseAsJSON;
    }
    
    public void setResponseAsJSON()
    {
        setResponseAsJSON(true);
    }
    
    public void setResponseAsJSON(boolean responseAsJSON)
    {
        this.responseAsJSON = responseAsJSON;
    }

    public void setJsonOutputType(JsonOutputType jsonOutputType)
    {
        this.jsonOutputType = jsonOutputType;
    }

    public enum JsonOutputType
    {
        JSONML ("jsonml"),
        JSON ("json");

        private final String value;

        JsonOutputType(String value)
        {
            this.value = value;
        }
        
        @Override
        public String toString()
        {
            return this.value;
        }
    }
}