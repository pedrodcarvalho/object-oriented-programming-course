package cacabugs.entidades;

import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/** Aponta direto para você: o alvo é a casa onde o Debugger está. */
public class NullPointer extends Bug {

    public NullPointer(Posicao inicio, Posicao cantoDeDispersao) {
        super("NullPointer", "bug.nullpointer", inicio, cantoDeDispersao, 0);
    }

    @Override
    public Posicao calcularAlvo(Partida partida) {
        return partida.debugger().getPosicao();
    }
}
