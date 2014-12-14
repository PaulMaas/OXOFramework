package com.centropoly.oxo.converters;

import com.centropoly.oxo.OXORequest;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.Iterator;
import java.util.Map;

public class OXORequestConverter implements Converter
{
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
        Map parameterMap = request.getParameterMap();

        writer.startNode("user");
        context.convertAnother(request.getUser());
        writer.endNode();
        
        writer.startNode("contextPath");
        if (request.getContextPath() != null)
        {
            writer.setValue(request.getContextPath());
        }
        writer.endNode();

        writer.startNode("servletPath");
        if (request.getServletPath() != null)
        {
            writer.setValue(request.getServletPath());
        }
        writer.endNode();

        writer.startNode("parameters");
        for (Iterator iterator = parameterMap.entrySet().iterator(); iterator.hasNext();)
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            writer.startNode("parameter");
            writer.addAttribute("name", entry.getKey().toString());
            String[] values = (String[]) entry.getValue();
            for (int i = 0; i < values.length; i++)
            {
                writer.startNode("value");
                writer.setValue(values[i]);
                writer.endNode();
            }
            writer.endNode();
        }
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}