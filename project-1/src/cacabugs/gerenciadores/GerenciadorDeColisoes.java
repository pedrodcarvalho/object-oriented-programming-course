package cacabugs.gerenciadores;

import cacabugs.coletaveis.Coletavel;
import cacabugs.entidades.Bug;
import cacabugs.entidades.Debugger;
import cacabugs.nucleo.Partida;

/**
 * Detecta quando o Debugger divide a casa com um coletável ou um bug e
 * repassa o efeito ao próprio objeto, sem nenhum "if" por tipo.
 */
public class GerenciadorDeColisoes {

    public void verificar(Partida partida) {
        Debugger debugger = partida.debugger();
        Coletavel coletavel = partida.labirinto().celula(debugger.getPosicao()).getColetavel();
        if (coletavel != null) {
            coletavel.aoSerColetado(partida);
        }
        for (Bug bug : partida.bugs()) {
            if (bug.getPosicao().equals(debugger.getPosicao())) {
                bug.aoEncostar(partida);
            }
        }
    }
}
