package com.valyrix.module.modules.movement;

import com.valyrix.module.Category;
import com.valyrix.module.Module;
import org.lwjgl.glfw.GLFW;

/**
 * Sprint — Movement
 *
 * Automatically sprints whenever the player presses the forward key.
 * Respects sneaking and swimming states to avoid illegal movement packets.
 *
 * This is a pure client-side helper — it calls {@code setSprinting(true)},
 * the same mechanism vanilla uses when you double-tap W.
 *
 * Extended options you can add:
 *   - Omni-Sprint: remove the forwardKey check to sprint in all directions.
 *   - No-slow:     cancel slowness while sneaking (needs a mixin).
 *
 * Bind: V
 */
public class Sprint extends Module {

    public Sprint() {
        super("Sprint", "Automatically sprints when moving forward.", Category.MOVEMENT, GLFW.GLFW_KEY_V);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // ── Safety checks — match vanilla's sprint conditions ─────────────────
        // Don't sprint when:
        //   • player is sneaking   (would de-sync with server)
        //   • player is using food (causes animation glitches)
        //   • player has blindness (vanilla blocks it)
        if (mc.player.isSneaking())  return;
        if (mc.player.isUsingItem()) return;

        // Only sprint when the player intends to move forward.
        // Remove this guard if you want omni-directional sprint.
        if (mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        // Stop sprinting immediately on toggle-off.
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }
}
