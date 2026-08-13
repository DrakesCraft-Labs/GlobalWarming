package me.poma123.globalwarming.eventos;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Nieve que cuaja donde normalmente no cuajaria.
 *
 * POR QUE ASI
 *
 * Minecraft solo deja nieve en biomas frios. Este evento la pone en cualquiera, que es justo lo
 * que lo hace llamativo: ver nieve en una sabana se nota.
 *
 * REGLAS QUE NO SE ROMPEN
 *
 *  - Solo se coloca sobre bloques solidos con aire encima. Nada se sustituye.
 *  - Solo sobre suelo natural SIN TRABAJAR: hierba, tierra, arena, grava, barro. Nada de piedra,
 *    losas, terracota ni granito.
 *
 *    Esto se aprendio en produccion. La primera version aceptaba STONE, DEEPSLATE, TERRACOTTA y
 *    GRANITE por considerarlos "naturales", y son materiales de CONSTRUCCION: la primera nevada
 *    dejo nieve encima de las casas de medio servidor. Un jugador pidio ayuda por el chat y otro
 *    dio por muertos sus cultivos.
 *
 *    La regla ahora es al reves: si alguien pudiera haberlo colocado a proposito, no nieva encima.
 *  - Se coloca poca y cerca de quien esta jugando: nadie ve la nieve que cae a 500 bloques, y
 *    colocarla igualmente solo gasta CPU.
 *  - Se derrite sola cuando sube la temperatura. No deja rastro permanente.
 */
final class Nevada {

    /** Radio alrededor del jugador donde puede cuajar. */
    private static final int RADIO = 12;

    private Nevada() {}

    /**
     * Intenta poner unos copos alrededor del jugador.
     *
     * @param p       sobre quien nieva
     * @param intentos cuantos sitios se prueban; no todos valdran
     */
    static void copos(@Nonnull Player p, int intentos) {
        World mundo = p.getWorld();
        Location centro = p.getLocation();
        ThreadLocalRandom azar = ThreadLocalRandom.current();

        for (int i = 0; i < intentos; i++) {
            int x = centro.getBlockX() + azar.nextInt(-RADIO, RADIO + 1);
            int z = centro.getBlockZ() + azar.nextInt(-RADIO, RADIO + 1);

            // Solo se trabaja sobre chunk ya cargado: forzar la carga por un copo de nieve seria
            // pagar carisimo un detalle estetico.
            if (!mundo.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }

            Block encima = mundo.getHighestBlockAt(x, z);
            Block suelo = encima.getType().isAir() ? encima.getRelative(0, -1, 0) : encima;
            Block hueco = suelo.getRelative(0, 1, 0);

            if (!hueco.getType().isAir()) {
                continue;
            }
            if (!esSueloValido(suelo.getType())) {
                continue;
            }
            // Bajo techo no nieva.
            if (mundo.getHighestBlockYAt(x, z) > suelo.getY() + 1) {
                continue;
            }
            // Ni dentro del radio de un climatizador encendido: es la razon principal para
            // construir uno, y sin esto la maquina solo cambiaria un numero en el termometro.
            if (RegistroClimatizadores.estaCubierto(hueco.getLocation())) {
                continue;
            }

            hueco.setType(Material.SNOW, false);
        }
    }

    /**
     * Si sobre ese bloque tiene sentido que cuaje.
     *
     * Lista blanca a proposito, no lista negra: con una lista negra, cualquier bloque nuevo que
     * saque Mojang entraria por defecto y acabariamos tapando algo que no toca.
     */
    private static boolean esSueloValido(@Nonnull Material tipo) {
        switch (tipo) {
            // Suelo natural sin trabajar. Nada de esto se coloca a mano para construir.
            case GRASS_BLOCK:
            case DIRT:
            case COARSE_DIRT:
            case ROOTED_DIRT:
            case PODZOL:
            case MYCELIUM:
            case SAND:
            case RED_SAND:
            case GRAVEL:
            case CLAY:
            case MUD:
            case MOSS_BLOCK:
            case SNOW_BLOCK:
            case POWDER_SNOW:
                return true;
            default:
                return false;
        }
    }
}
