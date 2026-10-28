package cacabugs.gerenciadores;

import cacabugs.nucleo.Tempo;

/**
 * Números que mudam de fase para fase: velocidades, duração do breakpoint,
 * valor do café, ondas de dispersão/perseguição e espera no backlog.
 * Baseado nas tabelas do Pac-Man original, com velocidades em casas por segundo.
 */
public final class ConfiguracaoDaFase {
    private static final double VELOCIDADE_MAXIMA = 8.5;
    private static final int[] VALOR_DO_CAFE = {100, 300, 500, 500, 700, 700, 1000, 1000, 2000, 2000, 3000, 3000, 5000};

    private final int numero;

    private ConfiguracaoDaFase(int numero) {
        this.numero = numero;
    }

    public static ConfiguracaoDaFase para(int numeroDaFase) {
        return new ConfiguracaoDaFase(Math.max(1, numeroDaFase));
    }

    public int numero() {
        return numero;
    }

    private double velocidade(double fase1, double fases2a4, double depois) {
        double fator = numero == 1 ? fase1 : numero <= 4 ? fases2a4 : depois;
        return VELOCIDADE_MAXIMA * fator;
    }

    public double velocidadeDebugger() {
        return velocidade(0.80, 0.90, 1.00);
    }

    public double velocidadeBug() {
        return velocidade(0.75, 0.85, 0.95);
    }

    public double velocidadeBugVulneravel() {
        return velocidade(0.50, 0.55, 0.60);
    }

    public double velocidadeBugNoTunel() {
        return velocidade(0.40, 0.45, 0.50);
    }

    public double velocidadeNoBacklog() {
        return velocidade(0.40, 0.45, 0.50);
    }

    public double velocidadeBugCapturado() {
        return VELOCIDADE_MAXIMA * 1.6;
    }

    /** 6 s na fase 1, um segundo a menos por fase, nunca menos de 1 s. */
    public int duracaoDoBreakpoint() {
        return Tempo.emTicks(Math.max(1, 7 - numero));
    }

    /** Nos últimos instantes do breakpoint os bugs piscam em branco. */
    public int ticksPiscando() {
        return Math.min(Tempo.emTicks(2), duracaoDoBreakpoint() / 2);
    }

    public int valorDoCafe() {
        return VALOR_DO_CAFE[Math.min(numero, VALOR_DO_CAFE.length) - 1];
    }

    /** Durações alternadas: dispersão, perseguição, dispersão... A última onda (perseguição) não acaba. */
    public int[] ondasEmTicks() {
        double[] segundos;
        if (numero == 1) {
            segundos = new double[]{7, 20, 7, 20, 5, 20, 5};
        } else if (numero <= 4) {
            segundos = new double[]{7, 20, 7, 20, 5};
        } else {
            segundos = new double[]{5, 20, 5, 20, 5};
        }
        int[] ondas = new int[segundos.length + 1];
        for (int i = 0; i < segundos.length; i++) {
            ondas[i] = Tempo.emTicks(segundos[i]);
        }
        ondas[segundos.length] = Integer.MAX_VALUE;
        return ondas;
    }

    /** Quanto tempo cada bug espera no backlog no começo da rodada (ordem 0 já começa fora). */
    public int atrasoDeSaida(int ordem) {
        if (ordem == 0) {
            return 0;
        }
        double intervalo = numero == 1 ? 4 : numero <= 4 ? 3 : 2;
        return Tempo.emTicks(1 + (ordem - 1) * intervalo);
    }
}
