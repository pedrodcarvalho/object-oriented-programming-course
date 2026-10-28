package cacabugs.coletaveis;

import cacabugs.eventos.TipoEvento;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.nucleo.Tempo;

/** Bônus que aparece duas vezes por fase, vale mais a cada fase e some em 10 segundos. */
public class Cafe extends Coletavel {
    private static final int DURACAO = Tempo.emTicks(10);
    private int ticksRestantes = DURACAO;

    public Cafe(Posicao posicao, int pontos) {
        super(posicao, pontos);
    }

    @Override
    public void atualizar(Partida partida) {
        if (--ticksRestantes <= 0) {
            desativar();
        }
    }

    @Override
    public void aoSerColetado(Partida partida) {
        coletar(partida, TipoEvento.CAFE_COLETADO);
    }

    @Override
    public void aoReiniciarRodada(Partida partida) {
        desativar();
    }

    @Override
    protected String chaveDoSprite() {
        return "cafe";
    }
}
