package cacabugs.mapa;

/** Lançada quando o arquivo de uma fase está malformado. */
public class MapaInvalidoException extends Exception {
    private static final long serialVersionUID = 1L;

    public MapaInvalidoException(String mensagem) {
        super(mensagem);
    }
}
