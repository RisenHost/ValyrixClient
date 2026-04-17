package com.valyrix.module;

import com.valyrix.module.modules.donutsmp.DonutSMPMacro;
import com.valyrix.module.modules.movement.Sprint;
import com.valyrix.module.modules.visual.Fullbright;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central registry for all {@link Module} instances.
 *
 * Adding a new module:
 *   1. Create your class extending {@link Module}.
 *   2. Call {@code register(new YourModule())} inside {@link #init()}.
 *   Done. The tick loop and keybind polling are automatic.
 */
public class ModuleManager {

    // Raw list — insertion order is registration order.
    private final List<Module> modules = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Initialisation — register ALL modules here
    // -------------------------------------------------------------------------

    public void init() {
        // ── Visual ───────────────────────────────────────────────────────────
        register(new Fullbright());

        // ── Movement ─────────────────────────────────────────────────────────
        register(new Sprint());

        // ── DonutSMP ─────────────────────────────────────────────────────────
        register(new DonutSMPMacro());

        // ── Combat (add your classes here) ───────────────────────────────────
        // register(new KillAura());
        // register(new Reach());

        // ── Utility (add your classes here) ──────────────────────────────────
        // register(new FastPlace());
        // register(new Scaffold());

        // ─────────────────────────────────────────────────────────────────────
        // Register a single END_CLIENT_TICK listener to drive all modules.
        // One event registration vs N registrations — keeps overhead minimal.
        // ─────────────────────────────────────────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (int i = 0; i < modules.size(); i++) {
                Module m = modules.get(i);
                m.checkKeybind();           // Poll bound key
                if (m.isEnabled()) m.onTick(); // Dispatch tick
            }
        });
    }

    // -------------------------------------------------------------------------
    // Registration
    // -------------------------------------------------------------------------

    private void register(Module module) {
        modules.add(module);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** All registered modules regardless of state. */
    public List<Module> getModules() {
        return modules;
    }

    /**
     * Enabled modules sorted by name length DESCENDING.
     * This produces the classic "longest name at top" arraylist look.
     * Result is a new list — safe to iterate without ConcurrentModificationException.
     */
    public List<Module> getEnabledModulesSorted() {
        return modules.stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> m.getName().length()).reversed())
                .collect(Collectors.toList());
    }

    /** Enabled modules filtered to a specific category. */
    public List<Module> getEnabledByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.isEnabled() && m.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Type-safe module lookup.
     *
     * Usage:  Sprint sprint = manager.getModule(Sprint.class);
     *         if (sprint != null && sprint.isEnabled()) { ... }
     */
    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) return (T) m;
        }
        return null;
    }
}
