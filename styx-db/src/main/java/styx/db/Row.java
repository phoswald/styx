package styx.db;

public final class Row {
    public final String parent; // The key of the parent.
    public final String name;   // The local name in STYX syntax.
    public final int    suffix; // The local part of the key.
    public final String value;  // The local value in STYX syntax.

    public Row(String parent, String name, int suffix, String value) {
        this.parent = parent;
        this.name   = name;
        this.suffix = suffix;
        this.value  = value;
    }

    public String key() {
        return parent + suffix + "/";
    }

    public int level() {
        int length = parent.length();
        int level  = 0;
        for(int i = 0; i < length; i++) {
            if(parent.charAt(i) == '/') {
                level++;
            }
        }
        return level;
    }

    public String prefix(int level) {
        int length = parent.length();
        for(int i = 0; i < length; i++) {
            if(parent.charAt(i) == '/') {
                if(level-- == 0) {
                    return parent.substring(0, i + 1);
                }
            }
        }
        return key();
    }
}