package tynk.zim.mbwrf;

import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.CustomMode;
import de.marcely.bedwars.api.arena.Team;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.event.player.PlayerIngamePostRespawnEvent;
import de.marcely.bedwars.api.event.player.PlayerIngameRespawnEvent;
import org.bukkit.DyeColor;
import org.bukkit.Material;
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
    
    private ItemStack applyTeamColorToWool(ItemStack item, Team team) {
        if (item == null || team == null) return item;
        
        Material material = item.getType();
        String materialName = material.name();
        
        // Check if it's any wool material
        if (materialName.endsWith("_WOOL")) {
            DyeColor teamColor = team.getDyeColor();
            if (teamColor != null) {
                String teamWoolName = teamColor.name() + "_WOOL";
                Material teamWoolMaterial = Material.getMaterial(teamWoolName);
                if (teamWoolMaterial != null && teamWoolMaterial != material) {
                    ItemStack coloredItem = item.clone();
                    coloredItem.setType(teamWoolMaterial);
                    return coloredItem;
                }
            }
        }
        
        return item;
    }

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
            
            Team team = arena.getPlayerTeam(player);
            
            MBwRFPlugin.getInstance().getLogger().info(
                "[RushFight] Applying kit for " + player.getName() + 
                " (UUID: " + uuid + ")" +
                " - hasCustomLayout: " + kitData.hasCustomLayout() + 
                ", mappings: " + kitData.getSlotMappings() +
                ", team: " + (team != null ? team.name() : "null")
            );
            
            if (kitData.hasCustomLayout()) {
                for (Map.Entry<Integer, Integer> entry : kitData.getSlotMappings().entrySet()) {
                    int slot = entry.getKey();
                    int itemIndex = entry.getValue();
                    
                    if (itemIndex >= 0 && itemIndex < kitItems.size() && slot >= 0 && slot < 36) {
                        ItemStack originalItem = kitItems.get(itemIndex).clone();
                        ItemStack coloredItem = applyTeamColorToWool(originalItem, team);
                        player.getInventory().setItem(slot, coloredItem);
                    }
                }
            } else {
                for (int i = 0; i < kitItems.size() && i < 36; i++) {
                    ItemStack originalItem = kitItems.get(i).clone();
                    ItemStack coloredItem = applyTeamColorToWool(originalItem, team);
                    player.getInventory().setItem(i, coloredItem);
                }
            }
            
            player.getInventory().setArmorContents(kitArmor);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRoundStart(RoundStartEvent event) {
        Arena arena = event.getArena();
        CustomMode customMode = arena.getCustomMode();
        
        if (customMode != null && customMode.getName().equals("rushfight")) {
            event.setGivingItems(false);
            
            List<ItemStack> kitItems = MapConfig.getInstance().getKitItems();
            ItemStack[] kitArmor = MapConfig.getInstance().getKitArmor();
            
            for (Player player : arena.getPlayers()) {
                Team team = arena.getPlayerTeam(player);
                UUID uuid = player.getUniqueId();
                PlayerKitData kitData = DatabaseManager.getInstance().getPlayerKitData(uuid);
                
                MBwRFPlugin.getInstance().getLogger().info(
                    "[RushFight] Applying kit for " + player.getName() + 
                    " (UUID: " + uuid + ")" +
                    " - hasCustomLayout: " + kitData.hasCustomLayout() + 
                    ", mappings: " + kitData.getSlotMappings() +
                    ", team: " + (team != null ? team.name() : "null")
                );
                
                if (kitData.hasCustomLayout()) {
                    for (Map.Entry<Integer, Integer> entry : kitData.getSlotMappings().entrySet()) {
                        int slot = entry.getKey();
                        int itemIndex = entry.getValue();
                        
                        if (itemIndex >= 0 && itemIndex < kitItems.size() && slot >= 0 && slot < 36) {
                            ItemStack originalItem = kitItems.get(itemIndex).clone();
                            ItemStack coloredItem = applyTeamColorToWool(originalItem, team);
                            player.getInventory().setItem(slot, coloredItem);
                        }
                    }
                } else {
                    for (int i = 0; i < kitItems.size() && i < 36; i++) {
                        ItemStack originalItem = kitItems.get(i).clone();
                        ItemStack coloredItem = applyTeamColorToWool(originalItem, team);
                        player.getInventory().setItem(i, coloredItem);
                    }
                }
                
                player.getInventory().setArmorContents(kitArmor);
            }
        }
    }
}
