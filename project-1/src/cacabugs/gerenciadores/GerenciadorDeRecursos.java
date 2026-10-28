package cacabugs.gerenciadores;

import cacabugs.terminal.Estilo;
import cacabugs.terminal.Sprite;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Carrega sprites e arquivos de texto (o labirinto) uma única vez e os entrega por nome.
 * Os sprites vêm de um arquivo .properties: trocar o arquivo muda o visual do jogo inteiro.
 */
public class GerenciadorDeRecursos {
    private static final String PASTA = "/recursos/";

    private final Map<String, Sprite> sprites = new HashMap<>();
    private final Map<String, List<String>> textos = new HashMap<>();
    private final boolean ascii;

    public GerenciadorDeRecursos(boolean ascii) {
        this.ascii = ascii;
        carregarSprites(ascii ? "sprites-ascii.properties" : "sprites.properties");
    }

    public boolean modoAscii() {
        return ascii;
    }

    public Sprite sprite(String nome) {
        Sprite sprite = sprites.get(nome);
        if (sprite == null) {
            throw new IllegalArgumentException("Sprite não encontrado: " + nome);
        }
        return sprite;
    }

    public List<String> mapa() {
        return linhas("labirinto.txt");
    }

    public List<String> linhas(String arquivo) {
        return textos.computeIfAbsent(arquivo, this::lerLinhas);
    }

    private void carregarSprites(String arquivo) {
        Properties propriedades = new Properties();
        try (Reader leitor = new InputStreamReader(abrir(arquivo), StandardCharsets.UTF_8)) {
            propriedades.load(leitor);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + arquivo, e);
        }
        for (String nome : propriedades.stringPropertyNames()) {
            sprites.put(nome, interpretar(nome, propriedades.getProperty(nome)));
        }
    }

    /** Formato: texto;cor da frente;cor de fundo;negrito. */
    private static Sprite interpretar(String nome, String valor) {
        String[] partes = valor.split(";");
        if (partes.length != 4) {
            throw new IllegalStateException("Sprite mal definido: " + nome + "=" + valor);
        }
        String texto = partes[0].equals("espaco") ? " " : partes[0];
        Estilo estilo = new Estilo(Integer.parseInt(partes[1].trim()), Integer.parseInt(partes[2].trim()),
                partes[3].trim().equals("1"));
        return new Sprite(texto, estilo);
    }

    private List<String> lerLinhas(String arquivo) {
        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(abrir(arquivo), StandardCharsets.UTF_8))) {
            return leitor.lines().filter(linha -> !linha.isBlank()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + arquivo, e);
        }
    }

    private static InputStream abrir(String arquivo) {
        InputStream entrada = GerenciadorDeRecursos.class.getResourceAsStream(PASTA + arquivo);
        if (entrada == null) {
            throw new IllegalStateException("Recurso não encontrado: " + PASTA + arquivo
                    + " (a pasta 'recursos' precisa estar no classpath)");
        }
        return entrada;
    }
}
