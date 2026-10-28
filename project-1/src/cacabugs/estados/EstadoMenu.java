package cacabugs.estados;

import cacabugs.Jogo;
import cacabugs.gerenciadores.Comando;
import cacabugs.gerenciadores.GerenciadorDeRecursos;
import cacabugs.mapa.MapaInvalidoException;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Tempo;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Sprite;
import cacabugs.terminal.Tela;

/** Tela inicial: título, quem é quem no labirinto e o recorde. */
public class EstadoMenu implements EstadoDoJogo {
    private static final int COLUNA_SPRITE = 12;
    private static final int COLUNA_NOME = 15;
    private static final int COLUNA_DESCRICAO = 31;

    private final Jogo jogo;
    private int ticks;
    private String erro;

    public EstadoMenu(Jogo jogo) {
        this.jogo = jogo;
    }

    @Override
    public void tratar(Comando comando) {
        if (comando != Comando.CONFIRMAR) {
            return;
        }
        try {
            Partida partida = jogo.novaPartida();
            jogo.estados().trocar(new EstadoJogando(jogo, partida));
        } catch (MapaInvalidoException e) {
            erro = "Mapa inválido: " + e.getMessage();
        }
    }

    @Override
    public void atualizar() {
        ticks++;
    }

    @Override
    public void desenhar(Tela tela) {
        int largura = TextoGrande.largura("CAÇA-") + 1 + TextoGrande.largura("BUGS");
        int coluna = (Tela.LARGURA - largura) / 2;
        TextoGrande.desenhar(tela, coluna, 1, "CAÇA-", Estilo.DESTAQUE);
        TextoGrande.desenhar(tela, coluna + TextoGrande.largura("CAÇA-") + 1, 1, "BUGS", Estilo.ALERTA);
        tela.escreverCentralizado(5, 0, Tela.LARGURA,
                "um clone de Pac-Man em que o labirinto é uma placa de circuito", Estilo.APAGADO);

        GerenciadorDeRecursos recursos = jogo.recursos();
        item(tela, 7, recursos.sprite("debugger.direita"), "Debugger", "você: colete todos os bits");
        item(tela, 8, recursos.sprite("bit"), "bit", "10 pontos");
        item(tela, 9, recursos.sprite("breakpoint"), "breakpoint", "50 pontos e deixa os bugs vulneráveis");
        item(tela, 10, recursos.sprite("cafe"), "café", "bônus que aparece duas vezes por fase");
        item(tela, 12, recursos.sprite("bug.nullpointer"), "NullPointer", "aponta direto para você");
        item(tela, 13, recursos.sprite("bug.racecondition"), "RaceCondition", "corre para chegar antes");
        item(tela, 14, recursos.sprite("bug.deadlock"), "Deadlock", "fecha o cerco junto com o NullPointer");
        item(tela, 15, recursos.sprite("bug.heisenbug"), "Heisenbug", "foge quando você chega perto");

        tela.escreverCentralizado(17, 0, Tela.LARGURA, "RECORDE  " + jogo.placar().getRecorde(), Estilo.TITULO);
        if ((ticks / Tempo.emTicks(0.5)) % 2 == 0) {
            tela.escreverCentralizado(19, 0, Tela.LARGURA, "Pressione ENTER para começar", Estilo.DESTAQUE);
        }
        if (erro != null) {
            tela.escreverCentralizado(21, 0, Tela.LARGURA, erro, Estilo.ALERTA);
        }
        PainelLateral.desenharAjuda(tela, "Setas ou WASD: mover  ·  P: pausa  ·  ENTER: começar  ·  Q: sair");
    }

    private static void item(Tela tela, int linha, Sprite sprite, String nome, String descricao) {
        tela.escrever(COLUNA_SPRITE, linha, sprite.texto(), sprite.estilo());
        tela.escrever(COLUNA_NOME, linha, nome, sprite.estilo());
        tela.escrever(COLUNA_DESCRICAO, linha, descricao, Estilo.PADRAO);
    }
}
