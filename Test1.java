class Test1 {
    public static void main(String[] a){
        System.out.println(a);
    }
}

class A {
    int i;
    int j;
    int k;

    public int foo() {
        return 1;
    }

    public int bar() {
        return 1;
    }
}

class B extends A {
    int i;
    int j;

    public int foo() {
        return 1;
    }

    public int boo(int a, int b, int c, int d) {
        return 1;
    }

    public int coo(int a, int b, int c) {
        return 1;
    }
}

class C extends B {
    int i;
    int j;

    public int foor(int a, int b) {
        return 1;
    }

    public int boo(int a, int b, int c, int d) {
        return 1;
    }
}