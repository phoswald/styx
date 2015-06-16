package styx.db.mmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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

    private final static byte[] MAGIC = "STYX-DB-0001____".getBytes(StandardCharsets.UTF_8);

    private final static long ADDRESS_MAGIC =  0;
    private final static long ADDRESS_ROOT  = 16;
    private final static long ADDRESS_NEXT  = 24;
    private final static long ADDRESS_FIRST = 32;

    private String url;

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
    private MmapDatabase(String url, ByteBuffer buffer, long size) {
        this.url    = url;
        this.size   = (size + 7) & ~7;
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
            throw new RuntimeException("Not a valid database (" + url + ").");
        }
    }

    public static MmapDatabase fromFile(Path path) throws IOException {
        try(FileChannel fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.SPARSE)) {
            long size = fc.size();
            System.out.println("Database: opened '" + path + "', size = " + size + " bytes.");

            if(size == 0) {
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
                size = 512 << 20;
                fc.position(size);
                fc.write(ByteBuffer.allocate(1)); // write a single byte at the end (this allocates a single page).
                fc.truncate(size); // remove the byte written above (leaving zero pages allocated).
                fc.position(0);
                size = fc.size();
                System.out.println("Database: grown to " + size + " bytes.");
            }

            ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
            System.out.println("Database: mapped.");

            return new MmapDatabase(path.toString(), buffer, size);
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
            System.out.println("Database: closed (" + url + ").");
        }
    }

    public void dump(PrintStream stm) {
        if(bytes != null) {
            stm.println("ByteBuffer properties (" + url + "):");
//          stm.println("- isLoaded: " + bytes.isLoaded());
            stm.println("- isDirect: " + bytes.isDirect());
            stm.println("- capacity: " + bytes.capacity());
            stm.println("Database properties (" + url + "):");
            stm.println("- size: " + size);
            stm.println("- root: " + getLong(ADDRESS_ROOT));
            stm.println("- next: " + getLong(ADDRESS_NEXT));
        }
    }

    public long getRoot() {
        synchronized(this) {
            return getLong(ADDRESS_ROOT);
        }
    }

    public void setRoot(long address) {
        synchronized(this) {
            putLong(ADDRESS_ROOT, address);
            notifyAll();
        }
    }

    public boolean testAndSetRoot(long address, long expectedAddress) {
        synchronized(this) {
            if(getLong(ADDRESS_ROOT) != expectedAddress) {
                return false;
            }
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

    private final MmapAvlTree sentinel = new MmapAvlTree(this, 0);

//    private static final Session session = SessionManager.getDetachedSession(); // TODO static? detached? or something else?

    private static Session session;

    static {
        try {
            session = SessionManager.createMemorySessionFactory(false).createSession(); // TODO static? detached? or something else?
        } catch (StyxException e) {
            e.printStackTrace();
        }
    }

    public MmapAvlTree getSentinel() {
        return sentinel;
    }

    public Complex makeComplex(long address) {
        return new ConcreteComplex(new MmapAvlTree(this, address));
    }

    public Value loadValue(long address) throws StyxException {
        if(address == -1) {
            return null; // not used by MmapAvlTree, only required when [/][*] = null
        }
        if((address & 1) == 0) {
            return makeComplex(address);
        } else {
            address--;
            int size = getInt(address);
            byte[] data = new byte[size];
            getArray(address + 4, data);
            ByteArrayInputStream stm = new ByteArrayInputStream(data);
            return session.deserialize(stm);
        }
    }

    public long storeValue(Value value) throws StyxException {
        if(value == null) {
            return -1; // not used by MmapAvlTree, only required when [/][*] = null
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
            return tree.store();
        } else {
            ByteArrayOutputStream stm = new ByteArrayOutputStream();
            session.serialize(value, stm, false);
            long address = alloc(stm.size() + 4);
            putInt(address, stm.size());
            putArray(address + 4, stm.toByteArray());
            return address | 1;
        }
    }

    public long alloc(int size) {
        long address = getLong(ADDRESS_NEXT);
        putLong(ADDRESS_NEXT, address + ((size + 7) & ~7));
        return address;
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
}
