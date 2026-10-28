package cacabugs.modos;

import cacabugs.entidades.Bug;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.terminal.Sprite;

/**
 * Estado de um bug (padrão State). Cada modo define alvo, velocidade,
 * aparência, o que acontece ao encostar no Debugger e para qual modo ir
 * quando algo muda. Os métodos default valem para a maioria dos modos.
 */
public interface ModoDoBug {

    /** Casa que o bug tenta alcançar; null = vagar ao acaso. */
    Posicao alvo(Bug bug, Partida partida);

    /** O que acontece quando o bug e o Debugger dividem a mesma casa. */
    void aoEncostar(Bug bug, Partida partida);

    /** Texto curto mostrado no painel lateral. */
    String descricao();

    default double velocidade(Bug bug, Partida partida) {
        return partida.config().velocidadeBug();
    }

    default Sprite aparencia(Bug bug, Partida partida) {
        return partida.recursos().sprite(bug.getChaveDoSprite());
    }

    /** Pode passar pela porta e andar dentro do backlog? */
    default boolean atravessaPorta() {
        return false;
    }

    /** Segue o menor caminho até o alvo em vez da regra de "vizinho mais próximo"? */
    default boolean usaMenorCaminho() {
        return false;
    }

    default boolean desaceleraNoTunel() {
        return true;
    }

    /** Chamado a cada quadro; devolve o próximo modo (ou ele mesmo). */
    default ModoDoBug atualizar(Bug bug, Partida partida) {
        return this;
    }

    default ModoDoBug aoMudarOnda(Bug bug, ModoDoBug proximo) {
        return this;
    }

    default ModoDoBug aoAtivarBreakpoint(Bug bug) {
        return this;
    }

    default ModoDoBug aoTerminarBreakpoint(Bug bug, ModoDoBug modoGlobal) {
        return this;
    }
}
