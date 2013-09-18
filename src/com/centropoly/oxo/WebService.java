package com.centropoly.oxo;

import java.io.IOException;

/**
 * This is basically a web service wrapper. To create a web service extend
 * the WebService class and implement the executeOperation method.
 * This method should return a Message object. It is then automatically
 * transformed and sent as a valid XML message to the client.
 *
 * @author Paul van der Maas
 */
public abstract class WebService extends OXOServlet
{
    @Override
    final protected void handleRequest(OXORequest request, OXOResponse response) throws IOException
    {
        // Providing a default for the response output type.
        response.setOutputType(OXOResponse.OutputType.XML);

        // Must be called last.
        super.handleRequest(request, response);
    }
}