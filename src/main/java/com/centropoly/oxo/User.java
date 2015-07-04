package com.centropoly.oxo;

/**
 * @author Paul van der Maas
 */
public class User
{
    protected Client client;
    protected Preferences preferences;

    public User(Client client)
    {
        this.client = client;

        Object persistedPreferences = client.getSession().getAttribute("preferences");
        if (persistedPreferences != null && Preferences.class.isAssignableFrom(persistedPreferences.getClass()))
        {
            preferences = new Preferences(client, (Preferences)persistedPreferences);
        }
        else
        {
            preferences = new Preferences(client);
        }

        // We store the preferences in the session.
        client.getSession().setAttribute("preferences", preferences);
    }

    public Client getClient()
    {
        return client;
    }
    
    public Preferences getPreferences()
    {
        return preferences;
    }
}