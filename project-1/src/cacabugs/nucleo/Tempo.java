package cacabugs.nucleo;

/** Relógio do jogo: o laço principal roda a 60 quadros (ticks) por segundo. */
public final class Tempo {
    public static final int TICKS_POR_SEGUNDO = 60;

    private Tempo() {
    }

    public static int emTicks(double segundos) {
        return (int) Math.round(segundos * TICKS_POR_SEGUNDO);
    }

    public static int emSegundos(int ticks) {
        return (ticks + TICKS_POR_SEGUNDO - 1) / TICKS_POR_SEGUNDO;
    }
}
