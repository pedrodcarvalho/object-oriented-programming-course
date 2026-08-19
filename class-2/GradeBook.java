public class GradeBook {
    // Variável de instância (atributo)
    private String courseName;

    // 1º Construtor: não recebe parâmetros e define um valor padrão
    public GradeBook() {
        this.courseName = "Sem curso definido"; // ou null / ""
    }

    // 2º Construtor: recebe o nome do curso como parâmetro (sobrecarga)
    public GradeBook(String name) {
        this.courseName = name;
    }

    // Método para configurar o nome do curso (modificador/setter)
    public void setCourseName(String name) {
        this.courseName = name;
    }

    // Método para recuperar o nome do curso (acessador/getter)
    public String getCourseName() {
        return courseName;
    }

    // Método para exibir a mensagem de boas-vindas
    public void displayMessage() {
        System.out.printf("Welcome to the GradeBook for\n%s!\n", getCourseName());
    }
}
