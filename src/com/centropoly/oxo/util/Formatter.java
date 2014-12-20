package com.centropoly.oxo.util;

import com.centropoly.oxo.OXOContext;
import java.text.NumberFormat;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

/**
 * If need be, one can access the methods in this class easily from XSLT by use of
 * special features available in Apache's Xalan XSLT Processor. See Xalan's
 * documentation on this: http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext.
 * 
 * @author Paul van der Maas
 */
public final class Formatter
{
    // Do not allow instantiation.
    private Formatter()
    {
    }
    
    public static String formatNumber(double number) {
        return NumberFormat.getNumberInstance(OXOContext.getUser().getPreferences().getLocale()).format(number);
    }
    
    public static String formatCurrency(double currency)
    {
        return NumberFormat.getCurrencyInstance(OXOContext.getUser().getPreferences().getLocale()).format(currency);
    }
    
    public static String formatDateTime(String dateTime, String pattern)
    {
        return new DateTime(dateTime).toString(DateTimeFormat.forPattern(pattern).withLocale(OXOContext.getUser().getPreferences().getLocale()));
    }
    
    public static String formatShortDate(String date)
    {
        return new DateTime(date).toString(DateTimeFormat.shortDate().withLocale(OXOContext.getUser().getPreferences().getLocale()));
    }
    
    public static String formatMediumDate(String date)
    {
        return new DateTime(date).toString(DateTimeFormat.mediumDate().withLocale(OXOContext.getUser().getPreferences().getLocale()));
    }
    
    public static String formatLongDate(String date)
    {
        return new DateTime(date).toString(DateTimeFormat.longDate().withLocale(OXOContext.getUser().getPreferences().getLocale()));
    }
    
    public static String formatFullDate(String date)
    {
        return new DateTime(date).toString(DateTimeFormat.fullDate().withLocale(OXOContext.getUser().getPreferences().getLocale()));
    }
    
    public static String toLowerCase(String string)
    {
        return string.toLowerCase();
    }
    
    public static String toUpperCase(String string)
    {
        return string.toUpperCase();
    }
}
