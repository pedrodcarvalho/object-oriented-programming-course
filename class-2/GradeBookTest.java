import java.util.Scanner;

public class GradeBookTest {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Testando o 1º Construtor (sem parâmetros)
        GradeBook gradeBookDefault = new GradeBook();
        System.out.printf("Nome inicial (gradeBookDefault): %s\n\n", gradeBookDefault.getCourseName());

        // Testando o 2º Construtor (com parâmetro)
        GradeBook myGradeBook = new GradeBook("DESENVOLVIMENTO DE SISTEMAS ORIENTADOS A OBJETOS");
        System.out.printf("Initial course name is: %s\n\n", myGradeBook.getCourseName());

        // Solicitando e alterando o nome do curso via setCourseName
        System.out.println("Please enter the course name:");
        String theName = input.nextLine();
        myGradeBook.setCourseName(theName);

        System.out.println();

        // Exibindo a mensagem formatada
        myGradeBook.displayMessage();

        input.close();
    }
}
