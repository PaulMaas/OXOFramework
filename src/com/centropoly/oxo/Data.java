package com.centropoly.oxo;

/**
 * A Data object represents/contains the data needed to build the 'serveable' resource
 * by OXOServlet (or a subclass thereof). Objects stored in a Data object will be automatically
 * transformed to XML and made available wrapped up with other contextual data
 * to the template engine.
 * 
 * We use XStream to do the transformation to XML automatically.
 *
 * @author Paul van der Maas
 */
public abstract class Data
{
}