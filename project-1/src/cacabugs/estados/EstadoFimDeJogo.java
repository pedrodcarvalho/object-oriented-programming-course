package cacabugs.estados;

import cacabugs.Jogo;
import cacabugs.gerenciadores.Comando;
import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Partida;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

/** Acabaram as vidas: mostra os pontos, salva o recorde e volta ao menu com ENTER. */
public class EstadoFimDeJogo implements EstadoDoJogo {
    private final Jogo jogo;
    private final Partida partida;

    public EstadoFimDeJogo(Jogo jogo, Partida partida) {
        this.jogo = jogo;
        this.partida = partida;
    }

    @Override
    public void entrar() {
        jogo.placar().salvarRecorde();
    }

    @Override
    public void tratar(Comando comando) {
        if (comando == Comando.CONFIRMAR) {
            jogo.estados().trocar(new EstadoMenu(jogo));
        }
    }

    @Override
    public void atualizar() {
        // tela parada
    }

    @Override
    public void desenhar(Tela tela) {
        partida.desenharAte(tela, Camada.COLETAVEIS);
        jogo.painel().desenhar(tela, partida);
        boolean recorde = partida.placar().bateuRecorde();
        Avisos.caixa(tela, partida, Estilo.ALERTA,
                new Avisos.Linha("FIM DE JOGO", Estilo.ALERTA),
                new Avisos.Linha("", Estilo.PADRAO),
                new Avisos.Linha("Pontos: " + partida.placar().getPontos(), Estilo.TITULO),
                new Avisos.Linha(recorde ? "NOVO RECORDE!" : "Fase alcançada: " + partida.numeroDaFase(),
                        recorde ? Estilo.DESTAQUE : Estilo.PADRAO),
                new Avisos.Linha("", Estilo.PADRAO),
                new Avisos.Linha("ENTER: voltar ao menu", Estilo.APAGADO));
        PainelLateral.desenharAjuda(tela, "ENTER: menu  ·  Q: sair");
    }
}
