package cacabugs.mapa;

import cacabugs.coletaveis.Coletavel;
import cacabugs.entidades.Bug;
import cacabugs.nucleo.Posicao;

import java.util.List;

/** Tudo que a FabricaDeFase monta a partir do arquivo do labirinto. */
public record Fase(Labirinto labirinto, Posicao inicioDoDebugger, List<Bug> bugs, List<Coletavel> coletaveis) {
}
