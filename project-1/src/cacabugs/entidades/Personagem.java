package cacabugs.entidades;

import cacabugs.mapa.Labirinto;
import cacabugs.nucleo.Direcao;
import cacabugs.nucleo.Entidade;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.nucleo.Tempo;

/**
 * Algo que anda pelo labirinto, casa a casa.
 * A velocidade é em casas por segundo: a cada quadro o personagem acumula
 * um pouco de "progresso" e dá um passo sempre que o progresso chega a 1.
 */
public abstract class Personagem extends Entidade {
    private Posicao inicio;
    private final Direcao direcaoInicial;
    protected Direcao direcao;
    private double progresso;

    protected Personagem(Posicao inicio, Direcao direcaoInicial) {
        super(inicio);
        this.inicio = inicio;
        this.direcaoInicial = direcaoInicial;
        this.direcao = direcaoInicial;
    }

    public Direcao getDirecao() {
        return direcao;
    }

    public Posicao getInicio() {
        return inicio;
    }

    public void definirInicio(Posicao novoInicio) {
        this.inicio = novoInicio;
    }

    public void voltarAoInicio() {
        posicao = inicio;
        direcao = direcaoInicial;
        progresso = 0;
    }

    /** Acumula o deslocamento do quadro e dá um passo a cada casa completa. */
    protected void avancar(double casasPorSegundo, Partida partida) {
        progresso += casasPorSegundo / Tempo.TICKS_POR_SEGUNDO;
        while (progresso >= 1.0) {
            progresso -= 1.0;
            if (!darPasso(partida)) {
                progresso = 0;
                return;
            }
        }
    }

    /** Decide a direção e anda uma casa. Devolve false se ficou parado. */
    protected abstract boolean darPasso(Partida partida);

    protected void mover(Labirinto labirinto) {
        posicao = labirinto.vizinho(posicao, direcao);
    }
}
