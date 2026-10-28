package cacabugs.mapa;

/**
 * Tipos de casa do labirinto.
 * "aberta" = qualquer um passa; "restrita" = só bugs com acesso ao backlog passam.
 */
public enum TipoCelula {
    PAREDE(false, false),
    CORREDOR(true, false),
    TUNEL(true, false),
    PORTA(false, true),
    BACKLOG(false, true);

    private final boolean aberta;
    private final boolean restrita;

    TipoCelula(boolean aberta, boolean restrita) {
        this.aberta = aberta;
        this.restrita = restrita;
    }

    public boolean passavel(boolean acessoAoBacklog) {
        return aberta || (restrita && acessoAoBacklog);
    }
}
