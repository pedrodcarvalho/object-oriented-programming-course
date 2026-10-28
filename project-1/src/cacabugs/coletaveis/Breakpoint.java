package cacabugs.coletaveis;

import cacabugs.eventos.TipoEvento;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Tela;

/** 50 pontos e deixa os bugs vulneráveis por alguns segundos. Pisca no labirinto. */
public class Breakpoint extends Coletavel {
    public static final int PONTOS = 50;
    private static final int TICKS_POR_PISCADA = Tempo.emTicks(0.25);

    public Breakpoint(Posicao posicao) {
        super(posicao, PONTOS);
    }

    @Override
    public void aoSerColetado(Partida partida) {
        coletar(partida, TipoEvento.BREAKPOINT_COLETADO);
    }

    @Override
    public void desenhar(Tela tela, Partida partida) {
        if ((partida.tick() / TICKS_POR_PISCADA) % 2 == 0) {
            super.desenhar(tela, partida);
        }
    }

    @Override
    protected String chaveDoSprite() {
        return "breakpoint";
    }
}
