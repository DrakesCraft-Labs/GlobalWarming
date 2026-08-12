package me.poma123.globalwarming.eventos;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.poma123.globalwarming.GlobalWarmingPlugin;

/**
 * Quien ha pedido que el clima deje de escribirle.
 *
 * POR QUE PERSISTE EN DISCO
 *
 * Guardarlo solo en memoria significa que al reiniciar el servidor todo el mundo vuelve a recibir
 * los mensajes. Quien silencio algo lo silencio porque le molestaba: obligarle a repetirlo cada
 * reinicio es peor que no tener la opcion.
 *
 * Se guarda por UUID y no por nombre, que los nombres cambian.
 */
public final class Silenciados {

    private final Set<UUID> silenciados = new HashSet<>();
    private final File fichero;

    public Silenciados(@Nonnull GlobalWarmingPlugin plugin) {
        this.fichero = new File(plugin.getDataFolder(), "silenciados.yml");
        cargar();
    }

    private void cargar() {
        if (!fichero.exists()) {
            return;
        }
        YamlConfiguration datos = YamlConfiguration.loadConfiguration(fichero);
        for (String bruto : datos.getStringList("silenciados")) {
            try {
                silenciados.add(UUID.fromString(bruto));
            } catch (IllegalArgumentException ignorado) {
                // Una entrada corrupta no debe impedir cargar el resto.
            }
        }
    }

    private void guardar() {
        YamlConfiguration datos = new YamlConfiguration();
        datos.set("silenciados", silenciados.stream().map(UUID::toString).toList());
        try {
            datos.save(fichero);
        } catch (IOException e) {
            GlobalWarmingPlugin.getInstance().getLogger().log(Level.WARNING, e,
                    () -> "No se pudo guardar la lista de jugadores que silenciaron el clima");
        }
    }

    /** Si a este jugador NO hay que escribirle. */
    public boolean estaSilenciado(@Nonnull Player p) {
        return silenciados.contains(p.getUniqueId());
    }

    /**
     * Cambia el estado y lo guarda.
     *
     * @return true si a partir de ahora esta silenciado
     */
    public boolean alternar(@Nonnull Player p) {
        boolean ahoraSilenciado;
        if (silenciados.remove(p.getUniqueId())) {
            ahoraSilenciado = false;
        } else {
            silenciados.add(p.getUniqueId());
            ahoraSilenciado = true;
        }
        guardar();
        return ahoraSilenciado;
    }
}
