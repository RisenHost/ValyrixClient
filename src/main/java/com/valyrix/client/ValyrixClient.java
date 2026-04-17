package com.valyrix;

import com.valyrix.module.ModuleManager;
import com.valyrix.ui.HudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Valyrix Client â€” Fabric 1.21.1
 * Main entrypoint. Boots subsystems in order: Modules â†’ HUD.
 *
 * Registered in fabric.mod.json under "client" entrypoints.
 * @Environment(CLIENT) guarantees this class never touches the server classpath.
 */
@Environment(EnvType.CLIENT)
public class ValyrixClient implements ClientModInitializer {

    public static final String MOD_ID      = "valyrix";
    public static final String MOD_NAME    = "Valyrix Client";
    public static final String MOD_VERSION = "1.0.0";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    // Singleton â€” safe because this only runs on the client thread.
    private static ValyrixClient INSTANCE;

    private ModuleManager moduleManager;

    // -------------------------------------------------------------------------
    // Entrypoint
    // -------------------------------------------------------------------------

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        LOGGER.info("[{}] Booting v{} on Fabric 1.21.1...", MOD_NAME, MOD_VERSION);

        // 1. Register all modules and their tick/keybind hooks.
        moduleManager = new ModuleManager();
        moduleManager.init();

        // 2. Hook into Fabric's HUD render event.
        HudRenderer.register();

        LOGGER.info("[{}] Ready â€” {} module(s) loaded.", MOD_NAME, moduleManager.getModules().size());
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public static ValyrixClient getInstance()     { return INSTANCE; }
    public ModuleManager getModuleManager()        { return moduleManager; }
}
