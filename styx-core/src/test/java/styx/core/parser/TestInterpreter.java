package styx.core.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import styx.Complex;
import styx.Determinism;
import styx.Function;
import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.Stack;
import styx.core.sessions.AbstractSessionFactory;

public class TestInterpreter {

    private static SessionFactory sf = SessionManager.createMemorySessionFactory(false);

    @Test
    public void testWhitespace() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(123, evaluate(session, " 123 ").asNumber().toInteger());
            assertEquals(123, evaluate(session, " 123 \n ").asNumber().toInteger());
            assertEquals(123, evaluate(session, " \n 123 \n  \n ").asNumber().toInteger());
            assertEquals(123, evaluate(session, " //xxx\n 123 //xxx\n \n //xxx\n ").asNumber().toInteger());
            assertEquals(123, evaluate(session, "/* xxx */ 123 /* xxx */").asNumber().toInteger());
            assertEquals(123, evaluate(session, "/* xx/xx */ 123 /* xx*xx */").asNumber().toInteger());
            assertEquals(123, evaluate(session, "  123  // xxx\n\n\n").asNumber().toInteger());
            assertEquals(123, evaluate(session, "//xxx\n123").asNumber().toInteger());
            assertEquals(123, evaluate(session, " 123 // xxx").asNumber().toInteger());
            assertEquals(123, evaluate(session, " /* xx /* yy */ yy */ 123 ").asNumber().toInteger());
        }
    }

    @Test
    public void testNull() throws StyxException {
        try(Session session = sf.createSession()) {
            assertNull(session.parse(null, false));
            assertNull(session.parse(null, true));
            assertNull(session.parse("", false));
            assertNull(session.parse("", true));
            assertNull(session.parse("  ", false));
            assertNull(session.parse("  ", true));

            assertNull(session.evaluate(null));
            assertNull(session.evaluate(""));
            assertNull(session.evaluate("  "));
        }
    }

    @Test
    public void testMath() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(3, evaluate(session, "1+2").asNumber().toInteger());
            assertEquals(3, evaluate(session, " 1 + 2 ").asNumber().toInteger());
            assertEquals(12, evaluate(session, "1++2").asNumber().toInteger());
            assertEquals(12, evaluate(session, " 1 ++ 2 ").asNumber().toInteger());
        }
    }

    @Test
    public void testConsts() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals("[4:123456]", evaluate(session, " c1 :=== 123 , c2 :=== 456 , c3 :=== c1 ++ c2 , c3 ").toString());

            assertEquals(4, evaluate(session, " c :=== if(1 == 2) 3 else 4, c ", 2).asNumber().toInteger());
            assertEquals(9, evaluate(session, " c :=== (x,y) -> { return x + y + 2 } (3,4), c ", 2).asNumber().toInteger());

            try {
                parse(session, "c1 :=== 123, c2 :=== 456, c2 :=== 789");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'c2' is already defined.", e.getMessage());
            }

            try {
                parse(session, "v1 := 123, c1 :=== v1");
                fail();
            } catch(StyxException e) {
                assertEquals("Expression must be compile-time constant.", e.getMessage());
            }

            try {
                parse(session, " c :=== if(1 == 2) 3 else [/][*] ");
                fail();
            } catch(StyxException e) {
                assertEquals("Expression must be compile-time constant.", e.getMessage());
            }

            try {
                parse(session, " c :=== (x,y) -> { return x + y + ([/][*] ?? 0) } (3,4) ");
                fail();
            } catch(StyxException e) {
                assertEquals("Expression must be compile-time constant.", e.getMessage());
            }
        }
    }

    @Test
    public void testVars() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(46, evaluate(session, " v1 := 12 , v2 :== 34 , v1 +v2", 3).asNumber().toInteger());
            assertEquals(1234, evaluate(session, " v1 := 12 , v2 :== 34 , v1 ++ v2", 3).asNumber().toInteger());

            assertEquals(15, evaluate(session, " v1 := 12, v2 :== 34, v1 = v1 + 3, v1", 4).asNumber().toInteger());
            assertEquals(49, evaluate(session, " v1 := 12, v2 :== 34, v1 = v1 + 3, v1 + v2", 4).asNumber().toInteger());

            try {
                parse(session, " v1 := 12, v1 := 12");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'v1' is already defined.", e.getMessage());
            }

            try {
                parse(session, " v1 = 12");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'v1' is not defined.", e.getMessage());
            }

            try {
                parse(session, " v1 :== 12, v1 = 567");
                fail();
            } catch(StyxException e) {
                assertEquals("The variable 'v1' is not mutable.", e.getMessage());
            }

            assertEquals(98, evaluate(session, "v1 := 12, v2 :== 34, v1 = v1 + 3, v1+(v1+v2)+v2", 4).asNumber().toInteger());
        }
    }

    @Test
    public void testExprs() throws IOException, StyxException {
        try(Session session = sf.createSession()) {
            evaluate(session, "x := 100");
            evaluate(session, "y := 200");
            assertNull(evaluate(session, "null"));
            assertEquals("void", evaluate(session, "void").toString());
            assertEquals(false, evaluate(session, "false").asBool().toBool());
            assertEquals(true, evaluate(session, "true").asBool().toBool());
            assertEquals("void", evaluate(session, "\"void\"").toString());
            assertEquals(false, evaluate(session, "\"false\"").asBool().toBool());
            assertEquals(true, evaluate(session, "\"true\"").asBool().toBool());
            assertEquals(true, evaluate(session, "nix := ()->{}(), nix == null", 2).asBool().toBool());
            assertEquals("1", evaluate(session, "x := 100, y := 200, if(\"x\" == \"x\") 1 else 2", 3).toString());
            assertEquals("2", evaluate(session, "x := 100, y := 200, if(\"x\" == \"y\") 1 else 2", 3).toString());
            assertEquals("100", evaluate(session, "x := 100, y := 200, if(\"x\" == \"x\") x else y", 3).toString());
            assertEquals("200", evaluate(session, "x := 100, y := 200, if(\"x\" == \"y\") x else y", 3).toString());
            assertEquals("1", evaluate(session, "x := 100, y := 200, if(x == x) 1 else 2", 3).toString());
            assertEquals("2", evaluate(session, "x := 100, y := 200, if(x == y) 1 else 2", 3).toString());
        }
    }

    @Test
    public void testExprsComplex() throws IOException, StyxException {
        try(Session session = sf.createSession()) {
            String exprs = "x := 100, y := 200, ";

            assertEquals("[/]", evaluate(session, exprs + "[/]", 3).toString());
            assertEquals("[/100]", evaluate(session, exprs + "[/][x]", 3).toString());
            assertEquals("[/100/200]", evaluate(session, exprs + "[/][x][y]", 3).toString());
            assertEquals("[/100/200/100]", evaluate(session, exprs + "[/][x][y][x]", 3).toString());
            assertEquals("[/100/200/100]", evaluate(session, exprs + "[/][100][200][100]", 3).toString());
            assertEquals("[]", evaluate(session, exprs + "[]", 3).toString());
            assertEquals("[100]", evaluate(session, exprs + "[(x)]", 3).toString());
            assertEquals("[100,200]", evaluate(session, exprs + "[(x),(y)]", 3).toString());
            assertEquals("[100,200]", evaluate(session, exprs + " \n [ \n (x) \n (y) \n ] \n ", 3).toString());
            assertEquals("[100,200]", evaluate(session, exprs + " \n [ \n (x) , \n (y) \n ] \n ", 3).toString());
            assertEquals("[100,200,100]", evaluate(session, exprs + "[(x),(y),(x)]", 3).toString());
            assertEquals("[100:y,200:x]", evaluate(session, exprs + "[(x):y,(y):x]", 3).toString());
            assertEquals("[x:200,y:100]", evaluate(session, exprs + "[x:(y),y:(x)]", 3).toString());
            assertEquals("[100:200,200:100]", evaluate(session, exprs + "[(x):(y),(y):(x)]", 3).toString());
            assertEquals("[100:200,200:100]", evaluate(session, exprs + "[(x):(y),(y):(x)]", 3).toString());

            assertEquals("[x:200,y:100]", evaluate(session, exprs + " \n [ \n x : (y) \n y : (x) \n ] \n ", 3).toString());
            assertEquals("[100:200,200:100]", evaluate(session, exprs + " \n [ \n (x) : (y) \n (y) : (x) \n ] \n ", 3).toString());

            assertEquals("[x:200,y:100]", evaluate(session, exprs + " \n [ \n x : (y) , \n y : (x) \n ] \n ", 3).toString());
            assertEquals("[100:200,200:100]", evaluate(session, exprs + " \n [ \n (x) : (y) , \n (y) : (x) \n ] \n ", 3).toString());

            assertEquals("[100:200,200:101]", evaluate(session, exprs + "[(x):(y),(y):x,200:101]", 3).toString());
            assertEquals("[1,2,3,5:5,7:6]", evaluate(session, exprs + "[1,2,3,(null),5,(null),6]", 3).toString());
            assertEquals("[1,2,3,5:5,7:6]", evaluate(session, exprs + "[(1),(2),(3),(null),(5),(null),(6)]", 3).toString());
            assertEquals("[11:11,33:33]", evaluate(session, exprs + "[11:11,22:(null),33:33]", 3).toString());
            assertEquals("[100:200]", evaluate(session, exprs + "[(x):(y)]", 3).toString());
            assertEquals("[300:400]", evaluate(session, exprs + "[(x+y): (y+y)]", 3).toString());
        }
    }

    @Test
    public void testLoop() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(22, evaluate(session, " v := 0, loop { v += 10, if(v > 20) { break }, v += 2 }, v ", 3).asNumber().toInteger());
        }
        try(Session session = sf.createSession()) {
            assertEquals(20, evaluate(session, " v := 0, while(v < 20) { v += 10 }, v ", 3).asNumber().toInteger());
        }
        try(Session session = sf.createSession()) {
            assertEquals(0, evaluate(session, " v := 0, while(v < 0) { v += 10 }, v ", 3).asNumber().toInteger());
        }
        try(Session session = sf.createSession()) {
            assertEquals(20, evaluate(session, " v := 0, do { v += 10 } while(v < 20), v ", 3).asNumber().toInteger());
        }
        try(Session session = sf.createSession()) {
            assertEquals(10, evaluate(session, " v := 0, do { v += 10 } while(v < 0), v ", 3).asNumber().toInteger());
        }
    }

    @Test
    public void testFor() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(40, evaluate(session, " v := 0, for(i := 0, i < 4, i+=1) { v += 10 }, v ", 3).asNumber().toInteger());
            assertEquals(40, evaluate(session, " f := () -> { v := 0, for(i := 0, i < 4, i+=1) { v += 10 }, return v }, f() ", 2).asNumber().toInteger());

            try {
                session.evaluate(" v := 0, for(i :== 0, i < 4, i+=1) { v += 10 }, v ");
                fail();
            } catch(StyxException e) {
                assertEquals("The variable 'i' is not mutable.", e.getMessage());
            }

            try {
                session.evaluate(" v := 0, for(i :=== 0, i < 4, i+=1) { v += 10 }, v ");
                fail();
            } catch(StyxException e) {
                assertEquals("The left expression is not assignable (Constant).", e.getMessage());
            }
        }
    }

    @Test
    public void testForExpr() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("[1,4,9,16]", evaluate(session, " for(i := 1, i <= 4, i+=1) yield i*i ").toString());
            assertEquals("[[1,1],[2,4],[3,9],[4,16]]", evaluate(session, " for(i := 1, i <= 4, i+=1) yield [(i), (i*i)] ").toString());
        }
    }

    @Test
    public void testForEach() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(70, evaluate(session, " sum := 0, foreach(e in [ 10, 15, 20, 25 ]) { sum += e }, sum ", 3).asNumber().toInteger());
            assertEquals(0,  evaluate(session, " sum := 0, foreach(e in [ ]) { sum += e }, sum ", 3).asNumber().toInteger());
            assertEquals(0,  evaluate(session, " sum := 0, foreach(e in session.null()) { sum += e }, sum ", 3).asNumber().toInteger());
            assertEquals(70, evaluate(session, " vals := [ 10, 15, 20, 25 ], sum := 0, foreach(e in vals) { sum += e }, sum ", 4).asNumber().toInteger());
            assertEquals(80, evaluate(session, " vals := [ 10, 15, 20, 25 ], sum := 0, foreach(k, v in vals) { sum += k + v }, sum ", 4).asNumber().toInteger());
            assertEquals(70, evaluate(session, " f := () -> { vals := [ 10, 15, 20, 25 ], sum := 0, foreach(e in vals) { sum += e }, return sum }, f() ", 2).asNumber().toInteger());
            assertEquals(80, evaluate(session, " f := () -> { vals := [ 10, 15, 20, 25 ], sum := 0, foreach(k, v in vals) { sum += k + v }, return sum }, f() ", 2).asNumber().toInteger());

            try {
                session.evaluate(" sum := 0, foreach(e in \"xxx\") { sum += e }, sum");
                fail();
            } catch(ClassCastException e) {
                assertEquals("The value is not a complex value.", e.getMessage());
            }

        }
    }

    @Test
    public void testForEachExpr() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals("[1,4,9,16]", evaluate(session, " foreach(i in [1,2,3,4]) yield i*i ").toString());
            assertEquals("[[1,1],[2,4],[3,9],[4,16]]", evaluate(session, " foreach(i in [1,2,3,4]) yield [(i), (i*i)] ").toString());
        }
    }

    @Test
    public void testScope() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(123, evaluate(session, "v := 123, v", 2).asNumber().toInteger());

            try {
                session.evaluate("if(1 == 0) { v := 123 }, v");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'v' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("while(1 == 0) { v := 123 }, v");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'v' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("do { v := 123 } while (1 == 0), v");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'v' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("for(i := 1, i < 3, i+=1) { v := 123 }, v");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'v' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("for(i := 1, i < 3, i+=1) { v := 123 }, i");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'i' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("foreach(i in [1,2,3]) { v := 123 }, i");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'i' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("foreach(i, j in [1,2,3]) { v := 123 }, i");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'i' is not defined.", e.getMessage());
            }

            try {
                session.evaluate("foreach(i, j in [1,2,3]) { v := 123 }, j");
                fail();
            } catch(StyxException e) {
                assertEquals("The symbol 'j' is not defined.", e.getMessage());
            }
        }
    }

    @Test
    public void testBatchUnallowed() throws StyxException {
        try(Session session = sf.createSession()) {
            List<String > scripts = Arrays.asList(
                    "return",
                    "return 5",
                    "yield 5",
                    "break",
                    "continue",
                    "if(1==1) { return }",
                    "if(1==1) { return 5 }",
                    "if(1==1) { yield 5 }",
                    "if(1==1) { break }",
                    "if(1==1) { continue }",
                    "if(return) { }",
                    "if(return 5) { }",
                    "if(yield 5) { }",
                    "if(break) { }",
                    "if(continue) { }",
                    "if(throw \"xx\") { }",
                    "if(retry) { }",
                    "for(i := 1, return i, i+=1) { }",
                    "for(i := 1, j := 1, i+=1) { }",
                    "for(i := 1, i = 2, i+=1) { }",
                    "if(x := 1) { }");

            for(String script : scripts) {
                try {
                    session.evaluate(script);
                    fail(script);
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("not allowed"));
                }
            }
        }
    }

    @Test
    public void testFunctionUnallowed() throws StyxException {
        try(Session session = sf.createSession()) {
            List<String > scripts = Arrays.asList(
                    "() -> { yield 5 }",
                    "() -> { break }",
                    "() -> { continue }",
                    "() -> { if(1==1) { yield 5 } }",
                    "() -> { if(1==1) { break } }",
                    "() -> { if(1==1) { continue } }",
                    "() -> { if(return) { } }",
                    "() -> { if(return 5) { } }",
                    "() -> { if(yield 5) { } }",
                    "() -> { if(break) { } }",
                    "() -> { if(continue) { } }",
                    "() -> { if(throw \"xx\") { } }",
                    "() -> { if(retry) { } }",
                    "() -> { for(i := 1, return i, i+=1) { } }",
                    "() -> { for(i := 1, j := 1, i+=1) { } }",
                    "() -> { for(i := 1, i = 2, i+=1) { } }",
                    "() -> { if(x := 1) { } }");

            for(String script : scripts) {
                try {
                    session.evaluate(script);
                    fail(script);
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("not allowed"));
                }
            }
        }
    }

    @Test
    public void testLamdaUnallowed() throws StyxException {
        try(Session session = sf.createSession()) {
            List<String > scripts = Arrays.asList(
                    "() -> return",
                    "() -> return 5",
                    "() -> yield 5",
                    "() -> break",
                    "() -> continue",
                    "() -> if(1==1) { return }",
                    "() -> if(1==1) { return 5 }",
                    "() -> if(1==1) { break }",
                    "() -> if(1==1) { continue }",
                    "() -> if(return) { }",
                    "() -> if(return 5) { }",
                    "() -> if(yield 5) { }",
                    "() -> if(break) { }",
                    "() -> if(continue) { }",
                    "() -> if(throw \"xx\") { }",
                    "() -> if(retry) { }",
                    "() -> for(i := 1, return i, i+=1) { }",
                    "() -> for(i := 1, j := 1, i+=1) { }",
                    "() -> for(i := 1, i = 2, i+=1) { }",
                    "() -> if(x := 1) { }");

            for(String script : scripts) {
                try {
                    session.evaluate(script);
                    fail(script);
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("not allowed"));
                }
            }
        }
    }

    @Test
    public void testFunctions() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            String exprs = "add := (x,y) -> { return x+y }, cat := (x,y) -> { return x++y }\n ";
            assertEquals(11, evaluate(session, exprs + " add ( 5 , 6 ) ", 3).asNumber().toInteger());
            assertEquals(11, evaluate(session, exprs + " add ( 5 , 6 ) ", 3).asNumber().toInteger());
            assertEquals(56, evaluate(session, exprs + " cat ( 5 , 6 ) ", 3).asNumber().toInteger());
            assertEquals(1156, evaluate(session, exprs + " add ( 5 , 6 ) ++ cat ( 5 , 6 ) ", 3).asNumber().toInteger());
            assertEquals(1113, evaluate(session, exprs + "cat(add(5,6),add(6,7))", 3).asNumber().toInteger());

            assertEquals(7, evaluate(session, "() -> { return 7 } ()").asNumber().toInteger());
            assertNull(evaluate(session, "() -> { return } ()"));
        }
    }

    @Test
    public void testFunctions2() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            String exprs = "add :=== (x,y) -> { if(x == \"\" || y == \"\") { throw \"empty\" }, if(x < 0 || y < 0) { return 0 } else { return x + y } }\n";
            assertEquals(11, evaluate(session, exprs + " add ( 5 , 6) ", 2).asNumber().toInteger());
            assertEquals(0, evaluate(session, exprs + " add ( -1 , -1 ) ", 2).asNumber().toInteger());

            try {
                evaluate(session, exprs + "add(\"\",\"\")");
                fail();
            } catch(StyxException e) {
                assertEquals("The script invoked a 'throw' statement.", e.getMessage());
                assertEquals("empty", e.getValue().asText().toTextString());
            }

            exprs += "add_safe :=== (x,y) -> { try { return add(x,y) } catch(e) { return 42 } finally { if(x < -10 || y < -10) { return -100 } } }\n";
            assertEquals(11, evaluate(session, exprs + " add_safe ( 5 , 6) ", 3).asNumber().toInteger());
            assertEquals(0, evaluate(session, exprs + " add_safe ( -1 , -1 ) ", 3).asNumber().toInteger());
            assertEquals(42, evaluate(session, exprs + " add_safe ( \"\" , \"\" ) ", 3).asNumber().toInteger());
            assertEquals(-100, evaluate(session, exprs + " add_safe ( -20 , -20 ) ", 3).asNumber().toInteger());

            exprs += "dup :=== (s, n) -> { res := \"\", num := n, if(num < 0) { return }, while(num > 0) { num = num - 1, if(num >= 1000) { return \"huge\" }, if(num >= 100) { res = \"large\", break }, if(num >= 10) { res = res ++ \".\", continue }, res = res ++ s }, return res }\n";
            assertEquals("", evaluate(session, exprs + "dup(\"xy\", 0)", 4).asText().toTextString());
            assertEquals("xyxyxy", evaluate(session, exprs + "dup(\"xy\", 3)", 4).asText().toTextString());
            assertEquals("..........xyxyxyxyxyxyxyxyxyxy", evaluate(session, exprs + "dup(\"xy\", 20)", 4).asText().toTextString());
            assertEquals("large", evaluate(session, exprs + "dup(\"xy\", 101)", 4).asText().toTextString());
            assertEquals("huge", evaluate(session, exprs + "dup(\"xy\", 1001)", 4).asText().toTextString());
            assertNull(evaluate(session, exprs + "dup(\"xy\", -1)", 4));
        }
    }

    @Test
    public void testFunctions3() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            assertEquals(10.72, evaluate(session, "math.E + math.pow(2,3)").asNumber().toDouble(), 0.01);
            assertEquals(10.72, evaluate(session, "() -> { return math.E + math.pow(2,3) } ()").asNumber().toDouble(), 0.01);
            assertEquals(10.72, evaluate(session, "(x) -> { return math.E + math.pow(x,x+1) } (2)").asNumber().toDouble(), 0.01);
        }
    }

    @Test
    public void testFunctions4() throws StyxException, IOException {
        try(Session session = sf.createSession()) {
            evaluate(session, " f := () -> {} () , f() ");
            fail();
        } catch(NullPointerException e) {
            assertEquals("The target of the function call is null.", e.getMessage());
        }
    }

    @Test
    public void testFunctionExplicit() throws IOException, StyxException {
        try(Session session = sf.createSession()) {
            String exprs = "add := (x,y) -> { return x+y }\n cat := (x,y) -> { return x++y }\n ";
            Function cat = evaluate(session, exprs + "cat", 3).asFunction();
            Value arg1 = evaluate(session, exprs + "add(5,6)", 3);
            Value arg2 = evaluate(session, exprs + "add(6,7)", 3);
            assertEquals(1113, cat.invoke(session, new Value[] { arg1, arg2 }).asNumber().toInteger());
        }
    }

    @Test
    public void testFunctionUserCompiled() throws StyxException, IOException {
        AbstractSessionFactory sf2 = (AbstractSessionFactory /* TODO (cleanup) AbstractSessionFactory vs. SessionFactory? */) SessionManager.createMemorySessionFactory(false);
        try(Session session = sf2.createSession()) {
            Complex def = new CompiledFunction(sf2.getRegistry(), "test_1", Determinism.PURE, 2) {
                @Override
                public Value invoke(Stack stack) throws StyxException {
                    return stack.session().text(stack.getFrameValue(0).asText().toTextString() + stack.getFrameValue(1).asText().toTextString());
                }
            };
            Function func = session.function(def);
            String funcstr = "-> @CompiledFunction test_1";
            assertEquals(funcstr, session.serialize(func, true));
            assertSame(def, session.deserialize(funcstr).asFunction().definition());
            assertSame(func, session.deserialize(funcstr));
            session.write(session.root(), func);
            assertEquals("xxxyyy", evaluate(session, "root :=== [/], (root[*])(\"xxx\",\"yyy\")", 2).asText().toTextString());
            assertEquals("aaabbb", evaluate(session, "(-> @CompiledFunction test_1)(\"aaa\",\"bbb\")").asText().toTextString());

            try {
                new CompiledFunction(sf2.getRegistry(), "test_1", Determinism.PURE, 2) {
                    @Override public Value invoke(Stack stack) throws StyxException { return null; }
                };
                fail();
            } catch(StyxException e) {
                assertEquals("Already registiered compiled function 'test_1'.", e.getMessage());
            }

            try {
                session.deserialize("-> @CompiledFunction test_X");
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize"));
                assertTrue(e.getCause().getMessage().contains("Unknown compiled function"));
            }

            try {
                session.deserialize("-> @XXX YYY");
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize"));
                assertTrue(e.getCause().getMessage().contains("Unknown tag"));
            }

            try {
                session.deserialize("-> [ ]");
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize"));
                assertTrue(e.getCause().getMessage().contains("Cannot decode function from"));
            }

            try {
                session.deserialize("-> foo");
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize"));
                assertTrue(e.getCause().getMessage().contains("Cannot decode function from"));
            }
        }

        try(Session session = sf.createSession()) {
            try {
                String funcstr = "-> @CompiledFunction test_1";
                session.deserialize(funcstr);
                fail();
            } catch(StyxException e) {
                assertTrue(e.getMessage().contains("Failed to deserialize"));
                assertTrue(e.getCause().getMessage().contains("Unknown compiled function"));
            }
        }
    }

    @Test
    public void testTryAndFrame() throws IOException, StyxException {
        try(Session session = sf.createSession()) {

            assertEquals(3009, evaluate(session, "                                  a := 1000, b:= 2000, i := 1, while(i <= 3) { try { x := 1, y := 2, throw \"x\" } catch(e) { a2 := a + 1, b2 := b + 2, a = a2, b = b2 }, i2 := i+1, i = i2 },        a + b      ", 5).asNumber().toInteger());
            assertEquals(3009, evaluate(session, "() ->                           { a := 1000, b:= 2000, i := 1, while(i <= 3) { try { x := 1, y := 2, throw \"x\" } catch(e) { a2 := a + 1, b2 := b + 2, a = a2, b = b2 }, i2 := i+1, i = i2 }, return a + b } () ").   asNumber().toInteger());
            assertEquals(3009, evaluate(session, "        t := (x) -> { throw x },  a := 1000, b:= 2000, i := 1, while(i <= 3) { try { x := 1, y := 2, t(\"x\")    } catch(e) { a2 := a + 1, b2 := b + 2, a = a2, b = b2 }, i2 := i+1, i = i2 },        a + b      ", 6).asNumber().toInteger());
            assertEquals(3009, evaluate(session, "() -> { t := (x) -> { throw x },  a := 1000, b:= 2000, i := 1, while(i <= 3) { try { x := 1, y := 2, t(\"x\")    } catch(e) { a2 := a + 1, b2 := b + 2, a = a2, b = b2 }, i2 := i+1, i = i2 }, return a + b } () ").   asNumber().toInteger());

            assertEquals("A12eA23eA34e", evaluate(session, "                                  res := \"\", i := 1, while(i <= 3) { j :== i, try { res = res ++ \"A\", res = res ++ j ++ j+1, throw j ++ j+1, res = res ++ \"B\" } catch(e) { res = res ++ \"e\" }, i2 := i+1, i = i2 }, res ", 4).asText().toTextString());
            assertEquals("A12eA23eA34e", evaluate(session, "t2 := (x, y) -> { throw x ++ y }, res := \"\", i := 1, while(i <= 3) { j :== i, try { res = res ++ \"A\", res = res ++ j ++ j+1, t2(j,j+1),      res = res ++ \"B\" } catch(e) { res = res ++ \"e\" }, i2 := i+1, i = i2 }, res ", 5).asText().toTextString());

            assertEquals("11tfe22tfe33tfe", evaluate(session, "        t := (x) -> { throw \"x\" }, res := \"\", i := 1, while(i <= 3) { j :== i, res = res ++ j, try { res = res ++ j, try { res = res ++ \"t\", t(\"x\") } finally { res = res ++ \"f\" } } catch(e) { res = res ++ \"e\" }, i2 := i+1, i = i2 },        res      ", 5).asText().toTextString());
            assertEquals("11tfe22tfe33tfe", evaluate(session, "() -> { t := (x) -> { throw \"x\" }, res := \"\", i := 1, while(i <= 3) { j :== i, res = res ++ j, try { res = res ++ j, try { res = res ++ \"t\", t(\"x\") } finally { res = res ++ \"f\" } } catch(e) { res = res ++ \"e\" }, i2 := i+1, i = i2 }, return res } () ")   .asText().toTextString());
        }
    }

    @Test
    public void testAtomicAndFrame() throws IOException, StyxException, InterruptedException {
        final SessionFactory sf2 = SessionManager.createMemorySessionFactory(true);
        final String[] res = new String[1];

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try(Session session = sf2.createSession()) {
                    res[0] = evaluate(session, "() -> { r :=== (x) -> { tmp := [/][*], if(x <= 3) { retry } }, res := \"\", i := 1, atomic { res = res ++ \"a\", j :== i, res = res ++ j, atomic { res = res ++ \"b\", res = res ++ j, i2 := i+1, i = i2, r(i), res = res ++ \"X\" } }, [/][*] = \"done\", return res }  ()").asText().toTextString();
                } catch (StyxException e) {
                    fail(e.toString());
                }
            }
        });
        t.start();

        try(Session session = sf2.createSession()) {
            int i = 1;
            while(evaluate(session, "([/][*] ?? \"\") == \"done\"").asBool().toBool() == false) {
                session.write(session.root(), session.number(i));
                Thread.sleep(50);
            }
        }

        t.join(60000);

        assertEquals("a1b1a2b2a3b3X", res[0]);
    }

    @Test
    public void testFunctionLiterals() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(11, evaluate(session, "f1 := (x,y) -> { return x+y }\nf1(5,6)\n", 2).asNumber().toInteger());
            assertEquals(11, evaluate(session, "f1 := -> @Function [ args: [ [name: x], [name: y] ], body: @Block [ @Return @Add [ expr1: @Variable x, expr2: @Variable y ] ] ]\nf1(5,6)\n", 2).asNumber().toInteger());
        }
    }

    @Test
    public void testFunctionLiteralsConsts() throws StyxException {
        try(Session session = sf.createSession()) {
            assertEquals(Math.PI, evaluate(session, "f := () -> { return math.PI } \n f() \n", 2).asNumber().toDouble(), 0.1);
            assertEquals(Math.PI, evaluate(session, "f := -> @Function [ args: [ ], body: @Block [ @Return @Child [ expr1: @Variable math, expr2: @Constant PI ] ] ] \n f() \n", 2).asNumber().toDouble(), 0.1);
            assertEquals(Math.PI, evaluate(session, "f := () -> { return () -> { return math.PI } } \n f() () \n", 2).asNumber().toDouble(), 0.1);
            assertEquals(Math.PI, evaluate(session, "f := -> @Function [ args: [ ], body: @Block [ @Return @Constant -> @Function [ args: [ ], body: @Block [ @Return @Child [ expr1: @Variable math, expr2: @Constant PI ] ] ] ] ] \n f() ()\n", 2).asNumber().toDouble(), 0.1);
        }
    }

    @Test
    public void testQuoteParseReserved() throws StyxException {
        try(Session session = sf.createSession()) {
            List<String> scripts = Arrays.asList(
                    "if + else", "try + throw", "while := for",
                    "for := 123", "{for}", "for := 123, {for}",
                    "{try, catch, finally}");
            for(String script : scripts) {
                try {
                    parse(session, script);
                    fail(script);
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("cannot be used as a symbol"));
                }
            }

            scripts = Arrays.asList(
                    "return := 123", "break := 123", "continue := 123");
            for(String script : scripts) {
                try {
                    parse(session, script);
                    fail(script);
                } catch(StyxException e) {
                    assertTrue(e.getMessage().contains("Cannot handle"));
                }
            }
        }
    }

    private Function parse(Session session, String script) throws StyxException {
        return session.parse(script, true);
    }

    private Value evaluate(Session session, String script) throws StyxException {
        try {
            Value parsed   = session.parse(script, true);
            String   serial   = session.serialize(parsed, true);
            Value reparsed = session.deserialize(serial);
            String   reserial = session.serialize(reparsed, true);
            assertEquals(parsed.getClass(), reparsed.getClass());
            assertEquals(serial, reserial);
        } catch (StyxException e) {
            throw new IllegalArgumentException("Failed to parse: " + script, e);
        }
        return session.evaluate(script);
    }

    private Value evaluate(Session session, String script, int idx) throws StyxException {
        return evaluate(session, script).asComplex().get(session.number(idx));
    }
}
