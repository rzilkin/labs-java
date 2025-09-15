package myfirstpackage;

public class MySecondClass{
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