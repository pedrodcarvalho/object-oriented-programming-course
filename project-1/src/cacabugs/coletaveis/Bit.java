package cacabugs.coletaveis;

import cacabugs.eventos.TipoEvento;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/** Uma linha de código revisada: 10 pontos. */
public class Bit extends Coletavel {
    public static final int PONTOS = 10;

    public Bit(Posicao posicao) {
        super(posicao, PONTOS);
    }

    @Override
    public void aoSerColetado(Partida partida) {
        coletar(partida, TipoEvento.BIT_COLETADO);
    }

    @Override
    protected String chaveDoSprite() {
        return "bit";
    }
}
