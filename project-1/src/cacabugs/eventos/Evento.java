package cacabugs.eventos;

import cacabugs.nucleo.Posicao;

/** Um acontecimento: o tipo, onde aconteceu e um valor (ex.: pontos). */
public record Evento(TipoEvento tipo, Posicao posicao, int valor) {
}
