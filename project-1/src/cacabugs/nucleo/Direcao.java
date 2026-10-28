package cacabugs.nucleo;

/**
 * As quatro direções do labirinto. A ordem dos valores é a ordem de desempate
 * do Pac-Man original: cima, esquerda, baixo, direita.
 */
public enum Direcao {
    CIMA(0, -1),
    ESQUERDA(-1, 0),
    BAIXO(0, 1),
    DIREITA(1, 0);

    private final int dx;
    private final int dy;

    Direcao(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    public Direcao oposta() {
        return switch (this) {
            case CIMA -> BAIXO;
            case BAIXO -> CIMA;
            case ESQUERDA -> DIREITA;
            case DIREITA -> ESQUERDA;
        };
    }
}
