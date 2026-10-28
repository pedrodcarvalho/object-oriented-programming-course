package cacabugs.entidades;

import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/** Corre para chegar antes: mira 4 casas à frente do Debugger. */
public class RaceCondition extends Bug {
    private static final int CASAS_A_FRENTE = 4;

    public RaceCondition(Posicao inicio, Posicao cantoDeDispersao) {
        super("RaceCondition", "bug.racecondition", inicio, cantoDeDispersao, 1);
    }

    @Override
    public Posicao calcularAlvo(Partida partida) {
        Debugger debugger = partida.debugger();
        return debugger.getPosicao().mover(debugger.getDirecao(), CASAS_A_FRENTE);
    }
}
