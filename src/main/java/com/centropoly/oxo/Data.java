package com.centropoly.oxo;

import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 * An instance of this class should contain all the information
 * necessary to build a response to a request in the OXO framework.
 * 
 * If neither of the cache control methods is implemented, caching is effectively turned off.
 * 
 * @author Paul van der Maas
 */
public abstract class Data
{
    /**
     * This method must be implemented and should load any information needed to
     * build the associated response.
     * 
     * @param request
     * @param response
     * @throws Exception 
     */
    public abstract void build(OXORequest request, OXOResponse response) throws Exception;

    /**
     * Override this method to take advantage of automated caching by specifying
     * when the data was last modified.
     * 
     * By default, the return value is null.
     * 
     * @return Last modified date/time.
     */
    public DateTime getLastModifiedDateTime()
    {
        return null;
    }

    /**
     * Override this method to take advantage of automated caching by specifying
     * how long the data can be reused before it expires and needs to be refreshed.
     * 
     * By default, the return value is null.
     * 
     * @return Duration object which may be specified in milliseconds.
     */
    public Duration getExpirationDuration()
    {
        return null;
    }
}
