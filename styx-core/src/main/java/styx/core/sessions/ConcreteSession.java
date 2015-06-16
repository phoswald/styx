package styx.core.sessions;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import styx.Binary;
import styx.Bool;
import styx.Complex;
import styx.Function;
import styx.Numeric;
import styx.Reference;
import styx.Session;
import styx.StyxException;
import styx.Text;
import styx.Type;
import styx.Value;
import styx.Void;
import styx.core.DataProvider;
import styx.core.EvalProvider;
import styx.core.FuncProvider;
import styx.core.TypeProvider;
import styx.core.utils.Serializer;
import styx.core.values.AbstractValue;

public class ConcreteSession implements Session {

    private final Complex      complex;
    private final DataProvider data;
    private final TypeProvider type;
    private final FuncProvider func;
    private final EvalProvider eval;
    private final Complex      environment;

    public ConcreteSession(DataProvider data, TypeProvider type, FuncProvider func, EvalProvider eval, Complex environment) {
        this(AbstractValue.complex(), data, type, func, eval, environment);
    }

    public ConcreteSession(Complex complex, DataProvider data, TypeProvider type, FuncProvider func, EvalProvider eval, Complex environment) {
        this.complex     = Objects.requireNonNull(complex);
        this.data        = Objects.requireNonNull(data);
        this.type        = Objects.requireNonNull(type);
        this.func        = Objects.requireNonNull(func);
        this.eval        = Objects.requireNonNull(eval);
        this.environment = Objects.requireNonNull(environment);
    }

    @Override
    public void close() throws StyxException {
        data.close();
    }

    @Override
    public Text text(String val) {
        return AbstractValue.text(val);
    }

    @Override
    public Bool bool(boolean val) {
        return AbstractValue.bool(val);
    }

    @Override
    public Void empty() {
        return AbstractValue.empty();
    }

    @Override
    public Numeric number(int val) {
        return AbstractValue.number(val);
    }

    @Override
    public Numeric number(long val) {
        return AbstractValue.number(val);
    }

    @Override
    public Numeric number(double val) {
        return AbstractValue.number(val);
    }

    @Override
    public Numeric number(String val) {
        return AbstractValue.number(val);
    }

    @Override
    public Binary binary(byte[] val) {
        return AbstractValue.binary(val);
    }

    @Override
    public Binary binary(String val) {
        return AbstractValue.binary(val);
    }

    @Override
    public Reference root() {
        return AbstractValue.root();
    }

    @Override
    public Complex complex() {
        return complex;
    }

    @Override
    public Type type(Value definition) throws StyxException {
        return type.type(this, environment, definition);
    }

    @Override
    public Function function(Value definition) throws StyxException {
        return func.function(this, environment, definition);
    }

    @Override
    public Value read(Reference ref) throws StyxException {
        return data.read(this, ref);
    }

    @Override
    public void write(Reference ref, Value val) throws StyxException {
        data.write(this, ref, val);
    }

    @Override
    public List<Value> browse(Reference ref) throws StyxException {
        return data.browse(this, ref, null, null, null, true);
    }

    @Override
    public List<Value> browse(Reference ref, Value after, Value before, Integer maxResults, boolean forward) throws StyxException {
        return data.browse(this, ref, after, before, maxResults, forward);
    }

    @Override
    public boolean hasTransaction() {
        return data.hasTransaction();
    }

    @Override
    public void beginTransaction() throws StyxException {
        data.beginTransaction(this);
    }

    @Override
    public void commitTransaction() throws StyxException {
        data.commitTransaction(this);
    }

    @Override
    public void abortTransaction(boolean retry) throws StyxException {
        data.abortTransaction(this, retry);
    }

    @Override
    public String serialize(Value val, boolean indent) throws StyxException {
        return Serializer.serialize(val, indent);
    }

    @Override
    public void serialize(Value val, Path file, boolean indent) throws StyxException {
        Serializer.serialize(val, file, indent);
    }

    @Override
    public void serialize(Value val, OutputStream stm, boolean indent) throws StyxException {
        Serializer.serialize(val, stm, indent);
    }

    @Override
    public void serialize(Value val, Writer stm, boolean indent) throws StyxException {
        Serializer.serialize(val, stm, indent);
    }

    @Override
    public Value deserialize(String str) throws StyxException {
        return Serializer.deserialize(this, str);
    }

    @Override
    public Value deserialize(Path file) throws StyxException {
        return Serializer.deserialize(this, file);
    }

    @Override
    public Value deserialize(InputStream stm) throws StyxException {
        return Serializer.deserialize(this, stm);
    }

    @Override
    public Value deserialize(Reader stm) throws StyxException {
        return Serializer.deserialize(this, stm);
    }

    @Override
    public Function parse(String script) throws StyxException {
        return eval.parse(this, environment, script, true);
    }

    @Override
    public Function parse(String script, boolean compile) throws StyxException {
        return eval.parse(this, environment, script, compile);
    }

    @Override
    public Value evaluate(String script) throws StyxException {
        Function func = eval.parse(this, environment, script, true);
        return func == null ? null : func.invoke(this, null);
    }
}
