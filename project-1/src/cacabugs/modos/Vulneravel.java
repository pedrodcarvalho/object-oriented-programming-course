package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.eventos.Evento;
import cacabugs.eventos.TipoEvento;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Sprite;

/** Depois de um breakpoint: o bug fica azul, lento, vaga ao acaso e pode ser capturado. */
public class Vulneravel implements ModoDoBug {
    private static final int TICKS_POR_PISCADA = Tempo.emTicks(0.2);

    @Override
    public Posicao alvo(Bug bug, Partida partida) {
        return null;
    }

    @Override
    public void aoEncostar(Bug bug, Partida partida) {
        int valor = partida.placar().registrarCaptura();
        bug.mudarPara(new Capturado());
        partida.eventos().publicar(new Evento(TipoEvento.BUG_CAPTURADO, bug.getPosicao(), valor));
    }

    @Override
    public double velocidade(Bug bug, Partida partida) {
        return partida.config().velocidadeBugVulneravel();
    }

    @Override
    public Sprite aparencia(Bug bug, Partida partida) {
        boolean branco = partida.modos().vulneravelAcabando()
                && (partida.tick() / TICKS_POR_PISCADA) % 2 == 0;
        return partida.recursos().sprite(branco ? "bug.piscando" : "bug.vulneravel");
    }

    @Override
    public ModoDoBug aoTerminarBreakpoint(Bug bug, ModoDoBug modoGlobal) {
        return modoGlobal;
    }

    @Override
    public String descricao() {
        return "vulnerável";
    }
}
