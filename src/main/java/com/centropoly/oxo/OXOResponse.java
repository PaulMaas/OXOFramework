package com.centropoly.oxo;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Response wrapper for OXO Framework.
 * 
 * TODO: Notifications are used for announcements and notifications currently.
 * They should be separated. Automated caching is currently disabled if there are 
 * notifications present. If the notifications are really announcements, not set
 * based on some predicate, then they should not disable caching.
 * 
 * @author Paul van der Maas
 */
public class OXOResponse extends HttpServletResponseWrapper
{
    private TransformationOutputType transformationOutputType = null;

    private List<Exception> exceptions;
    private List<Notification> notifications;

    private Data data = null;
    private boolean isCommitted = false;

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
        return data;
    }

    /**
     * When #OXOServlet.processRequest() or #Data.build() have committed (to) the response
     * this should return false to indicate no further response generation/transformation is needed/wanted.
     * 
     * It is possible for #OXOServlet.processRequest() or #Data.build() to write to the response directly,
     * which means the response is committed and we should not try to alter it. It is expected that
     * #isCommitted(true) is called in such an event.
     * 
     * @return 
     */
    @Override
    public boolean isCommitted()
    {
        if (this.isCommitted)
        {
            return true;
        }
        else
        {
            return super.isCommitted();
        }
    }

    public void isCommitted(boolean isCommitted)
    {
        this.isCommitted = isCommitted;
    }

    public OXORequest getRequest() {
        return OXOContext.getRequest();
    }

    public void setTransformationOutputType(TransformationOutputType transformationOutputType) {
        this.transformationOutputType = transformationOutputType;
    }
    
    public TransformationOutputType getTransformationOutputType() {
        return transformationOutputType;
    }

    public List<Exception> getExceptions()
    {
        return this.exceptions;
    }

    /**
     * Add an exception. No action is taken, except to store the exception.
     * Any exceptions that are stores in the request object indicate that there was something
     * wrong with the client's request. Most likely, they did not complete an action correctly.
     *
     * @param exception
     */
    public void addException(Exception exception)
    {
        this.exceptions.add(exception);
    }

    // For convenience...
    public void addException(String exception)
    {
        this.exceptions.add(new Exception(exception));
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

    // For convenience...
    public void addNotification(String notification)
    {
        this.notifications.add(new Notification(notification));
    }

    /**
     * Check if this request generated notifications.
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