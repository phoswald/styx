package styx.core.sessions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import styx.Complex;
import styx.ConcurrentException;
import styx.Determinism;
import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.Stack;

public abstract class TestAnyTransaction extends TestBase {

    protected final Session detached = SessionManager.getDetachedSession();

    protected final AbstractSessionFactory sf;

    protected TestAnyTransaction(AbstractSessionFactory sf) {
        this.sf = sf;
    }

    @Test
    public void testReadWriteTxns() throws StyxException {
        try(Session session = sf.createSession()) {
            Reference root = session.root();

            session.write(root, null);
            assertNull(session.read(root));

            session.write(root, session.deserialize("[me:[name:\"philip\",age:36],you:[name:\"foo\",age:\"bar\"]]"));

            Value val = session.read(root);
            assertEquals("[me:[age:36,name:\"philip\"],you:[age:\"bar\",name:\"foo\"]]", session.serialize(val, false));

            val = session.read(root.child(session.text("you")));
            assertEquals("[age:\"bar\",name:\"foo\"]", session.serialize(val, false));

            session.write(root.child(session.text("new")), session.deserialize("[\"ene\",\"mene\",\"muh\",[\"x\",\"y\",\"z\"]]"));

            val = session.read(root);
            assertEquals("[me:[age:36,name:\"philip\"],new:[\"ene\",\"mene\",\"muh\",[\"x\",\"y\",\"z\"]],you:[age:\"bar\",name:\"foo\"]]", session.serialize(val, false));

            session.write(root.child(session.text("new")), null);
            session.write(root.child(session.text("me")).child(session.text("age")), null);
            session.write(root.child(session.text("you")).child(session.text("name")), session.text("john doe"));
            session.write(root.child(session.text("you")).child(session.text("age")), session.text("99"));

            val = session.read(root);
            assertEquals("[me:@name \"philip\",you:[age:99,name:\"john doe\"]]", session.serialize(val, false));

            assertFalse(session.hasTransaction());
            session.beginTransaction();
            assertTrue(session.hasTransaction());
            session.write(root, null);
            val = session.read(root);
            assertNull(val);
            session.abortTransaction(false);
            assertFalse(session.hasTransaction());

            val = session.read(root);
            assertNotNull(val);
            session.beginTransaction();
            session.write(root, null);
            session.commitTransaction();
            val = session.read(root);
            assertNull(val);
        }
    }

    @Test
    public void testSingle() throws IOException, StyxException {
        try(Session session = sf.createSession()) {
            session.write(session.root(), session.complex());

            Reference ref = session.root().child(session.text("foo"));
            session.write(ref, session.text("bar"));
            assertEquals("bar", session.read(ref).asText().toTextString());

            assertFalse(session.hasTransaction());
            session.beginTransaction();
            assertTrue(session.hasTransaction());
            assertEquals("bar", session.read(ref).asText().toTextString());
            session.write(ref, session.text("baz"));
            assertEquals("baz", session.read(ref).asText().toTextString());
            session.abortTransaction(false);
            assertFalse(session.hasTransaction());
            assertEquals("bar", session.read(ref).asText().toTextString());

            session.beginTransaction();
            session.write(ref, session.text("baz"));
            session.commitTransaction();
            assertFalse(session.hasTransaction());
            assertEquals("baz", session.read(ref).asText().toTextString());
        }
    }

    @Test
    public void testAtomicReadWrite() throws IOException, StyxException {
        try(Session session = sf.createSession(); Session session2 = sf.createSession()) {
            session.write(session.root(), null);
            session.evaluate("[/][*] = [foo:\"bar\",xxx:\"yyy\"]");

            assertEquals("bar", evaluate(session, "[/foo][*]").asText().toTextString());
            assertEquals("bar", evaluate(session2, "[/foo][*]").asText().toTextString());

            session.evaluate("\n r1 :=== [/foo] \n r2 :=== [/xxx] \n atomic { \n r1[*]=\"baz\" \n r2[*]=\"YYY\" \n } \n ");
            session2.evaluate("\n atomic { \n atomic { \n [/zzz][*] = \"ZZZ\" \n } \n } \n ");

            assertEquals("[foo:\"baz\",xxx:\"YYY\",zzz:\"ZZZ\"]", session.serialize(evaluate(session, "[/][*]"), false));
            assertEquals("[foo:\"baz\",xxx:\"YYY\",zzz:\"ZZZ\"]", session2.serialize(evaluate(session2, "[/][*]"), false));
        }
    }

