package com.centropoly.oxo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class CachingOutputStream extends OutputStream
{
    OutputStream delegate;
    ByteArrayOutputStream cache;

    CachingOutputStream(OutputStream out)
    {
        delegate = out;
        cache = new ByteArrayOutputStream(4096);
    }

    public ByteArrayOutputStream getBuffer()
    {
        return cache;
    }

    @Override
    public void write(int b) throws IOException
    {
        delegate.write(b);
        cache.write(b);
    }

    @Override
    public void write(byte b[]) throws IOException
    {
        delegate.write(b);
        cache.write(b);
    }

    @Override
    public void write(byte buf[], int offset, int len) throws IOException
    {
        delegate.write(buf, offset, len);
        cache.write(buf, offset, len);
    }
}