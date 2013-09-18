package com.centropoly.oxo;

import com.centropoly.oxo.converter.OXOResponseConverter;
import com.centropoly.oxo.converter.OXORequestConverter;
import com.centropoly.oxo.converter.LocaleConverter;
import com.centropoly.oxo.converter.ClientConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.TraxSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/*
 * To enable us to localize the templates that are used to transform the XML representation of the page into output, we will use entities.
 * Every entity that is to be localized should be an external entity.
 * A custom entity resolver (OXOEntityResolver) will look up the value for the (localizable) entities.
 * 
 * An opaque URI with a custom scheme apropriately named 'property' will tell the entity resolver where to find a value for the requested entity.
 * The scheme specific part tells the entity resolver the name of the resource bundle, and the fragment represents the key to the desired data.
 * This method has the benefit of using an XML-native technology. XML entities are well-known and this also allows us to use any SAX2 Core compatible parser
 * to do the work. We'll be using XML entities and URI's in a legal way, without breaking any protocols.
 * 
 * @author Paul van der Maas
 */
public abstract class OXOServlet extends HttpServlet
{
    /**
     * This method should be overridden to do any servlet-specific initialization.
     */
    protected void initialize(OXORequest request, OXOResponse response) {}
    
    /**
     * Executes the right operation(s), which should be determined by the properties of the request object.
     * 
     * Input that may change which resource is created should be handled at this level.
     * Input that may change the resource internally should be handled by the resource.
     * 
     * Either way, this method should (normally) create a resource object and attach it to the response.
     *
     * @param request Contains information about the request which can be used to build the resource.
     * @param response Should be loaded with a resource based on the request information.
     * @throws Exception An exception thrown here could not be handled internally. A general error page should be shown.
     */
    protected abstract void processRequest(OXORequest request, OXOResponse response) throws Exception;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    protected void handleRequest(OXORequest request, OXOResponse response) throws IOException
    {
        try
        {
            // For the OXO Framework to function properly a few parameters need
            // to be set in the deployment descriptor (web.xml). The following
            // logic will load these parameters and fail if they cannot be located.

            String templatesPackage = this.getServletContext().getInitParameter("templatesPackage");

            if (templatesPackage == null)
            {
                throw new Exception("The templatesPackage context parameter is undefined! This context parameter needs to be defined in the deployment descriptor for the OXO Framework to function properly.");
            }
            else
            {
                OXOContext.setTemplatesPackage(templatesPackage);
            }

            String propertiesPackage = this.getServletContext().getInitParameter("propertiesPackage");

            if (propertiesPackage == null)
            {
                throw new Exception("The propertiesPackage context parameter is undefined! This context parameter needs to be defined in the deployment descriptor for the OXO Framework to function properly.");
            }
            else
            {
                OXOContext.setPropertiesPackage(propertiesPackage);
            }

            String servletsPackage = this.getServletContext().getInitParameter("servletsPackage");

            if (servletsPackage == null)
            {
                throw new Exception("The servletsPackage context parameter is undefined! This context parameter needs to be defined in the deployment descriptor for the OXO Framework to function properly.");
            }
            else
            {
                OXOContext.setServletsPackage(servletsPackage);
            }
            
            // Initialize all contextual information needed by the
            // OXO Framework into OXOContext.
            OXOContext.setRequest(request);
            OXOContext.setResponse(response);
            OXOContext.setUser(new User(new Client(request, response)));
            
            // Perform servlet specific initialization.
            initialize(request, response);
            
            // Process request/load up response.
            processRequest(request, response);

            // Send the response.
            sendResponse(response);
        }
        catch (Exception exception)
        {
            PrintWriter out = response.getWriter();
            out.println("An exception was encountered:");
            out.println(exception.getMessage());
            out.println();
            exception.printStackTrace(out);
            out.close();
        }
    }
    
