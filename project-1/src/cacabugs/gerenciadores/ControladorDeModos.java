package cacabugs.gerenciadores;

import cacabugs.entidades.Bug;
import cacabugs.modos.Dispersao;
import cacabugs.modos.ModoDoBug;
import cacabugs.modos.Perseguicao;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;

/**
 * Relógio dos bugs: alterna ondas de dispersão e perseguição e
 * liga o modo vulnerável quando um breakpoint é coletado.
 * Durante o breakpoint o relógio das ondas fica parado.
 */
public class ControladorDeModos {
    private int[] ondas = {Integer.MAX_VALUE};
    private int indiceDaOnda;
    private int ticksNaOnda;
    private int ticksDeBreakpoint;
    private int ticksPiscando;

    public void iniciar(ConfiguracaoDaFase config) {
        ondas = config.ondasEmTicks();
        indiceDaOnda = 0;
        ticksNaOnda = 0;
        ticksDeBreakpoint = 0;
        ticksPiscando = config.ticksPiscando();
    }

    /** Ondas pares são de dispersão; ímpares, de perseguição. */
    public ModoDoBug novoModoGlobal() {
        return indiceDaOnda % 2 == 0 ? new Dispersao() : new Perseguicao();
    }

    public void atualizar(Partida partida) {
        if (ticksDeBreakpoint > 0) {
            if (--ticksDeBreakpoint == 0) {
                for (Bug bug : partida.bugs()) {
                    bug.aoTerminarBreakpoint(novoModoGlobal());
                }
            }
            return;
        }
        if (indiceDaOnda < ondas.length - 1 && ++ticksNaOnda >= ondas[indiceDaOnda]) {
            indiceDaOnda++;
            ticksNaOnda = 0;
            for (Bug bug : partida.bugs()) {
                bug.aoMudarOnda(novoModoGlobal());
            }
        }
    }

    public void ativarBreakpoint(Partida partida) {
        ticksDeBreakpoint = partida.config().duracaoDoBreakpoint();
        for (Bug bug : partida.bugs()) {
            bug.aoAtivarBreakpoint();
        }
    }

    public boolean breakpointAtivo() {
        return ticksDeBreakpoint > 0;
    }

    public boolean vulneravelAcabando() {
        return ticksDeBreakpoint > 0 && ticksDeBreakpoint <= ticksPiscando;
    }

    public int segundosDeBreakpoint() {
        return Tempo.emSegundos(ticksDeBreakpoint);
    }

    public String nomeDaOnda() {
        return indiceDaOnda % 2 == 0 ? "DISPERSÃO" : "PERSEGUIÇÃO";
    }
}
