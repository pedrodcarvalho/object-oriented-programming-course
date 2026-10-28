package cacabugs.coletaveis;

import cacabugs.eventos.Evento;
import cacabugs.eventos.TipoEvento;
import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Entidade;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.terminal.Tela;

/** Item parado numa casa que o Debugger pode pegar. Cada tipo reage do seu jeito. */
public abstract class Coletavel extends Entidade {
    private final int pontos;

    protected Coletavel(Posicao posicao, int pontos) {
        super(posicao);
        this.pontos = pontos;
    }

    public int getPontos() {
        return pontos;
    }

    /** O efeito de ser coletado pelo Debugger. */
    public abstract void aoSerColetado(Partida partida);

    protected abstract String chaveDoSprite();

    /** Sai do labirinto e avisa a partida (o Placar soma os pontos ao ouvir o evento). */
    protected void coletar(Partida partida, TipoEvento tipo) {
        desativar();
        partida.eventos().publicar(new Evento(tipo, posicao, pontos));
    }

    @Override
    public void atualizar(Partida partida) {
        // a maioria dos coletáveis fica parada
    }

    @Override
    public void desenhar(Tela tela, Partida partida) {
        tela.desenharNaCasa(posicao, partida.recursos().sprite(chaveDoSprite()));
    }

    @Override
    public Camada camada() {
        return Camada.COLETAVEIS;
    }
}
