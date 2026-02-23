package tynk.zim.mbwrf;

import de.marcely.bedwars.api.BedwarsAddon;
import de.marcely.bedwars.api.GameAPI;
import org.bukkit.plugin.Plugin;

public class MBwRFAddon extends BedwarsAddon {

    private final CustomKitMode customMode;

    public MBwRFAddon(Plugin plugin) {
        super(plugin);
        this.customMode = new CustomKitMode(plugin);
    }

    @Override
    public String getName() {
        return "RushFight";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String[] getAuthors() {
        return new String[]{"zim, maciejk2"};
    }

    public CustomKitMode getCustomMode() {
        return customMode;
    }

    public boolean registerMode() {
        return GameAPI.get().registerCustomMode(customMode);
    }

    public void unregisterMode() {
        GameAPI.get().unregisterCustomMode(customMode);
    }
}
