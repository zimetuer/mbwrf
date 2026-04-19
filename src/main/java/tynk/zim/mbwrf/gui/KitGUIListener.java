package tynk.zim.mbwrf.gui;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import tynk.zim.mbwrf.database.DatabaseManager;
import tynk.zim.mbwrf.database.PlayerKitData;

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
        
        UUID uuid = player.getUniqueId();
        
        // Save button
        if (slot == KitEditorGUI.SAVE_SLOT) {
            handleSave(player);
            player.closeInventory();
            return;
        }
        
        // Reset button
        if (slot == KitEditorGUI.RESET_SLOT) {
            handleReset(player);
            KitEditorGUI.open(player);
            return;
        }
        
        // Kit item selection (top row: slots 0-8)
        if (slot >= KitEditorGUI.KIT_ROW_START && slot < KitEditorGUI.KIT_ROW_START + 9) {
            handleKitItemSelect(player, slot);
            return;
        }
        
        // Assignment slots (rows 1-4: slots 9-44)
        if (slot >= KitEditorGUI.ASSIGN_ROW_START && slot < KitEditorGUI.ASSIGN_ROW_START + KitEditorGUI.ASSIGN_SLOT_COUNT) {
            handleAssignmentSlotClick(player, slot);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (KitEditorGUI.isKitEditorGUI(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (KitEditorGUI.isKitEditorGUI(event.getView().getTitle())) {
            KitEditorGUI.clearSession(event.getPlayer().getUniqueId());
        }
    }
    
    private void handleKitItemSelect(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        List<ItemStack> kitItems = KitEditorGUI.getKitItems();
        
        int kitIndex = slot;
        if (kitIndex < 0 || kitIndex >= kitItems.size()) {
            return;
        }
        
        KitEditorGUI.setSelectedKitItemIndex(uuid, kitIndex);
        player.sendMessage(ChatColor.GREEN + "Selected: " + kitItems.get(kitIndex).getType().name());
    }
    
    private void handleAssignmentSlotClick(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        Map<Integer, Integer> slotMappings = KitEditorGUI.getSlotMappings(uuid);
        List<ItemStack> kitItems = KitEditorGUI.getKitItems();
        int selectedIndex = KitEditorGUI.getSelectedKitItemIndex(uuid);
        
        int inventorySlot = slot - KitEditorGUI.ASSIGN_ROW_START;
        
        // If clicking the same slot that already has this item, clear it
        if (slotMappings.containsKey(inventorySlot) && slotMappings.get(inventorySlot) == selectedIndex) {
            slotMappings.remove(inventorySlot);
            player.getOpenInventory().getTopInventory().setItem(slot, KitEditorGUI.createSlotPlaceholder());
            player.sendMessage(ChatColor.YELLOW + "Cleared inventory slot " + inventorySlot);
            return;
        }
        
        // If item is already placed elsewhere, remove it from that slot first
        if (selectedIndex >= 0 && selectedIndex < kitItems.size()) {
            Integer existingSlot = null;
            for (Map.Entry<Integer, Integer> entry : slotMappings.entrySet()) {
                if (entry.getValue() == selectedIndex) {
                    existingSlot = entry.getKey();
                    break;
                }
            }
            
            if (existingSlot != null && existingSlot != inventorySlot) {
                slotMappings.remove(existingSlot);
                int existingGuiSlot = KitEditorGUI.ASSIGN_ROW_START + existingSlot;
                player.getOpenInventory().getTopInventory().setItem(existingGuiSlot, KitEditorGUI.createSlotPlaceholder());
                player.sendMessage(ChatColor.YELLOW + "Removed from slot " + existingSlot);
            }
        }
        
        // Check if slot already has something (different item) - replace it
        if (slotMappings.containsKey(inventorySlot)) {
            slotMappings.remove(inventorySlot);
        }
        
        if (selectedIndex < 0 || selectedIndex >= kitItems.size()) {
            player.sendMessage(ChatColor.RED + "Select a kit item first (click the top row)!");
            return;
        }
        
        // Assign selected item to this slot
        slotMappings.put(inventorySlot, selectedIndex);
        player.getOpenInventory().getTopInventory().setItem(slot, kitItems.get(selectedIndex).clone());
        player.sendMessage(ChatColor.GREEN + "Inventory slot " + inventorySlot + " → " + kitItems.get(selectedIndex).getType().name());
    }
    
    private void handleSave(Player player) {
        UUID uuid = player.getUniqueId();
        Map<Integer, Integer> slotMappings = KitEditorGUI.getSlotMappings(uuid);
        
        PlayerKitData kitData = new PlayerKitData();
        for (Map.Entry<Integer, Integer> entry : slotMappings.entrySet()) {
            kitData.setSlotMapping(entry.getKey(), entry.getValue());
        }
        
        DatabaseManager.getInstance().savePlayerKitData(uuid, kitData);
        
        player.sendMessage(ChatColor.GREEN + "Kit saved!");
        player.sendMessage(ChatColor.GRAY + "It will be applied in RushFight matches.");
    }
    
    private void handleReset(Player player) {
        UUID uuid = player.getUniqueId();
        DatabaseManager.getInstance().deletePlayerKitData(uuid);
        KitEditorGUI.clearSession(uuid);
        player.sendMessage(ChatColor.YELLOW + "Reset to default. Reopening...");
        KitEditorGUI.open(player);
    }
}
