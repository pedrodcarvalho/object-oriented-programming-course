package cacabugs.gerenciadores;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Converte teclas em comandos. Uma thread separada fica lendo o teclado e
 * guardando os comandos numa fila; o laço do jogo recolhe a fila a cada quadro.
 * Setas ou WASD movem, P pausa, ENTER/espaço confirma e Q sai.
 */
public class GerenciadorDeEntrada {
    private static final int ESC = 27;

    private final InputStream teclado;
    private final Queue<Comando> fila = new ConcurrentLinkedQueue<>();
    private volatile boolean houveEntrada;

    public GerenciadorDeEntrada(InputStream teclado) {
        this.teclado = teclado;
    }

    public void iniciar() {
        Thread leitor = new Thread(this::lerSempre, "leitor-do-teclado");
        leitor.setDaemon(true);
        leitor.start();
    }

    /** Devolve (e esvazia) os comandos recebidos desde a última chamada. */
    public List<Comando> coletar() {
        List<Comando> comandos = new ArrayList<>();
        Comando c;
        while ((c = fila.poll()) != null) {
            comandos.add(c);
        }
        return comandos;
    }

    /** true se chegou alguma tecla desde a última consulta (usado no modo sem teclas imediatas). */
    public boolean consumirSinalDeEntrada() {
        boolean houve = houveEntrada;
        houveEntrada = false;
        return houve;
    }

    private void lerSempre() {
        try {
            int b;
            while ((b = teclado.read()) != -1) {
                houveEntrada = true;
                Comando comando = b == ESC ? lerSequenciaDeEscape() : traduzir(b);
                if (comando != null) {
                    fila.add(comando);
                }
            }
        } catch (IOException e) {
            // teclado fechado: o jogo continua até o jogador sair
        }
    }

    /** Setas chegam como ESC [ A (ou ESC O A); modificadores como ESC [ 1 ; 5 A. */
    private Comando lerSequenciaDeEscape() throws IOException {
        int b = teclado.read();
        if (b != '[' && b != 'O') {
            return null;
        }
        int c = teclado.read();
        while ((c >= '0' && c <= '9') || c == ';') {
            c = teclado.read();
        }
        return switch (c) {
            case 'A' -> Comando.CIMA;
            case 'B' -> Comando.BAIXO;
            case 'C' -> Comando.DIREITA;
            case 'D' -> Comando.ESQUERDA;
            default -> null;
        };
    }

    private static Comando traduzir(int b) {
        return switch (Character.toLowerCase((char) b)) {
            case 'w' -> Comando.CIMA;
            case 's' -> Comando.BAIXO;
            case 'a' -> Comando.ESQUERDA;
            case 'd' -> Comando.DIREITA;
            case 'p' -> Comando.PAUSAR;
            case 'q' -> Comando.SAIR;
            case '\n', '\r', ' ' -> Comando.CONFIRMAR;
            default -> null;
        };
    }
}
