package styx.web.jetty;

import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Command line application and Windows Service or Unix Daemon for running a WAR file.
 * <p>
 * Supported operation modes:
 * <ul>
 * <li> run as a command line application, stop by pressing return or CTRL+C
 * <li> run as Windows Service with Apache Procrun, specifying "jvm" mode with entry points App.main() and App.stop().
 * <li> run as Unix Daemon, using a PID file and SIGTERM to stop.
 * <li> run using other integration techniques that either call System.exit() or App.stop() to stop.
 */
public class WebServer {

    private static final State state = new State();

    /**
     * Entry point for application and service startup.
     *
     * @param args the command line arguments for the application or service.
     */
    public static void main(String[] args) {
        try {
            String httpPort   = System.getProperty("styx.web.http.port", "8080");
            String warBase    = System.getProperty("styx.web.war.base");
            String warContext = System.getProperty("styx.web.war.context", "/");

            if(warBase != null) {
                System.out.print("Using specified content location: " + warBase);
            } else {
                warBase = WebServer.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
                if(warBase.endsWith(".war")) {
                    // We have been invoked from "java -jar styx-web-*.war". Now we can simply let embedded Jetty serve the WAR file.
                    // This should work exactly as if the WAR file was deployed to Jetty or any other JEE compliant Servlet container.
                    System.out.println("Executing WAR file: " + warBase);
                } else {
                    // We have been invoked from "java -cp target/classes styx.web.styxWebServer". The WAR file has not yet been packaged.
                    // Therefore, we let embedded Jetty serve the source webapp directory. This way, Servlet classes annotated with
                    // @WebServlet are not discovered automatically, we have do define them in WEB-INF/web.xml.
                    warBase = "src/main/webapp";
                    System.out.println("Executing unpackaged project. Content directory: " + warBase);
                }
            }

            WebAppContext handler = new WebAppContext();
            handler.setContextPath(warContext);
            handler.setWar(warBase);

            Server server = new Server(Integer.parseInt(httpPort));
            server.setHandler(handler);

            System.out.println("Starting embedded Jetty.");
            server.start();

            System.out.println("Running embedded Jetty at http://localhost:" + httpPort + warContext);
            System.out.println("[Press return or Ctrl+C to stop]");
            state.waitForStop();

            System.out.println("Stopping embedded Jetty.");
            server.stop();
            server.join();

        } catch(Exception e) {
            e.printStackTrace();

        } finally {
            System.out.println("Stopped.");
            state.signalDone();
        }
    }

    /**
     * Entry point for service shutdown.
     *
     * @param args not used.
     */
    public static void stop(String[] args) {
        // Note: calling System.exit(0) here results in errors in Apache Procrun.
        state.signalStopAndWait();
    }

    /**
     * Ensures clean shutdown if Ctrl+C is pressed or SIGTERM is received or if System.exit() is called.
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                state.signalStopAndWait();
            }
        });
    }

    private static final class State {
        private boolean stop = false;
        private boolean done = false;

        public synchronized void waitForStop() throws InterruptedException, IOException {
            // Wait until a line is available from the console or somebody signals us to stop.
            while(!stop && System.in.available() == 0) {
                wait(1000);
            }
            // Eat all the characters available from the console, or
            // otherwise they would spill out to the shell after we terminate.
            while(System.in.available() > 0) {
                System.in.read();
            }
        }

        public synchronized void signalStopAndWait() {
            try {
                stop = true;
                notifyAll();
                while(!done) {
                    wait(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("Interruppted.");
            }
        }

        public synchronized void signalDone() {
            done = true;
            notifyAll();
        }
    }
}
