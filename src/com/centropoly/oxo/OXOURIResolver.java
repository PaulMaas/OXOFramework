package com.centropoly.oxo;

import java.net.URI;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Paul van der Maas
 */
public class OXOURIResolver implements URIResolver
{
    EntityResolver entityResolver;

    public OXOURIResolver(EntityResolver entityResolver)
    {
        this.entityResolver = entityResolver;
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException
    {
        try
        {
            InputSource inputSource = this.entityResolver.resolveEntity(null, href);
            
            // If the entity resolver could not resolve the path, we will check whether the path is relative or absolute
            // and resolve it accordingly.
            // If the base parameter is not null, it will be used to resolve the relative path.
            // If the base parameter is null, the relative path is resolved using the class loader that loaded the document.
            if (inputSource == null)
            {
                URI uri = URI.create(base).resolve(href);
                inputSource = new InputSource(uri.toURL().openStream());
            }
            
            return new SAXSource(inputSource);
        }
        catch (Exception exception)
        {
            throw new TransformerException(exception);
        }
    }
}