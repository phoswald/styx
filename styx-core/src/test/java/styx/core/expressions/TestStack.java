package styx.core.expressions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import styx.Session;
import styx.SessionManager;

public class TestStack {

	private Session session = SessionManager.getDetachedSession();

	@Test
	public void testPush() {
		Stack stack = new Stack(session);
		assertEquals(0, stack.getFrameSize());
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		stack.push(session.text("c"));
		assertEquals(3, stack.getFrameSize());
		assertEquals("a", stack.getFrameValue(0).asText().toTextString());
		assertEquals("b", stack.getFrameValue(1).asText().toTextString());
		assertEquals("c", stack.getFrameValue(2).asText().toTextString());
	}

	@Test
	public void testSetValue() {
		Stack stack = new Stack(session);
		assertEquals(0, stack.getFrameSize());
		stack.setFrameValue(0, session.text("a"));
		stack.setFrameValue(2, session.text("c"));
		stack.setFrameValue(1, session.text("b"));
		stack.setFrameValue(1, session.text("b2"));
		assertEquals(3, stack.getFrameSize());
		assertEquals("a",  stack.getFrameValue(0).asText().toTextString());
		assertEquals("b2", stack.getFrameValue(1).asText().toTextString());
		assertEquals("c",  stack.getFrameValue(2).asText().toTextString());
	}

	@Test
	public void testCall() {
		Stack stack = new Stack(session);
		assertEquals(0, stack.getFrameSize());
		int pos1 = stack.prepareFrame();
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		stack.push(session.text("c"));
		stack.enterFrame(pos1);
		assertEquals(3, stack.getFrameSize()); // a + b + c
		stack.setFrameValue(3, session.text("d"));
		stack.setFrameValue(4, session.text("e"));
		assertEquals(5, stack.getFrameSize()); // a + b + c + d + e = 3 args + 2 vars
		stack.leaveFrame(pos1);
		assertEquals(0, stack.getFrameSize());
	}

	@Test
	public void testCallTwice() {
		Stack stack = new Stack(session);
		assertEquals(0, stack.getFrameSize());
		int pos1 = stack.prepareFrame();
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		stack.push(session.text("c"));
		stack.enterFrame(pos1);
		assertEquals(3, stack.getFrameSize()); // a + b + c
		stack.leaveFrame(pos1);
		assertEquals(0, stack.getFrameSize());
		int pos2 = stack.prepareFrame();
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		stack.push(session.text("c"));
		stack.enterFrame(pos2);
		assertEquals(3, stack.getFrameSize()); // a + b + c
		stack.leaveFrame(pos2);
		assertEquals(0, stack.getFrameSize());
	}

	@Test
	public void testCall2() {
		Stack stack = new Stack(session);
		stack.push(session.text("u"));
		stack.push(session.text("v"));
		assertEquals(2, stack.getFrameSize()); // u + v
		int pos1 = stack.prepareFrame();
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		stack.push(session.text("c"));
		stack.enterFrame(pos1);
		assertEquals(3, stack.getFrameSize()); // a + b + c
		stack.setFrameValue(3, session.text("d"));
		stack.setFrameValue(4, session.text("e"));
		assertEquals(5, stack.getFrameSize()); // a + b + c + d + e = 3 args + 2 vars
		stack.leaveFrame(pos1);
		assertEquals(2, stack.getFrameSize()); // u + v
	}

	@Test
	public void testCallNested() {
		Stack stack = new Stack(session);
		assertEquals(0, stack.getFrameSize());
		int pos1 = stack.prepareFrame();
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		stack.push(session.text("c"));
		stack.enterFrame(pos1);
		assertEquals(3, stack.getFrameSize());
		stack.setFrameValue(3, session.text("d"));
		stack.setFrameValue(4, session.text("e"));
		assertEquals(5, stack.getFrameSize());

		int pos2 = stack.prepareFrame();
		stack.push(session.text("x"));
		stack.push(session.text("y"));
		assertEquals("a", stack.getFrameValue(0).asText().toTextString());
		assertEquals("b", stack.getFrameValue(1).asText().toTextString());
		stack.enterFrame(pos2);
		assertEquals(2, stack.getFrameSize());
		assertEquals("x", stack.getFrameValue(0).asText().toTextString());
		assertEquals("y", stack.getFrameValue(1).asText().toTextString());
		stack.leaveFrame(pos2);

		assertEquals(5, stack.getFrameSize());
		assertEquals("a", stack.getFrameValue(0).asText().toTextString());
		assertEquals("e", stack.getFrameValue(4).asText().toTextString());

		stack.leaveFrame(pos1);
		assertEquals(0, stack.getFrameSize());
	}

	@Test
	public void testCallAborted() {
		Stack stack = new Stack(session);
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		assertEquals(2, stack.getFrameSize()); // a + b

		int pos1 = stack.prepareFrame();
		stack.push(session.text("x"));
		stack.push(session.text("y"));
		stack.push(session.text("z"));
		stack.leaveFrame(pos1); // abort x + y + z
		assertEquals(2, stack.getFrameSize()); // a + b
	}

	@Test
	public void testCallNestedAborted() {
		Stack stack = new Stack(session);
		stack.push(session.text("a"));
		stack.push(session.text("b"));
		assertEquals(2, stack.getFrameSize()); // a + b
		int pos1 = stack.prepareFrame();
		stack.push(session.text("x"));
		stack.push(session.text("y"));
		stack.push(session.text("z"));
		stack.enterFrame(pos1);
		assertEquals(3, stack.getFrameSize()); // x + y + z
		int pos2 = stack.prepareFrame();
		stack.push(session.text("a"));
		stack.leaveFrame(pos2); // abort a
		assertEquals(3, stack.getFrameSize()); // x + y + z
		stack.leaveFrame(pos1); // return from x + y + z
		assertEquals(2, stack.getFrameSize()); // a + b
	}
}
