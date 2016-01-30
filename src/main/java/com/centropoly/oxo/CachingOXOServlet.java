package com.centropoly.oxo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * This class adds automated caching capabilities to the OXO framework.
 * 
 * TODO: This class needs to be built/filled out more.
 * 
 * Any GET response is automatically cached as long no exceptions or notifications
 * were added to the response during the processing of the request.
 * Additionally, the data object set on the response is used to determine if 
 * and when to prime the cache.
 * 
 * @author Paul van der Maas
 */
public abstract class CachingOXOServlet extends OXOServlet
{
    HashMap<String, CachingOutputStream> cachedOutputStreams = new HashMap();
    //HashMap<String, Long> cachedLastModifieds = new HashMap();

    @Override
    protected void outputResponse(OXORequest request, OXOResponse response, OutputStream outputStream) throws IOException, SAXException, TransformerException, ReflectiveOperationException
    {
        if (request.getMethod().equals("GET") && OXOContext.cache())
        {
            String mapKey = request.getQueryString() + ';' + request.getPathInfo() + ';' + request.getServletPath();
            if (cachedOutputStreams.containsKey(mapKey))
            {
                if (OXOContext.debug()) System.out.println("Use cache...");
                cachedOutputStreams.get(mapKey).getBuffer().writeTo(response.getOutputStream());
            } else {
                if (OXOContext.debug()) System.out.println("Prime cache...");
                CachingOutputStream cachingOutputStream = new CachingOutputStream(outputStream);

                cachedOutputStreams.put(mapKey, cachingOutputStream);
                super.outputResponse(request, response, cachingOutputStream);
            }
        }
        else
        {
            super.outputResponse(request, response, outputStream);
        }
    }
}