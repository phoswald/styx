package styx.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import styx.core.utils.LineReader;

public class TestLineReader {

	@Test
	public void testLocation() throws IOException {
		try(LineReader reader = new LineReader(new StringReader("abcd\nefgh\r\nijkl"))) {
			assertEquals(1, reader.getLine());
			assertEquals(1, reader.getColumn());
			assertEquals(2, reader.read(new char[2]));
			assertEquals(1, reader.getLine());
			assertEquals(3, reader.getColumn());
			assertEquals(3, reader.read(new char[3]));
			assertEquals(2, reader.getLine());
			assertEquals(1, reader.getColumn());
			assertEquals(9, reader.read(new char[9]));
			assertEquals(3, reader.getLine());
			assertEquals(4, reader.getColumn());
		}
	}

	@Test
	public void testReadChar() throws IOException {
		try(LineReader reader = new LineReader(new StringReader("1234567890"))) {

			assertEquals('1', reader.read());
			assertEquals('2', reader.read());
			assertEquals(3, reader.getColumn());

			assertEquals('3', reader.read());
			assertEquals('4', reader.read());
			assertEquals('5', reader.read());
			assertEquals(6, reader.getColumn());

			reader.rewind(3);
			assertEquals(6 /* 3 */, reader.getColumn()); // Note: rewind() does not affect getLine() and getColumn()
			assertEquals('3', reader.read());
			assertEquals('4', reader.read());
			assertEquals('5', reader.read());
			assertEquals('6', reader.read());

			reader.rewind(4);
			assertEquals('3', reader.read());
			assertEquals('4', reader.read());
			assertEquals('5', reader.read());
			assertEquals('6', reader.read());
			assertEquals('7', reader.read());

			assertEquals('8', reader.read());
			assertEquals('9', reader.read());
			assertEquals('0', reader.read());

			reader.rewind(1);
			assertEquals('0', reader.read());
			assertEquals(-1, reader.read());
			assertEquals(-1, reader.read());
			assertEquals(-1, reader.read());
		}
	}

	@Test
	public void testReadArray() throws IOException {
		try(LineReader reader = new LineReader(new StringReader("1234567890"))) {
			char[] cbuf = new char[3];

			assertEquals(3, reader.read(cbuf));
			assertEquals('1', cbuf[0]);
			assertEquals('2', cbuf[1]);
			assertEquals('3', cbuf[2]);

			assertEquals(3, reader.read(cbuf));
			assertEquals('4', cbuf[0]);
			assertEquals('5', cbuf[1]);
			assertEquals('6', cbuf[2]);

			reader.rewind(3);
			assertEquals(3, reader.read(cbuf));
			assertEquals('4', cbuf[0]);
			assertEquals('5', cbuf[1]);
			assertEquals('6', cbuf[2]);

			assertEquals(3, reader.read(cbuf));
			assertEquals('7', cbuf[0]);
			assertEquals('8', cbuf[1]);
			assertEquals('9', cbuf[2]);

			assertEquals(1, reader.read(cbuf));
			assertEquals('0', cbuf[0]);
			assertEquals('8', cbuf[1]);
			assertEquals('9', cbuf[2]);
		}
	}


    @Test
    public void testBadRewind() throws IOException {
        try(LineReader reader = new LineReader(new StringReader("1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"))) {

            try {
                reader.rewind(2);
                fail();
            } catch(IOException e) {
                assertEquals("Cannot rewind, buffer exhausted.", e.getMessage());
            }

            assertEquals('1', reader.read());
            assertEquals('2', reader.read());
            assertEquals('3', reader.read());

            reader.rewind(2);
            assertEquals('2', reader.read());
            assertEquals('3', reader.read());

            reader.rewind(2);

            try {
                reader.rewind(2);
                fail();
            } catch(IOException e) {
                assertEquals("Cannot rewind, buffer exhausted.", e.getMessage());
            }

            reader.rewind(1);
            assertEquals('1', reader.read());
            assertEquals('2', reader.read());
            assertEquals('3', reader.read());

            assertEquals(7, reader.read(new char[7])); // 4..7
            assertEquals(25, reader.read(new char[25])); // A..Y
            assertEquals('Z', reader.read());

            reader.rewind(6);
            assertEquals('U', reader.read());
            assertEquals('V', reader.read());
            assertEquals('W', reader.read());
        }
    }
}
