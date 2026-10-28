package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/**
 * Esperando no backlog (a área central). Enquanto espera, vaga lá dentro;
 * quando a espera acaba, sai pela porta e assume o modo global da partida.
 */
public class NoBacklog implements ModoDoBug {
    private int espera;

    public NoBacklog(int esperaEmTicks) {
        this.espera = esperaEmTicks;
    }

    @Override
    public ModoDoBug atualizar(Bug bug, Partida partida) {
        if (espera > 0) {
            espera--;
            return this;
        }
        if (bug.getPosicao().equals(partida.labirinto().saidaDoBacklog())) {
            return partida.modos().novoModoGlobal();
        }
        return this;
    }

    @Override
    public Posicao alvo(Bug bug, Partida partida) {
        return espera > 0 ? null : partida.labirinto().saidaDoBacklog();
    }

    @Override
    public void aoEncostar(Bug bug, Partida partida) {
        // dentro do backlog o bug não alcança o Debugger
    }

    @Override
    public double velocidade(Bug bug, Partida partida) {
        return partida.config().velocidadeNoBacklog();
    }

    @Override
    public boolean atravessaPorta() {
        return espera <= 0;
    }

    @Override
    public boolean usaMenorCaminho() {
        return true;
    }

    @Override
    public String descricao() {
        return espera > 0 ? "no backlog" : "saindo";
    }
}
