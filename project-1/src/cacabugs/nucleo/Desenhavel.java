package cacabugs.nucleo;

import cacabugs.terminal.Tela;

/** Algo que sabe se desenhar na tela. */
public interface Desenhavel {
    void desenhar(Tela tela, Partida partida);
}
