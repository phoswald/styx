package styx.core.utils;

import java.io.IOException;
import java.io.Reader;

public final class LineReader extends Reader {

    private final Reader reader;

    private int curLine;
    private int curColumn;

    private int[] rewindBuf; // ring buffer containing the last characters from reader.
    private int   rewindPos; // where to put next character from reader, wraps around at rewindBuffer.length
    private int   rewindCap; // maximum number of characters we can rewind (initially 0, at most rewindBuffer.length)
    private int   rewindCnt; // cumulative number of rewinded characters (usually 0, at most rewindBuffer.length)

    public LineReader(Reader reader) {
        this.reader    = reader;
        this.curLine   = 1;
        this.curColumn = 1;

        rewindBuf = new int[16];
        rewindCap = 0;
        rewindPos = 0;
        rewindCnt = 0;
    }

    public int getLine() {
        return curLine;
    }

    public int getColumn() {
        return curColumn;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int num = 0;
        while(num < len) {
            int c = read();
            if(c == -1) {
                break;
            }
            cbuf[off + num] = (char) c;
            num++;
        }
        return num;
    }

    @Override
    public int read() throws IOException {
        if(rewindCnt > 0) {
            rewindCap++;
            rewindCnt--;
            return rewindBuf[(rewindPos - rewindCnt - 1 + rewindBuf.length) % rewindBuf.length];
        }
        int c = reader.read();
        if(c == '\n') {
            curLine++;
            curColumn = 1;
        } else if(c != -1 && c != '\r') {
            curColumn++;
        }
        rewindCap += rewindCap < rewindBuf.length ? 1 : 0;
        rewindBuf[rewindPos] = c;
        rewindPos = (rewindPos + 1) % rewindBuf.length;
        return c;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    public void rewind(int len) throws IOException {
        if(len > rewindCap) {
            throw new IOException("Cannot rewind, buffer exhausted.");
        }
        rewindCap -= len;
        rewindCnt += len;
    }
}
