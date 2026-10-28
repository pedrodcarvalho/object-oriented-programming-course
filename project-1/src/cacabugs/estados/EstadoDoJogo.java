package cacabugs.estados;

import cacabugs.gerenciadores.Comando;
import cacabugs.terminal.Tela;

/** Uma tela do jogo (padrão State): cada uma trata teclas, se atualiza e se desenha. */
public interface EstadoDoJogo {

    /** Chamado quando a tela passa a ser a atual. */
    default void entrar() {
    }

    void tratar(Comando comando);

    void atualizar();

    void desenhar(Tela tela);
}
