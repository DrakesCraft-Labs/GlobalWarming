package me.poma123.globalwarming.items.machines;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItem;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.items.settings.DoubleRangeSetting;
import com.github.drakescraft_labs.slimefun4.api.items.settings.IntRangeSetting;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.core.attributes.EnergyNetComponent;
import com.github.drakescraft_labs.slimefun4.core.networks.energy.EnergyNetComponentType;
import com.github.drakescraft_labs.slimefun4.core.handlers.BlockBreakHandler;
import com.github.drakescraft_labs.slimefun4.implementation.items.SimpleSlimefunItem;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.handlers.BlockTicker;
import com.github.drakescraft_labs.slimefun4.legacy.api.BlockStorage;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;

import me.poma123.globalwarming.eventos.RegistroClimatizadores;

/**
 * Climatiza la zona alrededor de la máquina.
 *
 * QUE HACE
 *
 * Mientras tenga energía, la temperatura dentro de su radio deja de ser la del mundo y pasa a ser
 * la que tenga configurada. Eso quiere decir que una ola de calor, una nevada o el calentamiento
 * acumulado dejan de notarse dentro de tu base, aunque fuera sigan.
 *
 * Y no es sólo el número del termómetro: mientras esté encendido, la nieve de una nevada tampoco
 * se posa dentro del radio. Ese es el motivo real para construir uno.
 *
 * POR QUE ASI
 *
 * El clima de DrakesCraft no hace daño a nadie -- no quema, no ralentiza, no mata -- así que una
 * máquina que "protege" de él no puede justificarse por daño evitado. Se justifica por control:
 * decides cómo se ve y cómo se comporta tu terreno, que es lo que la gente quiere de una base.
 *
 * Los dos ajustes son por objeto (Items.yml), no fijos en el código, porque el radio correcto
 * depende de cómo de grandes sean las bases en cada servidor.
 */
public class Climatizador extends SimpleSlimefunItem<BlockTicker> implements EnergyNetComponent {

    private final IntRangeSetting radio;
    private final DoubleRangeSetting objetivo;
    private final int consumo;
    private final int capacidad;

    @ParametersAreNonnullByDefault
    public Climatizador(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType,
                        ItemStack[] recipe, int consumo, int capacidad) {
        super(itemGroup, item, recipeType, recipe);

        this.consumo = consumo;
        this.capacidad = capacidad;
        this.radio = new IntRangeSetting(this, "radio", 4, 24, 64);
        this.objetivo = new DoubleRangeSetting(this, "temperatura-objetivo", -20.0, 21.0, 50.0);

        addItemSetting(radio, objetivo);
        addItemHandler(alRomper());
    }

    @Nonnull
    private BlockBreakHandler alRomper() {
        // Sin esto habria que esperar a que la entrada caduque sola, y durante esos segundos la
        // zona seguiria climatizada por una maquina que ya no existe.
        return new BlockBreakHandler(false, false) {

            @Override
            public void onPlayerBreak(BlockBreakEvent e, ItemStack item, List<ItemStack> drops) {
                RegistroClimatizadores.olvidar(e.getBlock().getLocation());
            }
        };
    }

    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public int getCapacity() {
        return capacidad;
    }

    public int getEnergyConsumption() {
        return consumo;
    }

    @Override
    public BlockTicker getItemHandler() {
        return new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem item, Config data) {
                if (getCharge(b.getLocation(), data) < getEnergyConsumption()) {
                    // Sin energia no se anuncia, y la entrada caduca sola en unos segundos.
                    return;
                }

                removeCharge(b.getLocation(), getEnergyConsumption());
                RegistroClimatizadores.anunciar(b.getLocation(),
                        radio.getValue(), objetivo.getValue());
            }

            @Override
            public boolean isSynchronized() {
                // Lee y escribe carga del bloque, que no es seguro fuera del hilo principal.
                return true;
            }
        };
    }

    /** Deja de anunciarse si el bloque desaparece por una via que no pase por el handler. */
    public static void olvidar(@Nonnull org.bukkit.Location l) {
        RegistroClimatizadores.olvidar(l);
        BlockStorage.clearBlockInfo(l);
    }
}
