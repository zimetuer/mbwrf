package tynk.zim.mbwrf;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapConfig {

    private static MapConfig instance;

    private final Set<String> enabledMaps;
    private final List<ItemStack> kitItems;
    private final ItemStack[] kitArmor;

    private MapConfig() {
        this.enabledMaps = new HashSet<>();
        this.kitItems = new ArrayList<>();
        this.kitArmor = new ItemStack[4];
    }

    public static MapConfig getInstance() {
        if (instance == null) {
            instance = new MapConfig();
        }
        return instance;
    }

    public void load() {
        enabledMaps.clear();
        kitItems.clear();
        for (int i = 0; i < kitArmor.length; i++) {
            kitArmor[i] = null;
        }

        List<String> maps = MBwRFPlugin.getInstance().getConfig().getStringList("enabled-maps");
        for (String map : maps) {
            enabledMaps.add(map.toLowerCase());
        }

        ConfigurationSection kitSection = MBwRFPlugin.getInstance().getConfig().getConfigurationSection("kit.items");
        if (kitSection != null) {
            for (String key : kitSection.getKeys(false)) {
                ConfigurationSection itemSection = kitSection.getConfigurationSection(key);
                if (itemSection != null) {
                    ItemStack item = parseItem(itemSection);
                    if (item != null) {
                        kitItems.add(item);
                    }
                }
            }
        }

        ConfigurationSection armorSection = MBwRFPlugin.getInstance().getConfig().getConfigurationSection("kit.armor");
        if (armorSection != null) {
            loadArmorPiece(armorSection, "helmet", 3);
            loadArmorPiece(armorSection, "chestplate", 2);
            loadArmorPiece(armorSection, "leggings", 1);
            loadArmorPiece(armorSection, "boots", 0);
        }

        int armorCount = 0;
        for (ItemStack a : kitArmor) {
            if (a != null) armorCount++;
        }

        MBwRFPlugin.getInstance().getLogger().info(
            "Loaded " + enabledMaps.size() + " enabled maps, " + kitItems.size() + " kit items, and " + armorCount + " armor pieces."
        );
        
        // Invalidate kit GUI cache so it reloads fresh kit items
        tynk.zim.mbwrf.gui.KitEditorGUI.invalidateCache();
    }

    private void loadArmorPiece(ConfigurationSection section, String key, int slot) {
        ConfigurationSection pieceSection = section.getConfigurationSection(key);
        if (pieceSection != null) {
            ItemStack item = parseItem(pieceSection);
            if (item != null) {
                kitArmor[slot] = item;
            }
        }
    }

    private ItemStack parseItem(ConfigurationSection section) {
        String materialName = section.getString("material");
        if (materialName == null) {
            return null;
        }

        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            MBwRFPlugin.getInstance().getLogger().warning("Invalid material: " + materialName);
            return null;
        }

        int amount = section.getInt("amount", 1);
        ItemStack item = new ItemStack(material, amount);

        List<String> enchantmentStrings = section.getStringList("enchantments");
        for (String enchantString : enchantmentStrings) {
            String[] parts = enchantString.split(":");
            if (parts.length == 2) {
                try {
                    Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                    if (enchantment != null) {
                        int level = Integer.parseInt(parts[1]);
                        item.addUnsafeEnchantment(enchantment, level);
                    } else {
                        MBwRFPlugin.getInstance().getLogger().warning("Invalid enchantment: " + parts[0]);
                    }
                } catch (NumberFormatException e) {
                    MBwRFPlugin.getInstance().getLogger().warning("Invalid enchantment level: " + parts[1]);
                }
            }
        }

        return item;
    }

    public void saveKitToConfig(List<Map<String, Object>> items) {
        FileConfiguration config = MBwRFPlugin.getInstance().getConfig();
        
        config.set("kit.items", null);
        
        for (int i = 0; i < items.size(); i++) {
            String path = "kit.items." + (i + 1);
            Map<String, Object> itemData = items.get(i);
            
            config.set(path + ".material", itemData.get("material"));
            config.set(path + ".amount", itemData.get("amount"));
            
            if (itemData.containsKey("enchantments")) {
                config.set(path + ".enchantments", itemData.get("enchantments"));
            }
        }
        
        MBwRFPlugin.getInstance().saveConfig();
    }

    public void saveArmorToConfig(List<Map<String, Object>> armor) {
        FileConfiguration config = MBwRFPlugin.getInstance().getConfig();
        
        config.set("kit.armor", null);
        
        String[] slots = {"boots", "leggings", "chestplate", "helmet"};
        for (int i = 0; i < armor.size() && i < 4; i++) {
            Map<String, Object> armorData = armor.get(i);
            if (armorData != null) {
                String path = "kit.armor." + slots[i];
                config.set(path + ".material", armorData.get("material"));
                config.set(path + ".amount", armorData.get("amount"));
                
                if (armorData.containsKey("enchantments")) {
                    config.set(path + ".enchantments", armorData.get("enchantments"));
                }
            }
        }
        
        MBwRFPlugin.getInstance().saveConfig();
    }

    public void addEnabledMap(String mapName) {
        FileConfiguration config = MBwRFPlugin.getInstance().getConfig();
        List<String> maps = config.getStringList("enabled-maps");
        
        if (!maps.contains(mapName)) {
            maps.add(mapName);
            config.set("enabled-maps", maps);
            MBwRFPlugin.getInstance().saveConfig();
        }
        
        enabledMaps.add(mapName.toLowerCase());
    }

    public boolean removeEnabledMap(String mapName) {
        FileConfiguration config = MBwRFPlugin.getInstance().getConfig();
        List<String> maps = config.getStringList("enabled-maps");
        
        boolean removed = false;
        if (maps.remove(mapName)) {
            config.set("enabled-maps", maps);
            MBwRFPlugin.getInstance().saveConfig();
            removed = true;
        }
        
        enabledMaps.remove(mapName.toLowerCase());
        return removed;
    }

    public boolean isEnabledMap(String arenaName) {
        return enabledMaps.contains(arenaName.toLowerCase());
    }

    public List<ItemStack> getKitItems() {
        return new ArrayList<>(kitItems);
    }

    public ItemStack[] getKitArmor() {
        ItemStack[] copy = new ItemStack[4];
        for (int i = 0; i < kitArmor.length; i++) {
            if (kitArmor[i] != null) {
                copy[i] = kitArmor[i].clone();
            }
        }
        return copy;
    }

    public Set<String> getEnabledMaps() {
        return new HashSet<>(enabledMaps);
    }
}
