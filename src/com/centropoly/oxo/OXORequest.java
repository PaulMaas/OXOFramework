package com.centropoly.oxo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author Paul van der Maas
 */
public class OXORequest extends HttpServletRequestWrapper
{
    private Map<String, String[]> dynamicParameters;

    public OXORequest(HttpServletRequest request) throws IOException
    {
        super(request);

        this.dynamicParameters = new HashMap<String, String[]>();
    }
    
    public URI getURI() throws URISyntaxException
    {
        return new URI(getServletPath() + ((getQueryString() != null) ? "?" + getQueryString() : ""));
    }

    /**
     * TODO: One should be able to overwrite request parameters that were set elsewhere
     * as well as dynamic parameters. Simply check dynamic parameters first when 'getting'.
     * 
     * @param name
     * @param values 
     */
    public void setParameters(String name, String[] values)
    {
        this.dynamicParameters.put(name, values);
    }

    public void setParameter(String name, String value)
    {
        this.dynamicParameters.put(name, new String[] {value});
    }

    @Override
    public String getParameter(String name)
    {
        String value = super.getParameter(name);
        if (value == null)
        {
            if (this.dynamicParameters.containsKey(name))
            {
                return this.dynamicParameters.get(name)[0];
            }
            else
            {
                return null;
            }
        }
        else
        {
            return value;
        }
    }

    @Override
    public Map getParameterMap()
    {
        Map<String, String[]> mergedParameterMap = new HashMap<String, String[]>();

        mergedParameterMap.putAll(super.getParameterMap());
        mergedParameterMap.putAll(this.dynamicParameters);

        return mergedParameterMap;
    }

    @Override
    public Enumeration getParameterNames()
    {
        return Collections.enumeration(this.getParameterMap().keySet());
    }

    @Override
    public String[] getParameterValues(String name)
    {
        String[] values = super.getParameterValues(name);
        if (values == null)
        {
            if (this.dynamicParameters.containsKey(name))
            {
                return this.dynamicParameters.get(name);
            }
            else
            {
                return null;
            }
        }
        else
        {
            return values;
        }
    }
}