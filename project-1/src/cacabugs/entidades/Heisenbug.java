package cacabugs.entidades;

import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/** Some quando observado de perto: persegue de longe e foge para o canto quando chega a 8 casas. */
public class Heisenbug extends Bug {
    private static final double DISTANCIA_DE_OBSERVACAO = 8;

    public Heisenbug(Posicao inicio, Posicao cantoDeDispersao) {
        super("Heisenbug", "bug.heisenbug", inicio, cantoDeDispersao, 3);
    }

    @Override
    public Posicao calcularAlvo(Partida partida) {
        Posicao debugger = partida.debugger().getPosicao();
        return posicao.distanciaAte(debugger) > DISTANCIA_DE_OBSERVACAO ? debugger : getCantoDeDispersao();
    }
}
