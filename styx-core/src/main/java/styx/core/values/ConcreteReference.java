package styx.core.values;

import java.util.Objects;

import styx.Reference;
import styx.Value;

/**
 * An implementation of references.
 */
final class ConcreteReference extends AbstractReference {

    /**
     * The instance that represents the top level reference value.
     */
    public static final ConcreteReference ROOT = new ConcreteReference();

    /**
     * The parent reference, never null except for root.
     */
    private final Reference parent;

    /**
     * The local name, never null except for root.
     */
    private final Value name;

    /**
     * Constructs a new root reference.
     */
    private ConcreteReference() {
        this.parent = null;
        this.name = null;
    }

    /**
     * Constructs a new non-root reference.
     * @param parent the parent reference, must not be null.
     * @param name the local name, must not be null.
     */
    private ConcreteReference(Reference parent, Value name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public Reference parent() {
        return parent;
    }

    @Override
    public Value name() {
        return name;
    }

    @Override
    public Reference child(Value name) {
        return new ConcreteReference(this, Objects.requireNonNull(name));
    }
}
