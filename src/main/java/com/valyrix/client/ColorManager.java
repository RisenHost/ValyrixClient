package com.valyrix.ui;

import java.awt.Color;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║                   Valyrix ColorManager                      ║
 * ║                                                              ║
 * ║  Change the entire client's look from ONE place.            ║
 * ║                                                              ║
 * ║  Quick-start:                                                ║
 * ║    • Solid colour  → setPrimaryColor(0xFF7B52FF)            ║
 * ║    • Chroma        → setChromaEnabled(true)                 ║
 * ║    • Chroma speed  → setChromaSpeed(1500f)  (ms/cycle)      ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * All colours are ARGB ints (0xAARRGGBB) to match DrawContext's API.
 *
 * Performance note: getChromaColor() calls Color.getHSBColor() once per
 * element per frame. This is a tiny float→int conversion — no GC pressure.
 * Do NOT cache the result across frames; that would break the animation.
 */
public final class ColorManager {

    // ── Default theme — Velaris-style deep purple ─────────────────────────────
    private static int     primaryColor  = 0xFF7B52FF;   // ARGB
    private static boolean chromaEnabled = false;
    private static float   chromaSpeed   = 2000f;         // milliseconds per full hue rotation
    private static float   chromaSat     = 0.80f;         // saturation  (0–1)
    private static float   chromaBri     = 1.00f;         // brightness  (0–1)

    // Pre-computed constants — avoids repeated boxing in the render loop.
    private static final int BG_DIM      = 0x80000000;   // 50 % black panel background
    private static final int BG_DARK     = 0xB0000000;   // 69 % black (darker panels)
    private static final int TEXT_WHITE  = 0xFFFFFFFF;
    private static final int TEXT_GRAY   = 0xFFAAAAAA;

    // Singleton utility — no instances needed.
    private ColorManager() {}

    // -------------------------------------------------------------------------
    // Primary colour — returns chroma if enabled, solid colour otherwise.
    // -------------------------------------------------------------------------

    /** Accent colour at x-offset 0 (e.g. watermark, accent bars). */
    public static int getPrimary() {
        return chromaEnabled ? chroma(0) : primaryColor;
    }

    /**
     * Accent colour with an x-pixel offset for flowing chroma across the screen.
     * Pass the element's screen-x so each module tag gets a slightly different hue.
     *
     * @param xOffset screen-x pixel offset (higher value = phase-shifted colour)
     */
    public static int getPrimary(int xOffset) {
        return chromaEnabled ? chroma(xOffset) : primaryColor;
    }

    // -------------------------------------------------------------------------
    // Background helpers
    // -------------------------------------------------------------------------

    /** Semi-transparent dark background for HUD panels. */
    public static int bgDim()  { return BG_DIM; }

    /** Darker variant for nested panels / shadows. */
    public static int bgDark() { return BG_DARK; }

    // -------------------------------------------------------------------------
    // Text helpers
    // -------------------------------------------------------------------------

    public static int textWhite() { return TEXT_WHITE; }
    public static int textGray()  { return TEXT_GRAY;  }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    /**
     * Override the alpha channel of any ARGB colour.
     *
     * @param color  source ARGB colour
     * @param alpha  0 (transparent) … 255 (opaque)
     */
    public static int withAlpha(int color, int alpha) {
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    /**
     * Interpolate linearly between two ARGB colours.
     *
     * @param a   start colour
     * @param b   end colour
     * @param t   blend factor 0.0 (full a) … 1.0 (full b)
     */
    public static int lerp(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF, aa = (a >> 24) & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF, ba = (b >> 24) & 0xFF;
        int r  = (int)(ar + (br - ar) * t);
        int g  = (int)(ag + (bg - ag) * t);
        int bv = (int)(ab + (bb - ab) * t);
        int av = (int)(aa + (ba - aa) * t);
        return (av << 24) | (r << 16) | (g << 8) | bv;
    }

    // -------------------------------------------------------------------------
    // Chroma engine
    // -------------------------------------------------------------------------

    /**
     * Produces a smooth rainbow colour that cycles over {@link #chromaSpeed} ms.
     * Each x-pixel offset shifts the hue slightly, creating a flowing left→right wave.
     */
    private static int chroma(int xOffset) {
        long   now  = System.currentTimeMillis();
        float  hue  = ((now + (long)(xOffset * 8)) % (long) chromaSpeed) / chromaSpeed;
        Color  c    = Color.getHSBColor(hue, chromaSat, chromaBri);
        return 0xFF000000 | (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
    }

    // -------------------------------------------------------------------------
    // Configuration setters
    // -------------------------------------------------------------------------

    /** Set the solid accent colour (ARGB int, e.g. 0xFF7B52FF). */
    public static void setPrimaryColor(int argb)         { primaryColor  = argb;    }

    /** Toggle chroma / rainbow mode. */
    public static void setChromaEnabled(boolean enabled) { chromaEnabled = enabled; }

    /** Set how many milliseconds one full colour rotation takes. Default: 2000. */
    public static void setChromaSpeed(float ms)          { chromaSpeed   = ms;      }

    /** Set chroma saturation (0.0 – 1.0). Default: 0.8. */
    public static void setChromaSaturation(float sat)    { chromaSat     = sat;     }

    /** Set chroma brightness (0.0 – 1.0). Default: 1.0. */
    public static void setChromaBrightness(float bri)    { chromaBri     = bri;     }

    // Getters
    public static int     getPrimaryRaw()     { return primaryColor;  }
    public static boolean isChromaEnabled()   { return chromaEnabled; }
    public static float   getChromaSpeed()    { return chromaSpeed;   }
}
