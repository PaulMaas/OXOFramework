package com.centropoly.oxo.converter;

import com.centropoly.oxo.OXOResponse;
import com.centropoly.oxo.OXOResponse.Notification;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class OXOResponseConverter implements Converter
{
    @Override
    public boolean canConvert(Class clazz)
    {
        return OXOResponse.class.isAssignableFrom(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        OXOResponse response = (OXOResponse) object;

        writer.startNode("notifications");
        for (Notification notification : response.getNotifications())
        {
            writer.startNode("notification");
            writer.startNode("message");
            writer.setValue(notification.getMessage());
            writer.endNode();
            writer.endNode();
        }
        writer.endNode();

        writer.startNode("exceptions");
        for (Exception exception : response.getExceptions())
        {
            writer.startNode("exception");
            writer.startNode("message");
            writer.setValue(exception.getMessage());
            writer.endNode();
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