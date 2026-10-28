package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;

/** Cada bug persegue o Debugger com a sua personalidade (polimorfismo em calcularAlvo). */
public class Perseguicao extends ModoHostil {

    @Override
    public Posicao alvo(Bug bug, Partida partida) {
        return bug.calcularAlvo(partida);
    }

    @Override
    public String descricao() {
        return "persegue";
    }
}
