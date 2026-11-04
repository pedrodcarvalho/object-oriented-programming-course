# Bug Catcher — como executar

Jogo simples feito em Java com o framework **FXGL**: mova a cesta, pegue os cafés e desvie dos bugs.

## Requisitos

- **JDK 21** ou mais novo.
- Conexão com a internet na primeira execução, para baixar o Maven, o FXGL, o JavaFX e o JUnit.
- Não é preciso instalar o Maven: o projeto traz o *Maven Wrapper* (`mvnw` / `mvnw.cmd`).

## Rodar pelo terminal

Na pasta onde está o `pom.xml`:

```bash
./mvnw javafx:run                  # Linux / macOS
mvnw.cmd javafx:run                # Windows
./mvnw javafx:run -Dplayer=Ana     # com o nome do jogador (argumento de linha de comando)
```

Para rodar os testes automatizados (16 testes JUnit):

```bash
./mvnw test
```

Se o Maven já estiver instalado, `mvn javafx:run` e `mvn test` também funcionam.

## Rodar pelo IntelliJ IDEA

1. *File → Open…* e selecione o `pom.xml`. Abra como projeto.
2. Espere o Maven importar as dependências.
3. Na aba **Maven** (lateral direita): *Plugins → javafx → javafx:run*.
   - Alternativa: rodar a classe `bugcatcher.BugCatcherApp` (método `main`).

## Controles

| Ação | Teclas |
| --- | --- |
| Mover a cesta | ← / → ou A / D |

## Estrutura

- `src/main/java/bugcatcher/BugCatcherApp.java`: o jogo em FXGL (janela, teclado, colisões, HUD).
- `src/main/java/bugcatcher/items/`: os itens que caem (`FallingItem` e subclasses) e as interfaces.
- `src/main/java/bugcatcher/player/`: o jogador (`Player`) e os recordes (`HighScores`).
- `src/test/java/`: testes JUnit das regras do jogo.
- `README.md`: diagrama de classes e mapa dos conceitos de OO no código.

Os recordes ficam no arquivo `highscores.txt`, criado na pasta do projeto após a primeira partida.
