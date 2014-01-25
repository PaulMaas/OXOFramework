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
    private TransformationOutputType transformationOutputType = null;
    
    private List<Exception> exceptions;
    private List<Notification> notifications;

    private Object data;

    public OXOResponse(HttpServletResponse response)
    {
        super(response);

        this.exceptions = new ArrayList<Exception>();
        this.notifications = new ArrayList<Notification>();
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Object getData() {
        return this.data;
    }

    public OXORequest getRequest() {
        return OXOContext.getRequest();
    }

    public void setTransformationOutputType(TransformationOutputType transformationOutputType) {
        this.transformationOutputType = transformationOutputType;
    }
    
    public TransformationOutputType getTransformationOutputType() {
        return this.transformationOutputType;
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

    public enum TransformationOutputType
    {
        XHTML ("xhtml"),
        HTML ("html"),
        TEXT ("text"),
        XML ("xml");

        private final String value;

        TransformationOutputType(String value)
        {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return this.value;
        }
    }
}