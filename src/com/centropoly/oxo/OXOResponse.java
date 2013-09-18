/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.centropoly.oxo;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author paul
 */
public class OXOResponse extends HttpServletResponseWrapper
{
    private Resource resource = null;
    private OutputType outputType = null;
    
    private List<Exception> exceptions = null;
    private List<Notification> notifications = null;

    public OXOResponse(HttpServletResponse response)
    {
        super(response);

        this.exceptions = new ArrayList<Exception>();
        this.notifications = new ArrayList<Notification>();
    }
    
    public void setResource(Resource resource) {
        this.resource = resource;
    }
    
    public Resource getResource() {
        return this.resource;
    }
    
    public void setOutputType(OutputType outputType) {
        this.outputType = outputType;
    }
    
    public OutputType getOutputType() {
        return this.outputType;
    }

    public List<Exception> getExceptions()
    {
        return this.exceptions;
    }

    /**
     * Add an exception. No action is taken, except to store the exception.
     * Any exceptions that are stores in the request object indicate that there was something
     * wrong with the client's request. Most likely, they did not complete a form correctly.
     *
     * @param exception
     */
    public void addException(Exception exception)
    {
        this.exceptions.add(exception);
    }

    /**
     * Check if this request generated exceptions.
     *
     * @return
     */
    public boolean hasExceptions()
    {
        return !this.exceptions.isEmpty();
    }

    public List<Notification> getNotifications()
    {
        return this.notifications;
    }

    public void addNotification(Notification notification)
    {
        this.notifications.add(notification);
    }

    /**
     * Check if this request generated exceptions.
     *
     * @return
     */
    public boolean hasNotifications()
    {
        return !this.notifications.isEmpty();
    }
    
    public final static class Notification
    {
        private String message = null;
        
        public Notification(String message)
        {
            this.message = message;
        }

        public String getMessage()
        {
            return message;
        }
    }

    public enum OutputType
    {
        XHTML ("xhtml"),
        HTML ("html"),
        XML ("xml"),
        TEXT ("text");

        private final String value;

        OutputType(String value)
        {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return this.value;
        }
    }
}