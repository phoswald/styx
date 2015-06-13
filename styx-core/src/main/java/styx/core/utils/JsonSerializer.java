package styx.core.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import styx.Complex;
import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * Serializes and deserializes arbitrary UDM values to and from JSON.
 * <p>
 * The JSON serializer uses the following canonical JSON format to exactly represent any UDM value:
 * <ul>
 * <li> null values: {@code {"@null":""}}, only valid at top level
 * <li> values of type text: {@code {"@text":"..."}}, if at top level.
 * <li> values of type reference: {@code {"@ref":[...]}}.
 * <li> values of type complex: {@code {"...":...,"...":...}}  if all keys are textual values,<br>
 *      or [{"@key":...,"@val":...},{"@key":...,"@val":...}] if at least one key has a non-texual value.<br>
 *      If the key contains the character '@', it has to be prefixed by '@:'.
 * <li> values of type type: {@code {"@type":...}}.
 * <li> values of type function: {@code {"@func":...}}.
 * </ul>
 */
public final class JsonSerializer {

    private static final JsonGeneratorFactory generatorDefault = Json.createGeneratorFactory(null);
    private static final JsonGeneratorFactory generatorIndent  = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, "1"));

    /**
     * Serializes an arbitrary UDM value into an OutputStream.
     * @param val the value, can be null.
     * @param stm the OutputStream, receives an JSON document encoded as UTF-8.
     * @param indent true if the output shall be formatted prettily.
     * @throws StyxException if an error occurs.
     * @throws NullPointerException if the given stream is null.
     */
    public static void serialize(Value val, OutputStream stm, boolean indent) throws StyxException {
        // TODO: The UTF-8 BOM is not supported by java.lang.*, java.io.* et al
        //       This would be very useful on systems with a character set other than UTF-8!
        // stm.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        Objects.requireNonNull(stm);
        try(JsonGenerator generator = (indent ? generatorIndent : generatorDefault).createGenerator(stm)) {
            serialize(val, generator);
        } catch (RuntimeException e) {
            throw new StyxException("Failed to serialize as JSON.", e);
        }
    }

    /**
     * Serializes an arbitrary UDM value into a Writer.
     * @param val the value, can be null.
     * @param stm the Writer, receives a JSON document.
     * @param indent true if the output shall be formatted prettily.
     * @throws StyxException if an error occurs.
     * @throws NullPointerException if the given writer is null.
     */
    public static void serialize(Value val, Writer stm, boolean indent) throws StyxException {
        Objects.requireNonNull(stm);
        try(JsonGenerator generator = (indent ? generatorIndent : generatorDefault).createGenerator(stm)) {
            serialize(val, generator);
        } catch (RuntimeException e) {
            throw new StyxException("Failed to serialize as JSON.", e);
        }
    }

    private static void serialize(Value val, JsonGenerator generator) {
        if(val == null) {
            generator.writeStartObject();
            generator.write("@null", "");
            generator.writeEnd();
        } else if(val.isText()) {
            generator.writeStartObject();
            serializeValue("@text", val, generator);
            generator.writeEnd();
        } else {
            serializeValue(null, val, generator);
        }
    }

    private static void serializeValue(String name, Value val, JsonGenerator generator) {
        if(val.isText()) {
            if(name != null) {
                generator.write(name, val.asText().toTextString());
            } else {
                generator.write(val.asText().toTextString());
            }
        } else if(val.isReference()) {
            writeStartObject(generator, name);
            generator.writeStartArray("@ref");
            Reference ref = val.asReference();
            int level = ref.level();
            for(int i = 1; i <= level; i++) {
                serializeValue(null, ref.parent(i).name(), generator);
            }
            generator.writeEnd();
            generator.writeEnd();
         } else if(val.isComplex()) {
            Complex complex = val.asComplex();
            if(hasTextKeys(complex)) {
                writeStartObject(generator, name);
                for(Pair<Value, Value> pair : complex) {
                    String key = pair.key().asText().toTextString();
                    serializeValue(key.indexOf('@') != -1 ? "@:" + key : key, pair.val(), generator);
                }
                generator.writeEnd();
            } else {
                writeStartArray(generator, name);
                for(Pair<Value, Value> pair : complex) {
                    generator.writeStartObject();
                    serializeValue("@key", pair.key(), generator);
                    serializeValue("@val", pair.val(), generator);
                    generator.writeEnd();
                }
                generator.writeEnd();
            }
       } else if(val.isType()) {
            writeStartObject(generator, name);
            serializeValue("@type", val.asType().definition(), generator);
            generator.writeEnd();
        } else if(val.isFunction()) {
            writeStartObject(generator, name);
            serializeValue("@func", val.asFunction().definition(), generator);
            generator.writeEnd();
        }
    }

    private static void writeStartObject(JsonGenerator generator, String name) {
        if(name != null) {
            generator.writeStartObject(name);
        } else {
            generator.writeStartObject();
        }
    }

    private static void writeStartArray(JsonGenerator generator, String name) {
        if(name != null) {
            generator.writeStartArray(name);
        } else {
            generator.writeStartArray();
        }
    }

    private static boolean hasTextKeys(Complex complex) {
        for(Pair<Value, Value> pair : complex) {
            if(!pair.key().isText()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deserializes an arbitrary UDM value from an InputStream.
     * @param session the session to be used to create values.
     * @param stm the InputStream, must contain a JSON document.
     * @return the deserialized UDM value, can be null.
     * @throws StyxException if an error occurs.
     * @throws NullPointerException if the given session or stream is null.
     */
    public static Value deserialize(Session session, InputStream stm) throws StyxException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try(JsonParser parser = Json.createParser(stm)) {
            return deserialize(session, parser);
        } catch (RuntimeException | StyxException e) {
            throw new StyxException("Failed to deserialize from JSON.", e);
        }
    }

    /**
     * Deserializes an arbitrary UDM value from a Reader.
     * @param session the session to be used to create values.
     * @param stm the Reader, must contain a JSON document.
     * @return the deserialized UDM value, can be null.
     * @throws StyxException if an error occurs.
     * @throws NullPointerException if the given session or reader is null.
     */
    public static Value deserialize(Session session, Reader stm) throws StyxException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try(JsonParser parser = Json.createParser(stm)) {
            return deserialize(session, parser);
        } catch (RuntimeException | StyxException e) {
            throw new StyxException("Failed to deserialize from JSON.", e);
        }
    }

    private static Value deserialize(final Session session, JsonParser parser) throws StyxException {
        Scope scope = new Scope(null);
        while(parser.hasNext()) {
            Event ev = parser.next();
            switch(ev) {
                case START_OBJECT:
                    scope = scope.onStartObject();
                    break;
                case START_ARRAY:
                    scope = scope.onStartArray();
                    break;
                case KEY_NAME:
                    scope = scope.onName(session, parser.getString());
                    break;
                case VALUE_NULL:
                case VALUE_FALSE:
                case VALUE_TRUE:
                case VALUE_NUMBER:
                case VALUE_STRING:
                    scope = scope.onValue(session, parser.getString());
                    break;
                case END_OBJECT:
                case END_ARRAY:
                    scope = scope.onEnd(session);
                    break;
            }
        }
        return scope.get(session);
    }

    private static class Scope {
        protected final Scope parent;
        protected Value val;

        Scope(Scope parent) {
            this.parent = parent;
        }

        Scope onStartObject() throws StyxException {
            return new ScopeObject(this); // nest scope
        }

        Scope onStartArray() throws StyxException {
            return new ScopeArray(this); // nest scope
        }

        Scope onName(Session session, String name) throws StyxException {
            return this; // should not occur, overridden in ScopeObject
        }

        Scope onValue(Session session, String val) throws StyxException {
            add(session.text(val));
            return this;
        }

        Scope onEnd(Session session) throws StyxException {
            parent.add(get(session));
            return parent; // un-nest
        }

        void add(Value val) throws StyxException {
            this.val = val; // overridden in ScopeObject, ScopeArray, ScopeReference
        }

        Value get(Session session) throws StyxException {
            return val;
        }
    }

    private static class ScopeObject extends Scope {
        private final List<Pair<Value,Value>> pairs = new ArrayList<Pair<Value,Value>>();
        private Value key;

        ScopeObject(Scope parent) { super(parent); }

        @Override
        Scope onName(Session session, String name) throws StyxException {
            if(name.equals("@null")) {
                if(parent.parent != null) {
                    throw new StyxException("@null is not allowed below root.");
                }
                return new Scope(parent) { // replace scope: restrict to empty textual value
                    @Override void add(Value val) throws StyxException {
                        if(val == null || !val.isText() || !val.asText().toTextString().isEmpty()) {
                            throw new StyxException("Empty value expected after '@null'.");
                        }
                    }

                    @Override Value get(Session session) {
                        return null;
                    }
                };
            }
            if(name.equals("@text")) {
                if(parent.parent != null) {
                    throw new StyxException("@text is not allowed below root.");
                }
                return new Scope(parent) { // replace scope: restrict to textual value
                    @Override void add(Value val) throws StyxException {
                        if(val == null || !val.isText()) {
                            throw new StyxException("Texual value expected after '@text'.");
                        }
                        super.add(val);
                    }
                };
            }
            if(name.equals("@ref")) {
                return new Scope(parent) { // replace scope: restrict to array, using ScopeReference
                    @Override Scope onStartObject() throws StyxException {
                        throw new StyxException("Array expected after '@ref'.");
                    }

                    @Override Scope onValue(Session session, String val) throws StyxException {
                        throw new StyxException("Array expected after '@ref'.");
                    }

                    @Override Scope onStartArray() throws StyxException {
                        return new ScopeReference(this);
                    }
                };
            }
            if(name.equals("@type")) {
                return new Scope(parent) { // replace scope: not complex but type
                    @Override Value get(Session session) throws StyxException {
                        return session.type(val);
                    }
                };
            }
            if(name.equals("@func")) {
                return new Scope(parent) { // replace scope: not complex but function
                    @Override Value get(Session session) throws StyxException {
                        return session.function(val);
                    }
                };
            }
            if(name.startsWith("@:")) {
                name = name.substring(2);
            }
            this.key = session.text(name);
            return this;
        }

        @Override void add(Value val) throws StyxException {
            pairs.add(new Pair<Value,Value>(key, val));
        }

        @Override Value get(Session session) {
            return session.complex().putAll(pairs);
        }
    };

    private static class ScopeArray extends Scope {
        private final List<Pair<Value,Value>> pairs = new ArrayList<Pair<Value,Value>>();
        private Value pairkey;
        private Value pairval;

        ScopeArray(Scope parent) { super(parent); }

        @Override
        Scope onStartObject() throws StyxException {
            return new ScopeObject(this) { // nest scope
                private String name;

                @Override Scope onName(Session session, String name) throws StyxException {
                    this.name = name;
                    return this;
                }

                @Override void add(Value val) throws StyxException {
                    switch(name) {
                        case "@key":
                            pairkey = val;
                            break;
                        case "@val":
                            pairval = val;
                            break;
                        default:
                            throw new StyxException("Only names '@key' and '@val' are accepted in array.");
                    }
                }
            };
        }

        @Override void add(Value val) throws StyxException {
            if(pairkey == null || pairval == null) {
                throw new StyxException("Names '@key' and '@val' are required in array.");
            }
            pairs.add(new Pair<Value,Value>(pairkey, pairval));
        }

        @Override Value get(Session session) {
            return session.complex().putAll(pairs);
        }
    }

    private static class ScopeReference extends Scope {
        private final List<Value> parts = new ArrayList<Value>();

        ScopeReference(Scope parent) { super(parent); }

        @Override void add(Value val) throws StyxException {
            parts.add(val);
        }

        @Override Value get(Session session) {
            Reference result = session.root();
            for(Value part : parts) {
                result = result.child(part);
            }
            return result;
        }
    }
}