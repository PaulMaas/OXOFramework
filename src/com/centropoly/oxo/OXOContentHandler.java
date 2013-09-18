/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.centropoly.oxo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 *
 * @author paul
 */
public class OXOContentHandler extends DefaultHandler2
{

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        System.out.println("uri: " + uri + "local name: " + localName + ", qname: " + qName);
        super.startElement(uri, localName, qName, attributes);
    }
    
    
}