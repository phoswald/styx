package styx.core.intrinsics;

import java.io.IOException;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Session;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.FuncRegistry;
import styx.core.expressions.Stack;

public class ConsoleIntrinsics {

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("read"), new CompiledFunction(registry, "console_read", Determinism.NON_DETERMINISTIC, 0) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try {
                            return stack.session().text(readLine());
                        } catch (IOException e) {
                            throw new StyxException("Error while reading from stdin.", e);
                        }
                    }
                }.function())
                .put(session.text("write"), new CompiledFunction(registry, "console_write", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) {
                        System.out.println(stack.getFrameValue(0).asText().toTextString());
                        return null;
                    }
                }.function());
    }

    public static String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while(true) {
            int b = System.in.read();
            if(b == -1 || b == '\n')
                break;
            if(b == '\r')
                continue;
            sb.append((char) b);
        }
        return sb.toString();
    }
}
