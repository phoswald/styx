package styx.db.mmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import styx.Session;
import styx.SessionFactory;
import styx.StyxException;
import styx.Value;

public class TestMmapDatabase {

    @Test
    public void testOpen() throws IOException {
        Path file = Paths.get("target", "styx-session", "TestMappedDatabase.1.db");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);

        try(MmapDatabase db = MmapDatabase.fromFile(file)) {
            db.dump(System.out);
        }
        try(MmapDatabase db = MmapDatabase.fromMemory(64 << 10)) {
            db.dump(System.out);
        }
        try(MmapDatabase db = MmapDatabase.fromArray(new byte[64 << 10])) {
            db.dump(System.out);
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
    public void testSession() throws StyxException, IOException {
        Path file = Paths.get("target", "styx-session", "TestMappedDatabase.2.db");
        Files.createDirectories(file.getParent());
        Files.deleteIfExists(file);

        SessionFactory sf = MmapSessionProvider.createSessionFactory(file);
        try(Session session = sf.createSession()) {
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
