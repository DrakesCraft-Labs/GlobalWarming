package me.poma123.globalwarming.items.machines;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import com.github.drakescraft_labs.slimefun4.api.items.ItemGroup;
import com.github.drakescraft_labs.slimefun4.api.items.SlimefunItemStack;
import com.github.drakescraft_labs.slimefun4.api.recipes.RecipeType;
import com.github.drakescraft_labs.slimefun4.core.attributes.RecipeDisplayItem;
import com.github.drakescraft_labs.slimefun4.legacy.Objects.SlimefunItem.abstractItems.AContainer;

import me.poma123.globalwarming.GlobalWarmingPlugin;
import me.poma123.globalwarming.Items;

/**
 * Fija bajo tierra el CO₂ capturado, y con ello baja la contaminación del mundo de verdad.
 *
 * POR QUE HACIA FALTA
 *
 * Antes la contaminación sólo subía. Existían mecanismos para bajarla -- que creciera un árbol,
 * criar animales, el Compresor de Aire -- pero con valores tan pequeños que eran decorativos: con
 * las 1.370 unidades acumuladas en el mundo de Slimefun, limpiarlo plantando árboles habrían sido
 * más de 270.000 árboles. La gente lo notó y lo dijo: la temperatura sólo subía.
 *
 * COMO CIERRA EL CICLO
 *
 * El Compresor de Aire ya convertía carbono en Bombonas de CO₂, pero esas bombonas no servían para
 * nada: capturabas el carbono y ahí se quedaba. Esta máquina las consume y descuenta contaminación
 * al hacerlo. Así la cadena entera tiene sentido:
 *
 *     máquinas contaminan  ->  Compresor de Aire captura  ->  Sumidero fija  ->  baja la temperatura
 *
 * Y sigue costando: hay que fabricar las bombonas, alimentarlas con carbono y darle energía al
 * sumidero. Revertir es más caro que ensuciar, que es justo lo que debe pasar; lo que cambia es
 * que ahora es posible.
 *
 * CUANTO DESCUENTA
 *
 * No se decide aquí. La cantidad va en el config, bajo `pollution.absorption.machines`, y la
 * aplica PollutionListener al terminar cada operación. Se hizo así para no duplicar la lógica que
 * ya existía para el Compresor de Aire.
 */
public abstract class SumideroCarbono extends AContainer implements RecipeDisplayItem {

    public static final RecipeType RECIPE_TYPE = new RecipeType(
            new NamespacedKey(GlobalWarmingPlugin.getInstance(), "sumidero_carbono"), Items.SUMIDERO_CARBONO
    );

    protected SumideroCarbono(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    protected void registerDefaultRecipes() {
        // Devuelve la bombona vacia para que se pueda reutilizar: si no, cada ciclo de limpieza
        // consumiria cristal y la unica forma de limpiar seria tambien una forma de gastar.
        registerRecipe(12, new ItemStack[] { Items.CO2_CANISTER }, new ItemStack[] { Items.EMPTY_CANISTER });
    }

    @Override
    public String getMachineIdentifier() {
        return "GW_SUMIDERO_CARBONO";
    }

    @Override
    public ItemStack getProgressBar() {
        return new ItemStack(Material.IRON_SHOVEL);
    }
}
