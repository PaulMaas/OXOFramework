package com.centropoly.oxo;

import java.io.IOException;
import java.util.HashMap;
import org.joda.time.DateTime;

/**
 * This class manages caches for the automated caching capabilities in the OXO framework.
 * 
 * @author Paul van der Maas
 */
public final class CachedResponseManager
{
    private final HashMap<String, CachedResponse> cache = new HashMap();

    public CachedResponse getCachedResponse(OXORequest request)
    {
        String cacheKey = createCacheKey(request);
        if (cache.containsKey(cacheKey))
        {
            return cache.get(cacheKey);
        }
        else
        {
            return null;
        }
    }
    
    public CachedOutputStream getCachedResponseOutputStream(OXORequest request)
    {
        CachedResponse cachedResponse = getCachedResponse(request);
        if (cachedResponse != null)
        {
            return cachedResponse.outputStream;
        }
        else
        {
            return null;
        }
    }

    public DateTime getCachedResponseDateTime(OXORequest request)
    {
        CachedResponse cachedResponse = getCachedResponse(request);
        if (cachedResponse != null)
        {
            return cachedResponse.dateTime;
        }
        else
        {
            return null;
        }
    }

    public CachedResponse createCachedResponse(OXORequest request, OXOResponse response) throws IOException
    {
        CachedResponse cachedResponse = new CachedResponse();
        cachedResponse.outputStream = new CachedOutputStream(response.getOutputStream());
        cachedResponse.dateTime = new DateTime();

        cache.put(createCacheKey(request), cachedResponse);
        
        return cachedResponse;
    }

    protected String createCacheKey(OXORequest request)
    {
        return request.getQueryString() + ';' + request.getPathInfo() + ';' + request.getServletPath();
    }
    
    public class CachedResponse
    {
        CachedOutputStream outputStream;
        DateTime dateTime;
    }
}