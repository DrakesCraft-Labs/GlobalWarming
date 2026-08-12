package me.poma123.globalwarming.eventos;

import javax.annotation.Nonnull;

/**
 * Los fenomenos que puede vivir un mundo.
 *
 * REGLA DE DISEÑO: ninguno toca al jugador.
 *
 * Nada de daño, debuffs, sed ni obligacion de buscar refugio. Un evento cambia el ENTORNO -- la
 * temperatura ambiente, el cielo, si nieva o no -- y el jugador decide si le importa. La idea es
 * que alguien entre, mire arriba y piense "vaya tormenta", no que tenga que administrar una barra.
 *
 * Tampoco destruyen nada construido: no queman bloques, no derriten cosas y no rompen granjas.
 * Lo unico que colocan es nieve, que se derrite sola y es reversible.
 */
public enum EventoClimatico {

    OLA_DE_CALOR(
            "&6&lOLA DE CALOR",
            "&eEl aire arde. La temperatura sube en todo el mundo.",
            +12.0,
            Cielo.DESPEJADO,
            0.20),

    OLA_DE_FRIO(
            "&b&lOLA DE FRIO",
            "&fUn frente frio cubre el mundo. Cuidado con lo que se congela.",
            -12.0,
            Cielo.DESPEJADO,
            0.20),

    NEVADA(
            "&f&lNEVADA",
            "&7Esta nevando en sitios donde no suele nevar.",
            -8.0,
            Cielo.LLUVIA,
            0.15),

    TORMENTA(
            "&8&lTORMENTA",
            "&7El cielo se cierra. Se viene una buena.",
            -6.0,
            Cielo.TORMENTA,
            0.25),

    BOCHORNO(
            "&e&lBOCHORNO",
            "&eCalor pegajoso y aire quieto. No corre ni una gota de viento.",
            +6.0,
            Cielo.DESPEJADO,
            0.20);

    /** Que hace el evento con el cielo del mundo. */
    public enum Cielo {
        DESPEJADO,
        LLUVIA,
        TORMENTA,
        /** No lo toca: se queda como estuviera. */
        IGUAL
    }

    private final String titulo;
    private final String descripcion;
    private final double desviacionCelsius;
    private final Cielo cielo;
    private final double peso;

    EventoClimatico(String titulo, String descripcion, double desviacionCelsius, Cielo cielo, double peso) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.desviacionCelsius = desviacionCelsius;
        this.cielo = cielo;
        this.peso = peso;
    }

    @Nonnull
    public String getTitulo() {
        return titulo;
    }

    @Nonnull
    public String getDescripcion() {
        return descripcion;
    }

    /** Cuanto suma o resta a la temperatura ambiente mientras dura. */
    public double getDesviacionCelsius() {
        return desviacionCelsius;
    }

    @Nonnull
    public Cielo getCielo() {
        return cielo;
    }

    /** Probabilidad relativa de que salga este y no otro. */
    public double getPeso() {
        return peso;
    }

    /** Si coloca nieve en superficie mientras dura. */
    public boolean nieva() {
        return this == NEVADA;
    }

    /** La clave con la que se lee su configuracion. */
    @Nonnull
    public String getClaveConfig() {
        return name().toLowerCase().replace('_', '-');
    }
}
