package concurrent;

import functions.ConstantFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        ConstantFunction constFunc = new ConstantFunction(-1);
        TabulatedFunction func = new LinkedListTabulatedFunction(constFunc, 1, 1000, 1000);

        Thread executionThreadRead = new Thread(new ReadTask(func));
        Thread executionThreadWrite = new Thread(new WriteTask(func, 0.5));

        executionThreadRead.start();
        executionThreadWrite.start();
    }
}
