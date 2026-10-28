package cacabugs.estados;

import cacabugs.Jogo;
import cacabugs.gerenciadores.Comando;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

/** Fica por cima do jogo (empilhada) e o devolve exatamente como estava. */
public class EstadoPausa implements EstadoDoJogo {
    private final Jogo jogo;
    private final EstadoDoJogo pausado;
    private final Partida partida;
    private int ticks;

    public EstadoPausa(Jogo jogo, EstadoDoJogo pausado, Partida partida) {
        this.jogo = jogo;
        this.pausado = pausado;
        this.partida = partida;
    }

    @Override
    public void tratar(Comando comando) {
        if (comando == Comando.PAUSAR) {
            jogo.estados().desempilhar();
        }
    }

    @Override
    public void atualizar() {
        ticks++;
    }

    @Override
    public void desenhar(Tela tela) {
        pausado.desenhar(tela);
        boolean aceso = (ticks / Tempo.emTicks(0.5)) % 2 == 0;
        Avisos.caixa(tela, partida, Estilo.DESTAQUE,
                new Avisos.Linha("PAUSADO", Estilo.DESTAQUE),
                new Avisos.Linha("", Estilo.PADRAO),
                new Avisos.Linha(aceso ? "P para continuar" : "", Estilo.PADRAO));
    }
}
