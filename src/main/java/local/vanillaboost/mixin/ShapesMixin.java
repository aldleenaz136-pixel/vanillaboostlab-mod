package local.vanillaboost.mixin;

import local.vanillaboost.PairCache;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Experimental: assumes shape geometry is immutable, as with published vanilla
 * block face shapes. Mods mutating a retained shape in place violate this assumption.
 * Deliberately opt-in until measured and tested in a real game instance.
 */
@Mixin(Shapes.class)
public abstract class ShapesMixin {
    @Unique private static final boolean VBL_ENABLED =
            Boolean.getBoolean("vanillaboost.cache");
    @Unique private static final boolean VBL_VALIDATE =
            Boolean.getBoolean("vanillaboost.validate");
    @Unique private static volatile boolean vblBroken;
    @Unique private static final ThreadLocal<PairCache> VBL_CACHE =
            ThreadLocal.withInitial(() -> new PairCache(1024));

    @Inject(method = "faceShapeOccludes(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void vblRead(VoxelShape a, VoxelShape b,
            CallbackInfoReturnable<Boolean> cir) {
        if (!VBL_ENABLED || VBL_VALIDATE || vblBroken) return;
        int cached = VBL_CACHE.get().get(a, b);
        if (cached >= 0) cir.setReturnValue(cached == 1);
    }

    @Inject(method = "faceShapeOccludes(Lnet/minecraft/world/phys/shapes/VoxelShape;Lnet/minecraft/world/phys/shapes/VoxelShape;)Z",
            at = @At("RETURN"))
    private static void vblWrite(VoxelShape a, VoxelShape b,
            CallbackInfoReturnable<Boolean> cir) {
        if (!VBL_ENABLED || vblBroken) return;
        PairCache cache = VBL_CACHE.get();
        boolean actual = cir.getReturnValueZ();
        if (VBL_VALIDATE) {
            int cached = cache.get(a, b);
            if (cached >= 0 && (cached == 1) != actual) {
                vblBroken = true;
                cache.clear();
                System.err.println("[VanillaBoost Lab] Shape changed or result conflict: cache disabled. Restart without this mod.");
                return;
            }
        }
        cache.put(a, b, actual);
    }
}
