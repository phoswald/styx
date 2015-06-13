package styx.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import styx.Pair;
import styx.Session;
import styx.Value;

public final class XmlExporter {

    public static Value importDocument(Session session, InputStream stm) {
        return null;
    }

    public static void exportDocument(Value val, OutputStream stm) throws IOException {
        XMLStreamWriter writer;
        try {
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stm);
            // writer.writeStartDocument();
            exportValue(val, writer);
            // writer.writeEndDocument();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            throw new IOException(e);
        }
    }

    private static void exportValue(Value val, XMLStreamWriter writer) throws XMLStreamException {
        if(val != null) {
            if(val.isComplex() && val.asComplex().hasSingle()) {
                Pair<Value, Value> pair = val.asComplex().single();
                String tag = pair.key().asText().toTextString();
                Value cont = pair.val();
                if(tag.startsWith("#")) {
                    if(cont.isText()) {
                        writer.writeAttribute(tag.substring(1), cont.asText().toTextString());
                    }
                } else {
                    writer.writeStartElement(tag);
                    if(cont.isComplex()) {
                        for(Pair<Value, Value> sub : cont.asComplex()) {
                            exportValue(sub.val(), writer);
                        }
                    } else if(cont.isText()) {
                        exportValue(cont, writer);
                    }
                    writer.writeEndElement();
                }
            } else if(val.isText()) {
                writer.writeCharacters(val.asText().toTextString());
            }
        }
    }
}
