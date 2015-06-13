package styx.app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.SystemConfiguration;
import styx.Value;
import styx.core.intrinsics.ConsoleIntrinsics;

public class Application {

    private static String sessionConfigFile  = System.getProperty("styx.app.session.config", "system.styx");
    private static String sessionFactoryName = System.getProperty("styx.app.session.factory");

    public static void main(String args[]) {
        try {
            boolean      interactive = true;
            List<String> scripts     = new ArrayList<>();

            for(String arg : args) {
                if(arg.startsWith("-factory=")) {
                    sessionFactoryName = arg.substring(9);
                } else if(arg.startsWith("-eval=")) {
                    scripts.add(arg.substring(6));
                    interactive = false;
                } else if(arg.startsWith("-file=")) {
                    try(Reader stm = new InputStreamReader(new FileInputStream(arg.substring(6)), Charset.forName("UTF-8"))) {
                        StringBuilder sb = new StringBuilder();
                        int c;
                        while((c = stm.read()) != -1) {
                            sb.append((char) c);
                        }
                        scripts.add(sb.toString());
                        interactive = false;
                    }
                } else if(arg.equals("-interactive")) {
                    interactive = true;
                } else if(arg.equals("-help")) {
                    printHelp();
                    interactive = false;
                } else {
                    System.out.println("ERROR: invalid command line argument '"+ arg + "'.");
                }
            }

            SystemConfiguration.load(sessionConfigFile);
            System.out.println("Loaded STYX configuration from: " + sessionConfigFile);
            System.out.println("Using STYX session factory: " + (sessionFactoryName == null || sessionFactoryName.length() == 0 ? "<default>" : sessionFactoryName));

            SessionFactory factory = SessionManager.lookupSessionFactory(sessionFactoryName);
            for(String script : scripts) {
                eval(factory, script);
            }
            if(interactive) {
                shell(factory);
            }

        } catch(RuntimeException | StyxException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("STYX Interpreter - (c) 2015 Philip Oswald");
        System.out.println();
        System.out.println("Syntax:");
        System.out.println("$ styx [-factory=...] [-eval=...] [-file=...] [-interactive] [-help]");
        System.out.println();
    }

    private static void eval(SessionFactory factory, String script) throws StyxException {
        try(Session session = factory.createSession()) {
            session.evaluate(script);
        }
    }

    private static void shell(SessionFactory factory) throws StyxException, IOException {
        try(Session session = factory.createSession()) {
            System.out.println("[Enter expressions to evaluate or an empty line to quit]");
            while(true) {
                System.out.print("STYX> ");
                String input = ConsoleIntrinsics.readLine();
                if(input.length() == 0)
                    break;
                evaluate(session, input);
            }
        }
    }

    private static void evaluate(Session session, String script) {
        try {
            Value result = session.parse(script, false);
            System.out.println("OK (parsed):      " + formatFunction(session, result));
        } catch(StyxException e) {
            System.out.println("ERROR (parse):    " + formatString(e.getMessage()) + '\n');
            e.printStackTrace();
            return;
        }
        try {
            Value result = session.parse(script, true);
            System.out.println("OK (compiled):    " + formatFunction(session, result));
        } catch(StyxException e) {
            System.out.println("ERROR (compile):  " + formatString(e.getMessage()) + '\n');
            e.printStackTrace();
            return;
        }
        try {
            Value result = session.evaluate(script);
            System.out.println("OK (evaluated):   " + formatValue(session, result));
        } catch(StyxException e) {
            System.out.println("ERROR (evaluate): " + formatString(e.getMessage()) + '\n');
            e.printStackTrace();
            return;
        }
    }

    private static String formatFunction(Session session, Value function) throws StyxException {
        // Function definition has the form @Function [ args: [], body: @Batch [...] ]
        Value code = function.asFunction().definition().asComplex().single().val().asComplex().get(session.text("body"));
        return formatValue(session, code);
    }

    private static String formatValue(Session session, Value value) throws StyxException {
        return formatString(session.serialize(value, true));
    }

    private static String formatString(String str) {
        return str == null ? "<null>" : str.replace("\n", "\n                  ");
    }
}
