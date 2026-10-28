package cacabugs.estados;

import cacabugs.Jogo;
import cacabugs.gerenciadores.Comando;
import cacabugs.mapa.MapaInvalidoException;
import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

/** Todos os bits coletados: o build passou! As paredes piscam e a próxima fase começa. */
public class EstadoFaseConcluida implements EstadoDoJogo {
    private static final int PARADO = Tempo.emTicks(1);
    private static final int DURACAO = Tempo.emTicks(3);
    private static final int TICKS_POR_PISCADA = Tempo.emTicks(0.2);

    private final Jogo jogo;
    private final Partida partida;
    private int ticks;

    public EstadoFaseConcluida(Jogo jogo, Partida partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    @Override
    public void entrar() {
        jogo.placar().salvarRecorde();
    }

    @Override
    public void tratar(Comando comando) {
        // espera a animação terminar
    }

    @Override
    public void atualizar() {
        ticks++;
        partida.labirinto().setPiscando(ticks > PARADO && (ticks / TICKS_POR_PISCADA) % 2 == 0);
        if (ticks < DURACAO) {
            return;
        }
        partida.labirinto().setPiscando(false);
        try {
            partida.avancarDeFase();
            jogo.estados().trocar(new EstadoJogando(jogo, partida));
        } catch (MapaInvalidoException e) {
            jogo.estados().trocar(new EstadoFimDeJogo(jogo, partida));
        }
    }

    @Override
    public void desenhar(Tela tela) {
        if (ticks < PARADO) {
            partida.desenhar(tela);
        } else {
            partida.desenharAte(tela, Camada.DEBUGGER);
        }
        Avisos.naLinhaDoCafe(tela, partida, "BUILD PASSOU!", Estilo.SUCESSO);
        jogo.painel().desenhar(tela, partida);
        PainelLateral.desenharAjuda(tela, "Fase " + partida.numeroDaFase() + " concluída. Prepare-se para a próxima!");
    }
}
