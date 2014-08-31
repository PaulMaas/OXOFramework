package com.centropoly.oxo.converter;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class DateTimeConverter implements Converter
{
    @Override
    public boolean canConvert(Class clazz)
    {
        return DateTime.class.isAssignableFrom(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        DateTime dateTime = (DateTime) object;
        
        writer.startNode("dateTime");
        writer.setValue(dateTime.toString(ISODateTimeFormat.dateTimeNoMillis()));
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}