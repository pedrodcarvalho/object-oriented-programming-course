public class Aluno {
    private String nome;
    private double nota1;
    private double nota2;

    public Aluno(String nome, double nota1, double nota2) {
        this.nome = nome;
        this.nota1 = nota1;
        this.nota2 = nota2;
    }

    // Overloaded constructor that allows creating an Aluno object with only the name, defaulting the grades to 0.0
    public Aluno(String nome) {
        this(nome, 0.0, 0.0);
    }

    public String toString() {
        return String.format("%s -- %f | %f %n", nome, nota1, nota2);
    }
}

public class Main {
    public static void main(String[] args) {
        Aluno aluno1 = new Aluno("Pedro", 10.0, 10.0);
        Aluno aluno2 = new Aluno("Léo");

        // Correctly printing the Aluno objects using the overridden toString() method
        System.out.println(aluno1);
        System.out.println(aluno2);
    }
}
