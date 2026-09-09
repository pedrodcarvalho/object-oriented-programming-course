import java.util.ArrayList;

public class Diario {
    private String nomeCurso;
    private int quantidadeNotas;
    private ArrayList<Aluno> alunos;

    public Diario(String nomeCurso, int quantidadeNotas) {
        this.nomeCurso = nomeCurso;
        this.quantidadeNotas = quantidadeNotas;
        this.alunos = new ArrayList<>(); // Inicializa a lista de alunos vazia
    }

    // Adiciona um novo aluno ao diário
    public void adicionarAluno(String nomeAluno) {
        alunos.add(new Aluno(nomeAluno, quantidadeNotas));
    }

    // Modifica a nota de um aluno específico através do nome
    public void modificarNota(String nomeAluno, int indiceAtividade, double novaNota) {
        for (Aluno aluno : alunos) {
            if (aluno.getNome().equalsIgnoreCase(nomeAluno)) {
                aluno.setNota(indiceAtividade, novaNota);
                return; // Encerra após encontrar e alterar
            }
        }
        System.out.println("Aluno '" + nomeAluno + "' não encontrado.");
    }

    // Gera relatório completo das notas e identifica a maior e menor nota
    public void gerarRelatorio() {
        System.out.println("\n=== Relatório de Notas: " + nomeCurso + " ===");
        if (alunos.isEmpty()) {
            System.out.println("Nenhum aluno cadastrado no diário.");
            return;
        }

        double maiorNotaGeral = Double.MIN_VALUE;
        double menorNotaGeral = Double.MAX_VALUE;
        String nomeMaiorNota = "";
        String nomeMenorNota = "";

        for (Aluno aluno : alunos) {
            System.out.printf("Aluno: %-10s | Notas: ", aluno.getNome());

            for (double nota : aluno.getNotas()) {
                System.out.printf("%5.1f ", nota);

                // Checa a maior nota
                if (nota > maiorNotaGeral) {
                    maiorNotaGeral = nota;
                    nomeMaiorNota = aluno.getNome();
                }
                // Checa a menor nota
                if (nota < menorNotaGeral) {
                    menorNotaGeral = nota;
                    nomeMenorNota = aluno.getNome();
                }
            }
            // Imprime a média do aluno
            System.out.printf("| Média: %5.1f%n", aluno.calcularMedia());
        }

        System.out.println("\n--- Estatísticas Gerais da Turma ---");
        System.out.printf("Maior Nota: %.1f (Aluno: %s)%n", maiorNotaGeral, nomeMaiorNota);
        System.out.printf("Menor Nota: %.1f (Aluno: %s)%n", menorNotaGeral, nomeMenorNota);
    }

    // Imprime um histograma de notas (escala de 0 a 10) para uma dada atividade
    public void imprimirHistograma(int indiceAtividade) {
        if (indiceAtividade < 0 || indiceAtividade >= quantidadeNotas) {
            System.out.println("Índice de atividade inválido.");
            return;
        }

        System.out.println("\n=== Histograma de Notas - Atividade " + (indiceAtividade + 1) + " ===");

        // Vetor de frequências para notas de 0 a 10
        int[] frequencia = new int[11];

        for (Aluno aluno : alunos) {
            // Trunca/Arredonda a nota para montar o gráfico
            int notaArredondada = (int) Math.round(aluno.getNotas().get(indiceAtividade));

            // Garantia de segurança para a nota não sair do limite do array
            if (notaArredondada > 10)
                notaArredondada = 10;
            if (notaArredondada < 0)
                notaArredondada = 0;

            frequencia[notaArredondada]++;
        }

        // Impressão do gráfico de barras em asteriscos
        for (int i = 0; i < frequencia.length; i++) {
            System.out.printf("Nota %2d: ", i);
            for (int asteriscos = 0; asteriscos < frequencia[i]; asteriscos++) {
                System.out.print("*");
            }
            System.out.println();
        }
    }
}
