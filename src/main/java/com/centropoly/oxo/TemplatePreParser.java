package com.centropoly.oxo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

// NOTE: Rename and modify this class so it can be used to translate property xml entities both in templates
// and in code. Possibly alter the property

/**
 * This class will resolve special XML entities within the XSLT. The special XML
 * entities as of now are strictly 'property' entities. They are of the form
 *
 * <code>&property:propertyFileName:propertyName</code>
 *
 * These special XML entities cannot be resolved by the XSLT parser, so here we
 * use this class to replace them with their corresponding values.
 */
@Deprecated
public class TemplatePreParser
{
    String propertiesPackage;
    Preferences preferences;

    public TemplatePreParser(String propertiesPackage, Preferences preferences)
    {
        this.propertiesPackage = propertiesPackage;
        this.preferences = preferences;
    }
    
    public InputSource parse(InputSource inputSource) throws SAXException, IOException
    {
        if (0 == 0) throw new IOException("Template pre parser is being used");
        
        // Matches OXO's special property entities.
        Pattern pattern = Pattern.compile("&property:([A-Za-z0-9-_\\.]+:[A-Za-z0-9-_\\.]+);");

        // Create a string buffered writer.
        StringWriter stringWriter = new StringWriter();

        // Wrap the string writer in a buffered writer.
        BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);

        BufferedReader bufferedReader;
        if (inputSource.getByteStream() != null)
        {
            bufferedReader = new BufferedReader(new InputStreamReader(inputSource.getByteStream()));
        }
        else if (inputSource.getCharacterStream() != null)
        {
            bufferedReader = new BufferedReader(inputSource.getCharacterStream());
        }
        else
        {
            throw new IOException(new NullPointerException("Invalid input source."));
        }

        String line;
        for (line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
        {
            Matcher matcher = pattern.matcher(line);
            
            while (matcher.find())
            {
                InputSource propertyValue = null;

                // Replace the property entity with its value.
                String[] parts = matcher.group(1).split(":", 2);

                if (parts.length == 2)
                {
                    // The first part reflects the name of the property resource bundle.
                    ResourceBundle resourceBundle = PropertyResourceBundle.getBundle(propertiesPackage + "." + parts[0], preferences.getLocale());

                    // The second part should lead us to the desired property inside the property resource bundle.
                    propertyValue = this.parse(new InputSource(new StringReader(resourceBundle.getString(parts[1]))));
                }
                else
                {
                    throw new SAXException("Malformed property XML entity: " + matcher.group());
                }

                // If we were able to resolve the entity, replace the entity with its value.
                if (propertyValue != null)
                {
                    BufferedReader propertyValueReader = new BufferedReader(propertyValue.getCharacterStream());
                    StringBuilder entityReplacement = new StringBuilder();
                    for (int character = propertyValueReader.read(); character != -1; character = propertyValueReader.read())
                    {
                        entityReplacement.append((char) character);
                    }

                    line = line.replace(matcher.group(), entityReplacement);
                }
                else
                {
                    throw new SAXException("No value for property XML entity: " + matcher.group());
                }
            }

            bufferedWriter.write(line);
        }

        bufferedWriter.flush();

        return new InputSource(new StringReader(stringWriter.toString()));
    }
}