package com.centropoly.oxo;

/**
 * OXOContext is intended to be an easy access path to the most important parameters
 * needed in the OXO Framework. It is used by the frameworks inner parts, but can
 * also be used by the end-user to gain easy access to all the major parts
 * that make up the framework. It's state properties will/should be initialized very early
 * on in the request/response cycle so that availability of the data is guaranteed.
 * 
 * This class is intended to only have static methods and as such should not be
 * instantiated. Hence the protected constructor.
 * 
 * End-users are encouraged to extend this class to bring additional functionality
 * to their particular environment. For example, by providing authorization/authentication methods.
 * 
 * TODO: This needs to be redesigned, rethought. There may be an issue here with multiple request interfering
 * with each other. Is a new OXOContext created for each servlet? Or are they shared between servlets.
 * This will cause issues.
 */

public class OXOContext
{
    private static User user = null;
    private static OXORequest request = null;
    private static OXOResponse response = null;
    
    private static String templatesPackage = null;
    private static String propertiesPackage = null;
    private static String servletsPackage = null;
    
    // These have defaults.
    private static boolean debug = false;
    private static boolean cache = true;

    // Do not allow instantiation.
    protected OXOContext()
    {
    }
    
    public static String getPropertiesPackage()
    {
        return propertiesPackage;
    }

    public static void setPropertiesPackage(String propertiesPackage)
    {
        OXOContext.propertiesPackage = propertiesPackage;
    }

    public static String getServletsPackage()
    {
        return servletsPackage;
    }

    public static void setServletsPackage(String servletsPackage)
    {
        OXOContext.servletsPackage = servletsPackage;
    }

    public static String getTemplatesPackage()
    {
        return templatesPackage;
    }

    public static void setTemplatesPackage(String templatesPackage)
    {
        OXOContext.templatesPackage = templatesPackage;
    }
    
    // Temporary, remove everything below after rewrite.
    
    public static User getUser()
    {
        return user;
    }

    public static void setUser(User user)
    {
        OXOContext.user = user;
    }

    public static OXORequest getRequest()
    {
        return request;
    }
    
    public static void setRequest(OXORequest request)
    {
        OXOContext.request = request;
    }

    public static OXOResponse getResponse()
    {
        return response;
    }

    public static void setResponse(OXOResponse response)
    {
        OXOContext.response = response;
    }

    public static boolean debug()
    {
        return debug;
    }

    public static void debug(boolean debug)
    {
        OXOContext.debug = debug;
    }

    public static boolean cache()
    {
        return cache;
    }

    public static void cache(boolean cache)
    {
        OXOContext.cache = cache;
    }
}