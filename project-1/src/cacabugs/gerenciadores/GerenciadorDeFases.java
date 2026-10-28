package cacabugs.gerenciadores;

import cacabugs.coletaveis.Cafe;
import cacabugs.eventos.TipoEvento;
import cacabugs.mapa.FabricaDeFase;
import cacabugs.mapa.Fase;
import cacabugs.mapa.MapaInvalidoException;
import cacabugs.nucleo.Partida;

import java.util.List;

/**
 * Monta cada fase a partir do arquivo do labirinto, aplica a configuração
 * da fase, faz o café aparecer e percebe quando o build passou (tudo coletado).
 */
public class GerenciadorDeFases {
    private static final int[] COLETAS_QUE_TRAZEM_CAFE = {70, 170};

    private final FabricaDeFase fabrica = new FabricaDeFase();
    private final List<String> mapa;
    private int numero;
    private int restantes;
    private int coletados;
    private ConfiguracaoDaFase config;

    public GerenciadorDeFases(List<String> mapa) {
        this.mapa = mapa;
    }

    public void conectar(Partida partida) {
        partida.eventos().inscrever(TipoEvento.BIT_COLETADO, e -> aoColetar(partida));
        partida.eventos().inscrever(TipoEvento.BREAKPOINT_COLETADO, e -> aoColetar(partida));
    }

    public void carregar(int numeroDaFase, Partida partida) throws MapaInvalidoException {
        Fase fase = fabrica.criar(mapa);
        numero = numeroDaFase;
        config = ConfiguracaoDaFase.para(numeroDaFase);
        restantes = fase.coletaveis().size();
        coletados = 0;
        partida.instalar(fase, config);
    }

    private void aoColetar(Partida partida) {
        restantes--;
        coletados++;
        for (int marca : COLETAS_QUE_TRAZEM_CAFE) {
            if (coletados == marca) {
                partida.colocarColetavel(new Cafe(partida.labirinto().posicaoDoCafe(), config.valorDoCafe()));
            }
        }
        if (restantes == 0) {
            partida.marcarFaseConcluida();
        }
    }

    public int numero() {
        return numero;
    }

    public int restantes() {
        return restantes;
    }

    public ConfiguracaoDaFase config() {
        return config;
    }
}
