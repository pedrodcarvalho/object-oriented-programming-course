public class X {
    private static int x = 1;

    public static void main(String[] args) {
        int x = 20;
        System.out.println(x);
        printaX();
        printaX(15);
    }

    public static void printaX() {
        System.out.println(x);
    }

    public static void printaX(int x) {
        System.out.println(x);
    }
}
