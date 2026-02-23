package tynk.zim.mbwrf;

import de.marcely.bedwars.api.BedwarsAPI;
import org.bukkit.plugin.java.JavaPlugin;
import tynk.zim.mbwrf.database.DatabaseManager;
import tynk.zim.mbwrf.gui.KitGUIListener;

public class MBwRFPlugin extends JavaPlugin {

    private static MBwRFPlugin instance;
    private MBwRFAddon addon;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        DatabaseManager.getInstance().init();

        MapConfig.getInstance().load();

        getServer().getPluginManager().registerEvents(new RespawnListener(), this);
        getServer().getPluginManager().registerEvents(new KitGUIListener(), this);

        getCommand("rushfight").setExecutor(new KitCommand());

        this.addon = new MBwRFAddon(this);
        if (addon.register()) {
            getLogger().info("MBwRF addon registered successfully!");
        } else {
            getLogger().severe("Failed to register MBwRF addon!");
        }

        BedwarsAPI.onReady(() -> {
            addon.registerMode();
        });

        getLogger().info("MBwRF has been enabled!");
    }

    @Override
    public void onDisable() {
        if (addon != null && addon.isRegistered()) {
            addon.unregisterMode();
            addon.unregister();
        }

        DatabaseManager.getInstance().close();

        getLogger().info("MBwRF has been disabled!");
    }

    public static MBwRFPlugin getInstance() {
        return instance;
    }

    public MBwRFAddon getAddon() {
        return addon;
    }
}
