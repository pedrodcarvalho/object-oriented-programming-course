package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/** Cada bug volta para o seu canto do labirinto: um respiro para o jogador. */
public class Dispersao extends ModoHostil {

    @Override
    public Posicao alvo(Bug bug, Partida partida) {
        return bug.getCantoDeDispersao();
    }

    @Override
    public String descricao() {
        return "dispersa";
    }
}
