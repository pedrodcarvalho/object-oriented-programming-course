package cacabugs.nucleo;

/**
 * Tudo que existe dentro do labirinto: jogador, bugs, coletáveis e efeitos.
 * O laço do jogo trata todas as entidades da mesma forma (polimorfismo):
 * chama atualizar() e desenhar() sem saber o tipo concreto de cada uma.
 */
public abstract class Entidade implements Atualizavel, Desenhavel {
    protected Posicao posicao;
    private boolean ativa = true;

    protected Entidade(Posicao posicao) {
        this.posicao = posicao;
    }

    public Posicao getPosicao() {
        return posicao;
    }

    public boolean estaAtiva() {
        return ativa;
    }

    /** Marca a entidade para ser retirada da partida no fim do quadro. */
    public void desativar() {
        ativa = false;
    }

    /** Em que camada a entidade é desenhada. */
    public abstract Camada camada();

    /**
     * Chamado quando a rodada recomeça (início de fase ou depois de perder uma vida).
     * Por padrão não faz nada; personagens voltam ao início e itens temporários somem.
     */
    public void aoReiniciarRodada(Partida partida) {
    }
}
