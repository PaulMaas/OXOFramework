package com.centropoly.oxo;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class resolves entities (not XML entities) within the XSLT that is used
 * to transform object XML to whatever output is needed.
 * This class handles template resolution for includes in XSLT.
 * 
 * NOTE: This should also start accepting the property protocol.
 */
public class OXOEntityResolver implements EntityResolver
{
    private String templatesFolder;
    private String propertiesPackage;

    /**
     * Create an OXOEntityResolver.
     * 
     * @param templatesPackage the package to search for templates
     */
    public OXOEntityResolver(String templatesPackage, String propertiesPackage)
    {
        this.templatesFolder = templatesPackage.replace(".", "/");
        this.propertiesPackage = propertiesPackage;
    }
    
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
    {
        URI uri;
        try
        {
            uri = new URI(systemId);
        }
        catch (URISyntaxException exception)
        {
            throw new IOException(exception);
        }

        if (uri.isAbsolute())
        {
            if (uri.getScheme().equalsIgnoreCase("template"))
            {
                String templatePath = this.templatesFolder + uri.getSchemeSpecificPart();

                try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath))
                {
                    if (inputStream != null)
                    {
                        return new InputSource(inputStream);
                    }
                    else
                    {
                        throw new IOException("A referenced external (template) entity could not be found: " + templatePath);
                    }
                }
            }
            else if (uri.getScheme().equalsIgnoreCase("property"))
            {
                String[] parts = uri.getSchemeSpecificPart().split(":", 2);

                String propertyBundlePath = this.propertiesPackage + "." + parts[0];
                
                if (parts.length == 2)
                {
                    // The first part reflects the name of the property resource bundle.
                    ResourceBundle resourceBundle = PropertyResourceBundle.getBundle(propertyBundlePath, OXOContext.getUser().getPreferences().getLocale());

                    // The second part should lead us to the desired property inside the property resource bundle.
                    return new InputSource(new StringReader(resourceBundle.getString(parts[1])));
                }
                else
                {
                    throw new IOException("A referenced external (property) entity could not be found: " + propertyBundlePath + ":" + parts[1]);
                }
            }
        }
        
        // Let the parser deal with this entity.
        return null;
    }
}