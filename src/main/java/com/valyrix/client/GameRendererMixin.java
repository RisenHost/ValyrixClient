package com.valyrix.mixin;

import com.valyrix.ValyrixClient;
import com.valyrix.module.modules.visual.Fullbright;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyReturnValue;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side mixin into {@link GameRenderer}.
 *
 * Use this for visual post-processing hooks: FOV modification, night vision,
 * shader injection, or render pipeline interception.
 *
 * ── Fullbright note ──────────────────────────────────────────────
 *  The {@link Fullbright} module uses the gamma SimpleOption directly,
 *  which is the cleanest approach and needs no mixin.
 *  If you need a lower-level brightness override (e.g. for custom shader
 *  pipelines), uncomment the getNightVisionStrength hook below.
 * ────────────────────────────────────────────────────────────────
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    // =========================================================================
    // Example ① — Override effective night-vision strength
    // Returns 1.0f (full brightness) regardless of actual potion status.
    // This is an ALTERNATIVE approach to the gamma method for Fullbright.
    // =========================================================================

    // @ModifyReturnValue(method = "getNightVisionStrength", at = @At("RETURN"))
    // private float valyrix$nightVision(float original) {
    //     Fullbright fb = ValyrixClient.getInstance().getModuleManager().getModule(Fullbright.class);
    //     return (fb != null && fb.isEnabled()) ? 1.0f : original;
    // }

    // =========================================================================
    // Example ② — FOV modifier
    // Hook into the computed FOV before it is applied to the projection matrix.
    // =========================================================================

    // @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    // private double valyrix$modifyFov(double fov) {
    //     // ZoomModule zoom = ValyrixClient.getInstance().getModuleManager().getModule(ZoomModule.class);
    //     // if (zoom != null && zoom.isEnabled()) return zoom.getZoomFov();
    //     return fov;
    // }

    // =========================================================================
    // Example ③ — Shader toggling
    // Inject after shaders are loaded to apply custom post-process effects.
    // =========================================================================

    // @Inject(method = "loadPostProcessor", at = @At("RETURN"))
    // private void valyrix$afterShaderLoad(CallbackInfo ci) {
    //     // custom shader logic here
    // }
}
