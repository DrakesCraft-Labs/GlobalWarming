package me.poma123.globalwarming;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.core.attributes.MachineTier;
import com.github.drakescraft_labs.slimefun4.core.attributes.MachineType;
import com.github.drakescraft_labs.slimefun4.utils.LoreBuilder;

public final class Items {
    public static final SlimefunItemStack THERMOMETER = new SlimefunItemStack("THERMOMETER", "24fa511f2628d56a8c8691ac5df3e3f82716384514a5ea5bae3eda86f48ad6e1", "&eTermómetro", "", "&7Muestra la temperatura donde estás", "", "&eClic derecho&7 para cambiar de unidad");
    public static final SlimefunItemStack AIR_QUALITY_METER = new SlimefunItemStack("AIR_QUALITY_METER", "179adc3d2dfda05497bb904bd6651922510ce2139a71c10eae3b27565292ebf0", "&bMedidor de Calidad del Aire", "", "&7Muestra cómo cambia la temperatura donde estás", "", "&eClic derecho&7 para cambiar de unidad");
    public static final SlimefunItemStack AIR_COMPRESSOR = new SlimefunItemStack("AIR_COMPRESSOR", Material.DISPENSER, "&bCompresor de Aire", "", "&aComprime dióxido de carbono", "", LoreBuilder.machine(MachineTier.ADVANCED, MachineType.MACHINE), LoreBuilder.powerBuffer(512), LoreBuilder.powerPerSecond(16));
    public static final SlimefunItemStack EMPTY_CANISTER = new SlimefunItemStack("EMPTY_CANISTER", Material.GLASS_BOTTLE, "&7Bombona de Aire");
    public static final SlimefunItemStack CO2_CANISTER;
    public static final SlimefunItemStack CINNABARITE = new SlimefunItemStack("CINNABARITE", "d67a8a3d7d5aa5db00dff5c82f846ea0aeb7d645f0e467d7e9d9a18e9fa5b012", "&cCinabrio");
    public static final SlimefunItemStack MERCURY = new SlimefunItemStack("MERCURY", Material.GRAY_DYE, "&7Mercurio");
    public static final SlimefunItemStack FILTER = new SlimefunItemStack("AIR_COMPRESSOR_FILTER", Material.GUNPOWDER, "&7Filtro");

    public static final SlimefunItemStack CLIMATIZADOR = new SlimefunItemStack("GW_CLIMATIZADOR", Material.BLUE_ICE, "&bClimatizador", "", "&7Mantiene su propio clima alrededor", "&7sin importar lo que pase fuera", "", "&aDentro de su radio no se posa nieve", "&ay la temperatura es la que le pongas", "", "&8&oRadio y temperatura se ajustan en Items.yml", "", LoreBuilder.machine(MachineTier.ADVANCED, MachineType.MACHINE), LoreBuilder.powerBuffer(512), LoreBuilder.powerPerSecond(24));
    public static final SlimefunItemStack SUMIDERO_CARBONO = new SlimefunItemStack("GW_SUMIDERO_CARBONO", Material.DEEPSLATE_TILES, "&2Sumidero de Carbono", "", "&7Fija bajo tierra el CO\u2082 capturado", "&7y baja la contaminaci\u00f3n del mundo", "", "&aConsume Bombonas de CO\u2082", "&ay devuelve la bombona vac\u00eda", "", LoreBuilder.machine(MachineTier.ADVANCED, MachineType.MACHINE), LoreBuilder.powerBuffer(512), LoreBuilder.powerPerSecond(32));

    static {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        ((PotionMeta) meta).setColor(Color.fromRGB(61, 61, 61));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(meta);

        CO2_CANISTER = new SlimefunItemStack("CO2_CANISTER", item, "&7Bombona de CO₂", "", "&8&oDióxido de carbono comprimido");
    }
}