package cacabugs.mapa;

import cacabugs.nucleo.Desenhavel;
import cacabugs.nucleo.Direcao;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.terminal.Sprite;
import cacabugs.terminal.Tela;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * A placa de circuito: uma grade de células lida do arquivo da fase.
 * As bordas "dão a volta" (o túnel lateral leva ao lado oposto da tela).
 */
public class Labirinto implements Desenhavel {
    private final Celula[][] celulas;
    private final boolean[][] borda;
    private final int largura;
    private final int altura;
    private final Posicao saidaDoBacklog;
    private final Posicao centroDoBacklog;
    private final Posicao posicaoDoCafe;
    private boolean piscando;

    public Labirinto(Celula[][] celulas, Posicao saidaDoBacklog, Posicao centroDoBacklog, Posicao posicaoDoCafe) {
        this.celulas = celulas;
        this.altura = celulas.length;
        this.largura = celulas[0].length;
        this.saidaDoBacklog = saidaDoBacklog;
        this.centroDoBacklog = centroDoBacklog;
        this.posicaoDoCafe = posicaoDoCafe;
        this.borda = calcularBordas();
    }

    public int getLargura() {
        return largura;
    }

    public int getAltura() {
        return altura;
    }

    /** Casa logo acima da porta: por onde os bugs entram e saem do backlog. */
    public Posicao saidaDoBacklog() {
        return saidaDoBacklog;
    }

    /** Casa logo abaixo da porta, dentro do backlog. */
    public Posicao centroDoBacklog() {
        return centroDoBacklog;
    }

    public Posicao posicaoDoCafe() {
        return posicaoDoCafe;
    }

    public Celula celula(Posicao p) {
        return celulas[Math.floorMod(p.y(), altura)][Math.floorMod(p.x(), largura)];
    }

    /** A casa vizinha na direção dada, dando a volta pelas bordas (túnel). */
    public Posicao vizinho(Posicao p, Direcao direcao) {
        Posicao n = p.mover(direcao);
        return new Posicao(Math.floorMod(n.x(), largura), Math.floorMod(n.y(), altura));
    }

    public boolean passavel(Posicao p, boolean acessoAoBacklog) {
        return celula(p).getTipo().passavel(acessoAoBacklog);
    }

    public boolean ehTunel(Posicao p) {
        return celula(p).getTipo() == TipoCelula.TUNEL;
    }

    public void setPiscando(boolean piscando) {
        this.piscando = piscando;
    }

    /**
     * Primeiro passo do menor caminho entre duas casas (busca em largura).
     * Usado pelos bugs capturados voltando ao backlog e pelos que estão saindo dele.
     */
    public Direcao direcaoDoMenorCaminho(Posicao origem, Posicao destino, boolean acessoAoBacklog) {
        if (origem.equals(destino)) {
            return null;
        }
        Direcao[][] primeiroPasso = new Direcao[altura][largura];
        boolean[][] visitada = new boolean[altura][largura];
        Queue<Posicao> fila = new ArrayDeque<>();
        visitada[origem.y()][origem.x()] = true;
        for (Direcao d : Direcao.values()) {
            Posicao n = vizinho(origem, d);
            if (passavel(n, acessoAoBacklog) && !visitada[n.y()][n.x()]) {
                visitada[n.y()][n.x()] = true;
                primeiroPasso[n.y()][n.x()] = d;
                fila.add(n);
            }
        }
        while (!fila.isEmpty()) {
            Posicao atual = fila.poll();
            if (atual.equals(destino)) {
                return primeiroPasso[atual.y()][atual.x()];
            }
            for (Direcao d : Direcao.values()) {
                Posicao n = vizinho(atual, d);
                if (passavel(n, acessoAoBacklog) && !visitada[n.y()][n.x()]) {
                    visitada[n.y()][n.x()] = true;
                    primeiroPasso[n.y()][n.x()] = primeiroPasso[atual.y()][atual.x()];
                    fila.add(n);
                }
            }
        }
        return null;
    }

    @Override
    public void desenhar(Tela tela, Partida partida) {
        Sprite paredeBorda = partida.recursos().sprite(piscando ? "parede.destaque" : "parede.borda");
        Sprite paredeInterna = partida.recursos().sprite("parede.interna");
        Sprite porta = partida.recursos().sprite("porta");
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                Posicao p = new Posicao(x, y);
                TipoCelula tipo = celulas[y][x].getTipo();
                if (tipo == TipoCelula.PAREDE) {
                    tela.preencherCasa(p, borda[y][x] ? paredeBorda : paredeInterna);
                } else if (tipo == TipoCelula.PORTA) {
                    tela.preencherCasa(p, porta);
                }
            }
        }
    }

    /** Paredes encostadas num caminho ficam mais claras; o miolo dos blocos, mais escuro. */
    private boolean[][] calcularBordas() {
        boolean[][] resultado = new boolean[altura][largura];
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (celulas[y][x].getTipo() != TipoCelula.PAREDE) {
                    continue;
                }
                for (int dy = -1; dy <= 1 && !resultado[y][x]; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int ny = y + dy;
                        int nx = x + dx;
                        if (ny >= 0 && ny < altura && nx >= 0 && nx < largura
                                && celulas[ny][nx].getTipo() != TipoCelula.PAREDE) {
                            resultado[y][x] = true;
                            break;
                        }
                    }
                }
            }
        }
        return resultado;
    }
}
