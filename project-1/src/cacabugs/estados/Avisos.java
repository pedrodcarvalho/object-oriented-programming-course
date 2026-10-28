package cacabugs.estados;

import cacabugs.nucleo.Partida;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

/** Mensagens desenhadas por cima do labirinto ("PRONTO!", caixas de pausa e de fim de jogo). */
final class Avisos {

    /** Uma linha de texto com o seu estilo. */
    record Linha(String texto, Estilo estilo) {
    }

    private Avisos() {
    }

    /** Texto curto na linha do café, centralizado no labirinto (como o "READY!" original). */
    static void naLinhaDoCafe(Tela tela, Partida partida, String texto, Estilo estilo) {
        int linha = partida.labirinto().posicaoDoCafe().y();
        tela.escreverCentralizado(linha, 0, larguraDoLabirinto(partida), " " + texto + " ", estilo);
    }

    /** Caixa com borda, centralizada sobre o labirinto. */
    static void caixa(Tela tela, Partida partida, Estilo borda, Linha... linhas) {
        int maior = 0;
        for (Linha l : linhas) {
            maior = Math.max(maior, l.texto().length());
        }
        int largura = maior + 6;
        int altura = linhas.length + 4;
        int coluna = (larguraDoLabirinto(partida) - largura) / 2;
        int linha = (partida.labirinto().getAltura() - altura) / 2;
        tela.caixa(coluna, linha, largura, altura, borda);
        for (int i = 0; i < linhas.length; i++) {
            tela.escreverCentralizado(linha + 2 + i, coluna, largura, linhas[i].texto(), linhas[i].estilo());
        }
    }

    private static int larguraDoLabirinto(Partida partida) {
        return partida.labirinto().getLargura() * Tela.COLUNAS_POR_CASA;
    }
}
