package cacabugs.estados;

import cacabugs.entidades.Bug;
import cacabugs.gerenciadores.ControladorDeModos;
import cacabugs.gerenciadores.GerenciadorDeRecursos;
import cacabugs.nucleo.Desenhavel;
import cacabugs.nucleo.Partida;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Sprite;
import cacabugs.terminal.Tela;

/** Painel à direita do labirinto: pontos, recorde, vidas, o modo de cada bug e a legenda. */
public class PainelLateral implements Desenhavel {
    public static final int COLUNA = 58;
    private static final int LARGURA = Tela.LARGURA - COLUNA - 1;
    private static final Estilo AZUL = Estilo.frente(33).emNegrito();

    @Override
    public void desenhar(Tela tela, Partida partida) {
        GerenciadorDeRecursos recursos = partida.recursos();
        tela.escrever(COLUNA, 0, "CAÇA-", Estilo.DESTAQUE);
        tela.escrever(COLUNA + 5, 0, "BUGS", Estilo.ALERTA);
        separador(tela, 1);

        valor(tela, 2, "PONTOS", partida.placar().getPontos(), Estilo.TITULO);
        valor(tela, 3, "RECORDE", partida.placar().getRecorde(), Estilo.PADRAO);
        valor(tela, 4, "FASE", partida.numeroDaFase(), Estilo.PADRAO);
        valor(tela, 5, "BITS", partida.coletaveisRestantes(), Estilo.PADRAO);
        tela.escrever(COLUNA, 6, "VIDAS", Estilo.APAGADO);
        Sprite vida = recursos.sprite("vida");
        int vidas = Math.min(partida.debugger().getVidas(), 6);
        for (int i = 0; i < vidas; i++) {
            tela.escrever(COLUNA + LARGURA - 1 - 2 * i, 6, vida.texto(), vida.estilo());
        }
        separador(tela, 7);

        int linha = 8;
        for (Bug bug : partida.bugs()) {
            Sprite sprite = recursos.sprite(bug.getChaveDoSprite());
            tela.escrever(COLUNA, linha, sprite.texto(), sprite.estilo());
            tela.escrever(COLUNA + 2, linha, bug.getNome(), sprite.estilo());
            tela.escrever(COLUNA + 2, linha + 1, bug.getModo().descricao(), Estilo.APAGADO);
            linha += 2;
        }
        separador(tela, 16);

        ControladorDeModos modos = partida.modos();
        if (modos.breakpointAtivo()) {
            tela.escrever(COLUNA, 17, "BREAKPOINT", AZUL);
            alinharADireita(tela, 17, modos.segundosDeBreakpoint() + "s", AZUL);
        } else {
            tela.escrever(COLUNA, 17, "ONDA", Estilo.APAGADO);
            alinharADireita(tela, 17, modos.nomeDaOnda(), Estilo.PADRAO);
        }
        separador(tela, 18);

        legenda(tela, 19, recursos.sprite("bit"), "bit", "10");
        legenda(tela, 20, recursos.sprite("breakpoint"), "breakpoint", "50");
        legenda(tela, 21, recursos.sprite("cafe"), "café", "bônus");
        legenda(tela, 22, recursos.sprite("bug.vulneravel"), "bug pausado", "200+");
    }

    /** Linha de ajuda no rodapé, comum a todas as telas. */
    public static void desenharAjuda(Tela tela, String texto) {
        tela.escreverCentralizado(Tela.ALTURA - 1, 0, Tela.LARGURA, texto, Estilo.APAGADO);
    }

    private static void valor(Tela tela, int linha, String rotulo, int numero, Estilo estilo) {
        tela.escrever(COLUNA, linha, rotulo, Estilo.APAGADO);
        alinharADireita(tela, linha, String.valueOf(numero), estilo);
    }

    private static void legenda(Tela tela, int linha, Sprite sprite, String nome, String pontos) {
        tela.escrever(COLUNA, linha, sprite.texto(), sprite.estilo());
        tela.escrever(COLUNA + 3, linha, nome, Estilo.APAGADO);
        alinharADireita(tela, linha, pontos, Estilo.APAGADO);
    }

    private static void alinharADireita(Tela tela, int linha, String texto, Estilo estilo) {
        tela.escrever(COLUNA + LARGURA - texto.length(), linha, texto, estilo);
    }

    private static void separador(Tela tela, int linha) {
        tela.escrever(COLUNA, linha, "─".repeat(LARGURA), Estilo.frente(238));
    }
}
