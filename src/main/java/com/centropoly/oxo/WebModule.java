package com.centropoly.oxo;

/**
 * @author Paul van der Maas
 */
public abstract class WebModule extends CachingOXOServlet
{
    @Override
    protected void init(OXORequest request, OXOResponse response)
    {
        super.init(request, response);

        // Providing a default for the response output type.
        response.setTransformationOutputType(OXOResponse.TransformationOutputType.XHTML);
    }
}