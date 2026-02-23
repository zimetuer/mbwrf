package tynk.zim.mbwrf;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import tynk.zim.mbwrf.database.DatabaseManager;
import tynk.zim.mbwrf.gui.KitEditorGUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(sender, player.hasPermission("rushfight.admin"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "editkit":
                return handleEditKit(player);
            case "resetkit":
                return handleResetKit(player);
            case "setkit":
                return checkAdmin(player) && handleSetKit(player);
            case "addmap":
                return checkAdmin(player) && handleAddMap(player, args);
            case "removemap":
                return checkAdmin(player) && handleRemoveMap(player, args);
            case "list":
                return handleList(player);
            case "reload":
                return checkAdmin(player) && handleReload(player);
            case "debug":
                return checkAdmin(player) && handleDebug(player);
            default:
                sendHelp(sender, player.hasPermission("rushfight.admin"));
                return true;
        }
    }

    private boolean checkAdmin(Player player) {
        if (!player.hasPermission("rushfight.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return false;
        }
        return true;
    }

    private boolean handleEditKit(Player player) {
        if (MapConfig.getInstance().getKitItems().isEmpty()) {
            player.sendMessage(ChatColor.RED + "No kit has been set by an admin yet!");
            return true;
        }
        
        KitEditorGUI.open(player);
        return true;
    }

    private boolean handleResetKit(Player player) {
        DatabaseManager.getInstance().deletePlayerKitData(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Your kit layout has been reset to default!");
        return true;
    }

    private boolean handleSetKit(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        
        List<Map<String, Object>> items = new ArrayList<>();
        List<Map<String, Object>> armorItems = new ArrayList<>();

        int itemCount = 0;
        for (ItemStack item : inventory) {
            if (item != null && item.getType() != Material.AIR) {
                Map<String, Object> itemData = serializeItem(item);
                items.add(itemData);
                itemCount++;
            }
        }

        int armorCount = 0;
        for (ItemStack item : armor) {
            if (item != null && item.getType() != Material.AIR) {
                Map<String, Object> itemData = serializeItem(item);
                armorItems.add(itemData);
                armorCount++;
            } else {
                armorItems.add(null);
            }
        }

        MapConfig.getInstance().saveKitToConfig(items);
        MapConfig.getInstance().saveArmorToConfig(armorItems);
        MapConfig.getInstance().load();

        player.sendMessage(ChatColor.GREEN + "Kit saved! " + itemCount + " items and " + armorCount + " armor pieces saved to config.");
        return true;
    }

    private boolean handleAddMap(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rushfight addmap <mapname>");
            return true;
        }

        String mapName = args[1];
        MapConfig.getInstance().addEnabledMap(mapName);
        player.sendMessage(ChatColor.GREEN + "Added map '" + mapName + "' to enabled maps.");
        return true;
    }

    private boolean handleRemoveMap(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /rushfight removemap <mapname>");
            return true;
        }

        String mapName = args[1];
        if (MapConfig.getInstance().removeEnabledMap(mapName)) {
            player.sendMessage(ChatColor.GREEN + "Removed map '" + mapName + "' from enabled maps.");
        } else {
            player.sendMessage(ChatColor.RED + "Map '" + mapName + "' was not in the enabled maps list.");
        }
        return true;
    }

    private boolean handleList(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== RushFight Maps ===");
        for (String map : MapConfig.getInstance().getEnabledMaps()) {
            player.sendMessage(ChatColor.YELLOW + "- " + map);
        }
        
        int armorCount = 0;
        for (ItemStack a : MapConfig.getInstance().getKitArmor()) {
            if (a != null) armorCount++;
        }
        
        player.sendMessage(ChatColor.GOLD + "Kit items: " + ChatColor.YELLOW + MapConfig.getInstance().getKitItems().size());
        player.sendMessage(ChatColor.GOLD + "Armor pieces: " + ChatColor.YELLOW + armorCount);
        return true;
    }

    private boolean handleReload(Player player) {
        MapConfig.getInstance().load();
        player.sendMessage(ChatColor.GREEN + "Config reloaded!");
        return true;
    }
    
    private boolean handleDebug(Player player) {
        tynk.zim.mbwrf.database.PlayerKitData kitData = DatabaseManager.getInstance().getPlayerKitData(player.getUniqueId());
        
        player.sendMessage(ChatColor.GOLD + "=== RushFight Debug Info ===");
        player.sendMessage(ChatColor.YELLOW + "Your UUID: " + ChatColor.WHITE + player.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + "Has custom layout: " + ChatColor.WHITE + kitData.hasCustomLayout());
        player.sendMessage(ChatColor.YELLOW + "Slot mappings: " + ChatColor.WHITE + kitData.getSlotMappings().toString());
        player.sendMessage(ChatColor.YELLOW + "Kit items count: " + ChatColor.WHITE + MapConfig.getInstance().getKitItems().size());
        
        return true;
    }

    private void sendHelp(CommandSender sender, boolean isAdmin) {
        sender.sendMessage(ChatColor.GOLD + "=== RushFight Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/rushfight editkit" + ChatColor.WHITE + " - Edit your personal kit layout");
        sender.sendMessage(ChatColor.YELLOW + "/rushfight resetkit" + ChatColor.WHITE + " - Reset your kit to default");
        sender.sendMessage(ChatColor.YELLOW + "/rushfight list" + ChatColor.WHITE + " - List enabled maps");
        
        if (isAdmin) {
            sender.sendMessage(ChatColor.RED + "--- Admin Commands ---");
            sender.sendMessage(ChatColor.YELLOW + "/rushfight setkit" + ChatColor.WHITE + " - Save your inventory as kit");
            sender.sendMessage(ChatColor.YELLOW + "/rushfight addmap <name>" + ChatColor.WHITE + " - Add a map");
            sender.sendMessage(ChatColor.YELLOW + "/rushfight removemap <name>" + ChatColor.WHITE + " - Remove a map");
            sender.sendMessage(ChatColor.YELLOW + "/rushfight reload" + ChatColor.WHITE + " - Reload config");
            sender.sendMessage(ChatColor.YELLOW + "/rushfight debug" + ChatColor.WHITE + " - Show debug info");
        }
    }

    private Map<String, Object> serializeItem(ItemStack item) {
        Map<String, Object> data = new HashMap<>();
        data.put("material", item.getType().name());
        data.put("amount", item.getAmount());

        if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
            List<String> enchantments = new ArrayList<>();
            for (Map.Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                enchantments.add(entry.getKey().getName() + ":" + entry.getValue());
            }
            data.put("enchantments", enchantments);
        }

        return data;
    }
}
