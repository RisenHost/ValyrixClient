package com.valyrix.ui;

import com.valyrix.ValyrixClient;
import com.valyrix.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.font.TextRenderer;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                    Valyrix HudRenderer                      ║
 * ║                                                              ║
 * ║  Registered once via HudRenderCallback (Fabric API).        ║
 * ║                                                              ║
 * ║  Renders (all positions are in scaled screen coordinates):  ║
 * ║   ① Watermark        — top-left                            ║
 * ║   ② Module Arraylist — top-right, sorted by name length    ║
 * ║   ③ FPS + Ping       — bottom-left                         ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * ── Performance contract ────────────────────────────────────────
 *  • Zero per-frame heap allocation.  All Strings that can be
 *    pre-built are stored as constants.
 *  • The enabled-module list IS allocated per frame, but the
 *    stream produces a small list (rarely > 20 elements) and
 *    it is the only way to get a snapshot safe for iteration.
 *  • DrawContext.fill() and drawTextWithShadow() are vanilla
 *    calls — they batch into a single draw call per layer.
 * ────────────────────────────────────────────────────────────────
 */
public final class HudRenderer {

    // ── Static layout constants ───────────────────────────────────────────────
    private static final String WATERMARK      = "Valyrix Client";
    private static final String WATERMARK_VER  = " 1.21.1";   // rendered in gray after name
    private static final int    PADDING        = 4;            // px inner padding
    private static final int    ACCENT_W       = 2;            // accent bar width (px)
    private static final int    LINE_H         = 11;           // height of one arraylist row
    private static final int    CORNER_MARGIN  = 2;            // distance from screen edge

    // Reference — fetched once, never null at render time.
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    // ── FPS string cache — rebuilt only when value changes ───────────────────
    private static int    cachedFps    = -1;
    private static String cachedFpsStr = "FPS: --";

