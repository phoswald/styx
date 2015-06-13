package styx.core.memory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import styx.Complex;
import styx.Session;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;
import styx.core.intrinsics.FileIntrinsics;
import styx.core.values.CompiledComplex;

public class TestSharedValueFile {

    private static final Session session = SessionManager.getDetachedSession();

    private final Path file = Paths.get("target", "styx-session", "TestSharedValueFile.styx");

    @BeforeClass
    public static void createFolder() throws IOException {
        Files.createDirectories(Paths.get("target", "styx-session"));
    }

    @Before
    public void prepare() throws IOException {
        Files.deleteIfExists(file);
    }

    @Test
    public void testSetFirst() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        assertFalse(Files.exists(file));
        val.set(session, session.deserialize("[1,2,3,4]")); // should write file with version 1
        assertTrue(Files.exists(file));

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1
        assertEquals("[1,2,3,4]", content.substring(32));
    }

    @Test
    public void testSetNext() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, "\t\t\t\t                           \n", "[1,2,3,4]"); // 0b00001111 = 15
        val.set(session, session.deserialize("[5,6,7,8]")); // should write file with version 16

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("    \t                          \n", content.substring(0, 32)); // 0x1000 = 16
        assertEquals("[5,6,7,8]", content.substring(32));
    }

    @Test
    public void testSetWrapAround() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, " \t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n", "[1,2,3,4]"); // 0b1...11111110 = max-1
        val.set(session, session.deserialize("[5,6,7,8]")); // should write file with version 0b1...11111111 = max

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n", content.substring(0, 32)); // 0b1...11111111 = max

        val.set(session, session.deserialize("[5,6,7,8]")); // should write file with version 1

        content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1

        val.set(session, session.deserialize("[5,6,7,8]")); // should write file with version 2

        content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals(" \t                             \n", content.substring(0, 32)); // 0b00000010 = 2
    }

    @Test
    public void testTestSetFirst() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        assertFalse(Files.exists(file));
        assertTrue(val.testset(session, session.deserialize("[1,2,3,4]"))); // should write file with version 1
        assertTrue(Files.exists(file));

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1
        assertEquals("[1,2,3,4]", content.substring(32));
    }

    @Test
    public void testTestSetBad() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, "\t\t\t\t                           \n", "[1,2,3,4]"); // 0b00001111 = 15
        assertFalse(val.testset(session, session.deserialize("[5,6,7,8]"))); // should write file with version 16
    }

    @Test
    public void testTestSet() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, "\t\t\t\t                           \n", "[1,2,3,4]"); // 0b00001111 = 15
        assertEquals("[1,2,3,4]", val.get(session).toString());
        assertTrue(val.testset(session, session.deserialize("[5,6,7,8]"))); // should write file with version 16

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("    \t                          \n", content.substring(0, 32)); // 0x1000 = 16
        assertEquals("[5,6,7,8]", content.substring(32));

        assertTrue(val.testset(session, session.deserialize("[1,2,3,4]"))); // should write file with version 17

        content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t   \t                          \n", content.substring(0, 32)); // 0x1001 = 17
        assertEquals("[1,2,3,4]", content.substring(32));

        assertEquals("[1,2,3,4]", val.get(session).toString());
    }

    @Test
    public void testMonitorSync() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, "\t\t\t\t                           \n", "[1,2,3,4]"); // 0b00001111 = 15
        assertEquals("[1,2,3,4]", val.get(session).toString());

        SharedValue other = val.clone();
        other.set(session, session.deserialize("[5,6,7,8]"));

        val.monitor(session);
        assertEquals("[5,6,7,8]", val.get(session).toString());
    }

    @Test
    public void testMonitorAsync() throws StyxException, IOException, InterruptedException {
        SharedValue val = new SharedValueFile(file, false);
        final SharedValue other = val.clone();

        storeFile(file, "\t\t\t\t                           \n", "[1,2,3,4]"); // 0b00001111 = 15
        assertEquals("[1,2,3,4]", val.get(session).toString());

        Thread t1 = new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(200);
                    other.set(session, session.deserialize("[5,6,7,8]"));
                } catch (InterruptedException | StyxException e) {
                    e.printStackTrace();
                }
            } });
        t1.start();

        val.monitor(session);
        assertEquals("[5,6,7,8]", val.get(session).toString());

        t1.join(60000);
    }

    @Test
    public void testGetAndSetFirst() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false, /* 3, */ 100, 100);

        assertFalse(Files.exists(file));
        assertNull(val.get(session));
        assertFalse(Files.exists(file));
        assertTrue(val.testset(session, session.deserialize("[1,2,3,4]"))); // should write file with version 1
        assertTrue(Files.exists(file));

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1
        assertEquals("[1,2,3,4]", content.substring(32));
    }

    @Test
    public void testGetAndSetNoVersion() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, null, "[1,2,3,4]"); // no version
        assertEquals("[1,2,3,4]", val.get(session).toString());
        assertTrue(val.testset(session, session.deserialize("[5,6,7,8]"))); // should write file with version 1

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1
        assertEquals("[5,6,7,8]", content.substring(32));
    }

    @Test
    public void testGetAndSetBadVersion() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, null, "                                                      [1,2,3,4]"); // bad version
        assertEquals("[1,2,3,4]", val.get(session).toString());
        assertTrue(val.testset(session, session.deserialize("[5,6,7,8]"))); // should write file with version 1

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1
        assertEquals("[5,6,7,8]", content.substring(32));
    }

    @Test
    public void testSlowReader() throws IOException, StyxException {
        storeFile(file, "\t\t\t\t                           \n", "[1,2,3,4]"); // 0b00001111 = 15

        SharedValue val = new SharedValueFile(file, false);
        assertEquals("[1,2,3,4]", val.get(session).toString());

        try(Reader stm1 = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String content1 = FileIntrinsics.readToEnd(stm1);
            assertEquals(32 + 9, content1.length());
            assertEquals("[1,2,3,4]", content1.substring(32));

            assertTrue(val.testset(session, session.deserialize("[5,6,7,8]")));

            assertTrue(val.testset(session, session.deserialize("[6,7,8,9]")));

            try(Reader stm2 = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String content2 = FileIntrinsics.readToEnd(stm2);
                assertEquals(32 + 9, content2.length());
                assertEquals("[6,7,8,9]", content2.substring(32));

                assertTrue(val.testset(session, session.deserialize("[7,8,9,0]")));

                assertTrue(val.testset(session, session.deserialize("[8,9,0,1]")));
            }
        }
    }

    @Test
    public void testGetBadData() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(file, false);

        storeFile(file, null, "!£%$"); // bad data

        try {
            val.get(session);
            fail();
        } catch(StyxException e) {
            assertNull(e.getCause());
        }
    }

    @Test
    public void testGetBadPath() throws StyxException, IOException {
        SharedValue val = new SharedValueFile(FileSystems.getDefault().getPath("/xxx/yyy"), false, /* 3, */ 100, 100);

        val.get(session); // does not fail, assuming new (could be optimized, for example considering parent directory)

        try {
            val.set(session, session.deserialize("[1,2,3,4]"));
            fail();
        } catch(StyxException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testSetLocked() throws IOException, StyxException {
        Path lock = FileSystems.getDefault().getPath(file.toString() + ".lock");
        SharedValue val = new SharedValueFile(file, false, /* 100, */ 3, 100);
        val.set(session, session.deserialize("[1,2,3,4]")); // should write file with version 1
        storeFile(lock, null, "xxx");

        try {
            val.set(session, session.deserialize("[5,6,7,8]"));
            fail();
        } catch(StyxException e) {
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IOException);
            assertTrue(Files.exists(lock));
        } finally {
            Files.delete(lock);
        }
    }

    @Test
    public void testBadStore() throws StyxException, IOException {
        Path lock = FileSystems.getDefault().getPath(file.toString() + ".lock");
        SharedValue val = new SharedValueFile(file, false);

        assertFalse(Files.exists(file));
        assertTrue(val.testset(session, session.deserialize("[1,2,3,4]"))); // should write file with version 1
        assertTrue(Files.exists(file));

        try {
            Value bad = new CompiledComplex() {
                @Override
                protected Complex toValue() {
                    throw new RuntimeException("XXX");
                }
            };
            val.testset(session, bad);
            fail();
        } catch(StyxException e) {
            assertFalse(Files.exists(lock));
        }

        String content = loadFile(file);
        assertEquals(32 + 9, content.length());
        assertEquals("\t                              \n", content.substring(0, 32)); // 0b00000001 = 1
        assertEquals("[1,2,3,4]", content.substring(32));

        assertTrue(val.testset(session, session.deserialize("[5,6,7,8]"))); // should write file with version 2
    }

    private static String loadFile(Path file) throws IOException {
        try(Reader stm = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return FileIntrinsics.readToEnd(stm);
        }
    }

    private static void storeFile(Path file, String header, String content) throws IOException {
        try(Writer stm = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            if(header != null) {
                assertEquals(32, header.length());
                stm.write(header);
            }
            stm.write(content);
        }
    }
}
