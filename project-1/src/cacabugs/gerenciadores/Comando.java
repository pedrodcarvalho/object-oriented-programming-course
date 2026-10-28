package cacabugs.gerenciadores;

import cacabugs.nucleo.Direcao;

/** O que o jogador pediu, já separado das teclas que usou para pedir. */
public enum Comando {
    CIMA(Direcao.CIMA),
    BAIXO(Direcao.BAIXO),
    ESQUERDA(Direcao.ESQUERDA),
    DIREITA(Direcao.DIREITA),
    PAUSAR(null),
    CONFIRMAR(null),
    SAIR(null);

    private final Direcao direcao;

    Comando(Direcao direcao) {
        this.direcao = direcao;
    }

    /** A direção deste comando, ou null se não for de movimento. */
    public Direcao direcao() {
        return direcao;
    }
}
