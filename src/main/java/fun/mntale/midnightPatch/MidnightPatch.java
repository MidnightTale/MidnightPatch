package fun.mntale.midnightPatch;

import fun.mntale.midnightPatch.chunk.block.MossBlockManager;
import fun.mntale.midnightPatch.entity.LootMobTargetManager;
import fun.mntale.midnightPatch.entity.ExtraBabyMobManager;
import fun.mntale.midnightPatch.entity.PosableArmorStandManager;
import org.bukkit.plugin.java.JavaPlugin;
import fun.mntale.midnightPatch.chunk.EnderPearlChunkManager;
import fun.mntale.midnightPatch.chunk.MinecartChunkManager;
import fun.mntale.midnightPatch.fishing.AutoFishManager;
import fun.mntale.midnightPatch.skin.SkinManager;
import fun.mntale.midnightPatch.skin.SkinCommand;
import io.papermc.paper.command.brigadier.BasicCommand;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import fun.mntale.midnightPatch.chunk.block.DesirePathManager;
import fun.mntale.midnightPatch.chunk.block.ReachAroundBlockManager;
import fun.mntale.midnightPatch.chunk.block.BoneMealManager;
import fun.mntale.midnightPatch.command.KillCommand;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;
import fun.mntale.midnightPatch.entity.ProjectileDamageManager;
import fun.mntale.midnightPatch.stats.ServerStatsManager;

public final class MidnightPatch extends JavaPlugin {
    public static MidnightPatch instance;
    public SkinManager skinManager;
    public EnderPearlChunkManager enderPearlChunkManager;
    public MinecartChunkManager minecartChunkManager;
    public AutoFishManager autoFishManager;
    public MossBlockManager mossBlockManager;
    public LootMobTargetManager lootMobTargetManager;
    public DesirePathManager desirePathManager;
    public ProjectileDamageManager projectileDamageManager;
    public ServerStatsManager serverStatsManager;
    public ReachAroundBlockManager reachAroundBlockManager;
    public ExtraBabyMobManager extraBabyMobManager;
    public PosableArmorStandManager posableArmorStandManager;
    public BoneMealManager boneMealManager;

    // Configuration
    boolean enableSkinManager = false;
    boolean enableEnderPearlChunkManager = false;
    boolean enableMinecartChunkManager = true;
    boolean enableAutoFishManager = true;
    boolean enableFreecamManager = true;
    boolean enableMossBlockManager = true;
    boolean enableLootMobTargetManager = true;
    boolean enableDesirePathManager = true;
    boolean enableProjectileDamageManager = true;
    boolean enableServerStatsManager = false;
    boolean enableReachAroundBlockManager = true;
    boolean enableExtraBabyMobManager = true;
    boolean enablePosableArmorStandManager = true;
    boolean enableBoneMealManager = true;
    boolean enableUpdraftLoot = true;
    boolean enableHarvestingLoot = true;
    boolean enableRecastingLoot = true;
    boolean enableFrostbiteLoot = true;
    boolean enableUpdraftLogic = true;
    boolean enableFrostbiteLogic = true;
    boolean enableAutoHarvestManager = true;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        // Load config values (still used by listeners at runtime)
        enableSkinManager = getConfig().getBoolean("enableSkinManager", enableSkinManager);
        enableEnderPearlChunkManager = getConfig().getBoolean("enableEnderPearlChunkManager", enableEnderPearlChunkManager);
        enableMinecartChunkManager = getConfig().getBoolean("enableMinecartChunkManager", enableMinecartChunkManager);
        enableAutoFishManager = getConfig().getBoolean("enableAutoFishManager", enableAutoFishManager);
        enableFreecamManager = getConfig().getBoolean("enableFreecamManager", enableFreecamManager);
        enableMossBlockManager = getConfig().getBoolean("enableMossBlockManager", enableMossBlockManager);
        enableLootMobTargetManager = getConfig().getBoolean("enableLootMobTargetManager", enableLootMobTargetManager);
        enableDesirePathManager = getConfig().getBoolean("enableDesirePathManager", enableDesirePathManager);
        enableProjectileDamageManager = getConfig().getBoolean("enableProjectileDamageManager", enableProjectileDamageManager);
        enableServerStatsManager = getConfig().getBoolean("enableServerStatsManager", enableServerStatsManager);
        enableReachAroundBlockManager = getConfig().getBoolean("enableReachAroundBlockManager", enableReachAroundBlockManager);
        enableExtraBabyMobManager = getConfig().getBoolean("enableExtraBabyMobManager", enableExtraBabyMobManager);
        enablePosableArmorStandManager = getConfig().getBoolean("enablePosableArmorStandManager", enablePosableArmorStandManager);
        enableBoneMealManager = getConfig().getBoolean("enableBoneMealManager", enableBoneMealManager);
        enableUpdraftLoot = getConfig().getBoolean("enableUpdraftLoot", enableUpdraftLoot);
        enableHarvestingLoot = getConfig().getBoolean("enableHarvestingLoot", enableHarvestingLoot);
        enableRecastingLoot = getConfig().getBoolean("enableRecastingLoot", enableRecastingLoot);
        enableFrostbiteLoot = getConfig().getBoolean("enableFrostbiteLoot", enableFrostbiteLoot);
        enableUpdraftLogic = getConfig().getBoolean("enableUpdraftLogic", enableUpdraftLogic);
        enableFrostbiteLogic = getConfig().getBoolean("enableFrostbiteLogic", enableFrostbiteLogic);
        enableAutoHarvestManager = getConfig().getBoolean("enableAutoHarvestManager", enableAutoHarvestManager);

