package me.poma123.globalwarming.eventos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Location;

/**
 * Lleva la cuenta de los climatizadores que están encendidos ahora mismo.
 *
 * POR QUE EXISTE
 *
 * La temperatura se calcula por bioma y para el mundo entero, una vez cada ciclo. Un climatizador,
 * en cambio, es local: afecta a una esfera alrededor de un bloque concreto. No encaja en el mapa
 * de biomas, así que hace falta consultarlo aparte, en el momento de preguntar por una ubicación.
 *
 * COMO SE MANTIENE AL DIA
 *
 * Cada climatizador se re-anuncia en cada tick mientras tenga energía, y se borra solo cuando deja
 * de anunciarse. Se hace así, y no marcando la baja al romper el bloque, porque un bloque puede
 * desaparecer de muchas maneras -- lo rompe una explosión, se descarga el chunk, se cae el
 * servidor -- y todas dejarían una entrada fantasma enfriando un sitio donde ya no hay máquina.
 * Al caducar por silencio, el peor caso es que una zona siga climatizada unos segundos de más.
 *
 * El coste de consultar es lineal en el número de climatizadores encendidos. Para un servidor con
 * decenas de máquinas es despreciable; si algún día fueran miles, habría que indexar por chunk.
 */
public final class RegistroClimatizadores {

    /** Si un climatizador no se anuncia en este tiempo, se considera apagado. */
    private static final long CADUCIDAD_MS = 5_000L;

    private static final Map<Location, Anuncio> ACTIVOS = new ConcurrentHashMap<>();

    private RegistroClimatizadores() {}

    /** Lo que un climatizador dice de sí mismo cada vez que se anuncia. */
    private static final class Anuncio {
        private final double radioAlCuadrado;
        private final double objetivo;
        private final long visto;

        private Anuncio(double radio, double objetivo) {
            this.radioAlCuadrado = radio * radio;
            this.objetivo = objetivo;
            this.visto = System.currentTimeMillis();
        }

        private boolean caducado(long ahora) {
            return ahora - visto > CADUCIDAD_MS;
        }
    }

    /** Un climatizador con energía avisa de que sigue encendido. */
    public static void anunciar(@Nonnull Location l, double radio, double objetivo) {
        ACTIVOS.put(l, new Anuncio(radio, objetivo));
    }

    /** Se llama al romper el bloque, para no esperar a que caduque. */
    public static void olvidar(@Nonnull Location l) {
        ACTIVOS.remove(l);
    }

    /**
     * La temperatura objetivo del climatizador que cubra esta ubicación, o null si no hay ninguno.
     *
     * Si se solapan varios, gana el más cercano: es lo que espera quien pone uno pequeño dentro de
     * una zona ya cubierta por otro, justamente para afinar un rincón.
     */
    @Nullable
    public static Double objetivoEn(@Nonnull Location l) {
        long ahora = System.currentTimeMillis();
        Double mejor = null;
        double mejorDistancia = Double.MAX_VALUE;

        for (Map.Entry<Location, Anuncio> entrada : ACTIVOS.entrySet()) {
            Location maquina = entrada.getKey();
            Anuncio a = entrada.getValue();

            if (a.caducado(ahora)) {
                ACTIVOS.remove(maquina);
                continue;
            }
            if (maquina.getWorld() == null || !maquina.getWorld().equals(l.getWorld())) {
                continue;
            }

            double distancia = maquina.distanceSquared(l);
            if (distancia <= a.radioAlCuadrado && distancia < mejorDistancia) {
                mejorDistancia = distancia;
                mejor = a.objetivo;
            }
        }
        return mejor;
    }

    /** Si esta ubicación está bajo el efecto de algún climatizador. */
    public static boolean estaCubierto(@Nonnull Location l) {
        return objetivoEn(l) != null;
    }

    /** Cuántos hay encendidos. Sólo para diagnóstico. */
    public static int activos() {
        long ahora = System.currentTimeMillis();
        ACTIVOS.entrySet().removeIf(e -> e.getValue().caducado(ahora));
        return ACTIVOS.size();
    }
}
