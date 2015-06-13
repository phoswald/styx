package styx;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

public class TestSessionManager {

	@Test(expected=NullPointerException.class)
	public void testNull() throws StyxException {
		SessionManager.createSessionFactory(null, null);
	}

	@Test
	public void testDetached() throws StyxException, IOException {
		SessionFactory factory = SessionManager.createSessionFactory("detached", null);
		assertNotNull(factory);

		try(Session session = factory.createSession()) {
			assertNotNull(session);
		}
	}

	@Test
	public void testMemory() throws StyxException, IOException {
		SessionFactory factory = SessionManager.createSessionFactory("memory", null);
		assertNotNull(factory);

		try(Session session = factory.createSession()) {
			assertNotNull(session);
		}
	}

	@Test(expected=StyxException.class)
	public void testInvalid() throws StyxException {
		SessionManager.createSessionFactory("xxx", null);
	}

	@Test(expected=NullPointerException.class)
	public void testRegisterInvalid1() throws StyxException {
		SessionManager.registerProvider(null);
	}

	@Test(expected=NullPointerException.class)
	public void testRegisterInvalid2() throws StyxException {
		SessionManager.registerProvider(new SessionProvider() {
            @Override public String getName() { return null; }
            @Override public SessionFactory createSessionFactory(Complex parameters) { return null; }
		});
	}

	@Test(expected=StyxException.class)
	public void testRegisterAgain() throws StyxException {
		SessionManager.registerProvider(new SessionProvider() {
		    @Override public String getName() { return "detached"; }
			@Override public SessionFactory createSessionFactory(Complex parameters) throws StyxException { return null; }
		});
	}
}
