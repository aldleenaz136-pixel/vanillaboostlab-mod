package local.vanillaboost;

/** Ordered identity keys, fixed storage, no equals()/hashCode() on game objects.
 * Callers MUST ensure the result remains valid while a key pair is retained.
 * Not thread-safe: each worker owns its own instance.
 */
public final class PairCache {
    private final Object[] first;
    private final Object[] second;
    private final byte[] values;
    private final int mask;

    public PairCache(int capacity) {
        if (capacity < 2 || Integer.bitCount(capacity) != 1)
            throw new IllegalArgumentException("capacity must be a power of two >= 2");
        first = new Object[capacity];
        second = new Object[capacity];
        values = new byte[capacity];
        mask = capacity - 1;
    }

    private int index(Object a, Object b) {
        int h = System.identityHashCode(a) * 0x9e3779b9;
        h ^= Integer.rotateLeft(System.identityHashCode(b), 13);
        h ^= h >>> 16;
        return h & mask;
    }

    /** -1 means miss, 0 false, 1 true. Hash collisions are never hits. */
    public int get(Object a, Object b) {
        int i = index(a, b);
        return values[i] != 0 && first[i] == a && second[i] == b
                ? values[i] - 1 : -1;
    }

    public void put(Object a, Object b, boolean result) {
        int i = index(a, b);
        first[i] = a;
        second[i] = b;
        values[i] = (byte) (result ? 2 : 1);
    }

    public void clear() {
        java.util.Arrays.fill(values, (byte) 0);
        java.util.Arrays.fill(first, null);
        java.util.Arrays.fill(second, null);
    }
}
