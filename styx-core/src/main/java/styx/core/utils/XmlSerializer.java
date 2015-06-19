package styx.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * Serializes and deserializes arbitrary STYX values to and from XML.
 * <p>
 * The XML serializer uses the following canonical XML format to exactly represent any STYX value:
 * <ul>
 * <li> null values: {@code <null/>}, only valid at top level.
 * <li> values of type text: {@code <text>...</text>}.
 * <li> values of type reference: {@code <reference>...</reference>},
 *      with a nested text, complex, reference, type or function element for each part of the reference.
 * <li> values of type complex: {@code <complex>...</complex>},
 *      with a nested text, complex, reference, type or function element for each child of the complex value. The key of a child is represented as follows:
 *      <ul>
 *      <li>for children with a textual key, a key attribute is added to the child: {@code <complex> <text key='...'>...</text> </complex>}.
 *      <li>for children with a non-textual key, a key element is added to the child: {@code <complex> <text> <key>...</key>...</text> </complex>}.
 *      <li>if a non-textual key has a textual value, the value has to be wrapped into a value element: {@code <complex> <text> <key>...</key> <value>...</value> </text> </complex>}.
 *      </ul>
 * <li> values of type type: {@code <type>...</type>}.
 * <li> values of type function: {@code <function>...</function>}.
 * </ul>
 */
public final class XmlSerializer {

    private static final XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newFactory();
    private static final XMLInputFactory INPUT_FACTORY = XMLInputFactory.newFactory();

    private static final byte[] BOM = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    /**
     * Serializes an arbitrary UDM value into an OutputStream.
     * @param val the value, can be null.
     * @param stm the OutputStream, receives an XML document encoded as UTF-8.
     * @param indent true if the output shall be formatted prettily.
     * @throws StyxException if an XML or IO related error occurs.
     */
    public static void serialize(Value val, OutputStream stm, boolean indent) throws StyxException {
        Objects.requireNonNull(stm);
        try {
            stm.write(BOM);
            serialize(val, OUTPUT_FACTORY.createXMLStreamWriter(stm, StandardCharsets.UTF_8.name()), indent, "utf-8");
        } catch (XMLStreamException | IOException e) {
            throw new StyxException("Failed to serialize as XML.", e);
        }
    }

    /**
     * Serializes an arbitrary UDM value into a Writer.
     * @param val the value, can be null.
     * @param stm the Writer, receives an XML document with no encoding.
     * @param indent true if the output shall be formatted prettily.
     * @throws StyxException if an XML or IO related error occurs.
     */
    public static void serialize(Value val, Writer stm, boolean indent) throws StyxException {
        Objects.requireNonNull(stm);
        try {
            serialize(val, OUTPUT_FACTORY.createXMLStreamWriter(stm), indent, null);
        } catch (XMLStreamException e) {
            throw new StyxException("Failed to serialize as XML.", e);
        }
    }

    private static void serialize(Value val, XMLStreamWriter writer, boolean indent, String encoding) throws XMLStreamException {
        if(encoding != null) {
            writer.writeStartDocument(encoding, "1.0");
        } else {
            writer.writeStartDocument();
        }
        if(val != null) {
            serializeValue(null, val, writer, indent ? 0 : -1);
        } else {
            writer.writeEmptyElement("null");
        }
        writer.writeEndDocument();
    }

    private static void serializeValue(Value key, Value val, XMLStreamWriter writer, int indent) throws XMLStreamException {
        if(val.isText()) {
            writeIndent(writer, indent);
            writer.writeStartElement("text");
            serializeKey(key, writer, incIndent(indent));
            if(key == null || key.isText()) {
                writer.writeCharacters(val.asText().toTextString());
            } else {
                writeIndent(writer, incIndent(indent));
                writer.writeStartElement("value");
                writer.writeCharacters(val.asText().toTextString());
                writer.writeEndElement();
                writeIndent(writer, indent);
            }
            writer.writeEndElement();
        } else if(val.isReference()) {
            writeIndent(writer, indent);
            writer.writeStartElement("reference");
            serializeKey(key, writer, incIndent(indent));
            Reference ref = val.asReference();
            int level = ref.level();
            for(int i = 1; i <= level; i++) {
                serializeValue(null, ref.parent(i).name(), writer, incIndent(indent));
            }
            writeIndent(writer, indent);
            writer.writeEndElement();
        } else if(val.isComplex()) {
            writeIndent(writer, indent);
            writer.writeStartElement("complex");
            serializeKey(key, writer, incIndent(indent));
            for(Pair<Value, Value> pair : val.asComplex()) {
                serializeValue(pair.key(), pair.val(), writer, incIndent(indent));
            }
            writeIndent(writer, indent);
            writer.writeEndElement();
        } else if(val.isType()) {
            writeIndent(writer, indent);
            writer.writeStartElement("type");
            serializeKey(key, writer, incIndent(indent));
            serializeValue(null, val.asType().definition(), writer, incIndent(indent));
            writeIndent(writer, indent);
            writer.writeEndElement();
        } else if(val.isFunction()) {
            writeIndent(writer, indent);
            writer.writeStartElement("function");
            serializeKey(key, writer, incIndent(indent));
            serializeValue(null, val.asFunction().definition(), writer, incIndent(indent));
            writeIndent(writer, indent);
            writer.writeEndElement();
        }
    }

    private static void serializeKey(Value key, XMLStreamWriter writer, int indent) throws XMLStreamException {
        if(key != null) {
            if(key.isText()) {
                writer.writeAttribute("key", key.asText().toTextString());
            } else {
                writeIndent(writer, indent);
                writer.writeStartElement("key");
                serializeValue(null, key, writer, incIndent(indent));
                writeIndent(writer, indent);
                writer.writeEndElement();
            }
        }
    }

