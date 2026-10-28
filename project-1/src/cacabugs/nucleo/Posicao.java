package cacabugs.nucleo;

/**
 * Uma casa do labirinto: x é a coluna e y é a linha.
 * É imutável: mover devolve uma nova posição em vez de alterar esta.
 */
public record Posicao(int x, int y) {

    public Posicao mover(Direcao direcao) {
        return mover(direcao, 1);
    }

    public Posicao mover(Direcao direcao, int casas) {
        return new Posicao(x + direcao.dx() * casas, y + direcao.dy() * casas);
    }

    public double distanciaAte(Posicao outra) {
        return Math.hypot(x - outra.x, y - outra.y);
    }
}
