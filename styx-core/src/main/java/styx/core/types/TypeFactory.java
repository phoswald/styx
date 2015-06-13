package styx.core.types;

import java.util.Objects;

import styx.Complex;
import styx.Pair;
import styx.Session;
import styx.StyxException;
import styx.Type;
import styx.Value;
import styx.core.TypeProvider;

public final class TypeFactory implements TypeProvider {

    @Override
    public Type type(Session session, Complex environment, Value definition) throws StyxException {
        return newType(Objects.requireNonNull(definition)).type();
    }

    private static AbstractType newType(Value value) throws StyxException {
        try {
            Pair<Value,Value> pair = value.asComplex().single();
            String key = pair.key().asText().toTextString();
            if(key.equals(Simple.TAG)) {
                return new Simple(pair.val().asComplex());
            }
            throw new StyxException("Unknown tag '" + key + "'.");
        } catch(RuntimeException | StyxException e) {
            throw new StyxException("Cannot decode type from: " + value + "\n" + e.getMessage(), e);
        }
    }
}
