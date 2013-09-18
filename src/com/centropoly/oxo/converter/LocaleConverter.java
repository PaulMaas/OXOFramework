package com.centropoly.oxo.converter;

import com.centropoly.oxo.OXOContext;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.Locale;

public class LocaleConverter implements Converter
{
    @Override
    public boolean canConvert(Class clazz)
    {
        return Locale.class.isAssignableFrom(clazz);
    }

    @Override
    public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context)
    {
        Locale locale = (Locale) object;
        
        writer.startNode("languageCode");
        writer.setValue(locale.getLanguage());
        writer.endNode();
        writer.startNode("language");
        writer.setValue(locale.getDisplayLanguage(OXOContext.getUser().getPreferences().getLocale()));
        writer.endNode();
        writer.startNode("countryCode");
        writer.setValue(locale.getCountry());
        writer.endNode();
        writer.startNode("country");
        writer.setValue(locale.getDisplayCountry(OXOContext.getUser().getPreferences().getLocale()));
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
    {
        throw new UnsupportedOperationException("Not supported.");
    }
}