package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.nucleo.Partida;

/** Base dos modos perigosos (Dispersão e Perseguição): encostar custa uma vida. */
public abstract class ModoHostil implements ModoDoBug {

    @Override
    public void aoEncostar(Bug bug, Partida partida) {
        partida.capturarDebugger();
    }

    @Override
    public ModoDoBug aoMudarOnda(Bug bug, ModoDoBug proximo) {
        bug.inverterDirecao();
        return proximo;
    }

    @Override
    public ModoDoBug aoAtivarBreakpoint(Bug bug) {
        bug.inverterDirecao();
        return new Vulneravel();
    }
}
