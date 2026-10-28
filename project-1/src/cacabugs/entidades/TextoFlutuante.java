package cacabugs.entidades;

import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Entidade;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

/** Pontos que aparecem por um instante onde algo foi capturado ou coletado. */
public class TextoFlutuante extends Entidade {
    private final String texto;
    private final Estilo estilo;
    private int ticksRestantes;

    public TextoFlutuante(Posicao posicao, String texto, Estilo estilo, int duracaoEmTicks) {
        super(posicao);
        this.texto = texto;
        this.estilo = estilo;
        this.ticksRestantes = duracaoEmTicks;
    }

    @Override
    public void atualizar(Partida partida) {
        if (--ticksRestantes <= 0) {
            desativar();
        }
    }

    @Override
    public void desenhar(Tela tela, Partida partida) {
        tela.desenharNaCasa(posicao, texto, estilo);
    }

    @Override
    public void aoReiniciarRodada(Partida partida) {
        desativar();
    }

    @Override
    public Camada camada() {
        return Camada.EFEITOS;
    }
}
