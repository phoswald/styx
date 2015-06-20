package styx.db.mmap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;

public class TestMmapDatabase {

    @Test
    public void testOpen() throws IOException, StyxException {
        Path file = Paths.get("target", "styx-session", "TestMappedDatabase.1.db");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);

        try(MmapDatabase db = MmapDatabase.fromFile(file, 64 << 10)) {
            assertEquals(65536, db.getSize());
            assertEquals(0,     db.getRoot());
            assertEquals(32,    db.getNext());
        }
        try(MmapDatabase db = MmapDatabase.fromMemory(64 << 10)) {
            assertEquals(65536, db.getSize());
            assertEquals(0,     db.getRoot());
            assertEquals(32,    db.getNext());
        }
        try(MmapDatabase db = MmapDatabase.fromArray(new byte[64 << 10])) {
            assertEquals(65536, db.getSize());
            assertEquals(0,     db.getRoot());
            assertEquals(32,    db.getNext());
        }
    }

    @Test
    public void testMemory() {
        try(MmapDatabase db = MmapDatabase.fromMemory(64 << 10)) {
            assertEquals( 0, db.getRoot());
            assertEquals( 32, db.alloc(32));
            assertEquals( 64, db.alloc(32));
            assertEquals( 96, db.alloc(1));
            assertEquals(104, db.alloc(7));
            assertEquals(112, db.alloc(8));
            assertEquals(120, db.alloc(8));

            // We expect Little Endian on all platforms
            db.putLong(64, 0xDEADBEEF12345678L);
            assertEquals(0xDEADBEEF12345678L, db.getLong(64));

            assertEquals(0x12345678, db.getInt(64));
            assertEquals(0xDEADBEEF, db.getInt(68));

            assertEquals((short) 0x5678, db.getShort(64));
            assertEquals((short) 0x1234, db.getShort(66));
            assertEquals((short) 0xBEEF, db.getShort(68));
            assertEquals((short) 0xDEAD, db.getShort(70));

            assertEquals((byte) 0x78, db.getByte(64));
            assertEquals((byte) 0x56, db.getByte(65));
            assertEquals((byte) 0x34, db.getByte(66));
            assertEquals((byte) 0x12, db.getByte(67));
            assertEquals((byte) 0xEF, db.getByte(68));
            assertEquals((byte) 0xBE, db.getByte(69));
            assertEquals((byte) 0xAD, db.getByte(70));
            assertEquals((byte) 0xDE, db.getByte(71));
        }
    }

    @Test
    public void testArray() {
        byte[] data = new byte[64 << 10];
        try(MmapDatabase db = MmapDatabase.fromArray(data)) {
            assertEquals(32, db.alloc(32));
            assertEquals(0, db.getLong(32));
            db.putLong(32, 0xDEADBEEF12345678L);
            assertEquals(0xDEADBEEF12345678L, db.getLong(32));
            assertEquals(0x12345678, db.getInt(32));
            assertEquals(0xDEADBEEF, db.getInt(36));
        }
        try(MmapDatabase db = MmapDatabase.fromArray(data)) {
            assertEquals(64, db.alloc(32)); // instead of 32
            assertEquals(0xDEADBEEF12345678L, db.getLong(32)); // instead of 0
            assertEquals(0x12345678, db.getInt(32));
            assertEquals(0xDEADBEEF, db.getInt(36));
        }
    }

    @Test
    public void testValues() {
        Session session = SessionManager.getDetachedSession();
        try(MmapDatabase db = MmapDatabase.fromMemory(64 << 10)) {
            assertEquals(65536, db.getSize());
            assertEquals(0,     db.getRoot());
            assertEquals(32,    db.getNext());
            long free = db.getNext();

            assertEquals(0, db.storeValue(null));

            assertEquals(0x1000000000000000L, db.storeValue(db.getEmpty()));
            assertEquals(0x2000000000000000L, db.storeValue(session.bool(false)));
            assertEquals(0x2000000000000001L, db.storeValue(session.bool(true)));
            assertEquals(0x3000000000000000L, db.storeValue(session.number(0)));
            assertEquals(0x3000000000000001L, db.storeValue(session.number(1)));
            assertEquals(0x3000000000000101L, db.storeValue(session.number(257)));
            assertEquals(0x30000000FFFFFFFFL, db.storeValue(session.number(-1)));
            assertEquals(0x300000007FFFFFFFL, db.storeValue(session.number(Integer.MAX_VALUE)));
            assertEquals(0x3000000080000000L, db.storeValue(session.number(Integer.MIN_VALUE)));
            assertEquals(0x4000000000000000L, db.storeValue(session.text("0x")));
            assertEquals(0x41000000000000ABL, db.storeValue(session.text("0xAB")));
            assertEquals(0x4200000000003412L, db.storeValue(session.text("0x1234")));
            assertEquals(0x44000000EFBEADDEL, db.storeValue(session.text("0xDEADBEEF")));
            assertEquals(0x47FF3412EFBEADDEL, db.storeValue(session.text("0xDEADBEEF1234FF")));
            assertEquals(0x5000000000000000L, db.storeValue(session.text("")));
            assertEquals(0x5100000000000041L, db.storeValue(session.text("A")));
            assertEquals(0x5200000000004241L, db.storeValue(session.text("AB")));
            assertEquals(0x5300000000434241L, db.storeValue(session.text("ABC")));
            assertEquals(0x5300000000AC82E2L, db.storeValue(session.text("€")));
            assertEquals(0x5747464544434241L, db.storeValue(session.text("ABCDEFG")));
            assertEquals(0x57AC82E244434241L, db.storeValue(session.text("ABCD€")));
            assertEquals(free, db.getNext()); // none of the above value needs heap space.

            assertNull(db.loadValue(0));
            assertEquals("[]",  db.loadValue(0x1000000000000000L).toString());
            assertEquals(false, db.loadValue(0x2000000000000000L).asBool().toBool());
            assertEquals(true,  db.loadValue(0x2000000000000001L).asBool().toBool());
            assertEquals(0,                 db.loadValue(0x3000000000000000L).asNumber().toInteger());
            assertEquals(1,                 db.loadValue(0x3000000000000001L).asNumber().toInteger());
            assertEquals(257,               db.loadValue(0x3000000000000101L).asNumber().toInteger());
            assertEquals(-1,                db.loadValue(0x30000000FFFFFFFFL).asNumber().toInteger());
            assertEquals(Integer.MAX_VALUE, db.loadValue(0x300000007FFFFFFFL).asNumber().toInteger());
            assertEquals(Integer.MIN_VALUE, db.loadValue(0x3000000080000000L).asNumber().toInteger());
            assertArrayEquals(new byte[] { },                          db.loadValue(0x4000000000000000L).asBinary().toByteArray());
            assertArrayEquals(new byte[] { (byte) 0xAB },              db.loadValue(0x41000000000000ABL).asBinary().toByteArray());
            assertArrayEquals(new byte[] {        0x12,        0x34 }, db.loadValue(0x4200000000003412L).asBinary().toByteArray());
            assertArrayEquals(new byte[] { (byte) 0xDE, (byte) 0xAD,
                                           (byte) 0xBE, (byte) 0xEF }, db.loadValue(0x44000000EFBEADDEL).asBinary().toByteArray());
            assertArrayEquals(new byte[] { (byte) 0xDE, (byte) 0xAD,
                                           (byte) 0xBE, (byte) 0xEF,
                                           0x12,  0x34, (byte) 0xFF}, db.loadValue(0x47FF3412EFBEADDEL).asBinary().toByteArray());
            assertEquals("",        db.loadValue(0x5000000000000000L).asText().toTextString());
            assertEquals("A",       db.loadValue(0x5100000000000041L).asText().toTextString());
            assertEquals("AB",      db.loadValue(0x5200000000004241L).asText().toTextString());
            assertEquals("ABC",     db.loadValue(0x5300000000434241L).asText().toTextString());
            assertEquals("€",       db.loadValue(0x5300000000AC82E2L).asText().toTextString());
            assertEquals("ABCDEFG", db.loadValue(0x5747464544434241L).asText().toTextString());
            assertEquals("ABCD€",   db.loadValue(0x57AC82E244434241L).asText().toTextString());
        }

    }

    @Test
    public void testSession() throws StyxException, IOException {
        Path file = Paths.get("target", "styx-session", "TestMappedDatabase.2.db");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);

        SessionFactory sf = MmapSessionProvider.createSessionFactory(file, 64 << 10);
        try(Session session = sf.createSession()) {
            assertNull(session.read(session.root()));
            session.write(session.root(), session.deserialize("[key1:val1,key2:val2,key3:[1,2,3,4,5]]"));
        }
        try(Session session = sf.createSession()) {
            Value val = session.read(session.root());
            assertEquals("[key1:val1,key2:val2,key3:[1,2,3,4,5]]", val.toString());
        }
        try(Session session = sf.createSession()) {
            Value val = session.read(session.root().child(session.text("key3")));
            assertEquals("[1,2,3,4,5]", val.toString());

            session.write(session.root(), session.text("foo"));
            assertEquals("foo", session.read(session.root()).toString());

            session.write(session.root(), session.complex());
            assertEquals("[]", session.read(session.root()).toString());

            session.write(session.root(), null);
            assertNull(session.read(session.root()));
        }
    }
}
