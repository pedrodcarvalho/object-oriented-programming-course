package cacabugs.entidades;

import cacabugs.mapa.Labirinto;
import cacabugs.mapa.TipoCelula;
import cacabugs.modos.ModoDoBug;
import cacabugs.modos.NoBacklog;
import cacabugs.nucleo.Camada;
import cacabugs.nucleo.Direcao;
import cacabugs.nucleo.Partida;
import cacabugs.nucleo.Posicao;
import cacabugs.terminal.Tela;

import java.util.ArrayList;
import java.util.List;

/**
 * Inimigo genérico. Todo o movimento mora aqui; cada subclasse só diz
 * para onde quer ir na perseguição (calcularAlvo), e o modo atual decide
 * se o bug persegue, se dispersa, foge ou volta ao backlog (padrão State).
 */
public abstract class Bug extends Personagem {
    private final String nome;
    private final String chaveDoSprite;
    private final Posicao cantoDeDispersao;
    private final int ordemDeSaida;
    private ModoDoBug modo;
    private boolean inverterNoProximoPasso;

    protected Bug(String nome, String chaveDoSprite, Posicao inicio, Posicao cantoDeDispersao, int ordemDeSaida) {
        super(inicio, Direcao.ESQUERDA);
        this.nome = nome;
        this.chaveDoSprite = chaveDoSprite;
        this.cantoDeDispersao = cantoDeDispersao;
        this.ordemDeSaida = ordemDeSaida;
    }

    /** A personalidade do bug: a casa que ele persegue no modo Perseguição. */
    public abstract Posicao calcularAlvo(Partida partida);

    @Override
    public final void atualizar(Partida partida) {
        mudarPara(modo.atualizar(this, partida));
        double velocidade = modo.velocidade(this, partida);
        if (partida.labirinto().ehTunel(posicao) && modo.desaceleraNoTunel()) {
            velocidade = Math.min(velocidade, partida.config().velocidadeBugNoTunel());
        }
        avancar(velocidade, partida);
    }

    @Override
    protected final boolean darPasso(Partida partida) {
        Direcao escolhida = escolherDirecao(partida);
        if (escolhida == null) {
            return false;
        }
        direcao = escolhida;
        mover(partida.labirinto());
        return true;
    }

    /**
     * Template Method: o mesmo algoritmo para todos os bugs.
     * Em cada casa o bug olha as saídas possíveis (sem dar meia-volta) e
     * escolhe a que mais o aproxima do alvo. Sem alvo, escolhe ao acaso.
     */
    protected final Direcao escolherDirecao(Partida partida) {
        Labirinto labirinto = partida.labirinto();
        boolean acesso = modo.atravessaPorta();

        if (inverterNoProximoPasso) {
            inverterNoProximoPasso = false;
            Direcao oposta = direcao.oposta();
            if (labirinto.passavel(labirinto.vizinho(posicao, oposta), acesso)) {
                return oposta;
            }
        }

        Posicao alvo = modo.alvo(this, partida);
        if (alvo != null && modo.usaMenorCaminho()) {
            Direcao caminho = labirinto.direcaoDoMenorCaminho(posicao, alvo, acesso);
            if (caminho != null) {
                return caminho;
            }
        }

        List<Direcao> opcoes = new ArrayList<>(4);
        for (Direcao d : Direcao.values()) {
            if (d != direcao.oposta() && labirinto.passavel(labirinto.vizinho(posicao, d), acesso)) {
                opcoes.add(d);
            }
        }
        if (opcoes.isEmpty()) {
            Direcao oposta = direcao.oposta();
            return labirinto.passavel(labirinto.vizinho(posicao, oposta), acesso) ? oposta : null;
        }
        if (alvo == null) {
            return opcoes.get(partida.aleatorio().nextInt(opcoes.size()));
        }
        Direcao melhor = opcoes.get(0);
        double menorDistancia = Double.MAX_VALUE;
        for (Direcao d : opcoes) {
            double distancia = labirinto.vizinho(posicao, d).distanciaAte(alvo);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                melhor = d;
            }
        }
        return melhor;
    }

    /** Começo de rodada: volta ao início, dentro ou fora do backlog. */
    @Override
    public void aoReiniciarRodada(Partida partida) {
        voltarAoInicio();
        inverterNoProximoPasso = false;
        boolean noBacklog = partida.labirinto().celula(getInicio()).getTipo() == TipoCelula.BACKLOG;
        modo = noBacklog
                ? new NoBacklog(partida.config().atrasoDeSaida(ordemDeSaida))
                : partida.modos().novoModoGlobal();
    }

    /** O Debugger encostou neste bug: o efeito depende do modo atual. */
    public void aoEncostar(Partida partida) {
        modo.aoEncostar(this, partida);
    }

    public void aoMudarOnda(ModoDoBug proximo) {
        mudarPara(modo.aoMudarOnda(this, proximo));
    }

    public void aoAtivarBreakpoint() {
        mudarPara(modo.aoAtivarBreakpoint(this));
    }

    public void aoTerminarBreakpoint(ModoDoBug modoGlobal) {
        mudarPara(modo.aoTerminarBreakpoint(this, modoGlobal));
    }

    public void mudarPara(ModoDoBug novoModo) {
        this.modo = novoModo;
    }

    /** Pede meia-volta no próximo passo (acontece quando o modo muda). */
    public void inverterDirecao() {
        inverterNoProximoPasso = true;
    }

    public ModoDoBug getModo() {
        return modo;
    }

    public String getNome() {
        return nome;
    }

    public String getChaveDoSprite() {
        return chaveDoSprite;
    }

    public Posicao getCantoDeDispersao() {
        return cantoDeDispersao;
    }

    @Override
    public void desenhar(Tela tela, Partida partida) {
        tela.desenharNaCasa(posicao, modo.aparencia(this, partida));
    }

    @Override
    public Camada camada() {
        return Camada.BUGS;
    }
}
