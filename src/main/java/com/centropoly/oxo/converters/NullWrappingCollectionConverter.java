package com.centropoly.oxo.converters;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * @author Paul van der Maas
 */
public class NullWrappingCollectionConverter extends CollectionConverter
{
    public NullWrappingCollectionConverter(Mapper mapper)
    {
        super(mapper);
    }

    public NullWrappingCollectionConverter(Mapper mapper, Class type)
    {
        super(mapper, type);
    }

    @Override
    protected void writeCompleteItem(Object item, MarshallingContext context, HierarchicalStreamWriter writer)
    {
        super.writeCompleteItem(item, context, writer);
    }

    @Override
    protected void writeNullItem(MarshallingContext context, HierarchicalStreamWriter writer)
    {
        super.writeNullItem(context, writer);
    }
}
