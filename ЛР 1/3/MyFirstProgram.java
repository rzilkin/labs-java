class MyFirstClass {
    public static void main(String[] s) {
        MySecondClass o = new MySecondClass(10, 15);
        System.out.println(o.max());
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++){
                o.setFirstVal(i);
                o.setSecondVal(j);
                System.out.print(o.max());
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}

class MySecondClass{
    private int firstVal;
    private int secondVal;

    public MySecondClass(int firstVal, int secondVal){
        this.firstVal = firstVal;
        this.secondVal = secondVal;
    }

    public void setFirstVal(int firstVal) {
        this.firstVal = firstVal;
    }

    public void setSecondVal(int secondVal) {
        this.secondVal = secondVal;
    }

    public int getFirstVal() {
        return firstVal;
    }

    public int getSecondVal() {
        return secondVal;
    }

    public int max(){
        return (firstVal > secondVal) ? firstVal : secondVal;
    }
}