package styx.core.utils;

import java.io.IOException;
import java.io.Writer;

public final class LimitingWriter extends Writer {

    private final Writer writer;
    private int limit;

    public LimitingWriter(Writer writer, int limit) {
        this.writer = writer;
        this.limit = limit;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if(limit < len) {
            throw new IOException("Length limit exceeded.");
        }
        limit -= len;
        writer.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        writer.flush();

    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}