    private static void writeIndent(XMLStreamWriter writer, int indent) throws XMLStreamException {
        if(indent >= 0) {
            writer.writeCharacters("\n");
            for(int i = 0; i < indent; i++) {
                writer.writeCharacters("    ");
            }
        }
    }

    private static int incIndent(int indent) {
        return indent == -1 ? -1 : indent + 1;
    }

    /**
     * Deserializes an arbitrary UDM value from an InputStream.
     * @param session the session to be used to create values.
     * @param stm the InputStream, must contain an XML document.
     * @return the deserialized UDM value, can be null.
     * @throws StyxException if an XML or IO related error occurs, including format violations.
     */
    public static Value deserialize(Session session, InputStream stm) throws StyxException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try {
            return deserialize(session, INPUT_FACTORY.createXMLStreamReader(stm));
        } catch (XMLStreamException | StyxException e) {
            throw new StyxException("Failed to deserialize from XML.", e);
        }
    }

    /**
     * Deserializes an arbitrary UDM value from a Reader.
     * @param session the session to be used to create values.
     * @param stm the Reader, must contain an XML document.
     * @return the deserialized UDM value, can be null.
     * @throws StyxException if an XML or IO related error occurs, including format violations.
     */
    public static Value deserialize(Session session, Reader stm) throws StyxException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try {
            return deserialize(session, INPUT_FACTORY.createXMLStreamReader(stm));
        } catch (XMLStreamException | StyxException e) {
            throw new StyxException("Failed to deserialize from XML.", e);
        }
    }

    private static Value deserialize(Session session, XMLStreamReader reader) throws XMLStreamException, StyxException {
        Scope scope = new Scope(null);
        while(reader.hasNext()) {
            reader.next();
            if(reader.isStartElement()) {
                scope = scope.onStartElement(session, reader.getLocalName(), reader);
            } else if(reader.isCharacters()) {
                scope = scope.onCharacters(session, reader.getText());
            } else if(reader.isEndElement()) {
                scope = scope.onEndElement(session, reader.getLocalName());
            }
        }
        return scope.get(session);
    }

    private static class Scope {
        protected final Scope parent;
        protected Value key;
        protected Value val;

        Scope(Scope parent) {
            this.parent = parent;
        }

        Scope onStartElement(Session session, String name, XMLStreamReader reader) throws StyxException {
            for(int i = 0; i < reader.getAttributeCount(); i++) {
                if(reader.getAttributeLocalName(i).equals("key")) {
                    key = session.text(reader.getAttributeValue(i));
                } else {
                    throw new StyxException("Invalid attribute name '" + reader.getAttributeLocalName(i) + "'.");
                }
            }
            switch(name) {
                case "null":
                    return new Scope(this);
                case "key":
                    return new Scope(this) {
                        @Override Scope onEndElement(Session session, String name) throws StyxException {
                            if(parent.parent == null) {
                                throw new StyxException("Invalid location of element 'key'.");
                            }
                            parent.parent.key = get(session);
                            return parent;
                        }
                    };
                case "text":
                    return new ScopeLeaf(this);
                case "value":
                    return new ScopeLeaf(this);
                case "reference":
                    return new Scope(this) {
                        private final List<Value> parts = new ArrayList<Value>();

                        @Override void add(Value val) {
                            parts.add(val);
                        }

                        @Override Value get(Session session) {
                            Reference result = session.root();
                            for(Value part : parts) {
                                result = result.child(part);
                            }
                            return result;
                        }
                    };
                case "complex":
                    return new Scope(this) {
                        private final List<Pair<Value,Value>> pairs = new ArrayList<Pair<Value,Value>>();

                        @Override void add(Value val) throws StyxException {
                            if(key == null) {
                                throw new StyxException("Child of element 'complex' has no attribute or element 'key'.");
                            }
                            pairs.add(new Pair<Value, Value>(key, val));
                        }

                        @Override Value get(Session session) {
                            return session.complex().putAll(pairs);
                        }
                    };
                case "type":
                   return new Scope(this) {
                        @Override Value get(Session session) throws StyxException {
                            if(val == null) {
                                throw new StyxException("Child of element 'type' missing.");
                            }
                            return session.type(val);
                        }
                    };
                case "function":
                    return new Scope(this) {
                        @Override Value get(Session session) throws StyxException {
                            if(val == null) {
                                throw new StyxException("Child of element 'function' missing.");
                            }
                            return session.function(val);
                        }
                    };
                default:
                    throw new StyxException("Invalid element name '" + reader.getLocalName() + "'.");
            }
        }

        Scope onCharacters(Session session, String text) {
            return this; // ignored, overridden for <text>
        }

        Scope onEndElement(Session session, String name) throws StyxException {
            parent.add(get(session));
            return parent; // un-nest
        }

        void add(Value val) throws StyxException {
            this.val = val;
        }

        Value get(Session session) throws StyxException {
            return val;
        }
    }

    private static class ScopeLeaf extends Scope {
        private final StringBuilder text = new StringBuilder();

        ScopeLeaf(Scope parent) { super(parent); }

        @Override Scope onCharacters(Session session, String text) {
            this.text.append(text);
            return this;
        }

        @Override Value get(Session session) {
            if(val != null) {
                return val; // ignore onCharacters(), shadowed by child
            } else {
                return session.text(text.toString());
            }
        }
    };
}