package styx.db.mmap;

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

public class MappedDatabase implements AutoCloseable {

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
    private MappedDatabase(String url, ByteBuffer buffer, long size) {
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

    public static MappedDatabase fromFile(Path file) throws IOException {
        try(FileChannel fc = FileChannel.open(file, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.SPARSE)) {
            long size = fc.size();
            System.out.println("Database: opened '" + file + "', size = " + size + " bytes.");

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

            return new MappedDatabase(file.toString(), buffer, size);
        }
    }

    public static MappedDatabase fromMemory(long size) {
        return new MappedDatabase("memory", ByteBuffer.allocateDirect((int) size), size);
    }

    public static MappedDatabase fromArray(byte[] bytes) {
        return new MappedDatabase("array", ByteBuffer.wrap(bytes), bytes.length);
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

    public void dump() {
        if(bytes != null) {
            System.out.println("ByteBuffer properties (" + url + "):");
//          System.out.println("- isLoaded: " + bytes.isLoaded());
            System.out.println("- isDirect: " + bytes.isDirect());
            System.out.println("- capacity: " + bytes.capacity());
            System.out.println("Database properties (" + url + "):");
            System.out.println("- size: " + size);
            System.out.println("- root: " + getLong(ADDRESS_ROOT));
            System.out.println("- next: " + getLong(ADDRESS_NEXT));
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
