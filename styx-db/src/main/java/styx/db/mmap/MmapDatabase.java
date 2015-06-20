package styx.db.mmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import styx.Complex;
import styx.Session;
import styx.SessionManager;
import styx.StyxException;
import styx.Value;
import styx.core.values.ConcreteComplex;

public class MmapDatabase implements AutoCloseable {

    private static final byte[] MAGIC = "STYX-DB-0001____".getBytes(StandardCharsets.UTF_8);

    private static final int  TAG_SHIFT = 60;
    private static final long TAG_MASK  = 0x0FL << TAG_SHIFT;

    private static final int  ARG_SHIFT = 56;
    private static final long ARG_MASK  = 0x0FL << ARG_SHIFT;

    private static final int TAG_COMPLEX = 0x1; //                   address        stored in lowest    56 bits
    private static final int TAG_BOOL    = 0x2; //                   0 or 1         stored in lowest    32 bits
    private static final int TAG_INTEGER = 0x3; //                   signed integer stored in lowest    32 bits
    private static final int TAG_BINARY  = 0x4; // arg 0..7: length, bytes          stored in lowest 0..56 bits
    private static final int TAG_TEXT    = 0x5; // arg 0..7: length, UTF-8 bytes    stored in lowest 0..56 bits
    private static final int TAG_OTHER   = 0x6; //                   address        stored in lowest    56 bits

    private static final long ADDRESS_MAGIC =  0;
    private static final long ADDRESS_ROOT  = 16;
    private static final long ADDRESS_NEXT  = 24;
    private static final long ADDRESS_FIRST = 32;

    private String path;

    /**
     * The size of the memory region, zero if not open, always a multiple of 8.
     */
    private long size;

    /**
     * The memory region, null of not open.
     */
    private ByteBuffer bytes;

    private ShortBuffer shorts;

    private IntBuffer ints;

    private LongBuffer longs;

    /**
     * Constructs a new instance with position zero.
     */
    private MmapDatabase(String path, ByteBuffer buffer, long size) {
        this.path   = path;
        this.size   = size;
//      this.size   = (size + 7) & ~7;
        this.bytes  = buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.shorts = bytes.asShortBuffer();
        this.ints   = bytes.asIntBuffer();
        this.longs  = bytes.asLongBuffer();

        init();
    }

    private void init() {
        boolean zero  = true;
        boolean valid = true;
        byte[]  magic = new byte[16];
        getArray(0, magic);
        for(int i = 0; i < magic.length; i++) {
            if(magic[i] != 0) {
                zero = false;
            }
            if(magic[i] != MAGIC[i]) {
                valid = false;
            }
        }
        if(zero) {
            putArray(ADDRESS_MAGIC, MAGIC);
            putLong(ADDRESS_ROOT, 0);
            putLong(ADDRESS_NEXT, ADDRESS_FIRST);
        } else if(!valid) {
            throw new RuntimeException("MMAP(" + path + "): Not a valid database.");
        }
    }

