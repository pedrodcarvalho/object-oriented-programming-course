package cacabugs.nucleo;

import cacabugs.coletaveis.Coletavel;
import cacabugs.entidades.Bug;
import cacabugs.entidades.Debugger;
import cacabugs.entidades.TextoFlutuante;
import cacabugs.eventos.CentralDeEventos;
import cacabugs.eventos.Evento;
import cacabugs.eventos.TipoEvento;
import cacabugs.gerenciadores.ConfiguracaoDaFase;
import cacabugs.gerenciadores.ControladorDeModos;
import cacabugs.gerenciadores.GerenciadorDeColisoes;
import cacabugs.gerenciadores.GerenciadorDeFases;
import cacabugs.gerenciadores.GerenciadorDeRecursos;
import cacabugs.gerenciadores.Placar;
import cacabugs.mapa.Fase;
import cacabugs.mapa.Labirinto;
import cacabugs.mapa.MapaInvalidoException;
import cacabugs.terminal.Estilo;
import cacabugs.terminal.Tela;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Uma partida em andamento: o labirinto, as entidades e os gerenciadores
 * que aplicam as regras. É o "mundo" que cada entidade recebe para se atualizar.
 */
public final class Partida {
    private static final int CONGELAMENTO_NA_CAPTURA = Tempo.emTicks(0.5);

    private final GerenciadorDeRecursos recursos;
    private final Placar placar;
    private final CentralDeEventos eventos = new CentralDeEventos();
    private final GerenciadorDeFases fases;
    private final ControladorDeModos modos = new ControladorDeModos();
    private final GerenciadorDeColisoes colisoes = new GerenciadorDeColisoes();
    private final Random aleatorio = new Random();
    private final List<Entidade> entidades = new ArrayList<>();
    private final List<Bug> bugs = new ArrayList<>();

    private Labirinto labirinto;
    private Debugger debugger;
    private ConfiguracaoDaFase config;
    private long tick;
    private int ticksCongelado;
    private boolean debuggerCapturado;
    private boolean faseConcluida;

    public Partida(GerenciadorDeRecursos recursos, Placar placar) throws MapaInvalidoException {
        this.recursos = recursos;
        this.placar = placar;
        this.fases = new GerenciadorDeFases(recursos.mapa());
        placar.novaPartida(eventos);
        fases.conectar(this);
        eventos.inscrever(TipoEvento.BREAKPOINT_COLETADO, e -> modos.ativarBreakpoint(this));
        eventos.inscrever(TipoEvento.VIDA_EXTRA, e -> debugger.ganharVida());
        eventos.inscrever(TipoEvento.BUG_CAPTURADO, this::aoCapturarBug);
        eventos.inscrever(TipoEvento.CAFE_COLETADO, e -> adicionar(new TextoFlutuante(
                e.posicao(), String.valueOf(e.valor()), Estilo.frente(137).emNegrito(), Tempo.emTicks(1.5))));
        fases.carregar(1, this);
    }

    /** Chamado pelo GerenciadorDeFases com a fase recém-montada. */
    public void instalar(Fase fase, ConfiguracaoDaFase novaConfig) {
        this.config = novaConfig;
        this.labirinto = fase.labirinto();
        entidades.clear();
        bugs.clear();
        entidades.addAll(fase.coletaveis());
        if (debugger == null) {
            debugger = new Debugger(fase.inicioDoDebugger());
        } else {
            debugger.definirInicio(fase.inicioDoDebugger());
        }
        entidades.add(debugger);
        bugs.addAll(fase.bugs());
        entidades.addAll(bugs);
        faseConcluida = false;
        reiniciarRodada();
    }

    /** Começo de fase ou depois de perder uma vida: cada entidade sabe o que fazer. */
    public void reiniciarRodada() {
        debuggerCapturado = false;
        ticksCongelado = 0;
        tick = 0;
        modos.iniciar(config);
        for (Entidade entidade : new ArrayList<>(entidades)) {
            entidade.aoReiniciarRodada(this);
        }
        entidades.removeIf(e -> !e.estaAtiva());
    }

    /** Um quadro de jogo: o laço trata todas as entidades da mesma forma. */
    public void atualizar() {
        if (ticksCongelado > 0) {
            ticksCongelado--;
            return;
        }
        tick++;
        modos.atualizar(this);
        for (Entidade entidade : new ArrayList<>(entidades)) {
            if (!entidade.estaAtiva()) {
                continue;
            }
            entidade.atualizar(this);
            colisoes.verificar(this);
            if (debuggerCapturado || ticksCongelado > 0) {
                break;
            }
        }
        entidades.removeIf(e -> !e.estaAtiva());
    }

    public void desenhar(Tela tela) {
        desenharAte(tela, Camada.EFEITOS);
    }

    /** Desenha o labirinto e as entidades até a camada indicada (as de cima ficam escondidas). */
    public void desenharAte(Tela tela, Camada limite) {
        labirinto.desenhar(tela, this);
        entidades.stream()
                .filter(e -> e.camada().compareTo(limite) <= 0)
                .sorted(Comparator.comparing(Entidade::camada))
                .forEach(e -> e.desenhar(tela, this));
    }

    public void adicionar(Entidade entidade) {
        entidades.add(entidade);
    }

    public void colocarColetavel(Coletavel coletavel) {
        labirinto.celula(coletavel.getPosicao()).colocar(coletavel);
        entidades.add(coletavel);
    }

    public void capturarDebugger() {
        if (!debuggerCapturado) {
            debuggerCapturado = true;
            eventos.publicar(new Evento(TipoEvento.DEBUGGER_CAPTURADO, debugger.getPosicao(), 0));
        }
    }

    private void aoCapturarBug(Evento evento) {
        ticksCongelado = CONGELAMENTO_NA_CAPTURA;
        adicionar(new TextoFlutuante(evento.posicao(), String.valueOf(evento.valor()), Estilo.INFO,
                Tempo.emTicks(0.75)));
    }

    public void marcarFaseConcluida() {
        faseConcluida = true;
    }

    public void avancarDeFase() throws MapaInvalidoException {
        fases.carregar(fases.numero() + 1, this);
    }

    public GerenciadorDeRecursos recursos() {
        return recursos;
    }

    public Placar placar() {
        return placar;
    }

    public CentralDeEventos eventos() {
        return eventos;
    }

    public ControladorDeModos modos() {
        return modos;
    }

    public Labirinto labirinto() {
        return labirinto;
    }

    public Debugger debugger() {
        return debugger;
    }

    public List<Bug> bugs() {
        return Collections.unmodifiableList(bugs);
    }

    public ConfiguracaoDaFase config() {
        return config;
    }

    public Random aleatorio() {
        return aleatorio;
    }

    public long tick() {
        return tick;
    }

    public int numeroDaFase() {
        return fases.numero();
    }

    public int coletaveisRestantes() {
        return fases.restantes();
    }

    public boolean debuggerFoiCapturado() {
        return debuggerCapturado;
    }

    public boolean faseFoiConcluida() {
        return faseConcluida;
    }
}
