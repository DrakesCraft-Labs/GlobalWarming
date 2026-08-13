package me.poma123.globalwarming;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.drakescraft_labs.slimefun4.api.SlimefunAddon;
import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.api.researches.Research;
import com.github.drakescraft_labs.slimefun4.core.handlers.ItemConsumptionHandler;
import com.github.drakescraft_labs.slimefun4.implementation.SlimefunItems;
import com.github.drakescraft_labs.slimefun4.implementation.items.SimpleSlimefunItem;
import com.github.drakescraft_labs.slimefun4.libraries.dough.config.Config;
import com.github.drakescraft_labs.slimefun4.libraries.dough.items.CustomItemStack;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;

import me.poma123.globalwarming.api.TemperatureType;
import me.poma123.globalwarming.commands.GlobalWarmingCommand;
import me.poma123.globalwarming.items.CinnabariteResource;
import me.poma123.globalwarming.items.machines.AirCompressor;
import me.poma123.globalwarming.items.machines.TemperatureMeter;
import me.poma123.globalwarming.listeners.PollutionListener;
import me.poma123.globalwarming.listeners.WorldListener;
import me.poma123.globalwarming.eventos.EventoClimatico;
import me.poma123.globalwarming.eventos.Silenciados;
import me.poma123.globalwarming.eventos.GestorEventos;
import me.poma123.globalwarming.tasks.BurnTask;
import me.poma123.globalwarming.tasks.FireTask;
import me.poma123.globalwarming.tasks.MeltTask;
import me.poma123.globalwarming.tasks.SlownessTask;


public class GlobalWarmingPlugin extends JavaPlugin implements SlimefunAddon {

    private static GlobalWarmingPlugin instance;
    private static Registry registry = new Registry();
    private static GestorEventos gestorEventos;
    private static Silenciados silenciados;
    private final TemperatureManager temperatureManager = new TemperatureManager();
    private final GlobalWarmingCommand command = new GlobalWarmingCommand(this);
    private final Config cfg = new Config(this);
    private Config messages;

