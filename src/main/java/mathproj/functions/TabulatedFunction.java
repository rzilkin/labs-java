package mathproj.functions;

//интерфейс табличной функции, расширяющий MathFunction
public interface TabulatedFunction extends MathFunction, Iterable<Point> {

    int getCount(); //получение count
    double getX(int index); //получение индекса x
    double getY(int index); //получение индекса y
    void setY(int index, double value);  //установка значения y по его индексу
    int indexOfX(double x);     //получение индекса по x
    int indexOfY(double y);     //получение индекса по y
    double leftBound();         //самый левый x
    double rightBound();        //самый правый x

}
