package com.centropoly.oxo.util;

import com.centropoly.oxo.OXOContext;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

/**
 * If need be, one can access the methods in this class easily from XSLT by use of
 * special features available in Apache's Xalan XSLT Processor. See Xalan's
 * documentation on this: http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext.
 * 
 * @author Paul van der Maas
 */
public final class Properties
{
    // Do not allow instantiation.
    private Properties() {
    }
    
    public static String getString(Locale locale, String bundle, String key, Object... arguments)
    {
        try
        {
            return String.format(PropertyResourceBundle.getBundle(OXOContext.getPropertiesPackage() + "." + bundle, locale).getString(key), (Object[]) arguments);
        }
        catch (NullPointerException exception)
        {
            return "ERROR";
        }
        catch (MissingResourceException exception)
        {
            return "ERROR";
        }
    }
    
    public static String getString(String bundle, String key, Object... arguments)
    {
        return getString(OXOContext.getUser().getPreferences().getLocale(), bundle, key, arguments);
    }

    /**
     *
     * ------------------START WORKAROUND---------------------------------------
     * 
     * The following six functions are part of a workaround to a problem in
     * the SAX parser. At the time of writing, the SAX parser cannot
     * resolve method calls in the XSL to variable-length method calls in Java.
     * 
     * Hopefully in the future, they will support this functionality and
     * we can get rid of this ugly workaround. It allows upto five parameters.
     */

    public static String getString(String bundle, String key)
    {
        return getString(bundle, key, new Object[] {});
    }

    public static String getString(String bundle, String key, Object argument)
    {
        return getString(bundle, key, new Object[] {argument});
    }

    public static String getString(String bundle, String key, Object argumentA, Object argumentB)
    {
        return getString(bundle, key, new Object[] {argumentA, argumentB});
    }

    public static String getString(String bundle, String key, Object argumentA, Object argumentB, Object argumentC)
    {
        return getString(bundle, key, new Object[] {argumentA, argumentB, argumentC});
    }

    public static String getString(String bundle, String key, Object argumentA, Object argumentB, Object argumentC, Object argumentD)
    {
        return getString(bundle, key, new Object[] {argumentA, argumentB, argumentC, argumentD});
    }

    public static String getString(String bundle, String key, Object argumentA, Object argumentB, Object argumentC, Object argumentD, Object argumentE)
    {
        return getString(bundle, key, new Object[] {argumentA, argumentB, argumentC, argumentD, argumentE});
    }

    /**
     * 
     * ------------------END WORKAROUND-----------------------------------------
     * 
     */
}
