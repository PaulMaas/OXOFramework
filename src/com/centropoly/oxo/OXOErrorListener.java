package com.centropoly.oxo;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

/**
 * @author Paul van der Maas
 */
public class OXOErrorListener implements ErrorListener {
    @Override
    public void warning(TransformerException exception) throws TransformerException {
        throw exception;
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
        throw exception;
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
        throw exception;
    }
}