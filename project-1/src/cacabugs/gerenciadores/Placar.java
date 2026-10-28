package cacabugs.gerenciadores;

import cacabugs.eventos.CentralDeEventos;
import cacabugs.eventos.Evento;
import cacabugs.eventos.TipoEvento;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Pontos, recorde (salvo em arquivo) e vida extra aos 10.000 pontos.
 * Capturas com o mesmo breakpoint valem 200, 400, 800 e 1.600 pontos.
 */
public class Placar {
    public static final int PONTOS_PARA_VIDA_EXTRA = 10_000;
    private static final int PRIMEIRA_CAPTURA = 200;

    private final Path arquivoDoRecorde;
    private CentralDeEventos eventos;
    private int pontos;
    private int recorde;
    private int recordeNoInicio;
    private int capturasSeguidas;
    private boolean vidaExtraConcedida;

    public Placar(Path arquivoDoRecorde) {
        this.arquivoDoRecorde = arquivoDoRecorde;
        this.recorde = lerRecorde();
    }

    /** Zera os pontos e passa a ouvir os eventos da nova partida. */
    public void novaPartida(CentralDeEventos eventosDaPartida) {
        this.eventos = eventosDaPartida;
        pontos = 0;
        capturasSeguidas = 0;
        vidaExtraConcedida = false;
        recordeNoInicio = recorde;
        eventos.inscrever(TipoEvento.BIT_COLETADO, e -> adicionar(e.valor()));
        eventos.inscrever(TipoEvento.CAFE_COLETADO, e -> adicionar(e.valor()));
        eventos.inscrever(TipoEvento.BREAKPOINT_COLETADO, e -> {
            capturasSeguidas = 0;
            adicionar(e.valor());
        });
    }

    /** Única porta de entrada para os pontos. */
    public void adicionar(int valor) {
        pontos += valor;
        if (pontos > recorde) {
            recorde = pontos;
        }
        if (!vidaExtraConcedida && pontos >= PONTOS_PARA_VIDA_EXTRA) {
            vidaExtraConcedida = true;
            eventos.publicar(new Evento(TipoEvento.VIDA_EXTRA, null, 1));
        }
    }

    /** Soma e devolve o valor de mais uma captura com o breakpoint atual. */
    public int registrarCaptura() {
        int valor = PRIMEIRA_CAPTURA << Math.min(capturasSeguidas, 3);
        capturasSeguidas++;
        adicionar(valor);
        return valor;
    }

    public int getPontos() {
        return pontos;
    }

    public int getRecorde() {
        return recorde;
    }

    public boolean bateuRecorde() {
        return pontos > 0 && pontos > recordeNoInicio;
    }

    public void salvarRecorde() {
        try {
            Files.writeString(arquivoDoRecorde, Integer.toString(recorde));
        } catch (IOException e) {
            // sem permissão de escrita: o recorde só não fica salvo
        }
    }

    private int lerRecorde() {
        try {
            return Integer.parseInt(Files.readString(arquivoDoRecorde).trim());
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }
}
