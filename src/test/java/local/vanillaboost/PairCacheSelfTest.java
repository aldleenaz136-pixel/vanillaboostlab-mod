package local.vanillaboost;

public final class PairCacheSelfTest {
    private static void check(boolean condition) {
        if (!condition) throw new AssertionError("cache check failed");
    }
    public static void main(String[] args) {
        PairCache cache = new PairCache(2);
        Object a = new Object(), b = new Object();
        check(cache.get(a, b) == -1);
        cache.put(a, b, true);
        check(cache.get(a, b) == 1);
        check(cache.get(b, a) == -1);
        cache.put(a, b, false);
        check(cache.get(a, b) == 0);
        cache.clear();
        check(cache.get(a, b) == -1);
        // Stress direct-map collisions: a hit must always belong to its exact pair.
        Object[] keys = new Object[128];
        for (int i = 0; i < keys.length; i++) keys[i] = new Object();
        for (int i = 0; i < keys.length; i++) {
            cache.put(keys[i], b, (i & 1) == 0);
            for (int j = 0; j <= i; j++) {
                int result = cache.get(keys[j], b);
                check(result == -1 || result == ((j & 1) == 0 ? 1 : 0));
            }
        }
        boolean rejected = false;
        try { new PairCache(3); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected);
        System.out.println("PairCache self-test passed (does not test Minecraft or Mixins).");
    }
}
