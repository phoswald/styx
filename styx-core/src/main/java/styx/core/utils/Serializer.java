package styx.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import styx.Complex;
import styx.Function;
import styx.Pair;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Text;
import styx.Type;
import styx.Value;

public final class Serializer {

    private static final int BOM = 0xFEFF;

    public static String serialize(Value val, boolean indent) throws StyxException {
        StringWriter stm = new StringWriter();
        serialize(val, stm, indent, false);
        return stm.toString();
    }

    public static void serialize(Value val, Path file, boolean indent) throws StyxException {
        Objects.requireNonNull(file);
        try(Writer stm = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            serialize(val, stm, indent, true);
        } catch(IOException e) {
            throw new StyxException("Cannot open file for writing.", e);
        }
    }

    /**
     * Serializes an arbitrary STYX value into an OutputStream.
     * @param val the value, can be null.
     * @param stm the OutputStream, receives an STYX text document encoded as UTF-8.
     * @param indent true if the output shall be formatted prettily.
     * @throws StyxException IO related error occurs.
     */
    public static void serialize(Value val, OutputStream stm, boolean indent) throws StyxException {
        Objects.requireNonNull(stm);
        serialize(val, new BufferedWriter(new OutputStreamWriter(stm, StandardCharsets.UTF_8)), indent, true);
    }

    /**
     * Serializes an arbitrary STYX value into a Writer.
     * @param val the value, can be null.
     * @param stm the Writer, receives an STYX text document.
     * @param indent true if the output shall be formatted prettily.
     * @throws StyxException IO related error occurs.
     */
    public static void serialize(Value val, Writer stm, boolean indent) throws StyxException {
        Objects.requireNonNull(stm);
        serialize(val, stm, indent, false);
    }

    private static void serialize(Value val, Writer stm, boolean indent, boolean bom) throws StyxException {
        try {
            if(bom) {
                stm.write(BOM);
            }
            if(val != null) {
                serializeValue(val, stm, indent ? 0 : -1, indent ? 4 : 0);
                stm.flush();
            }
        } catch(RuntimeException | IOException e) {
            throw new StyxException("Failed to serialize.", e);
        }
    }

    public static Value deserialize(Session session, String str) throws StyxException {
        return deserialize(session, new StringReader(str == null ? "" : str));
    }

