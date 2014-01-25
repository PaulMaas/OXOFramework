package com.centropoly.oxo;

import com.centropoly.oxo.OXOResponse.TransformationOutputType;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A web service has the special ability to transform XML to JSON if so requested.
 * 
 * @author Paul van der Maas
 */
public abstract class WebService extends OXOServlet
{
    private boolean responseAsJSON = false;
    private String jsonXslTemplate = "template:/xmltojsonml.xsl";

    @Override
    protected void initialize(OXORequest request, OXOResponse response) {
        super.initialize(request, response);

        // Providing a default for the response output type.
        response.setTransformationOutputType(TransformationOutputType.XML);
    }

    protected void outputResponseAsJSON(final OXOResponse response) throws IOException, SAXException, TransformerException {
        // We can only transform transformed XML or untransformed XML to JSON, hence the following requirement.
        if (response.getTransformationOutputType() == null || response.getTransformationOutputType() == OXOResponse.TransformationOutputType.XML) {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            // To prevent deadlock, we have to perform the piping in another thread.
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    try {
                        outputResponse(response, out);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    } finally {
                        try {
                            out.close();
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
                }
            };
            
            UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    try {
                        outputException(response, new Exception(throwable));
                    } catch (IOException exception) {
                    }
                }
            };

            Thread thread = new Thread(runner);
            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            thread.start();

            // Create an entity resolver which will be used to resolve the XSL template.
            OXOEntityResolver entityResolver = new OXOEntityResolver("com.centropoly.oxo.templates", null);

            String xslTemplate = this.jsonXslTemplate;

            // Retrieve the Json XSL template.
            InputSource inputSource = entityResolver.resolveEntity(null, xslTemplate);

            // Create an XML reader which will be used to process the XSL template.
            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setEntityResolver(entityResolver);

            // Create the SAXSource objects.
            SAXSource xslSource = new SAXSource(xmlReader, inputSource);
            SAXSource xmlSource = new SAXSource(xmlReader, new InputSource(in));
            
            // Create a transformer.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setErrorListener(new OXOErrorListener());

            try {
                Transformer transformer = transformerFactory.newTransformer(xslSource);

                String mediaType = transformer.getOutputProperty("media-type");
                if (mediaType == null)
                {
                    // Set the default.
                    mediaType = "application/json";
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

                // Transform the XML to JSON.
                transformer.transform(xmlSource, new StreamResult(response.getOutputStream()));
            } catch (TransformerException exception) {
                // Wrap the exception to add additional information.
                throw new TransformerException(exception.getMessage() + " The JSON stylesheet may contain an error: " + xslTemplate + ".", exception);
            }
        } else {
            throw new TransformerException("Only XML can be transformed to JSON. The response's output type was set to " + response.getTransformationOutputType());
        }
    }

    @Override
    protected void outputResponse(OXOResponse response) throws IOException, SAXException, TransformerException {
        if (this.responseAsJSON) {
            this.outputResponseAsJSON(response);
        } else {
            super.outputResponse(response);
        }
    }
    
    public boolean responseAsJSON() {
        return this.responseAsJSON;
    }
    
    public void setResponseAsJSON() {
        setResponseAsJSON(true);
    }
    
    public void setResponseAsJSON(boolean responseAsJSON) {
        this.responseAsJSON = responseAsJSON;
    }
}