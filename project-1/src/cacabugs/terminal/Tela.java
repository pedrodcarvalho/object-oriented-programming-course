package cacabugs.terminal;

import cacabugs.nucleo.Posicao;

import java.text.Normalizer;
import java.util.Arrays;

/**
 * Tela de 80x24 caracteres com buffer duplo.
 * Tudo é desenhado primeiro num quadro em memória; ao apresentar, só as células
 * que mudaram desde o quadro anterior são enviadas ao terminal. Assim a tela
 * atualiza sem piscar e com pouco texto trafegando.
 */
public final class Tela {
    public static final int LARGURA = 80;
    public static final int ALTURA = 24;
    /** Cada casa do labirinto ocupa 2 colunas, para ficar quadrada no terminal. */
    public static final int COLUNAS_POR_CASA = 2;

    private final char[][] glifos = new char[ALTURA][LARGURA];
    private final Estilo[][] estilos = new Estilo[ALTURA][LARGURA];
    private final char[][] glifosNoTerminal = new char[ALTURA][LARGURA];
    private final Estilo[][] estilosNoTerminal = new Estilo[ALTURA][LARGURA];
    private final boolean ascii;
    private boolean redesenharTudo = true;

    public Tela(boolean ascii) {
        this.ascii = ascii;
        limpar();
    }

    public boolean modoAscii() {
        return ascii;
    }

    public void limpar() {
        for (int y = 0; y < ALTURA; y++) {
            Arrays.fill(glifos[y], ' ');
            Arrays.fill(estilos[y], Estilo.PADRAO);
        }
    }

    public void escrever(int coluna, int linha, char glifo, Estilo estilo) {
        if (linha < 0 || linha >= ALTURA || coluna < 0 || coluna >= LARGURA) {
            return;
        }
        glifos[linha][coluna] = glifo;
        estilos[linha][coluna] = estilo;
    }

    public void escrever(int coluna, int linha, String texto, Estilo estilo) {
        String t = ascii ? paraAscii(texto) : texto;
        for (int i = 0; i < t.length(); i++) {
            escrever(coluna + i, linha, t.charAt(i), estilo);
        }
    }

    public void escreverCentralizado(int linha, int colunaInicial, int largura, String texto, Estilo estilo) {
        escrever(colunaInicial + Math.max(0, (largura - texto.length()) / 2), linha, texto, estilo);
    }

    /** Desenha o sprite a partir da primeira coluna da casa (personagens e itens). */
    public void desenharNaCasa(Posicao casa, Sprite sprite) {
        desenharNaCasa(casa, sprite.texto(), sprite.estilo());
    }

    public void desenharNaCasa(Posicao casa, String texto, Estilo estilo) {
        escrever(casa.x() * COLUNAS_POR_CASA, casa.y(), texto, estilo);
    }

    /** Preenche as duas colunas da casa com o mesmo caractere (paredes e porta). */
    public void preencherCasa(Posicao casa, Sprite sprite) {
        char glifo = sprite.texto().charAt(0);
        for (int i = 0; i < COLUNAS_POR_CASA; i++) {
            escrever(casa.x() * COLUNAS_POR_CASA + i, casa.y(), glifo, sprite.estilo());
        }
    }

    /** Caixa com borda e fundo preenchido, usada nas mensagens sobre o labirinto. */
    public void caixa(int coluna, int linha, int largura, int altura, Estilo borda) {
        char horizontal = ascii ? '-' : '─';
        char vertical = ascii ? '|' : '│';
        for (int y = linha; y < linha + altura; y++) {
            for (int x = coluna; x < coluna + largura; x++) {
                boolean topoOuBase = y == linha || y == linha + altura - 1;
                boolean lateral = x == coluna || x == coluna + largura - 1;
                char c = ' ';
                if (topoOuBase && lateral) {
                    c = cantoDaCaixa(y == linha, x == coluna);
                } else if (topoOuBase) {
                    c = horizontal;
                } else if (lateral) {
                    c = vertical;
                }
                escrever(x, y, c, borda);
            }
        }
    }

    private char cantoDaCaixa(boolean topo, boolean esquerda) {
        if (ascii) {
            return '+';
        }
        if (topo) {
            return esquerda ? '┌' : '┐';
        }
        return esquerda ? '└' : '┘';
    }

    /** Força o próximo quadro a ser enviado inteiro (ex.: depois de redimensionar). */
    public void invalidar() {
        redesenharTudo = true;
    }

    /**
     * Monta as sequências ANSI que levam o terminal do quadro anterior ao atual.
     * Os deslocamentos centralizam a tela de 80x24 em terminais maiores.
     */
    public String renderizar(int deslocamentoColuna, int deslocamentoLinha) {
        StringBuilder saida = new StringBuilder(redesenharTudo ? 16_000 : 512);
        Estilo estiloAtual = null;
        for (int y = 0; y < ALTURA; y++) {
            int x = 0;
            while (x < LARGURA) {
                if (!mudou(x, y)) {
                    x++;
                    continue;
                }
                saida.append("\033[").append(y + 1 + deslocamentoLinha).append(';')
                        .append(x + 1 + deslocamentoColuna).append('H');
                while (x < LARGURA && mudou(x, y)) {
                    Estilo estilo = estilos[y][x];
                    if (!estilo.equals(estiloAtual)) {
                        saida.append(estilo.sequenciaAnsi());
                        estiloAtual = estilo;
                    }
                    saida.append(glifos[y][x]);
                    glifosNoTerminal[y][x] = glifos[y][x];
                    estilosNoTerminal[y][x] = estilo;
                    x++;
                }
            }
        }
        redesenharTudo = false;
        if (saida.length() > 0) {
            saida.append("\033[0m");
        }
        return saida.toString();
    }

    private boolean mudou(int x, int y) {
        return redesenharTudo
                || glifos[y][x] != glifosNoTerminal[y][x]
                || !estilos[y][x].equals(estilosNoTerminal[y][x]);
    }

    /** Remove acentos e troca símbolos não ASCII, para terminais limitados. */
    static String paraAscii(String texto) {
        String semAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        StringBuilder sb = new StringBuilder(semAcentos.length());
        for (char c : semAcentos.toCharArray()) {
            if (c < 128) {
                sb.append(c);
            } else if (c == '·') {
                sb.append('-');
            } else if (c == '─' || c == '—') {
                sb.append('-');
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }
}