    public static MmapDatabase fromFile(Path path, long size) throws StyxException {
        long align = 4 << 10;
        size = (size+align-1) & ~(align-1);
        try(FileChannel fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.SPARSE)) {
            System.out.println("MMAP(" + path + "): opened, size = " + fc.size() + " bytes.");
            if(size > fc.size()) {
/*
                // Works, but probably not sparse under Windows.
                // Also, the file is opened twice. Is there a way to get RandomAccessFile from FileChannel?
                try(RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw")) {
                    raf.setLength(512 << 20);
                }
*/
/*
                // Works, but not sparse
                for(int i = 0; i < 1024; i++) {
                    ByteBuffer block = ByteBuffer.allocate(1024);
                    fc.write(block);
                }
*/
                // Works, verified to be sparse under Linux and expectedly also under Windows.
                fc.position(size);
                fc.write(ByteBuffer.allocate(1)); // write a single byte at the end (this allocates a single page).
                fc.truncate(size); // remove the byte written above (leaving zero pages allocated).
                fc.position(0);
                size = fc.size();
                System.out.println("MMAP(" + path + "): grown to " + size + " bytes.");
            }

            ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
            System.out.println("MMAP(" + path + "): mapped, size = " + size + " bytes.");

            return new MmapDatabase(path.toString(), buffer, size);
        } catch(IOException e) {
            throw new StyxException("Failed to open or map file.", e);
        }
    }

    public static MmapDatabase fromMemory(long size) {
        return new MmapDatabase("memory", ByteBuffer.allocateDirect((int) size), size);
    }

    public static MmapDatabase fromArray(byte[] bytes) {
        return new MmapDatabase("array", ByteBuffer.wrap(bytes), bytes.length);
    }

    @Override
    public void close() {
        if(bytes != null) {
            size   = 0;
            bytes  = null; // there's no way to unmap the buffer, the file will effectively be closed by the GC!
            shorts = null;
            ints   = null;
            longs  = null;
            System.out.println("MMAP(" + path + "): closed.");
        }
    }

    public long getSize() {
        return size;
    }

    public long getRoot() {
        synchronized(this) {
            return getLong(ADDRESS_ROOT);
        }
    }

    public void setRoot(long address) {
        synchronized(this) {
            // System.out.println("MMAP(" + path + "): comitted root = " + address + ".");
            putLong(ADDRESS_ROOT, address);
            notifyAll();
        }
    }

    public boolean testAndSetRoot(long address, long expectedAddress) {
        synchronized(this) {
            if(getLong(ADDRESS_ROOT) != expectedAddress) {
                return false;
            }
            // System.out.println("MMAP(" + path + "): comitted root = " + address + ".");
            putLong(ADDRESS_ROOT, address);
            notifyAll();
            return true;
        }
    }

    public void monitorRoot(long expectedAddress) {
        synchronized(this) {
            while(getLong(ADDRESS_ROOT) == expectedAddress) {
                try {
                    wait(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    public long getNext() {
        return getLong(ADDRESS_NEXT);
    }

    public long alloc(int size) {
        long address = getLong(ADDRESS_NEXT);
        putLong(ADDRESS_NEXT, address + ((size + 7) & ~7));
        return address;
    }

    private final MmapAvlTree sentinel = new MmapAvlTree(this, 0);

    private final Complex empty = new ConcreteComplex(sentinel);

    public MmapAvlTree getProxy(long address) {
        if(address == 0) {
            return sentinel;
        } else {
            return new MmapAvlTree(this, address);
        }
    }

    public Complex getEmpty() {
        return empty;
    }

//  private static final Session session = SessionManager.getDetachedSession(); // TODO static? detached? or something else?
    private static Session session;

    static {
        try {
            session = SessionManager.createMemorySessionFactory(false).createSession();
        } catch (StyxException e) {
            e.printStackTrace();
        }
    }

    public Value loadValue(long address) {
        try {
            if(address == 0) {
                return null; // not used by MmapAvlTree, only required when [/][*] = null
            }
            int tag = (int) ((address & TAG_MASK) >>> TAG_SHIFT);
            int arg = (int) ((address & ARG_MASK) >>> ARG_SHIFT);
            address = address & ~TAG_MASK;
            switch(tag) {
                case TAG_COMPLEX:
                    return new ConcreteComplex(new MmapAvlTree(this, address));
                case TAG_BOOL: // arg 0: false, arg 1: true
                    return session.bool((int) address != 0);
                case TAG_INTEGER:
                    return session.number((int) address); // stored in low 32 bits
                case TAG_BINARY: { // args: length (0..7)
                    byte[] bytes = longToBytes(address, arg);
                    return session.binary(bytes);
                }
                case TAG_TEXT: { // args: length (0..7)
                    byte[] bytes = longToBytes(address, arg);
                    return session.text(new String(bytes, StandardCharsets.UTF_8));
                }
                case TAG_OTHER: {
                    int size = getInt(address);
                    byte[] bytes = new byte[size];
                    getArray(address + 4, bytes);
                    ByteArrayInputStream stm = new ByteArrayInputStream(bytes);
                    return session.deserialize(stm);
                }
                default:
                    throw new StyxException("Illegal tag " + tag);
            }
        } catch(StyxException e) {
            throw new RuntimeException("MMAP: Failed to load value from address="+address, e);
        }
    }

    public long storeValue(Value value) {
        try {
            if(value == null) {
                return 0; // not used by MmapAvlTree, only required when [/][*] = null
            }
            if(value.isComplex()) {
                if(value instanceof ConcreteComplex == false) {
                    throw new StyxException("Unsupported implementation of Complex.");
                }
                ConcreteComplex complex = (ConcreteComplex) value;
                if(complex.children() instanceof MmapAvlTree == false) {
                    throw new StyxException("Unsupported implementation of ImmutableSortedMap.");
                }
                MmapAvlTree tree = (MmapAvlTree) complex.children();
                return ((long) TAG_COMPLEX << TAG_SHIFT) | tree.store();
            }
            if(value.isBool()) {
                return ((long) TAG_BOOL << TAG_SHIFT) | (value.asBool().toBool() ? 1 : 0);
            }
            if(value.isNumber() && value.asNumber().isInteger()) {
                return ((long) TAG_INTEGER << TAG_SHIFT) | (value.asNumber().toInteger() & 0x00000000FFFFFFFFL);
            }
            if(value.isBinary()) {
                byte[] bytes = value.asBinary().toByteArray();
                if(bytes.length <= 7) {
                    return ((long) TAG_BINARY << TAG_SHIFT) | ((long) bytes.length << ARG_SHIFT) | bytesToLong(bytes);
                }
            }
            if(value.isText()) {
                String text = value.asText().toTextString();
                if(text.length() <= 7) {
                    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
                    if(bytes.length <= 7) {
                        return ((long) TAG_TEXT << TAG_SHIFT) | ((long) bytes.length << ARG_SHIFT) | bytesToLong(bytes);
                    }
                }
            }
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            session.serialize(value, bytes, false);
            long address = alloc(bytes.size() + 4);
            putInt(address, bytes.size());
            putArray(address + 4, bytes.toByteArray());
            return ((long) TAG_OTHER << TAG_SHIFT) | address;
        } catch(StyxException e) {
            throw new RuntimeException("MMAP: Failed to store value.", e);
        }
    }

    public final byte getByte(long address) {
        return bytes.get((int) address /* currently limited to 4 GB */);
    }

    public final void putByte(long address, byte value) {
        bytes.put((int) address /* currently limited to 4 GB */, value);
    }

    public final short getShort(long address) {
        return shorts.get((int) (address >> 1) /* currently limited to 4 GB */);
    }

    public final void putShort(long address, short value) {
        shorts.put((int) (address >> 1) /* currently limited to 4 GB */, value);
    }

    public final int getInt(long address) {
        return ints.get((int) (address >> 2) /* currently limited to 4 GB */);
    }

    public final void putInt(long address, int value) {
        ints.put((int) (address >> 2) /* currently limited to 4 GB */, value);
    }

    public final long getLong(long address) {
        return longs.get((int) (address >> 3) /* currently limited to 4 GB */);
    }

    public final void putLong(long address, long value) {
        longs.put((int) (address >> 3) /* currently limited to 4 GB */, value);
    }

    public final void getArray(long address, byte[] data) {
        int pos = 0;
        int len = data.length;
        while(len-- > 0) {
            data[pos++] = getByte(address++);
        }
    }

    public final void putArray(long address, byte[] data) {
        int pos = 0;
        int len = data.length;
        while(len-- > 0) {
            putByte(address++, data[pos++]);
        }
    }

    private byte[] longToBytes(long value, int num) {
        byte[] data = new byte[num];
        for(int i = 0; i < num; i++) {
            data[i] = (byte) value;
            value >>= 8;
        }
        return data;
    }

    private long bytesToLong(byte[] data) {
        long value = 0;
        for(int i = data.length - 1; i >= 0; i--) {
            value <<= 8;
            value |= data[i] & 0xFF;
        }
        return value;
    }
}
