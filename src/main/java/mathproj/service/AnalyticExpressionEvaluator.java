package mathproj.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Безопасный вычислитель аналитических выражений.
 * Поддерживает базовые математические функции и константы.
 */
@Component
public class AnalyticExpressionEvaluator {
    private static final Logger log = LoggerFactory.getLogger(AnalyticExpressionEvaluator.class);
    private static final int POINT_COUNT = 200;
    
    private final ScriptEngine engine;
    private final boolean engineAvailable;

    public AnalyticExpressionEvaluator() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine jsEngine = manager.getEngineByName("javascript");
        this.engine = jsEngine;
        this.engineAvailable = (jsEngine != null);
        
        if (!engineAvailable) {
            log.warn("JavaScript engine недоступен, будет использован простой парсер выражений");
        } else {
            log.info("JavaScript engine доступен для вычисления выражений");
        }
    }

    /**
     * Предобработка выражения: замена котангенса на 1/tan.
     * Поддерживает ctg и cot в любом регистре, с пробелами или без.
     * 
     * @param expression исходное выражение
     * @return выражение с замененным котангенсом
     */
    private String preprocessCotangent(String expression) {
        if (expression == null || expression.isBlank()) {
            return expression;
        }
        
        String original = expression;
        String result = expression;
        
        // Case-insensitive замена ctg( на 1/tan(
        // Поддерживает: ctg(x), CTG(X), Ctg( x ), ctg (x) и т.д.
        String afterCtg = result.replaceAll("(?i)\\bctg\\s*\\(", "1/tan(");
        if (!afterCtg.equals(result)) {
            result = afterCtg;
            log.debug("Заменено ctg на 1/tan в выражении '{}'", original);
        }
        
        // Case-insensitive замена cot( на 1/tan(
        // Поддерживает: cot(x), COT(X), Cot( x ), cot (x) и т.д.
        String afterCot = result.replaceAll("(?i)\\bcot\\s*\\(", "1/tan(");
        if (!afterCot.equals(result)) {
            result = afterCot;
            log.debug("Заменено cot на 1/tan в выражении '{}'", original);
        }
        
        if (!result.equals(original)) {
            log.debug("Результат предобработки котангенса: '{}' -> '{}'", original, result);
        }
        
        return result;
    }

    /**
     * Вычисляет значение выражения для заданного x.
     * 
     * @param expression математическое выражение (например, "Math.sin(x) + 2*x")
     * @param x значение переменной
     * @return результат вычисления
     * @throws ScriptException если выражение невалидно
     */
    public double evaluate(String expression, double x) throws ScriptException {
        if (expression == null || expression.isBlank()) {
            throw new ScriptException("Выражение пусто");
        }

        // Предобработка: замена котангенса на 1/tan
        String preprocessed = preprocessCotangent(expression);

        if (engineAvailable) {
            try {
                return evaluateWithScriptEngine(preprocessed, x);
            } catch (ScriptException e) {
                log.debug("ScriptEngine не смог вычислить, пробуем простой парсер: {}", e.getMessage());
                // Fallback на простой парсер
                return evaluateWithSimpleParser(preprocessed, x);
            }
        } else {
            return evaluateWithSimpleParser(preprocessed, x);
        }
    }

    private double evaluateWithScriptEngine(String expression, double x) throws ScriptException {
        try {
            // Подготовка выражения: замена x на значение
            String preparedExpr = prepareExpression(expression, x);
            
            // Вычисление через ScriptEngine
            Object result = engine.eval(preparedExpr);
            
            if (result instanceof Number) {
                return ((Number) result).doubleValue();
            } else if (result instanceof Double) {
                return (Double) result;
            } else {
                throw new ScriptException("Результат не является числом: " + result);
            }
        } catch (ScriptException e) {
            log.debug("Ошибка вычисления выражения '{}' для x={}: {}", expression, x, e.getMessage());
            throw e;
        }
    }

    private String prepareExpression(String expression, double x) {
        // Выражение уже предобработано (ctg заменен на 1/tan) в методе evaluate
        String result = expression;
        
        // tg(x) -> Math.tan(x) (case-insensitive)
        result = result.replaceAll("(?i)\\btg\\s*\\(", "Math.tan(");
        // ln(x) -> Math.log(x) (case-insensitive)
        result = result.replaceAll("(?i)\\bln\\s*\\(", "Math.log(");
        
        // Добавляем Math. к функциям без префикса (если еще нет)
        result = result.replaceAll("\\b(?<!Math\\.)(sin|cos|tan|asin|acos|atan|sqrt|log|exp|abs|pow|floor|ceil|round)\\s*\\(", "Math.$1(");
        
        // Заменяем константы
        result = result.replaceAll("\\bPI\\b", "Math.PI");
        result = result.replaceAll("\\bE\\b", "Math.E");
        
        // Заменяем ^ на ** для степени (до замены x)
        result = result.replace("^", "**");
        
        // Заменяем все вхождения x (как отдельное слово) на значение
        result = result.replaceAll("\\bx\\b", String.valueOf(x));
        
        log.debug("Подготовка выражения: '{}' -> '{}' для x={}", expression, result, x);
        
        return result;
    }

    private double evaluateWithSimpleParser(String expression, double x) {
        try {
            log.debug("Простый парсер: вычисление '{}' для x={}", expression, x);
            
            // Выражение уже предобработано (ctg заменен на 1/tan) в методе evaluate
            String expr = expression;
            
            // tg(x) -> tan(x) (case-insensitive)
            expr = expr.replaceAll("(?i)\\btg\\s*\\(", "tan(");
            // ln(x) -> log(x) (case-insensitive)
            expr = expr.replaceAll("(?i)\\bln\\s*\\(", "log(");
            
            // Затем заменяем константы
            expr = expr.replace("Math.PI", String.valueOf(Math.PI))
                       .replace("Math.E", String.valueOf(Math.E))
                       .replace("PI", String.valueOf(Math.PI));
            // E заменяем только как отдельное слово, чтобы не сломать числа вроде 1E-5
            expr = expr.replaceAll("\\bE\\b", String.valueOf(Math.E));
            
            // Теперь заменяем x на значение (после преобразования функций)
            expr = expr.replaceAll("\\bx\\b", String.valueOf(x));
            
            // Заменяем ^ на ** для степени
            expr = expr.replace("^", "**");
            
            // Удаляем пробелы
            expr = expr.replaceAll("\\s+", "");
            
            log.debug("После замены x и констант: '{}'", expr);
            
            // Теперь заменяем Math функции (после замены x, чтобы аргументы были числами)
            expr = replaceMathFunctions(expr);
            
            log.debug("После замены Math функций: '{}'", expr);
            
            // Вычисляем выражение используя JavaScript синтаксис через ScriptEngine
            // Если ScriptEngine доступен, используем его, иначе используем простой eval
            if (engineAvailable && engine != null) {
                try {
                    Object result = engine.eval(expr);
                    if (result instanceof Number) {
                        double value = ((Number) result).doubleValue();
                        log.debug("ScriptEngine результат: {}", value);
                        return value;
                    }
                } catch (ScriptException e) {
                    log.debug("ScriptEngine не смог вычислить '{}', пробуем простой парсер: {}", expr, e.getMessage());
                    // Продолжаем с простым парсером
                }
            }
            
            // Простой парсер для базовых арифметических операций
            double result = evaluateArithmeticExpression(expr);
            log.debug("Простой парсер результат: {}", result);
            return result;
            
        } catch (Exception e) {
            log.warn("Ошибка простого парсера для '{}' при x={}: {}", expression, x, e.getMessage(), e);
            throw new RuntimeException("Не удалось вычислить выражение: " + e.getMessage(), e);
        }
    }

    private String replaceMathFunctions(String expr) {
        // Заменяем Math.sin(x) на вызов через рефлексию
        // Используем паттерны для замены функций
        
        // Паттерн для Math.func(arg) или func(arg)
        // Поддержка: sin, cos, tan, tg, ctg, cot, asin, acos, atan, sqrt, log, ln, exp, abs, pow, floor, ceil, round
        Pattern funcPattern = Pattern.compile("(Math\\.)?(sin|cos|tan|tg|ctg|cot|asin|acos|atan|sqrt|log|ln|exp|abs|pow|floor|ceil|round)\\s*\\(([^)]+)\\)");
        Matcher matcher = funcPattern.matcher(expr);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String funcName = matcher.group(2);
            String arg = matcher.group(3);
            
            // Вычисляем аргумент рекурсивно
            double argValue;
            try {
                argValue = evaluateArithmeticExpression(arg);
            } catch (Exception e) {
                // Если не удалось вычислить аргумент, оставляем как есть
                continue;
            }
            
            // Вызываем Math функцию через рефлексию
            double funcResult = callMathFunction(funcName, argValue);
            matcher.appendReplacement(result, String.valueOf(funcResult));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    private double callMathFunction(String funcName, double arg) {
        try {
            switch (funcName.toLowerCase()) {
                case "sin":
                    return Math.sin(arg);
                case "cos":
                    return Math.cos(arg);
                case "tan":
                case "tg":
                    return Math.tan(arg);
                case "ctg":
                case "cot":
                    // Котангенс = 1/tan(x) = cos(x)/sin(x)
                    double tanVal = Math.tan(arg);
                    if (Math.abs(tanVal) < 1e-15) {
                        return Double.NaN; // tan(x) = 0 означает ctg не определен
                    }
                    return 1.0 / tanVal;
                case "asin":
                    return Math.asin(arg);
                case "acos":
                    return Math.acos(arg);
                case "atan":
                    return Math.atan(arg);
                case "sqrt":
                    return Math.sqrt(arg);
                case "log":
                case "ln":
                    return Math.log(arg);
                case "exp":
                    return Math.exp(arg);
                case "abs":
                    return Math.abs(arg);
                case "floor":
                    return Math.floor(arg);
                case "ceil":
                    return Math.ceil(arg);
                case "round":
                    return Math.round(arg);
                case "pow":
                    // pow требует два аргумента, обрабатываем отдельно
                    throw new UnsupportedOperationException("pow требует два аргумента");
                default:
                    throw new UnsupportedOperationException("Неизвестная функция: " + funcName);
            }
        } catch (Exception e) {
            log.warn("Ошибка вызова Math функции '{}' с аргументом {}: {}", funcName, arg, e.getMessage());
            throw new RuntimeException("Ошибка вычисления функции " + funcName, e);
        }
    }

    private double evaluateArithmeticExpression(String expr) {
        // Очень простой парсер для арифметических выражений
        // Поддерживает: +, -, *, /, ** (степень), скобки, числа
        
        // Удаляем пробелы
        expr = expr.trim();
        
        // Если выражение - просто число
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // Не число, продолжаем парсинг
        }
        
        // Обработка скобок
        int openParen = expr.lastIndexOf('(');
        if (openParen != -1) {
            int closeParen = expr.indexOf(')', openParen);
            if (closeParen != -1) {
                String inner = expr.substring(openParen + 1, closeParen);
                double innerValue = evaluateArithmeticExpression(inner);
                String newExpr = expr.substring(0, openParen) + innerValue + expr.substring(closeParen + 1);
                return evaluateArithmeticExpression(newExpr);
            }
        }
        
        // Обработка операций по приоритету
        // Степень **
        int powIndex = expr.lastIndexOf("**");
        if (powIndex != -1) {
            double left = evaluateArithmeticExpression(expr.substring(0, powIndex));
            double right = evaluateArithmeticExpression(expr.substring(powIndex + 2));
            return Math.pow(left, right);
        }
        
        // Умножение и деление
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == '*' && (i == 0 || expr.charAt(i-1) != '*')) {
                double left = evaluateArithmeticExpression(expr.substring(0, i));
                double right = evaluateArithmeticExpression(expr.substring(i + 1));
                return left * right;
            }
            if (c == '/') {
                double left = evaluateArithmeticExpression(expr.substring(0, i));
                double right = evaluateArithmeticExpression(expr.substring(i + 1));
                if (right == 0) throw new ArithmeticException("Деление на ноль");
                return left / right;
            }
        }
        
        // Сложение и вычитание
        for (int i = expr.length() - 1; i >= 0; i--) {
            char c = expr.charAt(i);
            if (c == '+' && i > 0) {
                double left = evaluateArithmeticExpression(expr.substring(0, i));
                double right = evaluateArithmeticExpression(expr.substring(i + 1));
                return left + right;
            }
            if (c == '-' && i > 0) {
                // Проверяем, что это не унарный минус
                boolean isUnary = i == 0 || "+-*/(".indexOf(expr.charAt(i-1)) != -1;
                if (!isUnary) {
                    double left = evaluateArithmeticExpression(expr.substring(0, i));
                    double right = evaluateArithmeticExpression(expr.substring(i + 1));
                    return left - right;
                }
            }
        }
        
        // Унарный минус
        if (expr.startsWith("-")) {
            return -evaluateArithmeticExpression(expr.substring(1));
        }
        
        throw new IllegalArgumentException("Не удалось распарсить выражение: " + expr);
    }

    public int getPointCount() {
        return POINT_COUNT;
    }
}
