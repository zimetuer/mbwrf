package tynk.zim.mbwrf.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import tynk.zim.mbwrf.MapConfig;
import tynk.zim.mbwrf.database.DatabaseManager;
import tynk.zim.mbwrf.database.PlayerKitData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitEditorGUI {
    
    public static final String GUI_TITLE = ChatColor.GOLD + "Edit Your Kit";
    public static final int GUI_SIZE = 45;
    
    public static final int SAVE_SLOT = 40;
    public static final int RESET_SLOT = 38;
    public static final int HELMET_SLOT = 36;
    public static final int CHESTPLATE_SLOT = 37;
    public static final int LEGGINGS_SLOT = 42;
    public static final int BOOTS_SLOT = 43;
    
    private static final Map<UUID, Map<Integer, ItemStack>> editingSessions = new HashMap<>();
    
    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        
        PlayerKitData kitData = DatabaseManager.getInstance().getPlayerKitData(player.getUniqueId());
        List<ItemStack> kitItems = MapConfig.getInstance().getKitItems();
        ItemStack[] kitArmor = MapConfig.getInstance().getKitArmor();
        
        Map<Integer, ItemStack> sessionItems = new HashMap<>();
        
        if (kitData.hasCustomLayout()) {
            for (Map.Entry<Integer, Integer> entry : kitData.getSlotMappings().entrySet()) {
                int slot = entry.getKey();
                int itemIndex = entry.getValue();
                
                if (itemIndex >= 0 && itemIndex < kitItems.size() && slot >= 0 && slot < 36) {
                    ItemStack item = kitItems.get(itemIndex).clone();
                    gui.setItem(slot, item);
                    sessionItems.put(slot, item);
                }
            }
        } else {
            for (int i = 0; i < kitItems.size() && i < 36; i++) {
                ItemStack item = kitItems.get(i).clone();
                gui.setItem(i, item);
                sessionItems.put(i, item);
            }
        }
        
        editingSessions.put(player.getUniqueId(), sessionItems);
        
        if (kitArmor[3] != null) gui.setItem(HELMET_SLOT, kitArmor[3].clone());
        if (kitArmor[2] != null) gui.setItem(CHESTPLATE_SLOT, kitArmor[2].clone());
        if (kitArmor[1] != null) gui.setItem(LEGGINGS_SLOT, kitArmor[1].clone());
        if (kitArmor[0] != null) gui.setItem(BOOTS_SLOT, kitArmor[0].clone());
        
        gui.setItem(SAVE_SLOT, createSaveButton());
        gui.setItem(RESET_SLOT, createResetButton());
        
        player.openInventory(gui);
    }
    
    private static ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Save Kit");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to save your kit layout");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private static ItemStack createResetButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Reset to Default");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Click to reset your kit");
        lore.add(ChatColor.GRAY + "to the default layout");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    public static boolean isKitEditorGUI(String title) {
        return GUI_TITLE.equals(title);
    }
    
    public static Map<Integer, ItemStack> getSessionItems(UUID uuid) {
        return editingSessions.get(uuid);
    }
    
    public static void removeSession(UUID uuid) {
        editingSessions.remove(uuid);
    }
    
    public static boolean isActionButton(int slot) {
        return slot == SAVE_SLOT || slot == RESET_SLOT || 
               slot == HELMET_SLOT || slot == CHESTPLATE_SLOT || 
               slot == LEGGINGS_SLOT || slot == BOOTS_SLOT;
    }
}
