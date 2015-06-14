package styx.core.intrinsics;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;

import styx.Complex;
import styx.Determinism;
import styx.Session;
import styx.StyxException;
import styx.Value;
import styx.core.expressions.CompiledFunction;
import styx.core.expressions.FuncRegistry;
import styx.core.expressions.Stack;
import styx.core.utils.JsonSerializer;
import styx.core.utils.XmlSerializer;

public class FileIntrinsics {

    private static final int BOM = 0xFEFF;

    public static Complex buildEnvironment(FuncRegistry registry, Session session) throws StyxException {
        return session.complex()
                .put(session.text("read"), new CompiledFunction(registry, "file_read", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        return stack.session().deserialize(
                            FileSystems.getDefault().getPath(stack.getFrameValue(0).asText().toTextString()));
                    }
                }.function())
                .put(session.text("write"), new CompiledFunction(registry, "file_write", Determinism.NON_DETERMINISTIC, 3) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        stack.session().serialize(
                            stack.getFrameValue(0),
                            FileSystems.getDefault().getPath(stack.getFrameValue(1).asText().toTextString()),
                            stack.getFrameValue(2).asBool().toBool());
                        return null;
                    }
                }.function())
                .put(session.text("read_text"), new CompiledFunction(registry, "file_read_text", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(Reader stm = Files.newBufferedReader(Paths.get(stack.getFrameValue(0).asText().toTextString()), StandardCharsets.UTF_8)) {
                            return stack.session().text(readToEnd(stm));
                        } catch (IOException e) {
                            throw new StyxException("Cannot read text file.", e);
                        }
                    }
                }.function())
                .put(session.text("write_text"), new CompiledFunction(registry, "file_write_text", Determinism.NON_DETERMINISTIC, 3) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(Writer stm = Files.newBufferedWriter(Paths.get(stack.getFrameValue(0).asText().toTextString()), StandardCharsets.UTF_8)) {
                            stm.write(BOM);
                            stm.write(stack.getFrameValue(1).asText().toTextString());
                            return null;
                        } catch (IOException e) {
                            throw new StyxException("Cannot write text file.", e);
                        }
                    }
                }.function())
                .put(session.text("read_binary"), new CompiledFunction(registry, "file_read_binary", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(FileInputStream stm = new FileInputStream(stack.getFrameValue(0).asText().toTextString())) {
                            return stack.session().binary(readToEnd(stm));
                        } catch (IOException e) {
                            throw new StyxException("Cannot read binary file.", e);
                        }
                    }
                }.function())
                .put(session.text("write_binary"), new CompiledFunction(registry, "file_write_binary", Determinism.NON_DETERMINISTIC, 3) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(FileOutputStream stm = new FileOutputStream(stack.getFrameValue(0).asText().toTextString())) {
                            stm.write(stack.getFrameValue(1).asBinary().toByteArray());
                            return null;
                        } catch (IOException e) {
                            throw new StyxException("Cannot write binary file.", e);
                        }
                    }
                }.function())
                .put(session.text("read_xml"), new CompiledFunction(registry, "file_read_xml", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(FileInputStream stm = new FileInputStream(stack.getFrameValue(0).asText().toTextString())) {
                            return XmlSerializer.deserialize(stack.session(), stm);
                        } catch (IOException e) {
                            throw new StyxException("Cannot read XML file.", e);
                        }
                    }
                }.function())
                .put(session.text("write_xml"), new CompiledFunction(registry, "file_write_xml", Determinism.NON_DETERMINISTIC, 3) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(FileOutputStream stm = new FileOutputStream(stack.getFrameValue(0).asText().toTextString())) {
                            XmlSerializer.serialize(stack.getFrameValue(1), stm, stack.getFrameValue(2).asBool().toBool());
                            return null;
                        } catch (IOException e) {
                            throw new StyxException("Cannot write XML file.", e);
                        }
                    }
                }.function())
                .put(session.text("read_json"), new CompiledFunction(registry, "file_read_json", Determinism.NON_DETERMINISTIC, 1) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(FileInputStream stm = new FileInputStream(stack.getFrameValue(0).asText().toTextString())) {
                            return JsonSerializer.deserialize(stack.session(), stm);
                        } catch (IOException e) {
                            throw new StyxException("Cannot read JSON file.", e);
                        }
                    }
                }.function())
                .put(session.text("write_json"), new CompiledFunction(registry, "file_write_json", Determinism.NON_DETERMINISTIC, 3) {
                    @Override
                    public Value invoke(Stack stack) throws StyxException {
                        try(FileOutputStream stm = new FileOutputStream(stack.getFrameValue(0).asText().toTextString())) {
                            JsonSerializer.serialize(stack.getFrameValue(1), stm, stack.getFrameValue(2).asBool().toBool());
                            return null;
                        } catch (IOException e) {
                            throw new StyxException("Cannot write JSON file.", e);
                        }
                    }
                }.function());
    }

    public static byte[] readToEnd(InputStream stm) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int b;
        while((b = stm.read()) != -1) {
            buf.write(b);
        }
        return buf.toByteArray();
    }

    public static String readToEnd(Reader stm) throws IOException {
        StringBuffer buf = new StringBuffer();
        int c;
        while((c = stm.read()) != -1) {
            if(c == BOM) {
                continue;
            }
            buf.append((char) c);
        }
        return buf.toString();
    }
}
