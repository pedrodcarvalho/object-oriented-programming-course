package cacabugs.mapa;

import cacabugs.coletaveis.Bit;
import cacabugs.coletaveis.Breakpoint;
import cacabugs.coletaveis.Coletavel;
import cacabugs.entidades.Bug;
import cacabugs.entidades.Deadlock;
import cacabugs.entidades.Heisenbug;
import cacabugs.entidades.NullPointer;
import cacabugs.entidades.RaceCondition;
import cacabugs.nucleo.Direcao;
import cacabugs.nucleo.Posicao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monta uma fase a partir do texto do labirinto (padrão Factory).
 *
 * Legenda do arquivo:
 *   #  parede          .  bit             o  breakpoint
 *   (espaço) corredor  T  túnel           -  porta do backlog
 *   H  backlog         P  início do Debugger
 *   C  onde o café aparece
 *   1  NullPointer (fora do backlog)      2, 3, 4  RaceCondition, Deadlock, Heisenbug (no backlog)
 */
public class FabricaDeFase {

    public Fase criar(List<String> linhas) throws MapaInvalidoException {
        if (linhas.isEmpty()) {
            throw new MapaInvalidoException("O arquivo do labirinto está vazio.");
        }
        int altura = linhas.size();
        int largura = linhas.get(0).length();
        Celula[][] celulas = new Celula[altura][largura];
        List<Coletavel> coletaveis = new ArrayList<>();
        Map<Character, Posicao> inicioDosBugs = new HashMap<>();
        List<Posicao> portas = new ArrayList<>();
        Posicao inicioDoDebugger = null;
        Posicao posicaoDoCafe = null;

        for (int y = 0; y < altura; y++) {
            String linha = linhas.get(y);
            if (linha.length() != largura) {
                throw new MapaInvalidoException("A linha " + (y + 1) + " tem " + linha.length()
                        + " caracteres; todas precisam ter " + largura + ".");
            }
            for (int x = 0; x < largura; x++) {
                char c = linha.charAt(x);
                Posicao p = new Posicao(x, y);
                TipoCelula tipo = switch (c) {
                    case '#' -> TipoCelula.PAREDE;
                    case '.', 'o', ' ', 'P', 'C', '1' -> TipoCelula.CORREDOR;
                    case 'T' -> TipoCelula.TUNEL;
                    case '-' -> TipoCelula.PORTA;
                    case 'H', '2', '3', '4' -> TipoCelula.BACKLOG;
                    default -> throw new MapaInvalidoException("Caractere desconhecido '" + c
                            + "' na linha " + (y + 1) + ", coluna " + (x + 1) + ".");
                };
                celulas[y][x] = new Celula(tipo);
                switch (c) {
                    case '.' -> coletaveis.add(new Bit(p));
                    case 'o' -> coletaveis.add(new Breakpoint(p));
                    case 'P' -> inicioDoDebugger = p;
                    case 'C' -> posicaoDoCafe = p;
                    case '-' -> portas.add(p);
                    case '1', '2', '3', '4' -> {
                        if (inicioDosBugs.put(c, p) != null) {
                            throw new MapaInvalidoException("O bug '" + c + "' aparece mais de uma vez no mapa.");
                        }
                    }
                    default -> { }
                }
            }
        }

        exigir(inicioDoDebugger != null, "Falta o início do Debugger (P).");
        exigir(posicaoDoCafe != null, "Falta a posição do café (C).");
        exigir(!portas.isEmpty(), "Falta a porta do backlog (-).");
        for (char bug = '1'; bug <= '4'; bug++) {
            exigir(inicioDosBugs.containsKey(bug), "Falta o bug '" + bug + "'.");
        }

        Posicao porta = portas.get(0);
        Labirinto labirinto = new Labirinto(celulas, porta.mover(Direcao.CIMA), porta.mover(Direcao.BAIXO), posicaoDoCafe);
        for (Coletavel coletavel : coletaveis) {
            labirinto.celula(coletavel.getPosicao()).colocar(coletavel);
        }

        // Cantos de dispersão ficam fora do mapa, como no original: cada bug "mira" um canto.
        NullPointer nullPointer = new NullPointer(inicioDosBugs.get('1'), new Posicao(largura - 3, -2));
        RaceCondition raceCondition = new RaceCondition(inicioDosBugs.get('2'), new Posicao(2, -2));
        Deadlock deadlock = new Deadlock(inicioDosBugs.get('3'), new Posicao(largura - 1, altura + 1), nullPointer);
        Heisenbug heisenbug = new Heisenbug(inicioDosBugs.get('4'), new Posicao(0, altura + 1));
        List<Bug> bugs = List.of(nullPointer, raceCondition, deadlock, heisenbug);

        return new Fase(labirinto, inicioDoDebugger, bugs, coletaveis);
    }

    private static void exigir(boolean condicao, String mensagem) throws MapaInvalidoException {
        if (!condicao) {
            throw new MapaInvalidoException(mensagem);
        }
    }
}
