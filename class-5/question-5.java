public class Aluno {
    private String nome;

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}

public class Main {
    public static void main(String[] args) {
        // Correctly creating an instance of the Aluno class
        Aluno aluno = new Aluno();
        aluno.setNome("Joao");
        System.out.println(aluno.getNome());
    }
}
