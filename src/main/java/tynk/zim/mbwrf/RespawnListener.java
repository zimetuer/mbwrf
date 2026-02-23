package tynk.zim.mbwrf;

import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.CustomMode;
import de.marcely.bedwars.api.event.player.PlayerIngamePostRespawnEvent;
import de.marcely.bedwars.api.event.player.PlayerIngameRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import tynk.zim.mbwrf.database.DatabaseManager;
import tynk.zim.mbwrf.database.PlayerKitData;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RespawnListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerIngameRespawnEvent event) {
        Arena arena = event.getArena();
        CustomMode customMode = arena.getCustomMode();
        
        if (customMode != null && customMode.getName().equals("rushfight")) {
            event.setGivingItems(false);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPostRespawn(PlayerIngamePostRespawnEvent event) {
        Player player = event.getPlayer();
        Arena arena = event.getArena();
        CustomMode customMode = arena.getCustomMode();
        
        if (customMode != null && customMode.getName().equals("rushfight")) {
            List<ItemStack> kitItems = MapConfig.getInstance().getKitItems();
            ItemStack[] kitArmor = MapConfig.getInstance().getKitArmor();
            
            UUID uuid = player.getUniqueId();
            PlayerKitData kitData = DatabaseManager.getInstance().getPlayerKitData(uuid);
            
            MBwRFPlugin.getInstance().getLogger().info(
                "[RushFight] Applying kit for " + player.getName() + 
                " (UUID: " + uuid + ")" +
                " - hasCustomLayout: " + kitData.hasCustomLayout() + 
                ", mappings: " + kitData.getSlotMappings()
            );
            
            if (kitData.hasCustomLayout()) {
                for (Map.Entry<Integer, Integer> entry : kitData.getSlotMappings().entrySet()) {
                    int slot = entry.getKey();
                    int itemIndex = entry.getValue();
                    
                    if (itemIndex >= 0 && itemIndex < kitItems.size() && slot >= 0 && slot < 36) {
                        player.getInventory().setItem(slot, kitItems.get(itemIndex).clone());
                    }
                }
            } else {
                for (int i = 0; i < kitItems.size() && i < 36; i++) {
                    player.getInventory().setItem(i, kitItems.get(i).clone());
                }
            }
            
            player.getInventory().setArmorContents(kitArmor);
        }
    }
}
