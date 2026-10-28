package cacabugs.entidades;

import cacabugs.mapa.Labirinto;
import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Direcao;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.terminal.Tela;

import java.util.Locale;

/**
 * O jogador: a setinha amarela de execução do depurador.
 * Guarda a próxima curva pedida e vira no primeiro cruzamento em que ela for possível.
 */
public class Debugger extends Personagem {
    public static final int VIDAS_INICIAIS = 3;

    private Direcao direcaoPedida;
    private int vidas = VIDAS_INICIAIS;

    public Debugger(Posicao inicio) {
        super(inicio, Direcao.ESQUERDA);
        this.direcaoPedida = Direcao.ESQUERDA;
    }

    public void pedirDirecao(Direcao direcao) {
        this.direcaoPedida = direcao;
    }

    public int getVidas() {
        return vidas;
    }

    public boolean temVidas() {
        return vidas > 0;
    }

    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    public void ganharVida() {
        vidas++;
    }

    @Override
    public void atualizar(Partida partida) {
        avancar(partida.config().velocidadeDebugger(), partida);
    }

    @Override
    protected boolean darPasso(Partida partida) {
        Labirinto labirinto = partida.labirinto();
        if (labirinto.passavel(labirinto.vizinho(posicao, direcaoPedida), false)) {
            direcao = direcaoPedida;
        }
        if (!labirinto.passavel(labirinto.vizinho(posicao, direcao), false)) {
            return false;
        }
        mover(labirinto);
        return true;
    }

    @Override
    public void voltarAoInicio() {
        super.voltarAoInicio();
        direcaoPedida = getDirecao();
    }

    @Override
    public void aoReiniciarRodada(Partida partida) {
        voltarAoInicio();
    }

    @Override
    public void desenhar(Tela tela, Partida partida) {
        tela.desenharNaCasa(posicao, partida.recursos().sprite(chaveDoSprite(direcao)));
    }

    public static String chaveDoSprite(Direcao direcao) {
        return "debugger." + direcao.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Camada camada() {
        return Camada.DEBUGGER;
    }
}