    @Override
    public void onEnable() {
        instance = this;

        if (getConfig().getBoolean("options.auto-update") &&
            getDescription().getVersion().startsWith("Build")) {
        }


        final File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/messages.yml"), messagesFile.toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "No se pudo crear la configuración messages.yml", e);
            }
        }
        messages = new Config(this, "messages.yml");

        File biomeMapDirectory = new File(getDataFolder(), "biome-maps");
        if (!biomeMapDirectory.exists()) {
            biomeMapDirectory.mkdirs();
        }

        // Create biome map files
        final File pre118BiomeMap = new File(biomeMapDirectory, "pre-1.18.json");
        if (!pre118BiomeMap.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/biome-maps/pre-1.18.json"), pre118BiomeMap.toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "No se pudo crear la configuración biome-maps/pre-1.18.json", e);
            }
        }

        final File post118BiomeMap = new File(biomeMapDirectory, "post-1.18.json");
        if (!post118BiomeMap.exists()) {
            try {
                Files.copy(this.getClass().getResourceAsStream("/biome-maps/post-1.18.json"), post118BiomeMap.toPath());
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "No se pudo crear la configuración biome-maps/post-1.18.json", e);
            }
        }

        registerItems();
        registerResearches();
        registry.load(cfg, messages);
        silenciados = new Silenciados(this);

        scheduleTasks();

        command.register();
        Bukkit.getPluginManager().registerEvents(new PollutionListener(), this);
        Bukkit.getPluginManager().registerEvents(new WorldListener(), this);
    }

    private void registerItems() {
        ItemGroup itemGroup = new ItemGroup(new NamespacedKey(this, "global_warming"), new CustomItemStack(Items.THERMOMETER, "&2Calentamiento Global"));

        new TemperatureMeter(itemGroup, Items.THERMOMETER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.NICKEL_INGOT, new ItemStack(Material.GLASS), SlimefunItems.NICKEL_INGOT,
                SlimefunItems.NICKEL_INGOT, Items.MERCURY, SlimefunItems.NICKEL_INGOT,
                SlimefunItems.NICKEL_INGOT, new ItemStack(Material.GLASS), SlimefunItems.NICKEL_INGOT
        }) {
            @Override
            public void tick(Block b) {
                Location loc = b.getLocation();
                updateHologram(b, GlobalWarmingPlugin.getTemperatureManager().getTemperatureString(loc, TemperatureType.valueOf(BlockStorage.getLocationInfo(loc, "type"))));
            }
        }.register(this);

        new TemperatureMeter(itemGroup, Items.AIR_QUALITY_METER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.BILLON_INGOT, SlimefunItems.BILLON_INGOT, SlimefunItems.BILLON_INGOT,
                SlimefunItems.SOLDER_INGOT, Items.THERMOMETER, SlimefunItems.SOLDER_INGOT,
                SlimefunItems.SOLDER_INGOT, SlimefunItems.MAGNET, SlimefunItems.SOLDER_INGOT
        }) {
            @Override
            public void tick(Block b) {
                Location loc = b.getLocation();
                updateHologram(b, "&7Cambio del entorno: " + GlobalWarmingPlugin.getTemperatureManager().getAirQualityString(loc.getWorld(), TemperatureType.valueOf(BlockStorage.getLocationInfo(loc, "type"))));
            }
        }.register(this);

        new AirCompressor(itemGroup, Items.AIR_COMPRESSOR, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                SlimefunItems.SOLDER_INGOT, Items.FILTER, SlimefunItems.SOLDER_INGOT,
                SlimefunItems.ALUMINUM_BRASS_INGOT, SlimefunItems.ELECTRIC_MOTOR, SlimefunItems.ALUMINUM_BRASS_INGOT,
                SlimefunItems.SOLDER_INGOT, SlimefunItems.BATTERY, SlimefunItems.SOLDER_INGOT
        }) {
            @Override
            public int getEnergyConsumption() {
                return 16;
            }

            @Override
            public int getCapacity() {
                return 512;
            }

            @Override
            public int getSpeed() {
                return 1;
            }
        }.register(this);

        new SlimefunItem(itemGroup, Items.EMPTY_CANISTER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[] {
                null, SlimefunItems.SOLDER_INGOT, null,
                SlimefunItems.SOLDER_INGOT, new ItemStack(Material.GLASS_BOTTLE), SlimefunItems.SOLDER_INGOT,
                SlimefunItems.SOLDER_INGOT, SlimefunItems.SOLDER_INGOT, SlimefunItems.SOLDER_INGOT
        }).register(this);

        new SimpleSlimefunItem<ItemConsumptionHandler>(itemGroup, Items.CO2_CANISTER, AirCompressor.RECIPE_TYPE, new ItemStack[] {
                null, null, null,
                null, Items.EMPTY_CANISTER, null,
                null, null, null
        }) {
            @Override
            public ItemConsumptionHandler getItemHandler() {
                return (e, p, item) -> e.setCancelled(true);
            }
        }.register(this);

        new SlimefunItem(itemGroup, Items.CINNABARITE, RecipeType.GEO_MINER, new ItemStack[]{}).register(this);
        new CinnabariteResource().register();

        new SlimefunItem(itemGroup, Items.MERCURY, RecipeType.SMELTERY, new ItemStack[]{
                Items.CINNABARITE, null, null,
                null, null, null,
                null, null, null
        }).register(this);

        new SlimefunItem(itemGroup, Items.FILTER, RecipeType.ENHANCED_CRAFTING_TABLE, new ItemStack[]{
                null, new ItemStack(Material.GLASS), null,
                new ItemStack(Material.GLASS), SlimefunItems.GOLD_PAN, new ItemStack(Material.GLASS),
                null, new ItemStack(Material.GLASS), null
        }).register(this);
    }

    private void registerResearches() {
        registerResearch("thermometer", 69696969, "Termómetro", 10, Items.THERMOMETER);
        registerResearch("air_quality_meter", 69696970, "Medidor de Calidad del Aire", 30, Items.AIR_QUALITY_METER);
        registerResearch("air_compressor", 69696971, "Compresor de Aire", 40, Items.AIR_COMPRESSOR);
        registerResearch("canisters", 69696972, "Depósito de contaminación", 6, Items.EMPTY_CANISTER, Items.CO2_CANISTER);
        registerResearch("filter", 69696973, "Filtrado", 8, Items.FILTER);
        registerResearch("mercury", 69696973, "Mercurio", 12, Items.CINNABARITE, Items.MERCURY);
    }

    /**
     * Pone en marcha los fenomenos climaticos.
     *
     * Es lo unico "vivo" que le queda al plugin de cara al jugador, y a proposito: cambia el
     * mundo, no al jugador. Ver eventos/GestorEventos.
     */
    private void arrancarEventosClimaticos() {
        if (!cfg.getOrSetDefault("eventos-climaticos.enabled", true)) {
            return;
        }

        Map<EventoClimatico, Boolean> permitidos = new EnumMap<>(EventoClimatico.class);
        for (EventoClimatico e : EventoClimatico.values()) {
            permitidos.put(e, cfg.getOrSetDefault("eventos-climaticos.tipos." + e.getClaveConfig(), true));
        }

        gestorEventos = new GestorEventos(
                cfg.getOrSetDefault("eventos-climaticos.intervalo-sorteo-segundos", 600),
                cfg.getOrSetDefault("eventos-climaticos.probabilidad", 0.35),
                cfg.getOrSetDefault("eventos-climaticos.duracion-minima-segundos", 240),
                cfg.getOrSetDefault("eventos-climaticos.duracion-maxima-segundos", 600),
                permitidos,
                cfg.getOrSetDefault("eventos-climaticos.anunciar", true));
        gestorEventos.arrancar(1);
    }

    private void scheduleTasks() {
        if (cfg.getBoolean("mechanics.FOREST_FIRES.enabled")) {
            new FireTask(cfg.getOrSetDefault("mechanics.FOREST_FIRES.min-temperature-in-celsius", 40.0),
                    cfg.getOrSetDefault("mechanics.FOREST_FIRES.chance", 0.3),
                    cfg.getOrSetDefault("mechanics.FOREST_FIRES.fire-per-second", 10)
            ).scheduleRepeating(0, 20);
        }

        if (cfg.getBoolean("mechanics.ICE_MELTING.enabled")) {
            new MeltTask(cfg.getOrSetDefault("mechanics.ICE_MELTING.min-temperature-in-celsius", 2.0),
                    cfg.getOrSetDefault("mechanics.ICE_MELTING.chance", 0.5),
                    cfg.getOrSetDefault("mechanics.ICE_MELTING.melt-per-second", 10)
            ).scheduleRepeating(0, 20);
        }

        if (cfg.getBoolean("mechanics.SLOWNESS.enabled")) {
            new SlownessTask(cfg.getOrSetDefault("mechanics.SLOWNESS.chance", 0.8)).scheduleRepeating(0, 200);
        }

        if (cfg.getBoolean("mechanics.BURN.enabled")) {
            new BurnTask(cfg.getOrSetDefault("mechanics.BURN.chance", 0.8)).scheduleRepeating(0, 200);
        }

        arrancarEventosClimaticos();

        temperatureManager.runCalculationTask(0, 100);
    }

    private void registerResearch(String key, int id, String name, int defaultCost, ItemStack... items) {
        Research research = new Research(new NamespacedKey(this, key), id, name, defaultCost);

        for (ItemStack item : items) {
            SlimefunItem sfItem = SlimefunItem.getByItem(item);

            if (sfItem != null) {
                research.addItems(sfItem);
            }
        }

        research.register();
    }

    public static Registry getRegistry() {
        return registry;
    }

    public static TemperatureManager getTemperatureManager() {
        return instance.temperatureManager;
    }

    /** Los fenomenos climaticos. Null si estan apagados en la configuracion. */
    /** Quien ha pedido que el clima deje de escribirle. */
    public static Silenciados getSilenciados() {
        return silenciados;
    }

    public static GestorEventos getGestorEventos() {
        return gestorEventos;
    }

    public static GlobalWarmingPlugin getInstance() {
        return instance;
    }

    public static GlobalWarmingCommand getCommand() {
        return instance.command;
    }

    @Override
    public String getBugTrackerURL() {
        return "https://github.com/DrakesCraft-Labs/GlobalWarming/issues";
    }

    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    public static Config getCfg() {
        return instance.cfg;
    }

    public static Config getMessagesConfig() {
        return instance.messages;
    }

}