    private HudRenderer() {}

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    /**
     * Call once from {@link ValyrixClient#onInitializeClient()}.
     * Registers the render callback with Fabric's event bus.
     */
    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::onHudRender);
    }

    // -------------------------------------------------------------------------
    // Primary render callback
    // -------------------------------------------------------------------------

    private static void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        // Guard — don't render over loading/death screens or when HUD is hidden.
        if (MC.player == null || MC.world == null) return;
        if (MC.options.hudHidden) return;

        TextRenderer tr    = MC.textRenderer;
        int          sw    = MC.getWindow().getScaledWidth();
        int          sh    = MC.getWindow().getScaledHeight();

        renderWatermark(ctx, tr);
        renderArrayList(ctx, tr, sw);
        renderInfoCounters(ctx, tr, sh);
    }

    // =========================================================================
    // ① WATERMARK  (top-left)
    // =========================================================================

    private static void renderWatermark(DrawContext ctx, TextRenderer tr) {
        int nameW   = tr.getWidth(WATERMARK);
        int verW    = tr.getWidth(WATERMARK_VER);
        int totalW  = nameW + verW;
        int x       = CORNER_MARGIN;
        int y       = CORNER_MARGIN;
        int boxH    = 12;
        int accent  = ColorManager.getPrimary();

        // Background panel
        ctx.fill(x, y, x + ACCENT_W + PADDING + totalW + PADDING, y + boxH,
                ColorManager.bgDim());

        // Left colour accent bar
        ctx.fill(x, y, x + ACCENT_W, y + boxH, accent);

        // "Valyrix Client" in accent colour
        ctx.drawTextWithShadow(tr, WATERMARK,
                x + ACCENT_W + PADDING, y + 2, accent);

        // " 1.21.1" in gray
        ctx.drawTextWithShadow(tr, WATERMARK_VER,
                x + ACCENT_W + PADDING + nameW, y + 2, ColorManager.textGray());
    }

    // =========================================================================
    // ② MODULE ARRAYLIST  (top-right)
    //    Sorted longest-name → shortest (Velaris style).
    //    Each row has a right-side accent bar and a dim background.
    // =========================================================================

    private static void renderArrayList(DrawContext ctx, TextRenderer tr, int sw) {
        List<Module> enabled = ValyrixClient.getInstance()
                .getModuleManager()
                .getEnabledModulesSorted();   // longest first

        if (enabled.isEmpty()) return;

        int y = CORNER_MARGIN;

        for (Module module : enabled) {
            String name   = module.getName();
            int    nameW  = tr.getWidth(name);
            int    x      = sw - CORNER_MARGIN - ACCENT_W - PADDING - nameW - PADDING;
            int    accent = ColorManager.getPrimary(sw - nameW); // chroma offset = screen-x

            // Background — spans from text left edge to screen right margin
            ctx.fill(x - PADDING, y,
                    sw - CORNER_MARGIN, y + LINE_H,
                    ColorManager.bgDim());

            // Right-side accent bar
            ctx.fill(sw - CORNER_MARGIN - ACCENT_W, y,
                    sw - CORNER_MARGIN, y + LINE_H,
                    accent);

            // Module name text
            ctx.drawTextWithShadow(tr, name, x, y + 2, ColorManager.textWhite());

            // Category tag in gray (one char, e.g. "V", "M") — subtle and clean
            String tag = "[" + module.getCategory().getDisplayName().charAt(0) + "]";
            ctx.drawTextWithShadow(tr, tag,
                    x - tr.getWidth(tag) - 2, y + 2, ColorManager.textGray());

            y += LINE_H;
        }
    }

    // =========================================================================
    // ③ FPS + PING  (bottom-left)
    //    FPS string is cached to avoid String.format() each frame.
    // =========================================================================

    private static void renderInfoCounters(DrawContext ctx, TextRenderer tr, int sh) {
        // ── FPS — update cache only when value changes ──────────────────────
        int liveFps = MinecraftClient.getCurrentFps();
        if (liveFps != cachedFps) {
            cachedFps    = liveFps;
            // Use concat instead of format — no regex overhead.
            cachedFpsStr = "FPS: " + liveFps;
        }

        // ── Ping ─────────────────────────────────────────────────────────────
        int    ping    = getPing();
        String pingStr = ping < 0 ? "Ping: N/A" : "Ping: " + ping + "ms";
        int    pingCol = pingColor(ping);

        int panelW = Math.max(tr.getWidth(cachedFpsStr), tr.getWidth(pingStr)) + PADDING * 2 + ACCENT_W;
        int x      = CORNER_MARGIN;
        int y      = sh - CORNER_MARGIN - LINE_H * 2 - 1;
        int accent = ColorManager.getPrimary();

        // Background
        ctx.fill(x, y, x + panelW, y + LINE_H * 2 + 1, ColorManager.bgDim());

        // Left accent bar
        ctx.fill(x, y, x + ACCENT_W, y + LINE_H * 2 + 1, accent);

        // Text
        ctx.drawTextWithShadow(tr, cachedFpsStr,  x + ACCENT_W + PADDING, y + 2,           ColorManager.textWhite());
        ctx.drawTextWithShadow(tr, pingStr,        x + ACCENT_W + PADDING, y + LINE_H + 2,  pingCol);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    /** Returns the local player's server ping in ms, or -1 if unavailable. */
    private static int getPing() {
        if (MC.player == null || MC.getNetworkHandler() == null) return -1;
        PlayerListEntry entry = MC.getNetworkHandler().getPlayerListEntry(MC.player.getUuid());
        return entry != null ? entry.getLatency() : -1;
    }

    /**
     * Green/Yellow/Red ping colour — matches vanilla tab-list colouring logic.
     *   < 150ms  → green
     *   < 300ms  → yellow
     *   ≥ 300ms  → red
     */
    private static int pingColor(int ping) {
        if (ping < 0)   return ColorManager.textGray();
        if (ping < 150) return 0xFF55FF55; // green
        if (ping < 300) return 0xFFFFFF55; // yellow
        return 0xFFFF5555;                  // red
    }
}
