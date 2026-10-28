package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Sprite;

/** Bug capturado: só os "olhos" voltam correndo para o backlog e reaparecem pouco depois. */
public class Capturado implements ModoDoBug {
    private static final int ESPERA_NO_BACKLOG = Tempo.emTicks(0.5);

    @Override
    public Posicao alvo(Bug bug, Partida partida) {
        return partida.labirinto().centroDoBacklog();
    }

    @Override
    public ModoDoBug atualizar(Bug bug, Partida partida) {
        if (bug.getPosicao().equals(partida.labirinto().centroDoBacklog())) {
            return new NoBacklog(ESPERA_NO_BACKLOG);
        }
        return this;
    }

    @Override
    public void aoEncostar(Bug bug, Partida partida) {
        // olhos não fazem mal a ninguém
    }

    @Override
    public double velocidade(Bug bug, Partida partida) {
        return partida.config().velocidadeBugCapturado();
    }

    @Override
    public Sprite aparencia(Bug bug, Partida partida) {
        return partida.recursos().sprite("bug.capturado");
    }

    @Override
    public boolean atravessaPorta() {
        return true;
    }

    @Override
    public boolean usaMenorCaminho() {
        return true;
    }

    @Override
    public boolean desaceleraNoTunel() {
        return false;
    }

    @Override
    public String descricao() {
        return "capturado";
    }
}
