package styx.core.memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import styx.Session;
import styx.StyxException;
import styx.Value;

/**
 * Implementation of a shared value stored in a flat file.
 */
public final class SharedValueFile implements SharedValue {

    private static final Logger LOG = Logger.getLogger(SharedValueFile.class.toString());

    private final Path    file;
    private final boolean indent;
    private final int     storeRetires;   // 10..1000 ms, exponential back-off (attempts to lock file before reporting failure)
    private final int     monitorRetries; // 10..1000 ms, exponential back-off (attempts before assuming timeout)

    /**
     * The version of the last value read from or written to the file, zero if none.
     * <p>
     * The version is stored on the first line of the STYX text file as syntactically valid white space.
     * The first line consists of 31 characters which are either spaces (bit 0) or tabs (bit 1).
     * Therefore, the version is a 31-bit unsigned integer with wrap-around.
     */
    private int version = 0;

    public SharedValueFile(Path file, boolean indent) {
        this(file, indent, 100, 1000);
    }

    public SharedValueFile(Path file, boolean indent, int storeRetires, int monitorRetries) {
        this.file   = file;
        this.indent = indent;
        this.storeRetires   = storeRetires;
        this.monitorRetries = monitorRetries;
    }

    @Override
    public SharedValue clone() {
        return new SharedValueFile(file, indent, storeRetires, monitorRetries);
    }

    @Override
    public Value get(Session session) throws StyxException {
        return load(session);
    }

    @Override
    public void set(Session session, Value value) throws StyxException {
        store(session, value, false);
    }

    @Override
    public boolean testset(Session session, Value value) throws StyxException {
        return store(session, value, true);
    }

    @Override
    public void monitor(Session session) {
        int retries = monitorRetries;
        int millis = 10;
        while(readVersion(file) == version && retries-- > 0) {
            LOG.info("Monitoring file '"+ file + "' (version="+version+").");
            sleepSafely(millis);
            millis = Math.min(millis * 3 / 2, 1000);
        }
    }

    private Value load(Session session) throws StyxException {
        // Check if the file exists. If not, assume we are creating a new file with version zero.
        if(!Files.exists(file)) {
            // Assume success, reset the version.
            LOG.info("File '"+ file + "' not found (assuming new).");
            this.version = 0;
            return null;
        }

        // Load the file and try to read its version. If there is no version, assume zero.
        // Since new values are written to temporary files, we don't have to expect incomplete data.
        try(InputStream stm = Files.newInputStream(file)) {
            int version = readVersion(stm);
            Value value;
            if(version > 0) {
                // A version has been found, now read the value.
                value = session.deserialize(stm);
            } else {
                // No version has been found, try again with reading the value at the start.
                try(InputStream stm2 = Files.newInputStream(file)) {
                    value = session.deserialize(stm2);
                }
            }

            // Successful, remember the version.
            LOG.info("Loaded file '"+ file + "' (version="+version+").");
            this.version = version;
            return value;

        } catch(IOException | StyxException  e) {
            LOG.severe("Failed to load file '"+ file + "' (invalid path or data or access denied): " + e);
            throw new StyxException("Failed to load file '"+ file + "' (invalid path or data or access denied): " + e);
        }
    }

    private boolean store(Session session, Value value, boolean test) throws StyxException {
        Path file2 = FileSystems.getDefault().getPath(file.toString() + ".lock");
        int retries = storeRetires;
        int millis = 10;
        while(true) {
            // Create a temporary file. This operation is atomic and orders writers. If creation fails,
            // we assume that another session is currently writing a new value and wait until it is done.
            try {
                Files.createFile(file2);
            } catch(FileAlreadyExistsException e) {
                if(retries-- > 0) {
                    // Do not return false immediately since retrying before the new value is written is not very smart.
                    LOG.info("Waiting for file '"+ file + "' to be written (concurrent modification, old version="+version+").");
                    sleepSafely(millis);
                    millis = Math.min(millis * 3 / 2, 1000);
                    continue;
                } else {
                    LOG.severe("Failed to store file '"+ file + "' (locked and timeout expired): " + e);
                    throw new StyxException("Failed to store file '"+ file + "' (locked and timeout expired).", e);
                }
            } catch (IOException e) {
                LOG.severe("Failed to store file '"+ file + "' (invalid path or access denied): " + e);
                throw new StyxException("Failed to store file '"+ file + "' (invalid path or access denied).", e);
            }
            break;
        }

        // We got the lock, now check the version of the existing file.
        if(test && readVersion(file) != version) {
            LOG.info("Not storing file '"+ file + "' (concurrent modification, old version="+version+").");
            deleteSafely(file2);
            return false;
        }
        if(!test) {
            version = readVersion(file); // Use the latest version, even if that value has never been read.
        }

        // Write the value to the temporary file. This may take some time for large values.
        // It is safe as long as the file is truncated and not deleted and re-created.
        try(OutputStream stm = Files.newOutputStream(file2)) {
            writeVersion(stm, incrementVersion(version));
            session.serialize(value, stm, indent);
        } catch(RuntimeException | IOException | StyxException  e) {
            // If we fail, make sure to delete the temporary file since it would block other writers.
            deleteSafely(file2);
            LOG.severe("Failed to store file '"+ file + "' (could not write to temporary file): " + e);
            throw new StyxException("Failed to store file '"+ file + "' (could not write to temporary file).", e);
        }

        // Replace the original file with the temporary file.
        // This is guaranteed to be atomic (or it might fail on some platforms).
        try {
            Files.move(file2, file, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // If we fail, make sure to delete the temporary file since it would block other writers.
            deleteSafely(file2);
            LOG.severe("Failed to store file '"+ file + "' (could not move temporary file): " + e);
            throw new StyxException("Failed to store file '"+ file + "' (could not move temporary file).", e);
        }

        // Successful, remember the new version.
        LOG.info("Stored file '"+ file + "' (version="+incrementVersion(version)+").");
        version = incrementVersion(version);
        return true;
    }

    private static int incrementVersion(int version) {
        if(version < 0x7FFFFFFF) {
            return version + 1;
        } else {
            return 1; // 0x7FFFFFFF is followed by 1, not 0!
        }
    }

    private static int readVersion(Path file) {
        try(InputStream stm = Files.newInputStream(file)) {
            return readVersion(stm);
        } catch (IOException e) {
            return 0;
        }
    }

    private static int readVersion(InputStream stm) throws IOException {
        int version = 0;
        for(int i = 0; i < 31; i++) {
            int chr = stm.read();
            if(chr != '\t' && chr != ' ') {
                return 0;
            }
            int bit = (chr == '\t') ? 1 : 0;
            version |= bit << i;
        }
        int chr = stm.read();
        if(chr != '\n') {
            return 0;
        }
        return version;
    }

    private static void writeVersion(OutputStream stm, int version) throws IOException {
        for(int i = 0; i < 31; i++) {
            boolean bit = (version & (1<<i)) != 0;
            stm.write(bit ? (byte) '\t' : (byte) ' ');
        }
        stm.write((byte) '\n');
    }

    private static boolean sleepSafely(int millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private static void deleteSafely(Path file) {
        try {
            Files.delete(file);
        } catch (IOException e) {
            LOG.severe("Failed to delete temporary file after '"+file+"' error or concurrent modification: " + e);
        }
    }
}
