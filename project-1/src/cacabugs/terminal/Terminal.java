package cacabugs.terminal;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Prepara o terminal para o jogo e o devolve ao normal na saída.
 * Em Linux, macOS e WSL usa o comando "stty" para receber cada tecla na hora
 * (sem esperar ENTER) e sem eco. Onde isso não existe, o jogo ainda roda, mas
 * cada comando precisa ser seguido de ENTER.
 */
public class Terminal implements AutoCloseable {
    private static final String CSI = "\033[";

    private final PrintStream saida =
            new PrintStream(new FileOutputStream(FileDescriptor.out), false, StandardCharsets.UTF_8);
    private final AtomicBoolean ativo = new AtomicBoolean(false);
    private String configuracaoOriginal;
    private volatile int colunas = Tela.LARGURA;
    private volatile int linhas = Tela.ALTURA;

    public void iniciar() {
        configuracaoOriginal = stty("-g");
        if (configuracaoOriginal != null) {
            stty("-icanon -echo min 1 time 0");
        }
        atualizarTamanho();
        ativo.set(true);
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        // tela alternativa + cursor escondido + tela limpa
        escrever(CSI + "?1049h" + CSI + "?25l" + CSI + "0m" + CSI + "2J");
        iniciarMonitorDeTamanho();
    }

    /** true quando cada tecla chega na hora (modo sem buffer de linha). */
    public boolean teclasImediatas() {
        return configuracaoOriginal != null;
    }

    public InputStream entrada() {
        return System.in;
    }

    public int colunas() {
        return colunas;
    }

    public int linhas() {
        return linhas;
    }

    public void escrever(String texto) {
        saida.print(texto);
        saida.flush();
    }

    public void limparTela() {
        escrever(CSI + "0m" + CSI + "2J");
    }

    @Override
    public void close() {
        if (!ativo.getAndSet(false)) {
            return;
        }
        escrever(CSI + "0m" + CSI + "?25h" + CSI + "?1049l");
        if (configuracaoOriginal != null) {
            stty(configuracaoOriginal);
        }
    }

    private void iniciarMonitorDeTamanho() {
        Thread monitor = new Thread(() -> {
            while (ativo.get()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
                atualizarTamanho();
            }
        }, "monitor-de-tamanho");
        monitor.setDaemon(true);
        monitor.start();
    }

    private void atualizarTamanho() {
        String resposta = stty("size");
        if (resposta == null) {
            return;
        }
        String[] partes = resposta.trim().split("\\s+");
        if (partes.length != 2) {
            return;
        }
        try {
            int l = Integer.parseInt(partes[0]);
            int c = Integer.parseInt(partes[1]);
            if (l > 0 && c > 0) {
                linhas = l;
                colunas = c;
            }
        } catch (NumberFormatException ignorada) {
            // mantém o último tamanho conhecido
        }
    }

    /** Roda "stty" sobre o terminal real. Devolve a saída, ou null se não for possível. */
    private static String stty(String argumentos) {
        try {
            Process processo = new ProcessBuilder("sh", "-c", "stty " + argumentos + " < /dev/tty")
                    .redirectErrorStream(true)
                    .start();
            String resposta = new String(processo.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return processo.waitFor() == 0 ? resposta : null;
        } catch (IOException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
