package com.centropoly.oxo;

import java.util.Locale;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

/**
 * The Client class attempts to give a more intuitive
 * access path to some of the client related information.
 * It represents the browser/machine that makes the request
 * and receives the corresponding response.
 * 
 * @author Paul van der Maas
 */
public final class Client
{
    private final OXORequest request;
    private final OXOResponse response;

    public Client(OXORequest request, OXOResponse response)
    {
        this.request = request;
        this.response = response;
    }

    public String getAddress()
    {
        return this.request.getRemoteAddr();
    }

    public String getHost()
    {
        return this.request.getRemoteHost();
    }

    public Locale getLocale()
    {
        return this.request.getLocale();
    }

    public HttpSession getSession()
    {
        return this.request.getSession();
    }

    public Cookie[] getCookies()
    {
        return this.request.getCookies();
    }
    
    public Cookie getCookie(String name)
    {
        for (Cookie cookie : getCookies())
        {
            if (cookie.getName().equals(name))
            {
                return cookie;
            }
        }
        
        return null;
    }

    public void addCookie(Cookie cookie)
    {
        response.addCookie(cookie);
    }

    // Maybe add access to authentication stuff from request here to.
}