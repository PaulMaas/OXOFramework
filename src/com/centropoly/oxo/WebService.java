package com.centropoly.oxo;

import com.centropoly.oxo.OXOResponse.OutputType;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    @Override
    protected void initialize(OXORequest request, OXOResponse response) {
        super.initialize(request, response);

        // Providing a default for the response output type.
        response.setOutputType(OutputType.XML);
    }

    protected void outputResponseAsJSON(final OXOResponse response) throws IOException, SAXException, TransformerException {
        // We can only transform transformed XML or untransformed XML to JSON, hence the following requirement.
        if (response.getOutputType() == null || response.getOutputType() == OXOResponse.OutputType.XML || response.getOutputType() == OXOResponse.OutputType.XML_UNTRANSFORMED) {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);

            // To prevent deadlock, we have to perform the piping in another thread.
            Runnable runner = new Runnable() {
                @Override
                public void run() {
                    try {
                        outputResponse(response, out);
                        out.close();
                    } catch (Exception exception) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            throw new RuntimeException(exception);
                        }
                        throw new RuntimeException(exception);
                    }
                }
            };
            
//            UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
//                @Override
//                public void uncaughtException(Thread thread, Throwable throwable) {
//                    try {
//                        in.close();
//                    } catch (IOException exception) {
//                        throw new RuntimeException(exception);
//                    }
//                    throw new RuntimeException(throwable);
//                }
//            };

            Thread thread = new Thread(runner);
//            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            thread.start();

            // Create an entity resolver which will be used to resolve the XSL template.
            OXOEntityResolver entityResolver = new OXOEntityResolver("com.centropoly.oxo.templates", null);

            String xslTemplate = "template:/xmltojsonml.xsl";

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

            // TODO: It may be fine to remove this try if the original exception gives enough info.
            try
            {
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
            }
            catch (TransformerException exception)
            {
                throw new TransformerException(exception.getMessage() + ". There is a problem with the JSON XSL template.", exception);
            }
        } else {
            throw new TransformerException("Only XML can be transformed to JSON. The response's output type was set to " + response.getOutputType());
        }
    }

    @Override
    protected void outputResponse(OXOResponse response) throws IOException, SAXException, TransformerException {
        // TODO: how do we want to decide this?
        // Either through a request parameter or maybe the accept http header in the request.
        // Or by setting a class property? Or a combination?
        if (true) {
            this.outputResponseAsJSON(response);
        } else {
            super.outputResponse(response);
        }
    }
    
    
}