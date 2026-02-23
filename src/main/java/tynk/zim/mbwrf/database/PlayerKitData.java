package tynk.zim.mbwrf.database;

import java.util.HashMap;
import java.util.Map;

public class PlayerKitData {
    
    private final Map<Integer, Integer> slotMappings;
    private int helmetSlot = -1;
    private int chestplateSlot = -1;
    private int leggingsSlot = -1;
    private int bootsSlot = -1;
    
    public PlayerKitData() {
        this.slotMappings = new HashMap<>();
    }
    
    public void setSlotMapping(int inventorySlot, int kitItemIndex) {
        slotMappings.put(inventorySlot, kitItemIndex);
    }
    
    public Integer getKitItemIndex(int inventorySlot) {
        return slotMappings.get(inventorySlot);
    }
    
    public Map<Integer, Integer> getSlotMappings() {
        return new HashMap<>(slotMappings);
    }
    
    public void clearSlotMappings() {
        slotMappings.clear();
    }
    
    public int getHelmetSlot() {
        return helmetSlot;
    }
    
    public void setHelmetSlot(int helmetSlot) {
        this.helmetSlot = helmetSlot;
    }
    
    public int getChestplateSlot() {
        return chestplateSlot;
    }
    
    public void setChestplateSlot(int chestplateSlot) {
        this.chestplateSlot = chestplateSlot;
    }
    
    public int getLeggingsSlot() {
        return leggingsSlot;
    }
    
    public void setLeggingsSlot(int leggingsSlot) {
        this.leggingsSlot = leggingsSlot;
    }
    
    public int getBootsSlot() {
        return bootsSlot;
    }
    
    public void setBootsSlot(int bootsSlot) {
        this.bootsSlot = bootsSlot;
    }
    
    public boolean hasCustomLayout() {
        return !slotMappings.isEmpty();
    }
    
    public String serializeSlots() {
        if (slotMappings.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<Integer, Integer> entry : slotMappings.entrySet()) {
            if (!first) sb.append(",");
            sb.append(entry.getKey()).append(":").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
    
    public void deserializeSlots(String data) {
        slotMappings.clear();
        
        if (data == null || data.isEmpty() || data.equals("{}")) {
            return;
        }
        
        String content = data.substring(1, data.length() - 1);
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                try {
                    int slot = Integer.parseInt(parts[0]);
                    int index = Integer.parseInt(parts[1]);
                    slotMappings.put(slot, index);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }
}
