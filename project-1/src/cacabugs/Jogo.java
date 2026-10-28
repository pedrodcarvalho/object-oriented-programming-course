package cacabugs;

import cacabugs.estados.EstadoMenu;
import cacabugs.estados.PainelLateral;
import cacabugs.gerenciadores.Comando;
import cacabugs.gerenciadores.GerenciadorDeEntrada;
import cacabugs.gerenciadores.GerenciadorDeEstados;
import cacabugs.gerenciadores.GerenciadorDeRecursos;
import cacabugs.gerenciadores.Placar;
import cacabugs.mapa.MapaInvalidoException;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Tela;
import cacabugs.terminal.Terminal;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Ponto de entrada do Caça-Bugs. Cria os gerenciadores e roda o laço principal
 * a 60 quadros por segundo: ler teclado, atualizar, desenhar e esperar o próximo quadro.
 *
 * Uso: java -cp out cacabugs.Jogo [--ascii]
 */
public class Jogo {
    private static final long NANOS_POR_QUADRO = 1_000_000_000L / Tempo.TICKS_POR_SEGUNDO;

    private final Terminal terminal;
    private final Tela tela;
    private final GerenciadorDeRecursos recursos;
    private final GerenciadorDeEntrada entrada;
    private final GerenciadorDeEstados estados = new GerenciadorDeEstados();
    private final Placar placar = new Placar(Path.of("recorde.txt"));
    private final PainelLateral painel = new PainelLateral();

    private int deslocamentoColuna = -1;
    private int deslocamentoLinha = -1;
    private String avisoDeTamanho;

    public Jogo(Terminal terminal, boolean ascii) {
        this.terminal = terminal;
        this.tela = new Tela(ascii);
        this.recursos = new GerenciadorDeRecursos(ascii);
        this.entrada = new GerenciadorDeEntrada(terminal.entrada());
    }

    public static void main(String[] args) {
        boolean ascii = Arrays.asList(args).contains("--ascii");
        Terminal terminal = new Terminal();
        int codigoDeSaida = 0;
        try {
            terminal.iniciar();
            new Jogo(terminal, ascii).executar();
        } catch (RuntimeException e) {
            terminal.close();
            System.err.println("O Caça-Bugs encontrou um erro e precisou fechar:");
            e.printStackTrace();
            codigoDeSaida = 1;
        } finally {
            terminal.close();
        }
        System.exit(codigoDeSaida);
    }

    public Partida novaPartida() throws MapaInvalidoException {
        return new Partida(recursos, placar);
    }

    public GerenciadorDeEstados estados() {
        return estados;
    }

    public GerenciadorDeRecursos recursos() {
        return recursos;
    }

    public Placar placar() {
        return placar;
    }

    public PainelLateral painel() {
        return painel;
    }

    private void executar() {
        estados.trocar(new EstadoMenu(this));
        entrada.iniciar();
        long proximoQuadro = System.nanoTime();
        while (!estados.encerrado()) {
            for (Comando comando : entrada.coletar()) {
                if (comando == Comando.SAIR) {
                    estados.encerrar();
                } else {
                    estados.atual().tratar(comando);
                }
            }
            if (estados.encerrado()) {
                break;
            }
            if (terminalPequeno()) {
                avisarTamanho();
            } else {
                estados.atual().atualizar();
                apresentarQuadro();
            }
            proximoQuadro = esperarProximoQuadro(proximoQuadro);
        }
        placar.salvarRecorde();
    }

    private void apresentarQuadro() {
        int coluna = Math.max(0, (terminal.colunas() - Tela.LARGURA) / 2);
        int linha = Math.max(0, (terminal.linhas() - Tela.ALTURA) / 2);
        boolean ecoNaTela = !terminal.teclasImediatas() && entrada.consumirSinalDeEntrada();
        if (coluna != deslocamentoColuna || linha != deslocamentoLinha || avisoDeTamanho != null || ecoNaTela) {
            terminal.limparTela();
            tela.invalidar();
            deslocamentoColuna = coluna;
            deslocamentoLinha = linha;
            avisoDeTamanho = null;
        }
        tela.limpar();
        estados.atual().desenhar(tela);
        String quadro = tela.renderizar(coluna, linha);
        if (!terminal.teclasImediatas()) {
            // sem modo imediato, o terminal ecoa as teclas: deixa o cursor fora do jogo
            quadro += "\033[" + (linha + Tela.ALTURA) + ";1H";
        }
        if (!quadro.isEmpty()) {
            terminal.escrever(quadro);
        }
    }

    private boolean terminalPequeno() {
        return terminal.colunas() < Tela.LARGURA || terminal.linhas() < Tela.ALTURA;
    }

    private void avisarTamanho() {
        String aviso = "Aumente o terminal para pelo menos " + Tela.LARGURA + "x" + Tela.ALTURA
                + " (agora: " + terminal.colunas() + "x" + terminal.linhas() + "). Q sai.";
        if (!aviso.equals(avisoDeTamanho)) {
            terminal.escrever("\033[0m\033[2J\033[H" + aviso);
            avisoDeTamanho = aviso;
        }
    }

    private static long esperarProximoQuadro(long proximoQuadro) {
        long agora = System.nanoTime();
        long proximo = proximoQuadro + NANOS_POR_QUADRO;
        long espera = proximo - agora;
        if (espera > 0) {
            try {
                Thread.sleep(espera / 1_000_000, (int) (espera % 1_000_000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        } else if (espera < -10 * NANOS_POR_QUADRO) {
            proximo = agora; // atrasou demais: não tenta "recuperar" quadros perdidos
        }
        return proximo;
    }
}
