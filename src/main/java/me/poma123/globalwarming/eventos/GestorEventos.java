package me.poma123.globalwarming.eventos;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.poma123.globalwarming.GlobalWarmingPlugin;

/**
 * Lanza y mantiene los fenomenos climaticos.
 *
 * COMO FUNCIONA
 *
 * Cada cierto tiempo se sortea si empieza un evento en cada mundo vigilado. Si sale, se anuncia,
 * se le da un rato de duracion y se aplica: cambia el cielo y suma una desviacion a la
 * temperatura ambiente. Al terminar se anuncia el final y todo vuelve a la normalidad.
 *
 * QUE **NO** HACE
 *
 * No toca al jugador. Ni daño, ni efectos de pocion, ni sed, ni castigo por estar AFK. Esto es
 * deliberado: la queja que origino este sistema fue justamente que el clima obligaba a estar
 * pendiente, y la respuesta no es suavizar el castigo sino quitarlo y que el clima se note en el
 * mundo, no en la barra de vida.
 *
 * La nieve es lo unico que coloca bloques, y es reversible: se derrite sola.
 */
public class GestorEventos {

    /** Lo que esta pasando ahora mismo en cada mundo. */
    private final Map<String, EventoActivo> activos = new HashMap<>();

    /** Cada cuanto se sortea, en segundos. */
    private final int intervaloSorteo;
    /** Probabilidad de que en un sorteo empiece algo. */
    private final double probabilidad;
    /** Cuanto dura un evento, en segundos. */
    private final int duracionMinima;
    private final int duracionMaxima;
    /** Que eventos estan permitidos. */
    private final Map<EventoClimatico, Boolean> permitidos = new EnumMap<>(EventoClimatico.class);
    private final boolean anunciar;

    public GestorEventos(int intervaloSorteo, double probabilidad, int duracionMinima, int duracionMaxima,
                         Map<EventoClimatico, Boolean> permitidos, boolean anunciar) {
        this.intervaloSorteo = Math.max(30, intervaloSorteo);
        this.probabilidad = Math.max(0, Math.min(1, probabilidad));
        this.duracionMinima = Math.max(30, duracionMinima);
        this.duracionMaxima = Math.max(this.duracionMinima, duracionMaxima);
        this.permitidos.putAll(permitidos);
        this.anunciar = anunciar;
    }

    /** Lo que hay en marcha en un mundo, con el momento en que acaba. */
    private static final class EventoActivo {
        private final EventoClimatico evento;
        private final long acabaEn;

        private EventoActivo(EventoClimatico evento, long acabaEn) {
            this.evento = evento;
            this.acabaEn = acabaEn;
        }
    }

    /**
     * La desviacion que aporta el evento en curso, en grados Celsius.
     *
     * La consulta el calculo de temperatura. Devuelve 0 si no pasa nada, que es lo normal.
     */
    public double getDesviacion(@Nonnull World mundo) {
        EventoActivo activo = activos.get(mundo.getName());
        return activo == null ? 0 : activo.evento.getDesviacionCelsius();
    }

    /** El evento en curso, o null. Lo usa el termometro para decirlo. */
    @Nullable
    public EventoClimatico getEventoActivo(@Nonnull World mundo) {
        EventoActivo activo = activos.get(mundo.getName());
        return activo == null ? null : activo.evento;
    }