    /**
     * Take the resource and transform it, then send the result back to the client.
     * When called, all logic to generate the resource content must have completed.
     *
     * @param response
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     * @throws TransformerException
     */
    protected void sendResponse(OXOResponse response) throws Exception
    {
        // Retrieve the resource. If it is null, we will output nothing.
        Resource resource = response.getResource();

        // A committed response indicates that we should not try to generate the resource output.
        // One case of this would be if the required action is a redirect or if output has been written
        // directly by the resource for debugging or other purposes.
        if (!response.isCommitted() && resource != null)
        {
            // Create an entity resolver which will be used to resolve external entities referenced in the XSL template.
            OXOEntityResolver entityResolver = new OXOEntityResolver(OXOContext.getTemplatesPackage(), OXOContext.getPropertiesPackage());

            // Determine the XSL template to use in the transformation of this resource.
            StringBuilder stringBuilder = new StringBuilder("template:");
            stringBuilder.append(resource.getClass().getCanonicalName().toLowerCase().replaceFirst(OXOContext.getServletsPackage(), "").replace(".", "/"));
            stringBuilder.append(".");
            stringBuilder.append(response.getOutputType());
            stringBuilder.append(".xsl");

            String xslTemplate = stringBuilder.toString();

            // Retrieve the parent template.
            InputSource inputSource = entityResolver.resolveEntity(null, xslTemplate);

            // Create an XML reader which will be used to process the xsl template.
            // We assign our own custom entity resolver so that it can resolve
            // entities that are to be located using our customized template scheme.
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setEntityResolver(entityResolver);

            // Create a SAXSource object.
            SAXSource xslSource = new SAXSource(inputSource);
            xslSource.setXMLReader(xmlReader);

            // Convert the resource object into XML using XStream.
            XStream xStream = new XStream(new DomDriver());
            xStream.setMode(XStream.NO_REFERENCES);
            
            xStream.registerConverter(new ClientConverter(), XStream.PRIORITY_LOW);
            xStream.registerConverter(new OXORequestConverter());
            xStream.registerConverter(new OXOResponseConverter());

            xStream.aliasType("page", Page.class);
            xStream.aliasType("user", User.class);
            xStream.aliasType("preferences", Preferences.class);

            xStream.processAnnotations(resource.getClass());

            // Print the resource`s xml to the console for debugging.
            System.out.print(xStream.toXML(resource));

            TraxSource xmlSource = new TraxSource(resource, xStream);

            // Create a transformer.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(new OXOURIResolver(entityResolver));
            
            ErrorListener errorListener = new ErrorListener() {
                @Override
                public void warning(TransformerException exception) throws TransformerException {
                    throw new RuntimeException(exception.getMessage());
                }

                @Override
                public void error(TransformerException exception) throws TransformerException {
                    throw new RuntimeException(exception.getMessage());
                }

                @Override
                public void fatalError(TransformerException exception) throws TransformerException {
                    throw new RuntimeException(exception.getMessage());
                }
                
            };
            transformerFactory.setErrorListener(errorListener);

            try
            {
                Transformer transformer = transformerFactory.newTransformer(xslSource);

                String mediaType = transformer.getOutputProperty("media-type");
                if (mediaType == null)
                {
                    // Set the default.
                    mediaType = "text/xml"; // NOTE!! Seems this might need a little more. NOTE!!
                }

                String encoding = transformer.getOutputProperty("encoding");
                if (encoding == null)
                {
                    // Set the default.
                    encoding = "UTF-8";
                }

                // Set important response headers.
                response.setContentType(mediaType);
                response.setCharacterEncoding(encoding);

                // Write output.
                PrintWriter out = response.getWriter();
                transformer.transform(xmlSource, new StreamResult(out));
                out.close();
            }
            catch (TransformerConfigurationException exception)
            {
                throw new Exception(exception.getMessage() + ". This stylesheet may contain an error: " + xslTemplate + ".", exception);
            }
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        handleRequest(new OXORequest(request), new OXOResponse(response));
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        handleRequest(new OXORequest(request), new OXOResponse(response));
    }
}