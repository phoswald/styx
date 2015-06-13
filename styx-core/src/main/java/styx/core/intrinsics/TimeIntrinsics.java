package styx.core.intrinsics;

import java.util.Calendar;
import java.util.GregorianCalendar;

import styx.Complex;
import styx.Determinism;
import styx.StyxException;
import styx.Session;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.FuncRegistry;
import styx.core.expressions.Stack;

public class TimeIntrinsics {

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("current_millis"), new CompiledFunction(registry, "time_current_millis", Determinism.NON_DETERMINISTIC, 0) {
                    @Override
                    public Value invoke(Stack stack) {
                        return stack.session().number(System.currentTimeMillis());
                    }
                }.function())
                .put(session.text("current_text"), new CompiledFunction(registry, "time_current_text", Determinism.NON_DETERMINISTIC, 0) {
                    @Override
                    public Value invoke(Stack stack) {
                        GregorianCalendar cal = new GregorianCalendar();
                        return stack.session().text(formatCalendar(cal));
                    }
                }.function())
                .put(session.text("text_from_millis"), new CompiledFunction(registry, "time_text_from_millis", Determinism.PURE, 1) {
                    @Override
                    public Value invoke(Stack stack) {
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTimeInMillis(stack.getFrameValue(0).asNumber().toLong());
                        return stack.session().text(formatCalendar(cal));
                    }
                }.function());
    }

    public static String formatCalendar(Calendar cal) {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND));
    }
}
