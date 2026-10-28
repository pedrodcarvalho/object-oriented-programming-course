package cacabugs.estados;

import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

import java.util.Map;

/**
 * Letras grandes para o título. Cada letra é um desenho de 5x6 "pixels";
 * cada linha do terminal mostra dois pixels na vertical usando meios-blocos.
 */
public final class TextoGrande {
    private static final String[] VAZIO = {".....", ".....", ".....", ".....", ".....", "....."};
    private static final Map<Character, String[]> FONTE = Map.of(
            'C', new String[]{".####", "#....", "#....", "#....", ".####", "....."},
            'Ç', new String[]{".####", "#....", "#....", "#....", ".####", "..#.."},
            'A', new String[]{".###.", "#...#", "#####", "#...#", "#...#", "....."},
            '-', new String[]{".....", ".....", ".###.", ".....", ".....", "....."},
            'B', new String[]{"####.", "#...#", "####.", "#...#", "####.", "....."},
            'U', new String[]{"#...#", "#...#", "#...#", "#...#", ".###.", "....."},
            'G', new String[]{".####", "#....", "#..##", "#...#", ".####", "....."},
            'S', new String[]{".####", "#....", ".###.", "....#", "####.", "....."});

    public static final int LINHAS = 3;

    private TextoGrande() {
    }

    public static int largura(String texto) {
        return texto.length() * 6 - 1;
    }

    public static void desenhar(Tela tela, int coluna, int linha, String texto, Estilo estilo) {
        char cheio = tela.modoAscii() ? '#' : '█';
        char metadeDeCima = tela.modoAscii() ? '"' : '▀';
        char metadeDeBaixo = tela.modoAscii() ? '_' : '▄';
        int x = coluna;
        for (char letra : texto.toCharArray()) {
            String[] desenho = FONTE.getOrDefault(letra, VAZIO);
            for (int l = 0; l < LINHAS; l++) {
                for (int i = 0; i < 5; i++) {
                    boolean cima = desenho[2 * l].charAt(i) == '#';
                    boolean baixo = desenho[2 * l + 1].charAt(i) == '#';
                    if (cima || baixo) {
                        char c = cima && baixo ? cheio : cima ? metadeDeCima : metadeDeBaixo;
                        tela.escrever(x + i, linha + l, c, estilo);
                    }
                }
            }
            x += 6;
        }
    }
}
