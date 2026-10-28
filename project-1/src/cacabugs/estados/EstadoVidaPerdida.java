package cacabugs.estados;

import cacabugs.Jogo;
import cacabugs.entidades.Debugger;
import cacabugs.gerenciadores.Comando;
import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Direcao;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Tela;

/** Animação de quando um bug pega o Debugger: tudo congela, a setinha gira e "trava". */
public class EstadoVidaPerdida implements EstadoDoJogo {
    private static final int CONGELADO = Tempo.emTicks(0.7);
    private static final int GIRANDO = Tempo.emTicks(1.7);
    private static final int DURACAO = Tempo.emTicks(2.4);
    private static final int TICKS_POR_QUADRO = Tempo.emTicks(0.08);

    private final Jogo jogo;
    private final Partida partida;
    private int ticks;

    public EstadoVidaPerdida(Jogo jogo, Partida partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    @Override
    public void tratar(Comando comando) {
        // durante a animação as teclas são ignoradas
    }

    @Override
    public void atualizar() {
        if (++ticks < DURACAO) {
            return;
        }
        Debugger debugger = partida.debugger();
        debugger.perderVida();
        if (debugger.temVidas()) {
            partida.reiniciarRodada();
            jogo.estados().trocar(new EstadoJogando(jogo, partida));
        } else {
            jogo.estados().trocar(new EstadoFimDeJogo(jogo, partida));
        }
    }

    @Override
    public void desenhar(Tela tela) {
        if (ticks < CONGELADO) {
            partida.desenhar(tela);
        } else {
            partida.desenharAte(tela, Camada.COLETAVEIS);
            String chave = ticks < GIRANDO
                    ? Debugger.chaveDoSprite(Direcao.values()[(ticks / TICKS_POR_QUADRO) % 4])
                    : "debugger.falha";
            tela.desenharNaCasa(partida.debugger().getPosicao(), partida.recursos().sprite(chave));
        }
        jogo.painel().desenhar(tela, partida);
        PainelLateral.desenharAjuda(tela, "Um bug te pegou!");
    }
}