    public static Value deserialize(Session session, Path file) throws StyxException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(file);
        try(Reader stm = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return deserialize(session, stm);
        } catch (IOException e) {
            throw new StyxException("Cannot open file for reading.", e);
        }
    }

    /**
     * Deserializes an arbitrary STYX value from an InputStream.
     * @param session the session to be used to create values.
     * @param stm the InputStream, must contain an STYX text document.
     * @return the deserialized STYX value, can be null.
     * @throws StyxException IO related error occurs, including format violations.
     */
    public static Value deserialize(Session session, InputStream stm) throws StyxException {
        Objects.requireNonNull(stm);
        return deserialize(session, new BufferedReader(new InputStreamReader(stm, StandardCharsets.UTF_8)));
    }

    /**
     * Deserializes an arbitrary STYX value from a Reader.
     * @param session the session to be used to create values.
     * @param stm the Reader, must contain an STYX text document.
     * @return the deserialized STYX value, can be null.
     * @throws StyxException IO related error occurs, including format violations.
     */
    public static Value deserialize(Session session, Reader stm) throws StyxException {
        Objects.requireNonNull(session);
        Objects.requireNonNull(stm);
        try {
            LineReader stm2 = new LineReader(stm); // This also creates a BufferedReader (if necessary)
            if(stm2.read() != BOM) {
                stm2.rewind(1);
            }
            int c = skipWhite(stm2, true);
            stm2.rewind(1);
            Value val = deserializeValue(session, stm2);
            c = skipWhite(stm2, true);
            if(c != -1) {
                throw new StyxException(buildMessage(stm2, "End of input expected after value."));
            }
            return val;
        } catch(RuntimeException | IOException | StyxException e) {
            throw new StyxException("Failed to deserialize.", e);
        }
    }

    private static void serializeValue(Value val, Writer stm, int curIndent, int deltaIndent) throws IOException {
        if(val.isText()) {
            serializeText(val.asText(), stm);
        } else if(val.isReference()) {
            serializeReference(val.asReference(), stm, curIndent);
        } else if(val.isComplex()) {
            serializeComplex(val.asComplex(), stm, curIndent, deltaIndent);
        } else if(val.isType()) {
            serializeType(val.asType(), stm, curIndent, deltaIndent);
        } else if(val.isFunction()) {
            serializeFunction(val.asFunction(), stm, curIndent, deltaIndent);
        }
    }

    private static Value deserializeValue(Session session, LineReader stm) throws IOException, StyxException {
        int c = skipWhite(stm, false);
        if(c == -1) {
            return null;
        }
        if(isDigit(c)) {
            stm.rewind(1);
            return deserializeTextNumberOrBinary(session, stm);
        } else if(isLetter(c)) {
            stm.rewind(1);
            return deserializeTextUnquoted(session, stm);
        } else if(c == '"') {
            stm.rewind(1);
            return deserializeTextQuoted(session, stm);
        } else if(c == '[') {
            c = stm.read();
            if(c == '/') {
                stm.rewind(2);
                return deserializeReference(session, stm);
            } else {
                stm.rewind(2);
                return deserializeComplex(session, stm);
            }
        } else if(c == '@') {
            stm.rewind(1);
            return deserializeComplexTag(session, stm);
        } else if(c == ':') {
            c = stm.read();
            if(c == ':') {
                stm.rewind(2);
                return deserializeType(session, stm);
            } else {
                stm.rewind(2);
                return null;
            }
        } else if(c == '-') {
            c = stm.read();
            if(c == '>') {
                stm.rewind(2);
                return deserializeFunction(session, stm);
            } else {
                stm.rewind(2);
                return deserializeTextNumberOrBinary(session, stm);
            }
        } else {
            stm.rewind(1);
            return null;
        }
    }

    private static void serializeText(Text val, Writer stm) throws IOException {
        String  text   = val.toTextString();
        int     length = text.length();
        boolean quote  = !val.isNumber() && !val.isBinary() && !isIdent(text);
        if(quote) {
            stm.append('"');
        }
        for(int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if(c == '\\' || c == '"') {
                stm.append('\\');
                stm.append(c);
            } else if(c == '\t') {
                stm.append('\\');
                stm.append('t');
            } else if(c == '\r') {
                stm.append('\\');
                stm.append('r');
            } else if(c == '\n') {
                stm.append('\\');
                stm.append('n');
            } else if(c < 0x20) {
                stm.append('\\');
                stm.append('u');
                stm.append(encodeHex((c >> 12) & 0x0f));
                stm.append(encodeHex((c >>  8) & 0x0f));
                stm.append(encodeHex((c >>  4) & 0x0f));
                stm.append(encodeHex( c        & 0x0f));
            } else {
                stm.append(c);
            }
        }
        if(quote) {
            stm.append('"');
        }
    }

    private static Text deserializeTextNumberOrBinary(Session session, LineReader stm) throws IOException, StyxException {
        StringBuilder sb = new StringBuilder();
        int c = stm.read();
        int n = isDigit(c) ? 1 : 0;
        if(c != '-' && !isDigit(c)) {
            throw new StyxException(buildMessage(stm, "Invalid numeric value."));
        }
        sb.append((char) c);
        c = stm.read();
        if(c == 'x' && sb.charAt(0) == '0') {
            sb.append((char) c);
            c = stm.read();
            while(c != -1 && isHex(c)) {
                sb.append((char) c);
                c = stm.read();
            }
            if(sb.length() % 2 != 0) {
                throw new StyxException(buildMessage(stm, "Invalid binary value: odd number of digits."));
            }
        } else {
            while(c != -1 && isDigit(c)) {
                sb.append((char) c);
                c = stm.read();
                n++;
            }
            if(n == 0) {
                throw new StyxException(buildMessage(stm, "Invalid numeric value: digits expected."));
            }
            if(c == '.') {
                sb.append((char) c);
                c = stm.read();
                n = 0;
                while(c != -1 && isDigit(c)) {
                    sb.append((char) c);
                    c = stm.read();
                    n++;
                }
                if(n == 0) {
                    throw new StyxException(buildMessage(stm, "Invalid numeric value: fractional digits expected."));
                }
            }
            if(c == 'E') {
                sb.append((char) c);
                c = stm.read();
                n = 0;
                if(c == '-') {
                    sb.append((char) c);
                    c = stm.read();
                }
                while(c != -1 && isDigit(c)) {
                    sb.append((char) c);
                    c = stm.read();
                    n++;
                }
                if(n == 0) {
                    throw new StyxException(buildMessage(stm, "Invalid numeric value: exponential digits expected."));
                }
            }
        }
        stm.rewind(1);
        return session.text(sb.toString());
    }

    private static Text deserializeTextUnquoted(Session session, LineReader stm) throws IOException, StyxException {
        StringBuilder sb = new StringBuilder();
        int c = stm.read();
        if(!isLetter(c)) {
            throw new StyxException(buildMessage(stm, "Invalid numeric value."));
        }
        sb.append((char) c);
        c = stm.read();
        while(c != -1 && (isLetter(c) || isDigit(c))) {
            sb.append((char) c);
            c = stm.read();
        }
        if(sb.length() == 0) {
            throw new StyxException(buildMessage(stm, "Invalid text value: letter or digits expected."));
        }
        stm.rewind(1);
        return session.text(sb.toString());
    }

    private static Text deserializeTextQuoted(Session session, LineReader stm) throws IOException, StyxException {
        int c = stm.read();
        if(c != '"') {
            throw new StyxException(buildMessage(stm, "Invalid text value: '\"' expected."));
        }
        StringBuilder sb = new StringBuilder();
        c = stm.read();
        while(c != -1 && c != '"') {
            if(c == '\\') {
                c = stm.read();
                if(c == '\\' || c == '\'' || c == '"') {
                    // c = c;
                } else if(c == 't') {
                    c = '\t';
                } else if(c == 'r') {
                    c = '\r';
                } else if(c == 'n') {
                    c = '\n';
                } else if(c == 'u') {
                    c = 0;
                    for(int i = 0; i < 4; i++) {
                        int d = stm.read();
                        if(!isHex(d)) {
                            throw new StyxException(buildMessage(stm, "Invalid text value: Invalid unicode escape sequence."));
                        }
                        c = (c << 4) + decodeHex(d);
                    }
                } else {
                    throw new StyxException(buildMessage(stm, "Invalid text value: Invalid escape sequence."));
                }
            }
            sb.append((char) c);
            c = stm.read();
        }
        if(c == -1) {
            throw new StyxException(buildMessage(stm, "Invalid text value: Unexpected end of input before '\"'."));
        }
        return session.text(sb.toString());
    }

    private static void serializeReference(Reference val, Writer stm, int curIndent) throws IOException {
        stm.append('[');
        stm.append('/');
        int level = val.level();
        for(int i = 1; i <= level; i++) {
            if(i > 1) {
                stm.append('/');
            }
            serializeValue(val.parent(i).name(), stm, curIndent, 0);
        }
        stm.append(']');
    }

    private static Reference deserializeReference(Session session, LineReader stm) throws IOException, StyxException {
        int c1 = stm.read();
        int c2 = stm.read();
        if(c1 != '[' || c2 != '/') {
            throw new StyxException(buildMessage(stm, "Invalid reference value: '[/' expected."));
        }
        Reference ref = session.root();
        int c = skipWhite(stm, false);
        if(c != ']') {
            stm.rewind(1);
            do {
                Value child = deserializeValue(session, stm);
                if(child == null) {
                    throw new StyxException(buildMessage(stm, "Invalid reference value: part expected."));
                }
                ref = ref.child(child);
                c = skipWhite(stm, false);
            } while(c == '/');
            if(c != ']') {
                throw new StyxException(buildMessage(stm, "Invalid reference value: '/' or ']' expected."));
            }
        }
        return ref;
    }

    private static void serializeComplex(Complex val, Writer stm, int curIndent, int deltaIndent) throws IOException {
        if(val.isEmpty()) {
            stm.append('[');
            if(curIndent >= 0) {
                stm.append(' ');
            }
            stm.append(']');
            return;
        }
        if(val.hasSingle()) {
            Pair<Value, Value> pair = val.single();
            if(!pair.key().isNumber() && !pair.key().isBinary() && isIdent(pair.key().asText().toTextString())) {
                stm.append('@');
                serializeValue(pair.key(), stm, 0, 0);
                stm.append(' ');
                serializeValue(pair.val(), stm, curIndent, deltaIndent);
                return;
            }
        }
        stm.append('[');
        boolean first   = true;
        int     autokey = 1;
        for(Pair<Value, Value> child : val) {
            if(!first && deltaIndent == 0) {
                stm.append(',');
            }
            if(deltaIndent > 0) {
                stm.append('\n');
                for(int i = 0; i < curIndent + deltaIndent; i++) {
                    stm.append(' ');
                }
            } else if(curIndent >= 0 && !first) {
                stm.append(' ');
            }
            if(child.key().isNumber() && child.key().asNumber().isInteger() && child.key().asNumber().toInteger() == autokey) {
                autokey++;
            } else {
                serializeValue(child.key(), stm, curIndent, 0);
                stm.append(':');
                if(curIndent >= 0) {
                    stm.append(' ');
                }
            }
            serializeValue(child.val(), stm, curIndent + deltaIndent, deltaIndent);
            first = false;
        }
        if(deltaIndent > 0) {
            stm.append('\n');
            for(int i = 0; i < curIndent; i++) {
                stm.append(' ');
            }
        }
        stm.append(']');
    }

    private static Complex deserializeComplex(Session session, LineReader stm) throws IOException, StyxException {
        int c = stm.read();
        if(c != '[') {
            throw new StyxException(buildMessage(stm, "Invalid complex value: '[' expected."));
        }
        Complex complex = session.complex();
        c = skipWhite(stm, true);
        if(c != ']') {
            stm.rewind(1);
            do {
                Value childKey = deserializeValue(session, stm);
                if(childKey == null) {
                    throw new StyxException(buildMessage(stm, "Invalid complex value: key expected."));
                }
                c = skipWhite(stm, false);
                if(c == ':') {
                    Value childVal = deserializeValue(session, stm);
                    if(childVal == null) {
                        throw new StyxException("Invalid complex value: value expected.");
                    }
                    c = stm.read();
                    complex = complex.put(childKey, childVal);
                } else {
                    complex = complex.add(childKey);
                }
                stm.rewind(1);
                c = parseSep(stm);
            } while(c == ',');
            if(c != ']') {
                throw new StyxException(buildMessage(stm, "Invalid complex value: line break or ',' or ']' expected."));
            }
        }
        return complex;
    }

    private static Complex deserializeComplexTag(Session session, LineReader stm) throws IOException, StyxException {
        int c = stm.read();
        if(c != '@') {
            throw new StyxException(buildMessage(stm, "Invalid tagged value: '@' expected."));
        }
        Value childKey = deserializeValue(session, stm);
        if(childKey == null) {
            throw new StyxException(buildMessage(stm, "Invalid tagged value: key expected."));
        }
        Value childVal = deserializeValue(session, stm);
        if(childVal == null) {
            throw new StyxException("Invalid tagged value: value expected.");
        }
        return session.complex(childKey, childVal);
    }

    private static void serializeType(Type val, Writer stm, int curIndent, int deltaIndent) throws IOException {
        stm.append("::");
        if(curIndent >= 0) {
            stm.append(' ');
        }
        serializeValue(val.definition(), stm, curIndent, deltaIndent);
    }

    private static Type deserializeType(Session session, LineReader stm) throws IOException, StyxException {
        int c1 = stm.read();
        int c2 = stm.read();
        if(c1 != ':' || c2 != ':') {
            throw new StyxException(buildMessage(stm, "Invalid type value: '::' expected."));
        }
        Value def = deserializeValue(session, stm);
        return session.type(def);
    }

    private static void serializeFunction(Function val, Writer stm, int curIndent, int deltaIndent) throws IOException {
        stm.append("->");
        if(curIndent >= 0) {
            stm.append(' ');
        }
        serializeValue(val.definition(), stm, curIndent, deltaIndent);
    }

    private static Function deserializeFunction(Session session, LineReader stm) throws IOException, StyxException {
        int c1 = stm.read();
        int c2 = stm.read();
        if(c1 != '-' || c2 != '>') {
            throw new StyxException(buildMessage(stm, "Invalid function value: '->' expected."));
        }
        Value def = deserializeValue(session, stm);
        return session.function(def);
    }

    private static int parseSep(LineReader stm) throws IOException {
        boolean nline = false;
        while(true) {
            int c = skipWhite(stm, false);
            if(c == '\n') {
                nline = true;
            } else if(c == ',') {
                skipWhite(stm, true); // TODO (cleanup) can we make this more elegant?
                stm.rewind(1);
                return c;
            } else {
                if(nline && c != ']') {
                    stm.rewind(1);
                    return ',';
                }
                return c;
            }
        }
    }

    private static int skipWhite(LineReader stm, boolean ml) throws IOException {
        while(true) {
            int c = stm.read();
            if(c == ' ' || c == '\t' || c == '\r') {
                continue;
            } else if(c == '\n' && ml) {
                continue;
            } else if(c == '/') {
                c = stm.read();
                if(c == '*') {
                    // A C style comment ("/* xxxxxx */") has been found. We also support nested comments.
                    int nest = 1;
                    int c3, c2 = '/';
                    do {
                        c3 = c2;
                        c2 = c;
                        c = stm.read();
                        // Increase depth when "/*" is encountered
                        if(c2 == '/' && c == '*') {
                            nest++;
                        }
                        // Decrease depth when "*/" is encountered, but not when "/*/"
                        if(c3 != '/' && c2 == '*' && c == '/') {
                            nest--;
                        }
                    } while(nest > 0 && c != -1);
                } else if(c == '/') {
                    // A C++ style comment ("// xxxxxx") has been found. It is terminated _before_ the new line.
                    do {
                        c = stm.read();
                    } while(c != '\n' && c != -1);
                    stm.rewind(1);
                } else {
                    // A "/" has been encountered that does not belong to a comment.
                    stm.rewind(1);
                    return '/';
                }
            } else {
                return c;
            }
        }
    }

    private static char encodeHex(int val) {
        if(val <= 9) {
            return (char) ('0' + val);
        } else {
            return (char) ('A' + val - 10);
        }
    }

    private static int decodeHex(int val) {
        if(val <= '9') {
            return val - '0';
        } else {
            return val - 'A' + 10;
        }
    }

    private static boolean isIdent(String text) {
        int len = text.length();
        for(int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if(i == 0 && !isLetter(c) || i > 0 && !isLetter(c) && !isDigit(c)) {
                return false;
            }
        }
        return len > 0;
    }

    private static boolean isDigit(int c) {
        return (c >= '0' && c <= '9');
    }

    private static boolean isHex(int c) {
        return (c >= '0' && c <= '9') ||
               (c >= 'A' && c <= 'F');
    }

    private static boolean isLetter(int c) {
        return (c >= 'A' && c <= 'Z') ||
               (c >= 'a' && c <= 'z') ||
               (c == '_')             ||
               (c >= 0x0100);
    }

    private static String buildMessage(LineReader stm, String message) {
        return "(" + stm.getLine() + ":" + stm.getColumn() + "): " + message;
    }
}
