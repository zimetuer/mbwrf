package tynk.zim.mbwrf.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tynk.zim.mbwrf.MapConfig;
import tynk.zim.mbwrf.database.DatabaseManager;
import tynk.zim.mbwrf.database.PlayerKitData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitGUIListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!KitEditorGUI.isKitEditorGUI(event.getView().getTitle())) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        
        if (slot < 0 || slot >= KitEditorGUI.GUI_SIZE) {
            return;
        }
        
        if (slot == KitEditorGUI.SAVE_SLOT) {
            handleSave(player, event.getInventory());
            player.closeInventory();
            return;
        }
        
        if (slot == KitEditorGUI.RESET_SLOT) {
            handleReset(player);
            player.closeInventory();
            KitEditorGUI.open(player);
            return;
        }
        
        if (slot >= 0 && slot < 36) {
            handleSlotClick(player, slot, event.getInventory());
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!KitEditorGUI.isKitEditorGUI(event.getView().getTitle())) {
            return;
        }
        
        for (int slot : event.getRawSlots()) {
            if (KitEditorGUI.isActionButton(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!KitEditorGUI.isKitEditorGUI(event.getView().getTitle())) {
            return;
        }
        
        KitEditorGUI.removeSession(event.getPlayer().getUniqueId());
    }
    
    private void handleSlotClick(Player player, int slot, Inventory gui) {
        List<ItemStack> kitItems = MapConfig.getInstance().getKitItems();
        Map<Integer, ItemStack> sessionItems = KitEditorGUI.getSessionItems(player.getUniqueId());
        
        if (sessionItems == null) {
            return;
        }
        
        int nextIndex = findNextKitIndex(sessionItems, kitItems);
        
        if (nextIndex >= kitItems.size()) {
            player.sendMessage(org.bukkit.ChatColor.RED + "No more kit items available!");
            return;
        }
        
        if (sessionItems.containsKey(slot)) {
            sessionItems.remove(slot);
            gui.setItem(slot, null);
        } else {
            ItemStack item = kitItems.get(nextIndex).clone();
            gui.setItem(slot, item);
            sessionItems.put(slot, item);
        }
    }
    
    private int findNextKitIndex(Map<Integer, ItemStack> sessionItems, List<ItemStack> kitItems) {
        List<ItemStack> usedItems = new ArrayList<>(sessionItems.values());
        
        for (int i = 0; i < kitItems.size(); i++) {
            boolean used = false;
            for (ItemStack usedItem : usedItems) {
                if (isSameItem(kitItems.get(i), usedItem)) {
                    used = true;
                    break;
                }
            }
            if (!used) {
                return i;
            }
        }
        
        return kitItems.size();
    }
    
    private boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        return a.getType() == b.getType() && a.getAmount() == b.getAmount();
    }
    
    private void handleSave(Player player, Inventory gui) {
        PlayerKitData kitData = new PlayerKitData();
        
        Map<Integer, ItemStack> sessionItems = KitEditorGUI.getSessionItems(player.getUniqueId());
        List<ItemStack> kitItems = MapConfig.getInstance().getKitItems();
        
        if (sessionItems != null) {
            for (Map.Entry<Integer, ItemStack> entry : sessionItems.entrySet()) {
                int slot = entry.getKey();
                ItemStack item = entry.getValue();
                
                for (int i = 0; i < kitItems.size(); i++) {
                    if (isSameItem(kitItems.get(i), item)) {
                        kitData.setSlotMapping(slot, i);
                        break;
                    }
                }
            }
        }
        
        tynk.zim.mbwrf.MBwRFPlugin.getInstance().getLogger().info(
            "[RushFight] Saving kit for " + player.getName() + 
            " (UUID: " + player.getUniqueId() + ")" +
            " - mappings: " + kitData.getSlotMappings()
        );
        
        DatabaseManager.getInstance().savePlayerKitData(player.getUniqueId(), kitData);
        player.sendMessage(org.bukkit.ChatColor.GREEN + "Kit layout saved successfully!");
    }
    
    private void handleReset(Player player) {
        DatabaseManager.getInstance().deletePlayerKitData(player.getUniqueId());
        player.sendMessage(org.bukkit.ChatColor.YELLOW + "Kit reset to default layout.");
    }
}
