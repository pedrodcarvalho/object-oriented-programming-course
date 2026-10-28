package cacabugs.eventos;

/** Quem quer ser avisado de um tipo de evento (padrão Observer). */
@FunctionalInterface
public interface OuvinteDeEventos {
    void aoReceber(Evento evento);
}
