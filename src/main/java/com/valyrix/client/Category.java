package com.valyrix.module;

/**
 * Module categories shown in the ClickGUI and used for arraylist grouping.
 * Add new categories here — no other file needs to change.
 */
public enum Category {
    COMBAT   ("Combat",   "⚔"),
    MOVEMENT ("Movement", "🏃"),
    VISUAL   ("Visual",   "👁"),
    UTILITY  ("Utility",  "🔧"),
    DONUTSMP ("DonutSMP", "🍩"); // Server-specific macros / shortcuts

    private final String displayName;
    private final String icon;        // for future ClickGUI use

    Category(String displayName, String icon) {
        this.displayName = displayName;
        this.icon        = icon;
    }

    public String getDisplayName() { return displayName; }
    public String getIcon()        { return icon; }
}
