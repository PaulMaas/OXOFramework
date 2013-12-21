package com.centropoly.oxo;

/**
 * @author Paul van der Maas
 */
public abstract class WebModule extends OXOServlet
{
    @Override
    protected void initialize(OXORequest request, OXOResponse response) {
        super.initialize(request, response);

        // Providing a default for the response output type.
        response.setOutputType(OXOResponse.OutputType.XHTML);
    }
}