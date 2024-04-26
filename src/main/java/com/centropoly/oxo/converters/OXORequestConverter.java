package com.centropoly.oxo.converters;

import com.centropoly.oxo.OXORequest;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

public class OXORequestConverter implements Converter
{
    protected final static int MAX_PARAMETER_VALUE_LENGTH = 1000;

    @Override
    public boolean canConvert(Class clazz)
    {
        return OXORequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        @SuppressWarnings("unchecked")
        OXORequest request = (OXORequest) object;

        writer.startNode("user");
        if (request.getUser() != null)
        {
            context.convertAnother(request.getUser());
        }
        writer.endNode();

        Map<String, String[]> parameterMap = request.getParameterMap();
        writer.startNode("parameters");
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
        {
            writer.startNode("parameter");
            writer.addAttribute("name", entry.getKey());
            String[] values = entry.getValue();
            for (String value : values) {
                if (value != null) {
                    writer.startNode("value");
                    if (value.length() < MAX_PARAMETER_VALUE_LENGTH)
                    {
                        writer.setValue(value);
                    }
                    else
                    {
                        writer.setValue(">Value exceeds maximum length (" + MAX_PARAMETER_VALUE_LENGTH + ")<");
                    }
                    
                    writer.endNode();
                }
            }
            writer.endNode();
        }
        writer.endNode();

        this.marshalObjectMethodMappings(request, new LinkedHashMap() {{
            put("method", "getMethod");
            put("scheme", "getScheme");
            put("serverName", "getServerName");
            put("serverPort", "getServerPort");
            put("contextPath", "getContextPath");
            put("servletPath", "getServletPath");
        }}, writer);
    }

    private void marshalObjectMethodMappings(Object object, Map<String,String> methodMappings, HierarchicalStreamWriter writer) {
        for (Map.Entry<String, String> entry : methodMappings.entrySet()) {
            writer.startNode(entry.getKey());
            try {
                writer.setValue(object.getClass().getMethod(entry.getValue()).invoke(object).toString());
            } catch(ReflectiveOperationException exception) {
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}