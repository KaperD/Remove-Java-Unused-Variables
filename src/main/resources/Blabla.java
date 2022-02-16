import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Main {
    public int x, t, q;


    class A {
        int q;
        class B {
            int q;
            class C {
                int q;
                void f() {
                    System.out.println(Main.this.q);
                }
            }
        }
    }

    {
        x = 3;
    }
    public static void main(String[] args) {

    }

    public void f() {
        this.x = 4;
        int a = x;
        int b = 4;
        int t = 2;
    }

    public void g(int l) {
        int a = 3;
        //-----------------
        int r = a;
        r = a;

        //-----------------
        System.out.println(a);
        //-----------------
        List<String> z = new ArrayList<>(a);
        //-----------------
        class A {
            int a = 6;
            void bar() {
                a = 5;
            }
        }
        //-----------------
        class B {
            void bar() {
                System.out.println(a);
            }
        }
        //-----------------
        class C {
            int b = a;
        }
        //-----------------
        Consumer<Integer> c = e -> {
            System.out.println(e == a);
        };

    }
}
