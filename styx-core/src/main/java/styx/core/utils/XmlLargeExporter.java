package styx.core.utils;

import java.util.Deque;
import java.util.List;
import java.util.Objects;

import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;

public class XmlLargeExporter extends XmlExporter {

    private final int       limit;
    private final Reference swap;

    public XmlLargeExporter(Session session, int limit, Reference swap) {
        super(session);
        this.limit = limit;
        this.swap = Objects.requireNonNull(swap);
    }

    @Override
    protected Value collect(Deque<List<Value>> stack, List<Value> list) throws StyxException {
        if(list.contains(null)) {
            int level = stack.size() + 1;
            System.out.println("XmlExporter: collect("+ level+")");
            return session.read(session.root().child(session.text("swap")).child(session.number(level)));
        }
        return super.collect(stack, list);
    }

    @Override
    protected void append(Deque<List<Value>> stack, Value value) throws StyxException {
        List<Value> list = stack.getFirst();
        if(list.size() < limit) {
            super.append(stack, value);
        } else {
            int level = stack.size();
            Reference temp = swap.child(session.number(level));
            if(list.size() == limit) {
                System.out.println("XmlExporter: LIMIT REACHED ("+ level+")");
                session.write(temp, session.complex());
                int idx = 1;
                for(Value elem : list) {
                    System.out.println("XmlExporter: append("+ level+") " + idx);
                    session.write(temp.child(session.number(idx++)), elem);
                    list.set(idx-2, null);
                }
            }
            int idx = list.size() + 1;
            if(idx % limit == 0) {
                System.out.println("XmlExporter: append("+ level+") " + idx);
            }
            session.write(temp.child(session.number(idx)), value);
            list.add(null);
        }
    }
}