    @Test
    public void testAtomicCall() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(13, evaluate(session, "f :=== (x,y) -> { return x+y },                                                                                                                   sum := 0, atomic { sum = sum + f(6,7) }, sum", 4).asNumber().toInteger());
            assertEquals(13, evaluate(session, "f :=== -> @Function [ args: [ [name: \"x\"], [name: \"y\"] ], body: @Block [ @Return @Add [ expr1: @Variable \"x\", expr2: @Variable \"y\" ] ] ], sum := 0, atomic { sum = sum + f(6,7) }, sum", 4).asNumber().toInteger());
        }
    }

    @Test
    public void testRace() throws IOException, StyxException, InterruptedException {

        try(Session session = sf.createSession()) {
            session.write(session.root(), session.deserialize("[1,2,3,4]"));
        }

        final String[]       read    = new String[3];
        final boolean[]      commit  = new boolean[3];
        final StyxException[] writeex = new StyxException[3];
        final boolean[]      hastxn  = new boolean[3];
        final SyncState[]    state   = new SyncState[] { new SyncState(""), new SyncState(""), new SyncState("") };

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try(Session session = sf.createSession()) {

                    state[0].wait("can-begin");
                    session.beginTransaction();
                    state[1].set("begin-1-done");

                    state[0].wait("can-read");
                    read[1] = session.read(session.root()).toString();
                    state[1].set("read-1-done");

                    state[0].wait("can-write");
                    try {
                        session.write(session.root(), session.deserialize("[1,1,1,1]"));
                        session.commitTransaction();
                        commit[1] = true;
                    } catch(StyxException e) {
                        writeex[1] = e;
                        if(session.hasTransaction()) {
                            session.abortTransaction(false);
                        }
                    }
                    hastxn[1] = session.hasTransaction();
                    state[1].set("write-1-done");

                } catch (RuntimeException | StyxException | InterruptedException e) {
                    e.printStackTrace();
                }
            } });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try(Session session = sf.createSession()) {

                    state[0].wait("can-begin");
                    session.beginTransaction();
                    state[2].set("begin-2-done");

                    state[0].wait("can-read");
                    read[2] = session.read(session.root()).toString();
                    state[2].set("read-2-done");

                    state[0].wait("can-write");
                    try {
                        session.write(session.root(), session.deserialize("[1,1,1,1]"));
                        session.commitTransaction();
                        commit[2] = true;
                    } catch(StyxException e) {
                        writeex[2] = e;
                        if(session.hasTransaction()) {
                            session.abortTransaction(false);
                        }
                    }

                    hastxn[2] = session.hasTransaction();
                    state[2].set("write-2-done");

                } catch (RuntimeException | StyxException | InterruptedException e) {
                    e.printStackTrace();
                }
            } });

        t1.start();
        t2.start();

        state[0].set("can-begin");
        state[1].wait("begin-1-done");
        state[2].wait("begin-2-done");

        state[0].set("can-read");
        state[1].wait("read-1-done");
        state[2].wait("read-2-done");
        assertTrue(read[1] != null || read[2] != null);
        assertTrue(read[1] == null || read[1].equals("[1,2,3,4]"));
        assertTrue(read[2] == null || read[2].equals("[1,2,3,4]"));

        state[0].set("can-write");
        state[1].wait("write-1-done");
        state[2].wait("write-2-done");
        assertTrue(writeex[1] == null || writeex[1] instanceof ConcurrentException);
        assertTrue(writeex[2] == null || writeex[2] instanceof ConcurrentException);

        // both transactions are allowed to fail (pessimistic locking, write fails after read)
        // 1st transaction is allowed to succeed (optimistic locking)
        // 2nd transaction is allowed to succeed (pessimistic locking, can go on after first fails)
        assertTrue(!commit[1] || !commit[2]);
        assertTrue(!hastxn[1]);
        assertTrue(!hastxn[2]);

        t1.join(10000);
        t2.join(10000);

        try(Session session = sf.createSession()) {
            String result = session.read(session.root()).toString();
            assertTrue(result.equals("[1,2,3,4]") || result.equals("[1,1,1,1]") || result.equals("[2,2,2,2]"));
            System.out.println(result + "  (*** " + (result.equals("[1,1,1,1]") ? "OPTIMISTIC" : "PESSIMISTIC") + " ***)");
        }
    }

    @Test
    public void testProdCons() throws IOException, StyxException, InterruptedException {

        sf.addEnvironment(detached.text("recv"),
                new CompiledFunction(sf.getRegistry(), "recv_1", Determinism.COMMAND, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        Reference ref = stack.getFrameValue(0).asReference();
                        Complex data = stack.session().read(ref).asComplex();
                        Pair<Value, Value> first = data.iterator().next();
                        data = data.put(first.key(), null);
                        stack.session().write(ref, data);
                        return first.val();
                    }
        }.function()).
        addEnvironment(detached.text("trace"),
                new CompiledFunction(sf.getRegistry(), "trace_1", Determinism.COMMAND, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        System.out.println("[trace: " + stack.getFrameValue(0).asText().toTextString() + "]");
                        return null;
                    }
        }.function());

        try(Session session = sf.createSession()) {
            session.write(session.root(), null);
            session.evaluate(" [/][*] = [ input:[], output:0 ] ");
        }

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try(Session session = sf.createSession()) {
                    session.evaluate(
                            "empty :== [ ], done := 0 \n"+
                            "while(done == 0) { \n"+
                            "    atomic { \n"+
                            "        if([/input][*] == empty) { \n"+
                            "            trace(\"cons: retrying\") \n"+
                            "            retry \n"+
                            "        } \n"+
                            "        val := recv([/input]) \n"+
                            "        trace(\"cons: got \" ++ val) \n"+
                            "        if(val == 0) { \n"+
                            "            done = 1 \n"+
                            "        } \n"+
                            "        [/output][*] += val \n"+
                            "    } \n"+
                            "} \n");
                } catch (StyxException e) {
                    e.printStackTrace();
                    fail(e.toString());
                }
            }
        });
        t1.start();
        Thread.sleep(50);

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try(Session session = sf.createSession()) {
                    session.evaluate("atomic { trace(\"prod: A\"), [/input/A][*] = \"123\" }");
                    Thread.sleep(200);
                    session.evaluate("atomic { trace(\"prod: B+C\"), [/input/B][*] = \"234\", [/input/C][*] = \"345\" }");
                    Thread.sleep(20);
                    session.evaluate("atomic { trace(\"prod: D\"), [/input/D][*] = \"456\" }");
                    Thread.sleep(20);
                    session.evaluate("atomic { trace(\"prod: E\"), [/input/E][*] = \"567\" }");
                    Thread.sleep(20);
                    session.evaluate("atomic { trace(\"prod: F\"), [/input/F][*] = \"678\" }");
                    Thread.sleep(10);
                    session.evaluate("atomic { trace(\"prod: G\"), [/input/G][*] = \"789\" }");
                    Thread.sleep(10);
                    session.evaluate("atomic { [/input/Z][*] = 0 }");
                } catch (StyxException | InterruptedException e) {
                    e.printStackTrace();
                    fail(e.toString());
                }
            }
        });
        t2.start();

        t1.join(60000);
        t2.join(60000);

        try(Session session = sf.createSession()) {
            assertEquals(123 + 234 + 345 + 456 + 567 + 678 + 789, evaluate(session, " [/output][*] ").asNumber().toLong());
        }
    }
}
