package com.centropoly.oxo;

import java.io.IOException;

/**
 * @author Paul van der Maas
 */
public abstract class WebModule extends OXOServlet
{
    @Override
    final protected void handleRequest(OXORequest request, OXOResponse response) throws IOException
    {
        // Providing a default for the response output type.
        response.setOutputType(OXOResponse.OutputType.XHTML);

        // Must be called last.
        super.handleRequest(request, response);
    }
}