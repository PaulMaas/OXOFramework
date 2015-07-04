package com.centropoly.oxo;

/**
 * @author Paul van der Maas
 */
public abstract class WebModule extends OXOServlet
{
    /**
     * This is a good place to (for instance) set up your customized OXOContext
     * instance. For instance, by overriding this function in a subclass.
     * 
     * @param request
     * @param response 
     */
    @Override
    protected void initialize(OXORequest request, OXOResponse response) {
        super.initialize(request, response);

        // Providing a default for the response output type.
        response.setTransformationOutputType(OXOResponse.TransformationOutputType.XHTML);
    }
}