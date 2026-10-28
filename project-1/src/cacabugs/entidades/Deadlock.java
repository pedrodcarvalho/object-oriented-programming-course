package cacabugs.entidades;

import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/**
 * Depende de outro bug: pega o vetor que vai do NullPointer até 2 casas à
 * frente do Debugger e o dobra. Assim fecha o cerco pelo lado oposto.
 */
public class Deadlock extends Bug {
    private static final int CASAS_A_FRENTE = 2;
    private final NullPointer parceiro;

    public Deadlock(Posicao inicio, Posicao cantoDeDispersao, NullPointer parceiro) {
        super("Deadlock", "bug.deadlock", inicio, cantoDeDispersao, 2);
        this.parceiro = parceiro;
    }

    @Override
    public Posicao calcularAlvo(Partida partida) {
        Debugger debugger = partida.debugger();
        Posicao pivo = debugger.getPosicao().mover(debugger.getDirecao(), CASAS_A_FRENTE);
        Posicao origem = parceiro.getPosicao();
        return new Posicao(2 * pivo.x() - origem.x(), 2 * pivo.y() - origem.y());
    }
}
