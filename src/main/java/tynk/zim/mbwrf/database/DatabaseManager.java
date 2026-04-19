package tynk.zim.mbwrf.database;

import tynk.zim.mbwrf.MBwRFPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    
    private static DatabaseManager instance;
    
    private final File databaseFile;
    private Connection connection;
    private final ConcurrentHashMap<UUID, PlayerKitData> cache;
    
    private DatabaseManager() {
        this.databaseFile = new File(MBwRFPlugin.getInstance().getDataFolder(), "kits.db");
        this.cache = new ConcurrentHashMap<>();
    }
    
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    public void init() {
        if (!databaseFile.getParentFile().exists()) {
            databaseFile.getParentFile().mkdirs();
        }
        
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            createTables();
            MBwRFPlugin.getInstance().getLogger().info("Database initialized successfully!");
        } catch (ClassNotFoundException e) {
            MBwRFPlugin.getInstance().getLogger().severe("SQLite JDBC driver not found!");
        } catch (SQLException e) {
            MBwRFPlugin.getInstance().getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }
    
    private static final String CREATE_PLAYER_KITS_TABLE = 
        "CREATE TABLE IF NOT EXISTS player_kits (" +
        "uuid TEXT PRIMARY KEY, " +
        "slots TEXT, " +
        "helmet_slot INTEGER DEFAULT -1, " +
        "chestplate_slot INTEGER DEFAULT -1, " +
        "leggings_slot INTEGER DEFAULT -1, " +
        "boots_slot INTEGER DEFAULT -1)";
    
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_PLAYER_KITS_TABLE);
        }
        
        applyMigrations();
    }
    
    private void applyMigrations() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS schema_version (" +
                    "version INTEGER PRIMARY KEY, " +
                    "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            try (ResultSet rs = stmt.executeQuery("SELECT 1 FROM schema_version WHERE version = 1")) {
                if (!rs.next()) {
                    boolean hasNameColumn = false;
                    try (ResultSet cols = stmt.executeQuery("PRAGMA table_info(player_kits)")) {
                        while (cols.next()) {
                            String columnName = cols.getString("name");
                            if ("name".equalsIgnoreCase(columnName)) {
                                hasNameColumn = true;
                                break;
                            }
                        }
                    }
                    
                    if (hasNameColumn) {
                        stmt.execute("DROP TABLE player_kits");
                        stmt.execute(CREATE_PLAYER_KITS_TABLE);
                        MBwRFPlugin.getInstance().getLogger().info("Migrated player_kits table to new schema");
                    }
                    
                    stmt.execute("INSERT INTO schema_version (version) VALUES (1)");
                }
            }
        } catch (SQLException e) {
            MBwRFPlugin.getInstance().getLogger().warning("Failed to apply migrations: " + e.getMessage());
        }
    }
    
    public PlayerKitData getPlayerKitData(UUID uuid) {
        PlayerKitData cached = cache.get(uuid);
        if (cached != null) {
            MBwRFPlugin.getInstance().getLogger().info(
                "[RushFight] getPlayerKitData: Returning cached clone for UUID " + uuid + ", mappings: " + cached.getSlotMappings()
            );
            return cloneKitData(cached);
        }
        
        PlayerKitData data = loadFromDatabase(uuid);
        cache.put(uuid, data);
        MBwRFPlugin.getInstance().getLogger().info(
            "[RushFight] getPlayerKitData: Loaded from DB for UUID " + uuid + ", mappings: " + data.getSlotMappings()
        );
        return cloneKitData(data);
    }
    
    private PlayerKitData cloneKitData(PlayerKitData original) {
        PlayerKitData clone = new PlayerKitData();
        for (Map.Entry<Integer, Integer> entry : original.getSlotMappings().entrySet()) {
            clone.setSlotMapping(entry.getKey(), entry.getValue());
        }
        clone.setHelmetSlot(original.getHelmetSlot());
        clone.setChestplateSlot(original.getChestplateSlot());
        clone.setLeggingsSlot(original.getLeggingsSlot());
        clone.setBootsSlot(original.getBootsSlot());
        return clone;
    }
    
    private PlayerKitData loadFromDatabase(UUID uuid) {
        String uuidStr = uuid.toString();
        String sql = "SELECT * FROM player_kits WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuidStr);
            
            MBwRFPlugin.getInstance().getLogger().info(
                "[RushFight] loadFromDatabase: Querying for UUID: " + uuidStr
            );
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String dbUuid = rs.getString("uuid");
                    String slots = rs.getString("slots");
                    MBwRFPlugin.getInstance().getLogger().info(
                        "[RushFight] loadFromDatabase: Found row - dbUuid: " + dbUuid + ", slots: " + slots
                    );
                    
                    PlayerKitData data = new PlayerKitData();
                    data.deserializeSlots(slots);
                    data.setHelmetSlot(rs.getInt("helmet_slot"));
                    data.setChestplateSlot(rs.getInt("chestplate_slot"));
                    data.setLeggingsSlot(rs.getInt("leggings_slot"));
                    data.setBootsSlot(rs.getInt("boots_slot"));
                    return data;
                } else {
                    MBwRFPlugin.getInstance().getLogger().info(
                        "[RushFight] loadFromDatabase: No row found for UUID: " + uuidStr
                    );
                }
            }
        } catch (SQLException e) {
            MBwRFPlugin.getInstance().getLogger().warning("Failed to load player kit data: " + e.getMessage());
        }
        
        return new PlayerKitData();
    }
    
    public void savePlayerKitData(UUID uuid, PlayerKitData data) {
        MBwRFPlugin.getInstance().getLogger().info(
            "[RushFight] savePlayerKitData: Saving for UUID " + uuid + ", mappings: " + data.getSlotMappings()
        );
        cache.put(uuid, data);
        
        String sql = "INSERT OR REPLACE INTO player_kits (uuid, slots, helmet_slot, chestplate_slot, leggings_slot, boots_slot) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, data.serializeSlots());
            stmt.setInt(3, data.getHelmetSlot());
            stmt.setInt(4, data.getChestplateSlot());
            stmt.setInt(5, data.getLeggingsSlot());
            stmt.setInt(6, data.getBootsSlot());
            stmt.executeUpdate();
        } catch (SQLException e) {
            MBwRFPlugin.getInstance().getLogger().warning("Failed to save player kit data: " + e.getMessage());
        }
    }
    
    public void deletePlayerKitData(UUID uuid) {
        cache.remove(uuid);
        
        String sql = "DELETE FROM player_kits WHERE uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            MBwRFPlugin.getInstance().getLogger().warning("Failed to delete player kit data: " + e.getMessage());
        }
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                MBwRFPlugin.getInstance().getLogger().warning("Failed to close database: " + e.getMessage());
            }
        }
        cache.clear();
    }
    
    public void clearCache(UUID uuid) {
        cache.remove(uuid);
    }
}
