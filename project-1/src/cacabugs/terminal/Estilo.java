package cacabugs.terminal;

/**
 * Cor da frente, cor do fundo (paleta ANSI de 256 cores) e negrito.
 * É um valor imutável: dois estilos com os mesmos campos são iguais.
 */
public record Estilo(int frente, int fundo, boolean negrito) {
    public static final int PRETO = 16;

    public static final Estilo PADRAO = frente(252);
    public static final Estilo APAGADO = frente(244);
    public static final Estilo TITULO = frente(231).emNegrito();
    public static final Estilo DESTAQUE = frente(226).emNegrito();
    public static final Estilo ALERTA = frente(196).emNegrito();
    public static final Estilo SUCESSO = frente(46).emNegrito();
    public static final Estilo INFO = frente(51).emNegrito();

    public static Estilo frente(int cor) {
        return new Estilo(cor, PRETO, false);
    }

    public Estilo emNegrito() {
        return new Estilo(frente, fundo, true);
    }

    /** Sequência ANSI (SGR) que liga este estilo no terminal. */
    public String sequenciaAnsi() {
        return "\033[0" + (negrito ? ";1" : "") + ";38;5;" + frente + ";48;5;" + fundo + "m";
    }
}