    public void arrancar(int intervaloTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                repasar();
            }
        }.runTaskTimer(GlobalWarmingPlugin.getInstance(), 20L * 30, 20L * intervaloSorteo);

        // La nieve se coloca en su propia tarea, mas frecuente y en trozos pequeños, para no
        // clavar el servidor colocando miles de bloques de golpe.
        new BukkitRunnable() {
            @Override
            public void run() {
                nevar();
            }
        }.runTaskTimer(GlobalWarmingPlugin.getInstance(), 20L * 35, 20L * 3);
    }

    private void repasar() {
        long ahora = System.currentTimeMillis();

        for (World mundo : Bukkit.getWorlds()) {
            if (!GlobalWarmingPlugin.getRegistry().isWorldEnabled(mundo.getName())) {
                continue;
            }
            // Solo en la superficie: en el Nether y el End no hay clima que valga.
            if (mundo.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            EventoActivo activo = activos.get(mundo.getName());

            if (activo != null) {
                if (ahora >= activo.acabaEn) {
                    terminar(mundo, activo.evento);
                }
                continue;
            }

            // Sin nadie delante no se lanza nada: un evento que nadie ve solo gasta tiempo de CPU.
            if (mundo.getPlayers().isEmpty()) {
                continue;
            }

            if (ThreadLocalRandom.current().nextDouble() < probabilidad) {
                EventoClimatico elegido = sortear();
                if (elegido != null) {
                    empezar(mundo, elegido, ahora);
                }
            }
        }
    }

    @Nullable
    private EventoClimatico sortear() {
        double total = 0;
        for (EventoClimatico e : EventoClimatico.values()) {
            if (permitidos.getOrDefault(e, Boolean.TRUE)) {
                total += e.getPeso();
            }
        }
        if (total <= 0) {
            return null;
        }

        double tirada = ThreadLocalRandom.current().nextDouble() * total;
        for (EventoClimatico e : EventoClimatico.values()) {
            if (!permitidos.getOrDefault(e, Boolean.TRUE)) {
                continue;
            }
            tirada -= e.getPeso();
            if (tirada <= 0) {
                return e;
            }
        }
        return null;
    }

    private void empezar(@Nonnull World mundo, @Nonnull EventoClimatico evento, long ahora) {
        int duracion = ThreadLocalRandom.current().nextInt(duracionMinima, duracionMaxima + 1);
        activos.put(mundo.getName(), new EventoActivo(evento, ahora + duracion * 1000L));

        aplicarCielo(mundo, evento.getCielo(), duracion);

        if (anunciar) {
            avisar(mundo,
                    ChatColor.DARK_GRAY + "━━━ " + color(evento.getTitulo()) + ChatColor.DARK_GRAY + " ━━━",
                    color(evento.getDescripcion()));
        }
    }

    private void terminar(@Nonnull World mundo, @Nonnull EventoClimatico evento) {
        activos.remove(mundo.getName());

        // No se fuerza el cielo al acabar: se deja que el ciclo normal del juego siga su curso.
        // Forzar "despejado" al final de una tormenta se nota artificial.
        if (anunciar) {
            avisar(mundo, color("&7El " + ChatColor.stripColor(color(evento.getTitulo())).toLowerCase()
                    + " ha pasado. El tiempo vuelve a la normalidad."), null);
        }
    }

    private void aplicarCielo(@Nonnull World mundo, @Nonnull EventoClimatico.Cielo cielo, int duracionSegundos) {
        int ticks = duracionSegundos * 20;
        switch (cielo) {
            case DESPEJADO:
                mundo.setStorm(false);
                mundo.setThundering(false);
                mundo.setWeatherDuration(ticks);
                break;
            case LLUVIA:
                mundo.setStorm(true);
                mundo.setThundering(false);
                mundo.setWeatherDuration(ticks);
                break;
            case TORMENTA:
                mundo.setStorm(true);
                mundo.setThundering(true);
                mundo.setWeatherDuration(ticks);
                mundo.setThunderDuration(ticks);
                break;
            case IGUAL:
            default:
                break;
        }
    }

    /**
     * Coloca nieve alrededor de quien este conectado, durante una nevada.
     *
     * Solo pone capas de nieve, y solo sobre bloques solidos con aire encima. No sustituye nada:
     * si hay algo construido, se queda como esta. La nieve se derrite sola cuando sube la
     * temperatura, asi que no deja rastro permanente.
     */
    private void nevar() {
        for (Map.Entry<String, EventoActivo> entrada : activos.entrySet()) {
            if (!entrada.getValue().evento.nieva()) {
                continue;
            }
            World mundo = Bukkit.getWorld(entrada.getKey());
            if (mundo == null) {
                continue;
            }

            List<Player> jugadores = mundo.getPlayers();
            for (Player p : jugadores) {
                Nevada.copos(p, 6);
            }
        }
    }

    private void avisar(@Nonnull World mundo, @Nonnull String linea1, @Nullable String linea2) {
        for (Player p : mundo.getPlayers()) {
            // Respeta a quien lo silencio con /globalwarming silenciar.
            if (GlobalWarmingPlugin.getSilenciados() != null
                    && GlobalWarmingPlugin.getSilenciados().estaSilenciado(p)) {
                continue;
            }

            p.sendMessage("");
            p.sendMessage(linea1);
            if (linea2 != null) {
                p.sendMessage(linea2);
            }
            p.sendMessage("");
        }
    }

    @Nonnull
    private static String color(@Nonnull String texto) {
        return ChatColor.translateAlternateColorCodes('&', texto);
    }
}
