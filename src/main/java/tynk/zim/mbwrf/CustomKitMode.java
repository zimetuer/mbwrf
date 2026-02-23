package tynk.zim.mbwrf;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.CustomMode;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

public class CustomKitMode extends CustomMode implements Listener {

    private final Plugin plugin;

    public CustomKitMode(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "rushfight";
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public void onRegister() {
        Collection<Arena> arenas = GameAPI.get().getArenas();
        int applied = 0;
        
        for (Arena arena : arenas) {
            if (MapConfig.getInstance().isEnabledMap(arena.getName())) {
                if (attemptSet(arena)) {
                    applied++;
                    plugin.getLogger().info("Applied RushFight mode to arena: " + arena.getName());
                } else {
                    plugin.getLogger().warning("Failed to apply RushFight mode to arena: " + arena.getName());
                }
            }
        }
        
        plugin.getLogger().info("RushFight mode registered. Applied to " + applied + " arenas out of " + arenas.size() + " total.");
    }

    @Override
    public void onUnregister() {
        plugin.getLogger().info("RushFight mode unregistered.");
    }

    public boolean applyToArena(Arena arena) {
        return attemptSet(arena);
    }

    public boolean removeFromArena(Arena arena) {
        return unset(arena);
    }
}
