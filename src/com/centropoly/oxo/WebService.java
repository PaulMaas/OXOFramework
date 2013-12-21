package com.centropoly.oxo;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A web service has the special ability to transform XML to JSon if so requested.
 * 
 * @author Paul van der Maas
 */
public abstract class WebService extends OXOServlet
{
    @Override
    protected void initialize(OXORequest request, OXOResponse response) {
        super.initialize(request, response);

        // Providing a default for the response output type.
        response.setOutputType(OXOResponse.OutputType.XML);
    }

    protected void outputResponseAsJson(final OXOResponse response) throws Exception {
        if (response.getOutputType() == OXOResponse.OutputType.XML || response.getOutputType() == OXOResponse.OutputType.XML_UNTRANSFORMED) {
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            
            new Thread(
                new Runnable(){
                    @Override
                    public void run(){
                        try {
                            outputResponse(response, out);
                            out.close();
                        } catch (Exception e) {}
                    }
                }
            ).start();

            // Create an entity resolver which will be used to resolve the XSL template.
            OXOEntityResolver entityResolver = new OXOEntityResolver("com.centropoly.oxo.templates", null);

            String xslTemplate = "template:/json.xsl";

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
                    mediaType = "text/plain";
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

                transformer.transform(xmlSource, new StreamResult(response.getOutputStream()));
            }
            catch (TransformerConfigurationException exception)
            {
                throw new Exception(exception.getMessage() + ". This stylesheet may contain an error: " + xslTemplate + ".", exception);
            }
        } else {
            throw new Exception("Only XML can be transformed to JSon. The response's output type was set to " + response.getOutputType());
        }
    }

    @Override
    protected void outputResponse(OXOResponse response) throws Exception {
        // NOTE: how do we want to decide this?
        if (true) {
            this.outputResponseAsJson(response);
        } else {
            super.outputResponse(response);
        }
    }
    
    
}