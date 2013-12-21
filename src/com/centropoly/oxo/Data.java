package com.centropoly.oxo;

/**
 * A resource represents an entity that is 'serveable' by a servlet.
 * Objects stored in a resource will be automatically transformed to XML and
 * made available to the template engine.
 * 
 * We use XStream to do the transformation automatically. XML versions of the objects
 * stored within the resource will be present in the XML document representing the resource.
 *
 * @author Paul van der Maas
 */
public abstract class Data
{
}