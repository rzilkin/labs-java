package mathproj.ui.service;

import mathproj.functions.MathFunction;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.factory.ArrayTabulatedFunctionFactory;
import mathproj.functions.factory.TabulatedFunctionFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Base64;

@Service
public class CurrentFunctionService {

    private final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

    private volatile TabulatedFunction current;
    private volatile String currentId; // id функции в библиотеке, если current взят из неё

    public synchronized TabulatedFunction getOrThrow() {
        if (current == null) {
            throw new IllegalStateException("Функция ещё не создана. Сначала создайте, откройте или загрузите её.");
        }
        return current;
    }

    public synchronized String getCurrentId() {
        return currentId;
    }

    public synchronized void setCurrent(String id, TabulatedFunction f) {
        this.currentId = id;
        this.current = f;
    }

    public synchronized void setCurrentNoId(TabulatedFunction f) {
        this.currentId = null;
        this.current = f;
    }

    public synchronized void clear() {
        this.current = null;
        this.currentId = null;
    }

    public synchronized void clearIfCurrent(String id) {
        if (id != null && id.equals(this.currentId)) {
            clear();
        }
    }

    public synchronized TabulatedFunction createFromPoints(double[] x, double[] y) {
        TabulatedFunction f = factory.create(x, y);
        this.current = f;
        this.currentId = null;
        return f;
    }

    public synchronized TabulatedFunction createFromMathFunction(MathFunction base, double xFrom, double xTo, int count) {
        TabulatedFunction f = factory.create(base, xFrom, xTo, count);
        this.current = f;
        this.currentId = null;
        return f;
    }

    public synchronized String serializeToBase64() {
        TabulatedFunction f = getOrThrow();

        try (var baos = new ByteArrayOutputStream();
             var oos = new ObjectOutputStream(baos)) {

            oos.writeObject(f);
            oos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Ошибка сериализации функции", e);
        }
    }

    public synchronized TabulatedFunction deserializeFromBase64(String base64) {
        if (base64 == null || base64.isBlank()) {
            throw new IllegalArgumentException("base64 пустой");
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(base64);

            try (var bais = new ByteArrayInputStream(bytes);
                 var ois = new ObjectInputStream(bais)) {

                Object obj = ois.readObject();
                if (!(obj instanceof TabulatedFunction tf)) {
                    throw new IllegalArgumentException("Загруженный объект не является TabulatedFunction");
                }

                this.current = tf;
                this.currentId = null;

                return tf;
            }

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неверная строка base64", e);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Ошибка десериализации функции", e);
        }
    }
}


