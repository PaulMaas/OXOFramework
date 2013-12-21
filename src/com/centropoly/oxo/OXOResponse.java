package com.centropoly.oxo;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * @author Paul van der Maas
 */
public class OXOResponse extends HttpServletResponseWrapper
{
    private OutputType outputType = OutputType.XML_UNTRANSFORMED;
    
    private List<Exception> exceptions;
    private List<Notification> notifications;

    private Data data;

    public OXOResponse(HttpServletResponse response)
    {
        super(response);

        this.exceptions = new ArrayList<Exception>();
        this.notifications = new ArrayList<Notification>();
    }
    
    public void setData(Data data) {
        this.data = data;
    }
    
    public Data getData() {
        return this.data;
    }

    public OXORequest getRequest() {
        return OXOContext.getRequest();
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
        TEXT ("text"),
        XML ("xml"),
        XML_UNTRANSFORMED ("xml");

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