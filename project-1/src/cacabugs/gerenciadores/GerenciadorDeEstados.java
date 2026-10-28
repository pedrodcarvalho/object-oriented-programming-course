package cacabugs.gerenciadores;

import cacabugs.estados.EstadoDoJogo;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Controla a tela atual: menu, jogando, pausa, vida perdida, fase concluída e fim de jogo.
 * Usa uma pilha: a pausa é empilhada sobre o jogo e, ao sair, o jogo continua de onde parou.
 */
public class GerenciadorDeEstados {
    private final Deque<EstadoDoJogo> pilha = new ArrayDeque<>();
    private boolean encerrado;

    /** Troca a tela atual por outra (descarta a pilha). */
    public void trocar(EstadoDoJogo novo) {
        pilha.clear();
        pilha.push(novo);
        novo.entrar();
    }

    /** Coloca uma tela por cima da atual (ex.: pausa). */
    public void empilhar(EstadoDoJogo novo) {
        pilha.push(novo);
        novo.entrar();
    }

    /** Volta para a tela de baixo, sem reiniciá-la. */
    public void desempilhar() {
        if (pilha.size() > 1) {
            pilha.pop();
        }
    }

    public EstadoDoJogo atual() {
        return pilha.peek();
    }

    public void encerrar() {
        encerrado = true;
    }

    public boolean encerrado() {
        return encerrado;
    }
}
