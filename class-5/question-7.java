public class A {
    private int x;

    protected void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }
}

public class B extends A {
    public int getB() {
        setX(2);
        return getX();
    }
}

public class C {
    public static void main(String[] args) {
        A a = new A();
        B b = new B();

        System.out.println(a.getX());
        System.out.println(b.getB());
    }
}
