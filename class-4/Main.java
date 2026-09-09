public class Main {
    public static void main(String[] args) {

        // 1. Cria um diário com uma quantidade definida de notas (ex: 3 atividades)
        Diario diarioPOO = new Diario("Programação Orientada a Objetos", 3);

        // 2 & 3. Cria a lista de alunos e adiciona novos alunos
        diarioPOO.adicionarAluno("Pedro");
        diarioPOO.adicionarAluno("Ana");
        diarioPOO.adicionarAluno("Carlos");
        diarioPOO.adicionarAluno("Maria");
        diarioPOO.adicionarAluno("João");

        // 4. Modifica alguma nota de algum aluno
        diarioPOO.modificarNota("Pedro", 0, 8.5);
        diarioPOO.modificarNota("Pedro", 1, 9.0);
        diarioPOO.modificarNota("Pedro", 2, 7.5);

        diarioPOO.modificarNota("Ana", 0, 10.0);
        diarioPOO.modificarNota("Ana", 1, 9.5);
        diarioPOO.modificarNota("Ana", 2, 10.0);

        diarioPOO.modificarNota("Carlos", 0, 5.0);
        diarioPOO.modificarNota("Carlos", 1, 6.5);
        diarioPOO.modificarNota("Carlos", 2, 4.0);

        diarioPOO.modificarNota("Maria", 0, 8.0);
        diarioPOO.modificarNota("Maria", 1, 8.5);
        diarioPOO.modificarNota("Maria", 2, 9.0);

        diarioPOO.modificarNota("João", 0, 8.0);
        diarioPOO.modificarNota("João", 1, 6.0);
        diarioPOO.modificarNota("João", 2, 5.5);

        // 5 & 6. Gera relatório de notas (com nome, todas as notas, média e extremos)
        diarioPOO.gerarRelatorio();

        // 7. Histograma de notas para a Atividade 1 (índice 0)
        diarioPOO.imprimirHistograma(0);
    }
}
