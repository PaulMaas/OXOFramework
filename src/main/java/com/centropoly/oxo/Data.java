package com.centropoly.oxo;

import org.joda.time.DateTime;

/**
 * An instance of this class should contain all the information
 * necessary to build a response to a request in the OXO framework.
 * 
 * @author Paul van der Maas
 */
public abstract class Data
{
    public abstract void build(OXORequest request, OXOResponse response) throws Exception;

    /**
     * Override this method to take advantage of automated caching by specifying
     * when the data was last modified.
     * 
     * By default, the return value will be null which will disable caching altogether.
     * 
     * @return Date/time last modified.
     */
    public DateTime getDateTimeLastModified() {
        return null;
    }

    /**
     * Override this method to take advantage of automated caching by specifying
     * how long the data can be reused before it needs to be refreshed.
     * 
     * @return Expiration period in seconds.
     */
    public int getExpirationPeriod() {
        return 0;
    }
}
