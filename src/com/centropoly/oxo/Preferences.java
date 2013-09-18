package com.centropoly.oxo;

import com.centropoly.oxo.converter.LocaleConverter;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import java.io.Serializable;
import java.util.Locale;

/**
 * Preferences can be set manually by the application based on some
 * application specific information (like account information),
 * but default to values retrieved from the client through the request object.
 * 
 * @author Paul van der Maas
 */
public class Preferences implements Serializable
{
    // Used for fallback defaults.
    private transient Client client;

    @XStreamConverter(LocaleConverter.class)
    private Locale locale = null;

    public Preferences(Client client)
    {
        this.client = client;
    }

    public Preferences(Client client, Preferences preferences)
    {
        this(client);

        locale = preferences.locale;
    }

    public Locale getLocale()
    {
        if (locale == null)
        {
            return client.getLocale();
        }
        else
        {
            return locale;
        }
    }

    public void setLocale(Locale locale)
    {
        this.locale = locale;
    }
}