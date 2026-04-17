package com.valyrix.module;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Abstract base class for every Valyrix feature.
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │  Lifecycle hooks (override what you need):                  │
 * │    onEnable()  — fires once when toggled ON                 │
 * │    onDisable() — fires once when toggled OFF                │
 * │    onTick()    — fires every CLIENT tick while enabled      │
 * │    onRender()  — fires every render frame while enabled     │
 * │                   (keep this VERY lightweight — no allocs!) │
 * └─────────────────────────────────────────────────────────────┘
 *
 * Subclass pattern:
 *
 *   public class MyModule extends Module {
 *       public MyModule() {
 *           super("My Module", "What it does.", Category.UTILITY, GLFW.GLFW_KEY_G);
 *       }
 *       @Override public void onEnable()  { ... }
 *       @Override public void onDisable() { ... }
 *       @Override public void onTick()    { ... }
 *   }
 */
public abstract class Module {

    // Shortcut — every subclass can use mc without importing MinecraftClient.
    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    // -------------------------------------------------------------------------
    // Identity
    // -------------------------------------------------------------------------
    private final String   name;
    private final String   description;
    private final Category category;

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------
    private boolean    enabled = false;
    private KeyBinding keyBinding;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Full constructor with a keybind.
     *
     * @param name        Display name (used in arraylist + ClickGUI).
     * @param description Short description for tooltip.
     * @param category    Which {@link Category} this module belongs to.
     * @param keyCode     GLFW key constant, or {@code GLFW.GLFW_KEY_UNKNOWN} for none.
     */
    public Module(String name, String description, Category category, int keyCode) {
        this.name        = name;
        this.description = description;
        this.category    = category;

        if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
            this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.valyrix." + name.toLowerCase().replace(" ", "_"),
                    InputUtil.Type.KEYSYM,
                    keyCode,
                    "category.valyrix.keybinds"
            ));
        }
    }

    /** Convenience constructor — no keybind. */
    public Module(String name, String description, Category category) {
        this(name, description, category, GLFW.GLFW_KEY_UNKNOWN);
    }

    // -------------------------------------------------------------------------
    // Lifecycle hooks — override in subclasses
    // -------------------------------------------------------------------------

    /** Called once per client tick while this module is enabled. */
    public void onTick()    {}

    /** Called when the module transitions from OFF → ON. */
    public void onEnable()  {}

    /** Called when the module transitions from ON → OFF. */
    public void onDisable() {}

    /**
     * Called every render frame while enabled.
     * ⚠ Keep allocation-free — no new ArrayList / StringBuilder here.
     *    Cache objects as fields if you need them.
     */
    public void onRender()  {}

    // -------------------------------------------------------------------------
    // Toggle logic (final — subclasses use hooks, not this)
    // -------------------------------------------------------------------------

    public final void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    /** Idempotent setter — only fires hooks when state actually changes. */
    public final void setEnabled(boolean state) {
        if (enabled != state) toggle();
    }

    /** Called by {@link ModuleManager} each tick to poll the bound key. */
    public final void checkKeybind() {
        if (keyBinding != null && keyBinding.wasPressed()) toggle();
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public String   getName()        { return name; }
    public String   getDescription() { return description; }
    public Category getCategory()    { return category; }
    public boolean  isEnabled()      { return enabled; }
    public KeyBinding getKeyBinding(){ return keyBinding; }
}
