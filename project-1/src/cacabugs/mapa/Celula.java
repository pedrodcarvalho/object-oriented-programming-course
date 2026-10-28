package cacabugs.mapa;

import cacabugs.coletaveis.Coletavel;

/** Uma casa do labirinto: o tipo dela e, opcionalmente, um coletável em cima. */
public class Celula {
    private final TipoCelula tipo;
    private Coletavel coletavel;

    public Celula(TipoCelula tipo) {
        this.tipo = tipo;
    }

    public TipoCelula getTipo() {
        return tipo;
    }

    /** O coletável desta casa, ou null. Itens já coletados ou expirados somem sozinhos. */
    public Coletavel getColetavel() {
        if (coletavel != null && !coletavel.estaAtiva()) {
            coletavel = null;
        }
        return coletavel;
    }

    public void colocar(Coletavel novo) {
        this.coletavel = novo;
    }
}
