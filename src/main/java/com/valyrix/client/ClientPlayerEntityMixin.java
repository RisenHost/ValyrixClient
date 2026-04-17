package com.valyrix.mixin;

import com.valyrix.ValyrixClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side mixin into {@link ClientPlayerEntity}.
 *
 * ── Ghost client design principles ──────────────────────────────
 *  • This class is in the "client" mixin list in valyrix.mixins.json.
 *    It is NEVER loaded on a dedicated server — Fabric guarantees this.
 *  • Only @Inject (and @Redirect when truly necessary). Avoid @Overwrite:
 *    it breaks with every other mod that touches the same method.
 *  • Keep injected logic lightweight. Heavy work goes in the Module's
 *    onTick() — not here.
 * ────────────────────────────────────────────────────────────────
 *
 * ── Adding a new hook ────────────────────────────────────────────
 *  1. Find the Yarn-mapped method name in the 1.21.1 mappings.
 *  2. Add an @Inject below.
 *  3. Delegate to your module's logic (don't inline it here).
 *  4. Check module.isEnabled() before doing anything.
 * ─────────────────────────────────────────────────────────────────
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    // =========================================================================
    // Example ① — tick hook (currently inactive)
    // Use this to hook into every player tick for custom movement logic.
    // =========================================================================

    // @Inject(method = "tick", at = @At("HEAD"))
    // private void valyrix$onTickHead(CallbackInfo ci) {
    //     // Example: delegate to a module's mixin-level hook
    //     // Sprint sprint = ValyrixClient.getInstance().getModuleManager().getModule(Sprint.class);
    //     // if (sprint != null && sprint.isEnabled()) sprint.onMixinTick((ClientPlayerEntity)(Object)this);
    // }

    // =========================================================================
    // Example ② — velocity / knockback reduction hook
    // Inject at the point where the server pushes knockback velocity.
    // =========================================================================

    // @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    // private void valyrix$onKnockback(double strength, double x, double z, CallbackInfo ci) {
    //     AntiKnockback ak = ValyrixClient.getInstance().getModuleManager().getModule(AntiKnockback.class);
    //     if (ak != null && ak.isEnabled()) {
    //         ci.cancel(); // Cancel entirely — or scale velocity for partial resistance
    //     }
    // }

    // =========================================================================
    // Example ③ — movement input modification
    // Fires before client-side physics are applied to input.
    // =========================================================================

    // @Inject(method = "tickMovement", at = @At("HEAD"))
    // private void valyrix$onTickMovement(CallbackInfo ci) {
    //     // Safe injection point for speed / fly modules
    // }

    // =========================================================================
    // Example ④ — swim state override (useful for NoSlow)
    // =========================================================================

    // @Inject(method = "isSwimming", at = @At("RETURN"), cancellable = true)
    // private void valyrix$isSwimming(CallbackInfoReturnable<Boolean> cir) {
    //     // Override return value here if needed
    // }
}
