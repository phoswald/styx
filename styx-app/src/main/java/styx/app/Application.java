package styx.app;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import styx.Session;
import styx.SessionFactory;
import styx.SessionManager;
import styx.StyxException;
import styx.SystemConfiguration;
import styx.Value;
import styx.core.intrinsics.ConsoleIntrinsics;
import styx.core.intrinsics.FileIntrinsics;

public class Application {

    private static String sessionConfigFile  = System.getProperty("styx.app.session.config", "system.st");
    private static String sessionFactoryName = System.getProperty("styx.app.session.factory", "");

    public static void main(String args[]) {
        try {
            boolean      interactive = true;
            List<String> scripts     = new ArrayList<>();

            for(int i = 0; i < args.length; i++) {
                if(args[i].equals("-factory")) {
                    if(i + 1 >= args.length) {
                        printHelp();
                        return;
                    }
                    sessionFactoryName = args[++i];
                } else if(args[i].equals("-eval")) {
                    if(i + 1 >= args.length) {
                        printHelp();
                        return;
                    }
                    scripts.add(args[++i]);
                    interactive = false;
                } else if(args[i].equals("-file")) {
                    if(i + 1 >= args.length) {
                        printHelp();
                        return;
                    }
                    try(Reader stm = Files.newBufferedReader(Paths.get(args[++i]), StandardCharsets.UTF_8)) {
                        scripts.add(FileIntrinsics.readToEnd(stm));
                        interactive = false;
                    }
                } else if(args[i].equals("-interactive")) {
                    interactive = true;
                } else if(args[i].equals("-help")) {
                    printHelp();
                    interactive = false;
                } else {
                    printHelp();
                    return;
                }
            }

            SystemConfiguration.load(sessionConfigFile);
            System.out.println("Loaded STYX configuration from: " + sessionConfigFile);
            System.out.println("Using STYX session factory: " + sessionFactoryName);

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
        System.out.println("$ styx [-factory <name>] [-eval <expr>] [-file <file>] [-interactive] [-help]");
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
