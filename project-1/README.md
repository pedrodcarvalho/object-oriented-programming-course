# Caça-Bugs

Clone de *Pac-Man* para o terminal, com tema de programação. O labirinto é uma placa de circuito e você é o **Debugger**, a setinha amarela de execução. Sua missão é coletar todos os **bits** sem ser pego pelos quatro **bugs**.

Projeto Final de Programação Orientada a Objetos (Java). Este código implementa o documento de concepção do jogo.

## Como rodar

Precisa do **JDK 17 ou mais novo** e de um terminal com pelo menos **80x24** caracteres.

| Onde | Comando |
| --- | --- |
| Linux, macOS ou WSL | `./executar.sh` |
| Já compilado | `java -jar caca-bugs.jar` |
| Windows (cmd/PowerShell) | `executar.bat`: funciona, mas cada tecla precisa de ENTER. Para jogar de verdade, use o WSL. |
| IntelliJ | Abra a aba **Terminal** e rode `./executar.sh`. O console da aba *Run* não é um terminal de verdade e embaralha a tela. |

Se o labirinto aparecer com símbolos estranhos, acrescente `--ascii` (ex.: `./executar.sh --ascii`).

**Controles:** setas ou WASD movem · P pausa · ENTER confirma · Q sai.

O recorde fica salvo em `recorde.txt`, na pasta de onde o jogo foi aberto.

## Regras

- Bit `·` vale 10 pontos. Breakpoint `●` vale 50 e deixa os bugs vulneráveis (azuis) por alguns segundos.
- Capturar bugs com o mesmo breakpoint vale 200, 400, 800 e 1.600 pontos.
- O café `c~` aparece duas vezes por fase (após 70 e 170 coletas). Vale de 100 a 5.000 pontos e some em 10 s.
- Você começa com 3 vidas e ganha uma extra aos 10.000 pontos.
- Os bugs alternam ondas de **dispersão** (cada um vai para o seu canto) e **perseguição**. A cada fase ficam mais rápidos e o breakpoint dura menos.

| Bug | Na perseguição, mira... |
| --- | --- |
| NullPointer (vermelho) | a casa onde você está |
| RaceCondition (rosa) | 4 casas à sua frente |
| Deadlock (ciano) | o dobro do vetor que vai do NullPointer até 2 casas à sua frente |
| Heisenbug (laranja) | você, se estiver a mais de 8 casas; mais perto, foge para o canto dele |

## Estrutura

```
src/
  cacabugs/Jogo.java        ponto de entrada e laço principal (60 quadros/s)
  cacabugs/nucleo/          Entidade, Partida, Posicao, Direcao, interfaces Atualizavel e Desenhavel
  cacabugs/entidades/       Personagem, Debugger, Bug e os 4 bugs, TextoFlutuante
  cacabugs/coletaveis/      Coletavel, Bit, Breakpoint, Cafe
  cacabugs/modos/           ModoDoBug e os modos Dispersao, Perseguicao, Vulneravel, Capturado, NoBacklog
  cacabugs/mapa/            Labirinto, Celula, TipoCelula, FabricaDeFase, MapaInvalidoException
  cacabugs/eventos/         CentralDeEventos, OuvinteDeEventos, Evento, TipoEvento
  cacabugs/gerenciadores/   Placar, GerenciadorDeFases, ControladorDeModos, GerenciadorDeColisoes, ...
  cacabugs/estados/         telas do jogo: menu, jogando, pausa, vida perdida, fase concluída, fim
  cacabugs/terminal/        Terminal, Tela (buffer duplo), Estilo, Sprite
  recursos/                 labirinto.txt e os sprites (.properties)
```

## Onde está cada conceito de OO

| Conceito | Onde |
| --- | --- |
| Abstração | `Entidade`, `Personagem`, `Bug`, `Coletavel` e `ModoHostil` são abstratas |
| Herança | `Debugger` e `Bug` herdam o movimento de `Personagem`. Os 4 bugs herdam de `Bug`; `Bit`, `Breakpoint` e `Cafe`, de `Coletavel` |
| Polimorfismo | `Partida.atualizar()` chama `atualizar()`/`desenhar()` numa `List<Entidade>`. Cada bug sobrescreve `calcularAlvo()` e cada coletável, `aoSerColetado()` |
| Encapsulamento | Atributos privados. Vidas só mudam por `perderVida()`/`ganharVida()` e pontos só por `Placar.adicionar()` |
| Interfaces | `Atualizavel`, `Desenhavel`, `ModoDoBug` (com métodos *default*), `EstadoDoJogo`, `OuvinteDeEventos` |
| Composição | `Jogo` tem os gerenciadores, `Labirinto` é feito de `Celula`s, cada `Bug` tem um `ModoDoBug` e o `Deadlock` guarda uma referência ao `NullPointer` |
| State | Modos dos bugs (`modos/`) e telas do jogo (`estados/`, com pilha para a pausa) |
| Template Method | `Bug.escolherDirecao()` é `final`; só o alvo muda |
| Observer | `CentralDeEventos`: o `Bit` publica `BIT_COLETADO`, e `Placar` e `GerenciadorDeFases` reagem |
| Factory | `FabricaDeFase` monta a fase a partir de `labirinto.txt` |
| Enums, exceções, coleções | `Direcao`, `TipoCelula`, `Comando`, `Camada`; `MapaInvalidoException` (checada); `List`, `Map`, `EnumMap`, `Deque` |

## Personalizar

- **Labirinto:** edite `src/recursos/labirinto.txt`. Legenda: `#` parede, `.` bit, `o` breakpoint, espaço corredor, `T` túnel, `-` porta do backlog, `H` backlog, `P` Debugger, `C` café, `1` a `4` bugs. Todas as linhas precisam ter o mesmo tamanho. Um mapa inválido mostra o erro no menu.
- **Visual:** cores e símbolos ficam em `src/recursos/sprites.properties` (paleta ANSI de 256 cores).