        // Register all listeners unconditionally. Each listener checks config at runtime.
        if (enableSkinManager) {
            skinManager = new SkinManager();
            BasicCommand skinCommand = new SkinCommand(skinManager);
            registerCommand("midnightpatch", skinCommand);
            this.getServer().getPluginManager().registerEvents(skinManager, this);
            ComponentLogger.logger().info("enableSkinManager = true");
        } else {
            ComponentLogger.logger().info("enableSkinManager = false");
        }
        if (enableEnderPearlChunkManager) {
            enderPearlChunkManager = new EnderPearlChunkManager();
            this.getServer().getPluginManager().registerEvents(enderPearlChunkManager, this);
            ComponentLogger.logger().info("enderPearlChunkManager = true");
        } else {
            ComponentLogger.logger().info("enderPearlChunkManager = false");
        }
        if (enableMinecartChunkManager) {
            minecartChunkManager = new MinecartChunkManager();
            this.getServer().getPluginManager().registerEvents(minecartChunkManager, this);
            ComponentLogger.logger().info("enableMinecartChunkManager = true");
        } else {
            ComponentLogger.logger().info("enableMinecartChunkManager = false");
        }
        if (enableAutoFishManager) {
            autoFishManager = new AutoFishManager();
            this.getServer().getPluginManager().registerEvents(autoFishManager, this);
            ComponentLogger.logger().info("enableAutoFishManager = true");
        } else {
            ComponentLogger.logger().info("enableAutoFishManager = false");
        }
        if (enableMossBlockManager) {
            mossBlockManager = new MossBlockManager();
            this.getServer().getPluginManager().registerEvents(mossBlockManager, this);
            ComponentLogger.logger().info("enableMossBlockManager = true");
        } else {
            ComponentLogger.logger().info("enableMossBlockManager = false");
        }
        if (enableLootMobTargetManager) {
            lootMobTargetManager = new LootMobTargetManager();
            this.getServer().getPluginManager().registerEvents(lootMobTargetManager, this);
            ComponentLogger.logger().info("enableLootMobTargetManager = true");
        } else {
            ComponentLogger.logger().info("enableLootMobTargetManager = false");
        }
        if (enableDesirePathManager) {
            desirePathManager = new DesirePathManager();
            this.getServer().getPluginManager().registerEvents(desirePathManager, this);
            ComponentLogger.logger().info("enableDesirePathManager = true");
        } else {
            ComponentLogger.logger().info("enableDesirePathManager = false");
        }
        if (enableProjectileDamageManager) {
            projectileDamageManager = new ProjectileDamageManager();
            this.getServer().getPluginManager().registerEvents(projectileDamageManager, this);
            ComponentLogger.logger().info("enableProjectileDamageManager = true");
        } else {
            ComponentLogger.logger().info("enableProjectileDamageManager = false");
        }
        if (enableServerStatsManager) {
            serverStatsManager = new ServerStatsManager();
            serverStatsManager.enable();
            ComponentLogger.logger().info("enableServerStatsManager = true");
        } else {
            ComponentLogger.logger().info("enableServerStatsManager = false");
        }
        if (enableReachAroundBlockManager) {
            reachAroundBlockManager = new ReachAroundBlockManager();
            this.getServer().getPluginManager().registerEvents(reachAroundBlockManager, this);
            ComponentLogger.logger().info("enableReachAroundBlockManager = true");
            BasicCommand toggleReachAroundCommand = new ToggleReachAroundCommand();
            registerCommand("togglereacharound", toggleReachAroundCommand);
        } else {
            ComponentLogger.logger().info("enableReachAroundBlockManager = false");
        }
        if (enableExtraBabyMobManager) {
            extraBabyMobManager = new ExtraBabyMobManager();
            this.getServer().getPluginManager().registerEvents(extraBabyMobManager, this);
            ComponentLogger.logger().info("enableExtraBabyMobManager = true");
        } else {
            ComponentLogger.logger().info("enableExtraBabyMobManager = false");
        }
        if (enablePosableArmorStandManager) {
            posableArmorStandManager = new PosableArmorStandManager();
            this.getServer().getPluginManager().registerEvents(posableArmorStandManager, this);
            ComponentLogger.logger().info("enablePosableArmorStandManager = true");
        } else {
            ComponentLogger.logger().info("enablePosableArmorStandManager = false");
        }
        if (enableBoneMealManager) {
            boneMealManager = new BoneMealManager();
            this.getServer().getPluginManager().registerEvents(boneMealManager, this);
            ComponentLogger.logger().info("enableBoneMealManager = true");
        } else {
            ComponentLogger.logger().info("enableBoneMealManager = false");
        }

        // Register /kill command
        BasicCommand killCommand = new KillCommand();
        registerCommand("kill", killCommand);

        // Always register all custom enchantment logic and loot listeners
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.farming.AutoHarvestManager(), this);
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.entity.FrostbiteLogic(), this);
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.entity.UpdraftLogic(), this);
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.entity.UpdraftLootListener(), this);
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.entity.HarvestingLootListener(), this);
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.entity.RecastingLootListener(), this);
        getServer().getPluginManager().registerEvents(new fun.mntale.midnightPatch.entity.FrostbiteLootListener(), this);
    }

    @Override
    public void onDisable() {
        if (enderPearlChunkManager != null) {
            enderPearlChunkManager.shutdown();
        }
        if (minecartChunkManager != null) {
            minecartChunkManager.shutdown();
        }
        if (serverStatsManager != null) {
            serverStatsManager.disable();
        }
    }
}
