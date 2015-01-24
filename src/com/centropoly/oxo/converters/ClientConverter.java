package com.centropoly.oxo.converters;

import com.centropoly.oxo.Client;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import javax.servlet.http.Cookie;

public class ClientConverter implements Converter
{
    @Override
    public boolean canConvert(Class clazz)
    {
        return Client.class.isAssignableFrom(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        @SuppressWarnings("unchecked")
        Client client = (Client) object;

        Cookie[] cookies = client.getCookies();
        if (cookies != null)
        {
            writer.startNode("cookies");
            for (Cookie cookie : cookies)
            {
                writer.startNode("cookie");
                writer.addAttribute("name", cookie.getName());
                writer.startNode("value");
                writer.setValue(cookie.getValue());
                writer.endNode();
                writer.endNode();
            }
            writer.endNode();
        }

        writer.startNode("locale");
        context.convertAnother(client.getLocale(), new LocaleConverter());
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}