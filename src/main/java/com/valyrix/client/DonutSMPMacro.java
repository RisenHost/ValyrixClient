package com.valyrix.module.modules.donutsmp;

import com.valyrix.module.Category;
import com.valyrix.module.Module;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * DonutSMP Macro — DonutSMP category
 *
 * A "one-shot" macro template for server-specific shortcuts.
 * On activation it fires a sequence of commands/messages, then disables itself.
 *
 * ── How to extend ────────────────────────────────────────────────
 *  Extend this class (or copy-paste it) for each macro you need:
 *
 *    public class WarpNetherMacro extends DonutSMPMacro {
 *        public WarpNetherMacro() {
 *            super("Warp Nether", "Warps to the Nether hub.", GLFW.GLFW_KEY_N);
 *        }
 *        @Override
 *        protected void execute() {
 *            sendCommand("warp nether");
 *        }
 *    }
 *
 *  Then register it in ModuleManager.init():
 *    register(new WarpNetherMacro());
 * ─────────────────────────────────────────────────────────────────
 *
 * Bind: H
 */
public class DonutSMPMacro extends Module {

    // Cooldown guard — prevents spam when the key is held.
    private long lastFiredAt = 0L;
    private static final long COOLDOWN_MS = 2_000L; // 2 seconds

    public DonutSMPMacro() {
        super("Warp Hub", "Warps to the DonutSMP hub.", Category.DONUTSMP, GLFW.GLFW_KEY_H);
    }

    // ── Subclass constructor convenience ─────────────────────────────────────

    protected DonutSMPMacro(String name, String description, int keyCode) {
        super(name, description, Category.DONUTSMP, keyCode);
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onEnable() {
        long now = System.currentTimeMillis();

        if (now - lastFiredAt < COOLDOWN_MS) {
            // Cooldown not elapsed — give feedback and abort.
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("§c[Valyrix] Macro on cooldown!"), true
                );
            }
            setEnabled(false);
            return;
        }

        execute();
        lastFiredAt = now;

        // One-shot — disable immediately after firing.
        setEnabled(false);
    }

    // -------------------------------------------------------------------------
    // Override this in subclasses to define what the macro does.
    // -------------------------------------------------------------------------

    protected void execute() {
        // ── Example sequence for DonutSMP hub warp ───────────────────────────
        sendCommand("warp hub");

        // Sending a chat message (visible to other players):
        // sendChat("gg");

        // Sending multiple commands with a delay requires scheduling.
        // Use a tick counter in onTick() for timing, or a Scheduler utility.
        // Example (pseudo):
        //   scheduleCommand("spawn", 20);   // fire after 20 ticks (~1 second)
    }

    // -------------------------------------------------------------------------
    // Utilities — available to all subclasses
    // -------------------------------------------------------------------------

    /**
     * Sends a slash command without the '/' prefix.
     * Uses the modern 1.19+ chat command packet (not chat message packet).
     */
    protected final void sendCommand(String command) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatCommand(command);
        }
    }

    /**
     * Sends a visible chat message.
     * ⚠ Subject to server-side chat reporting — use sparingly.
     */
    protected final void sendChat(String message) {
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatMessage(message);
        }
    }

    /**
     * Displays a local action bar message (only visible to the client).
     * Use for feedback that doesn't need to go to the server.
     */
    protected final void showActionBar(String message) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(message), true);
        }
    }
}
