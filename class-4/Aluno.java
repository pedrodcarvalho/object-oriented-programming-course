import java.util.ArrayList;

public class Aluno {
    private String nome;
    private ArrayList<Double> notas;

    // Inicializa o aluno já com uma quantidade definida de notas (iniciadas com
    // 0.0)
    public Aluno(String nome, int quantidadeNotas) {
        this.nome = nome;
        this.notas = new ArrayList<>(quantidadeNotas);
        for (int i = 0; i < quantidadeNotas; i++) {
            this.notas.add(0.0);
        }
    }

    public String getNome() {
        return nome;
    }

    public ArrayList<Double> getNotas() {
        return notas;
    }

    public void setNota(int indice, double nota) {
        if (indice >= 0 && indice < notas.size()) {
            notas.set(indice, nota);
        } else {
            System.out.println("Índice de nota inválido para o aluno " + nome);
        }
    }

    public double calcularMedia() {
        double soma = 0;
        for (double nota : notas) {
            soma += nota;
        }
        return notas.isEmpty() ? 0 : soma / notas.size();
    }
}
