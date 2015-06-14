package styx.core.sessions;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;

public abstract class TestAnySession extends TestBase {

    protected final AbstractSessionFactory sf;

    protected TestAnySession(AbstractSessionFactory sf) {
        this.sf = sf;
    }

    @Test
    public void testOpen() throws StyxException {
        try(Session session = sf.createSession()) {
            // nop
        }
    }

    @Test
    public void testGetters() throws StyxException {
        try(Session session = sf.createSession()) {
            assertTrue(session.empty().isVoid());
            assertEquals(Integer.MAX_VALUE, session.number(Integer.MAX_VALUE).toInteger());
            assertEquals(Long.MAX_VALUE, session.number(Long.MAX_VALUE).toLong());
            assertEquals(Double.MAX_VALUE, session.number(Double.MAX_VALUE).toDouble(), 0.0);
            assertEquals("1234", session.number("1234").toDecimalString());
            assertEquals("abcd", session.text("abcd").toTextString());
            assertEquals("1234", session.binary(new byte [] { 0x12, 0x34 }).toHexString());
            assertEquals("1234", session.binary("1234").toHexString());
            assertTrue(session.root().isReference());
            assertTrue(session.complex(session.text("foo"), session.text("bar")).isComplex());
        }
    }

    @Test
    public void testSerializeString() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("1234", session.serialize(session.number(1234), false));
        }
    }

    @Test
    public void testDeserializeString() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(1234, session.deserialize("1234").asNumber().toLong());
        }
    }

    @Test
    public void testSerializeBytes() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            try(ByteArrayOutputStream stm = new ByteArrayOutputStream()) {
                session.serialize(session.number(1234), stm, false);
                assertArrayEquals(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '1', '2', '3', '4' }, stm.toByteArray());
            }
        }
    }

    @Test
    public void testDeserializeBytes() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            try(ByteArrayInputStream stm = new ByteArrayInputStream(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '1', '2', '3', '4' })) {
                assertEquals(1234, session.deserialize(stm).asNumber().toLong());
            }
        }
    }

    @Test
    public void testChild() throws StyxException {
        try(Session session = sf.createSession()) {
            Reference vr = session.root();
            Value v1 = session.number(1);
            Value v2 = session.number(2);

            assertEquals(vr.child(v1), vr.child(v1));
            assertTrue(vr.parent() == null);
            assertTrue(vr.name() == null);
            assertSame(vr, vr.child(v1).parent());
            assertSame(v1, vr.child(v1).name());
            assertEquals("[/]", session.serialize(vr, false));
            assertEquals("[/1]", session.serialize(vr.child(v1), false));
            assertEquals("[/1/2]", session.serialize(vr.child(v1).child(v2), false));
            assertEquals("[/2/1]", session.serialize(vr.child(v2).child(v1), false));
            assertEquals("[/[/]]", session.serialize(vr.child(vr), false));
            assertEquals("[/[/]/1/[/2]]", session.serialize(vr.child(vr).child(v1).child(vr.child(v2)), false));
        }
    }

    @Test(expected=NullPointerException.class)
    public void testChildInvalid1() throws StyxException {
        try(Session session = sf.createSession()) {
            session.root().child(null);
        }
    }

    @Test
    public void testWrite1() throws StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), null);
            assertNull(session.read(session.root()));

            Value v1 = session.text("v1");
            Value v2 = session.text("v2");
            Value v3 = session.text("v3");

            session.write(session.root(), session.deserialize("[v1:\"v2\",v3:\"v4\",v5:\"v6\"]"));
            assertEquals("[v1:\"v2\",v3:\"v4\",v5:\"v6\"]", session.serialize(session.read(session.root()), false));
            assertEquals("\"v2\"", session.serialize(session.read(session.root().child(v1)), false));
            assertEquals("",       session.serialize(session.read(session.root().child(v2)), false));
            assertEquals("\"v4\"", session.serialize(session.read(session.root().child(v3)), false));
            assertEquals("",       session.serialize(session.read(session.root().child(v3).child(v1)), false));

            session.write(session.root(), null);
            assertNull(session.read(session.root()));
            assertNull(session.read(session.root().child(v1)));
            assertNull(session.read(session.root().child(v2)));
            assertNull(session.read(session.root().child(v3).child(v1)));
            assertNull(session.read(session.root().child(v3).child(v2)));

            session.write(session.root(), session.complex());
            session.write(session.root().child(v1), v2);
            session.write(session.root().child(v2), v1);
            assertEquals("[v1:\"v2\",v2:\"v1\"]", session.serialize(session.read(session.root()), false));
        }
    }

    @Test
    public void testWrite2() throws StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), session.deserialize("[v1:\"v2\",v3:\"v4\",v5:\"v6\"]"));
            Value v1 = session.text("v1");
            Value v2 = session.text("v2");
            Value v3 = session.text("v3");

            session.write(session.root().child(v1), session.complex());
            session.write(session.root().child(v1).child(v2), v3);
            assertEquals("\"v3\"", session.serialize(session.read(session.root().child(v1).child(v2)), false));
            assertEquals("@v2 \"v3\"", session.serialize(session.read(session.root().child(v1)), false));
            assertEquals("[v1:@v2 \"v3\",v3:\"v4\",v5:\"v6\"]", session.serialize(session.read(session.root()), false));
        }
    }

    @Test
    public void testWrite3() throws StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), session.deserialize("[v1:\"v2\",v3:\"v4\",v5:\"v6\"]"));
            Value v1 = session.text("v1");
            Value v3 = session.text("v3");
            Value v5 = session.text("v5");

            session.write(session.root().child(v1), session.complex());
            session.write(session.root().child(v1).child(v1), session.complex());
            session.write(session.root().child(v1).child(v1).child(v1), session.complex());
            session.write(session.root().child(v1).child(v1).child(v1).child(v1), v1);
            session.write(session.root().child(v3), session.complex());
            session.write(session.root().child(v3).child(v3), session.complex());
            session.write(session.root().child(v3).child(v3).child(v3), session.complex());
            session.write(session.root().child(v3).child(v3).child(v3).child(v3), v3);
            session.write(session.root().child(v5), null);
            assertEquals("[v1:@v1 @v1 @v1 \"v1\",v3:@v3 @v3 @v3 \"v3\"]", session.serialize(session.read(session.root()), false));

            session.write(session.root().child(v1).child(v1).child(v1).child(v1), null);
            session.write(session.root().child(v3).child(v3).child(v3).child(v3), null);
            session.write(session.root().child(v5), v3);
            assertEquals("@v1 []", session.serialize(session.read(session.root().child(v1).child(v1)), false));
            assertEquals("[v1:@v1 @v1 [],v3:@v3 @v3 [],v5:\"v3\"]", session.serialize(session.read(session.root()), false));
            assertEquals("@v3 []", session.serialize(session.read(session.root().child(v3).child(v3)), false));

            session.write(session.root(), session.text("xxx"));
            session.write(session.root(), session.text("xxx"));
            assertEquals("\"xxx\"", session.serialize(session.read(session.root()), false));
            assertEquals("", session.serialize(session.read(session.root().child(v1).child(v1)), false));
            assertEquals("", session.serialize(session.read(session.root().child(v3).child(v3)), false));
        }
    }

    @Test
    public void testWriteSub1() throws StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), session.deserialize("[v1:\"v2\",v3:\"v4\",v5:\"v6\"]"));
            try {
                session.write(session.root().child(session.text("x")).child(session.text("y")).child(session.text("z")), null);
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-existing value.", e.getMessage());
            }
            try {
                session.write(session.root().child(session.text("x")).child(session.text("y")).child(session.text("z")), session.empty());
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-existing value.", e.getMessage());
            }
        }
    }

    @Test
    public void testWriteSub2() throws StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), session.text("xxx"));
            try {
                session.write(session.root().child(session.text("x")).child(session.text("y")).child(session.text("z")), null);
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-existing value.", e.getMessage());
            }
            try {
                session.write(session.root().child(session.text("x")), session.empty());
                fail();
            } catch(StyxException e) {
                assertEquals("Attempt to write a child of a non-complex value.", e.getMessage());
            }
        }
    }

    @Test
    public void testBrowse() throws StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), null);

            List<Value> res = session.browse(session.root());
            assertNull(res);

            session.write(session.root(), session.text("XXX"));

            res = session.browse(session.root());
            assertNull(res);

            session.write(session.root(), session.deserialize("[A:[],B:[],C:[],D:[],E:[],F:[]]"));

            res = session.browse(session.root());
            assertEquals(6, res.size());
            assertEquals("A", res.get(0).asText().toTextString());
            assertEquals("F", res.get(5).asText().toTextString());

            res = session.browse(session.root(), null, null, null, false);
            assertEquals(6, res.size());
            assertEquals("F", res.get(0).asText().toTextString());
            assertEquals("A", res.get(5).asText().toTextString());

            res = session.browse(session.root(), null, null, 3, true);
            assertEquals(3, res.size());
            assertEquals("A", res.get(0).asText().toTextString());
            assertEquals("C", res.get(2).asText().toTextString());

            res = session.browse(session.root(), null, null, 3, false);
            assertEquals(3, res.size());
            assertEquals("F", res.get(0).asText().toTextString());
            assertEquals("D", res.get(2).asText().toTextString());

            res = session.browse(session.root(), session.text("B"), session.text("E"), null, true);
            assertEquals(2, res.size());
            assertEquals("C", res.get(0).asText().toTextString());
            assertEquals("D", res.get(1).asText().toTextString());

            res = session.browse(session.root(), session.text("E"), session.text("B"), null, false);
            assertEquals(2, res.size());
            assertEquals("D", res.get(0).asText().toTextString());
            assertEquals("C", res.get(1).asText().toTextString());

            res = session.browse(session.root(), session.text("C"), null, 99, true);
            assertEquals(3, res.size());
            assertEquals("D", res.get(0).asText().toTextString());
            assertEquals("F", res.get(2).asText().toTextString());

            res = session.browse(session.root(), session.text("C"), null, 99, false);
            assertEquals(2, res.size());
            assertEquals("B", res.get(0).asText().toTextString());
            assertEquals("A", res.get(1).asText().toTextString());

            res = session.browse(session.root(), session.text("Z"), session.text("9"), null, true);
            assertEquals(0, res.size());

            session.write(session.root().child(session.text("A")), session.deserialize("[1,2,3]"));
            session.write(session.root().child(session.text("B")), session.deserialize("[X:0,Y:[[1,2,3]],Z:0]"));
            res = session.browse(session.root().child(session.text("A")));
            assertEquals(3, res.size());
            assertEquals("1", res.get(0).asText().toTextString());
            assertEquals("2", res.get(1).asText().toTextString());
            assertEquals("3", res.get(2).asText().toTextString());
            res = session.browse(session.root().child(session.text("B")));
            assertEquals(3, res.size());
            assertEquals("X", res.get(0).asText().toTextString());
            assertEquals("Y", res.get(1).asText().toTextString());
            assertEquals("Z", res.get(2).asText().toTextString());
        }
    }
}
