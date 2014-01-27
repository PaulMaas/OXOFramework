package com.centropoly.oxo;

/**
 * @author Paul van der Maas
 */
public class RequestParameterException extends Exception
{
    protected String parameterName;

    public RequestParameterException(String parameterName)
    {
        this.parameterName = parameterName;
    }

    public RequestParameterException(String parameterName, String message)
    {
        super(message);
        this.parameterName = parameterName;
    }

    public RequestParameterException(String parameterName, String message, Throwable cause)
    {
        super(message, cause);
        this.parameterName = parameterName;
    }

    public RequestParameterException(String parameterName, Throwable cause)
    {
        super(cause);
        this.parameterName = parameterName;
    }

    public String getParameterName()
    {
        return parameterName;
    }
}
