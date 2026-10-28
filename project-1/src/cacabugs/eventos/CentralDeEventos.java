package cacabugs.eventos;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Avisa os interessados quando algo acontece, sem que as classes se conheçam.
 * Ex.: o Bit só publica BIT_COLETADO; Placar e GerenciadorDeFases reagem cada um do seu jeito.
 */
public class CentralDeEventos {
    private final Map<TipoEvento, List<OuvinteDeEventos>> ouvintes = new EnumMap<>(TipoEvento.class);

    public void inscrever(TipoEvento tipo, OuvinteDeEventos ouvinte) {
        ouvintes.computeIfAbsent(tipo, t -> new ArrayList<>()).add(ouvinte);
    }

    public void publicar(Evento evento) {
        for (OuvinteDeEventos ouvinte : List.copyOf(ouvintes.getOrDefault(evento.tipo(), List.of()))) {
            ouvinte.aoReceber(evento);
        }
    }
}
