package com.valyrix.module.modules.visual;

import com.valyrix.module.Category;
import com.valyrix.module.Module;
import org.lwjgl.glfw.GLFW;

/**
 * Fullbright — Visual
 *
 * Raises Minecraft's gamma to an extreme value, eliminating all darkness.
 * Restores the previous gamma value when disabled.
 *
 * No mixin required: vanilla exposes gamma as a {@code SimpleOption<Double>}
 * accessible through {@code mc.options.getGamma()}.
 *
 * Bind: F  (change in constructor)
 */
public class Fullbright extends Module {

    // Gamma value applied when active. 100.0 = "No Darkness" equivalent.
    private static final double FULLBRIGHT_GAMMA = 100.0;

    // Stores the player's original gamma so we can restore it on disable.
    private double previousGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Removes all darkness from the world.", Category.VISUAL, GLFW.GLFW_KEY_F);
    }

    @Override
    public void onEnable() {
        // Capture current gamma before overriding it.
        previousGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(FULLBRIGHT_GAMMA);
    }

    @Override
    public void onDisable() {
        // Restore the player's original preference.
        mc.options.getGamma().setValue(previousGamma);
    }

    // ── Optional: keep gamma locked even if the user moves the slider ─────────
    // Uncomment if you want the module to re-apply every tick.
    //
    // @Override
    // public void onTick() {
    //     if (mc.options.getGamma().getValue() != FULLBRIGHT_GAMMA) {
    //         mc.options.getGamma().setValue(FULLBRIGHT_GAMMA);
    //     }
    // }
}
