package styx.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import styx.Pair;
import styx.Session;
import styx.Value;

public final class XmlExporter {

    private static final XMLOutputFactory OUTPUT_FACTORY = XMLOutputFactory.newFactory();
    private static final XMLInputFactory INPUT_FACTORY = XMLInputFactory.newFactory();

    private static final byte[] BOM = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    public static void exportDocument(Value val, OutputStream stm, boolean indent) throws IOException {
        try {
            stm.write(BOM);
            exportValue(val, OUTPUT_FACTORY.createXMLStreamWriter(stm), indent);
        } catch (XMLStreamException e) {
            throw new IOException("Failed to export as XML.", e);
        }
    }

    public static void exportDocument(Value val, Writer stm, boolean indent) throws IOException {
        try {
            exportValue(val, OUTPUT_FACTORY.createXMLStreamWriter(stm), indent);
        } catch (XMLStreamException e) {
            throw new IOException("Failed to export as XML.", e);
        }
    }

    private static void exportValue(Value val, XMLStreamWriter writer, boolean indent /* XXX */) throws XMLStreamException {
        if(val.isComplex()) {
            for(Pair<Value, Value> pair : val.asComplex()) {
                if(pair.key().isNumber()) {
                    exportValue(pair.val(), writer, indent);
                } else if(pair.key().isText()) {
                    String tag = pair.key().asText().toTextString();
                    if(tag.startsWith("#")) {
                        if(pair.val().isText()) {
                            writer.writeAttribute(tag.substring(1), pair.val().asText().toTextString());
                        } else {
                            throw new IllegalArgumentException("Cannot export as XML: Attribute values must be text.");
                        }
                    } else {
                        writer.writeStartElement(tag);
                        exportValue(pair.val(), writer, indent);
                        writer.writeEndElement();
                    }
                } else {
                    throw new IllegalArgumentException("Cannot export as XML: Keys must be numbers or tag or attributes names.");
                }
            }
        } else if(val.isText()) {
            writer.writeCharacters(val.asText().toTextString());
        } else {
            throw new IllegalArgumentException("Cannot export as XML: Values must be text or complex.");
        }
    }

    public static Value importDocument(Session session, InputStream stm) throws IOException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try {
            return importValue(session, INPUT_FACTORY.createXMLStreamReader(stm));
        } catch (XMLStreamException e) {
            throw new IOException("Failed to import from XML document.");
        }
    }

    public static Value importDocument(Session session, Reader stm) throws IOException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try {
            return importValue(session, INPUT_FACTORY.createXMLStreamReader(stm));
        } catch (XMLStreamException e) {
            throw new IOException("Failed to import from XML document.");
        }
    }

    private static Value importValue(Session session, XMLStreamReader reader) throws XMLStreamException {
        Deque<List<Value>> stack = new ArrayDeque<>();
        stack.addFirst(new ArrayList<>());
        while(reader.hasNext()) {
            reader.next();
            if(reader.isStartElement()) {
                stack.addFirst(new ArrayList<>());
                int numAttr = reader.getAttributeCount();
                for(int idxAttr = 0; idxAttr < numAttr; idxAttr++) {
                    String name = reader.getAttributeLocalName(idxAttr);
                    String text = reader.getAttributeValue(idxAttr);
                    stack.getFirst().add(session.complex(session.text("#" + name), session.text(text)));
                }
            } else if(reader.isCharacters()) {
                String text = reader.getText();
                stack.getFirst().add(session.text(text));
            } else if(reader.isEndElement()) {
                String name = reader.getLocalName();
                Value value = collect(session, stack.removeFirst());
                stack.getFirst().add(session.complex(session.text(name), value));
            }
        }
        return collect(session, stack.getFirst());
    }

    private static Value collect(Session session, List<Value> values) {
        if(values.size() == 1 && values.get(0).isText()) {
            return values.get(0);
        } else {
            return session.complex().addAll(values);
        }
    }
}
