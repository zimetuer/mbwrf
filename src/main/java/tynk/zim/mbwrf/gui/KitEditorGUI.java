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
    
    // GUI layout
    private static final int GUI_WIDTH = 9;
    private static final int GUI_ROW_COUNT = 6;
    public static final int GUI_SIZE = GUI_WIDTH * GUI_ROW_COUNT;
    
    // Slot definitions
    public static final int KIT_ROW_START = 0;
    public static final int ASSIGN_ROW_START = 9;
    public static final int ASSIGN_SLOT_COUNT = 36;
    public static final int BUTTON_ROW_START = 45;
    public static final int SAVE_SLOT = 48;
    public static final int RESET_SLOT = 50;
    
    private static final String GUI_TITLE = ChatColor.GOLD + "Edit Your Kit";
    
    // Session tracking
    private static final Map<UUID, Integer> selectedKitItemIndex = new HashMap<>();
    private static final Map<UUID, Map<Integer, Integer>> playerSlotMappings = new HashMap<>();
    
    // Cache
    private static List<ItemStack> cachedKitItems = new ArrayList<>();
    private static boolean cacheDirty = true;
    
    // Safe material getter for different Bukkit versions
    private static Material getMaterialSafe(String... names) {
        for (String name : names) {
            Material mat = Material.getMaterial(name);
            if (mat != null) return mat;
        }
        return Material.STONE; // fallback
    }
    
    public static void open(Player player) {
        if (cacheDirty) {
            cachedKitItems = MapConfig.getInstance().getKitItems();
            cacheDirty = false;
        }
        
        PlayerKitData savedData = DatabaseManager.getInstance().getPlayerKitData(player.getUniqueId());
        Map<Integer, Integer> slotMappings = new HashMap<>(savedData.getSlotMappings());
        playerSlotMappings.put(player.getUniqueId(), slotMappings);
        
        selectedKitItemIndex.put(player.getUniqueId(), cachedKitItems.isEmpty() ? -1 : 0);
        
        Inventory inv = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);
        
        // Row 0: Kit items (slots 0-8)
        Material blackPane = getMaterialSafe("BLACK_STAINED_GLASS_PANE", "BLACK_GLASS_PANE", "STAINED_GLASS_PANE");
        for (int i = 0; i < GUI_WIDTH; i++) {
            if (i < cachedKitItems.size()) {
                inv.setItem(i, cachedKitItems.get(i).clone());
            } else {
                inv.setItem(i, createPlaceholder(blackPane, " "));
            }
        }
        
        // Rows 1-4: Assignment slots (slots 9-44)
        Material grayPane = getMaterialSafe("LIGHT_GRAY_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
        for (int assignSlot = 0; assignSlot < ASSIGN_SLOT_COUNT; assignSlot++) {
            int guiSlot = ASSIGN_ROW_START + assignSlot;
            Integer kitIndex = slotMappings.get(assignSlot);
            if (kitIndex != null && kitIndex >= 0 && kitIndex < cachedKitItems.size()) {
                inv.setItem(guiSlot, cachedKitItems.get(kitIndex).clone());
            } else {
                inv.setItem(guiSlot, createSlotPlaceholder());
            }
        }
        
        // Row 5: Buttons
        inv.setItem(SAVE_SLOT, createSaveButton());
        inv.setItem(RESET_SLOT, createResetButton());
        
        player.openInventory(inv);
    }
    
    private static ItemStack createPlaceholder(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + name);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public static ItemStack createSlotPlaceholder() {
        Material grayPane = getMaterialSafe("LIGHT_GRAY_STAINED_GLASS_PANE", "GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE");
        ItemStack item = new ItemStack(grayPane);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createSaveButton() {
        ItemStack item = new ItemStack(Material.EMERALD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Save Kit");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to save your layout");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private static ItemStack createResetButton() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Reset to Default");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to reset to default");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
    
    public static boolean isKitEditorGUI(String title) {
        return GUI_TITLE.equals(title);
    }
    
    public static int getSelectedKitItemIndex(UUID uuid) {
        Integer idx = selectedKitItemIndex.get(uuid);
        return idx != null ? idx : -1;
    }
    
    public static void setSelectedKitItemIndex(UUID uuid, int index) {
        selectedKitItemIndex.put(uuid, index);
    }
    
    public static Map<Integer, Integer> getSlotMappings(UUID uuid) {
        return playerSlotMappings.computeIfAbsent(uuid, k -> new HashMap<>());
    }
    
    public static List<ItemStack> getKitItems() {
        return new ArrayList<>(cachedKitItems);
    }
    
    public static void invalidateCache() {
        cacheDirty = true;
    }
    
    public static void clearSession(UUID uuid) {
        selectedKitItemIndex.remove(uuid);
        playerSlotMappings.remove(uuid);
    }
    

}
