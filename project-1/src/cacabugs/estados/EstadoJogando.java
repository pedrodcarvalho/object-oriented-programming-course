package cacabugs.estados;

import cacabugs.Jogo;
import cacabugs.gerenciadores.Comando;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

/** A partida rodando. Começa com 2 segundos de "PRONTO!" com tudo parado. */
public class EstadoJogando implements EstadoDoJogo {
    private static final int DURACAO_DO_PRONTO = Tempo.emTicks(2);

    private final Jogo jogo;
    private final Partida partida;
    private int ticksDePreparo;

    public EstadoJogando(Jogo jogo, Partida partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    @Override
    public void entrar() {
        ticksDePreparo = DURACAO_DO_PRONTO;
    }

    @Override
    public void tratar(Comando comando) {
        if (comando.direcao() != null) {
            partida.debugger().pedirDirecao(comando.direcao());
        } else if (comando == Comando.PAUSAR) {
            jogo.estados().empilhar(new EstadoPausa(jogo, this, partida));
        }
    }

    @Override
    public void atualizar() {
        if (ticksDePreparo > 0) {
            ticksDePreparo--;
            return;
        }
        partida.atualizar();
        if (partida.debuggerFoiCapturado()) {
            jogo.estados().trocar(new EstadoVidaPerdida(jogo, partida));
        } else if (partida.faseFoiConcluida()) {
            jogo.estados().trocar(new EstadoFaseConcluida(jogo, partida));
        }
    }

    @Override
    public void desenhar(Tela tela) {
        partida.desenhar(tela);
        jogo.painel().desenhar(tela, partida);
        if (ticksDePreparo > 0) {
            Avisos.naLinhaDoCafe(tela, partida, "PRONTO!", Estilo.DESTAQUE);
        }
        PainelLateral.desenharAjuda(tela, "Setas ou WASD: mover  ·  P: pausa  ·  Q: sair");
    }
}
