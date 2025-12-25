// Validation utilities with Russian error messages

export function validateFunctionName(name: string): string | null {
    if (!name || name.trim().length === 0) {
        return 'Имя функции не может быть пустым';
    }
    if (name.trim().length > 100) {
        return 'Имя функции не должно превышать 100 символов';
    }
    return null;
}

export function validatePointCount(count: number | string): string | null {
    const num = typeof count === 'string' ? parseInt(count, 10) : count;
    if (isNaN(num)) {
        return 'Количество точек должно быть числом';
    }
    if (num < 2) {
        return 'Количество точек должно быть не менее 2';
    }
    if (num > 10000) {
        return 'Количество точек не должно превышать 10000';
    }
    return null;
}

export function validateNumber(value: string | number, label: string, min?: number, max?: number): string | null {
    const num = typeof value === 'string' ? parseFloat(value) : value;
    if (isNaN(num)) {
        return `${label} должно быть числом`;
    }
    if (!isFinite(num)) {
        return `${label} должно быть конечным числом`;
    }
    if (min !== undefined && num < min) {
        return `${label} должно быть не менее ${min}`;
    }
    if (max !== undefined && num > max) {
        return `${label} должно быть не более ${max}`;
    }
    return null;
}

export function validateRange(from: number | string, to: number | string): string | null {
    const fromNum = typeof from === 'string' ? parseFloat(from) : from;
    const toNum = typeof to === 'string' ? parseFloat(to) : to;

    if (isNaN(fromNum) || isNaN(toNum)) {
        return 'Границы диапазона должны быть числами';
    }
    if (fromNum >= toNum) {
        return 'Начальное значение должно быть меньше конечного';
    }
    return null;
}

export function validateExpression(expression: string): string | null {
    if (!expression || expression.trim().length === 0) {
        return 'Выражение не может быть пустым';
    }
    if (expression.trim().length > 500) {
        return 'Выражение не должно превышать 500 символов';
    }
    return null;
}

export function validateThreadCount(threads: number | string, maxThreads: number = 64): string | null {
    const num = typeof threads === 'string' ? parseInt(threads, 10) : threads;
    if (isNaN(num)) {
        return 'Количество потоков должно быть числом';
    }
    if (num < 1) {
        return 'Количество потоков должно быть не менее 1';
    }
    if (num > maxThreads) {
        return `Количество потоков не должно превышать ${maxThreads}`;
    }
    return null;
}

export function validatePoints(points: Array<{ x: number; y: number }>): string | null {
    if (!points || points.length === 0) {
        return 'Точки не могут быть пустыми';
    }
    if (points.length < 2) {
        return 'Должно быть не менее 2 точек';
    }

    // Check for duplicate x values
    const xValues = points.map(p => p.x);
    const uniqueX = new Set(xValues);
    if (uniqueX.size !== xValues.length) {
        return 'Значения x должны быть уникальными';
    }

    // Check if x values are sorted
    for (let i = 1; i < points.length; i++) {
        if (points[i].x <= points[i - 1].x) {
            return 'Значения x должны быть строго возрастающими';
        }
    }

    // Check for valid numbers
    for (const point of points) {
        if (isNaN(point.x) || !isFinite(point.x)) {
            return 'Все значения x должны быть валидными числами';
        }
        if (isNaN(point.y) || !isFinite(point.y)) {
            return 'Все значения y должны быть валидными числами';
        }
    }

    return null;
}

export function validateComponentIds(componentIds: number[]): string | null {
    if (!componentIds || componentIds.length === 0) {
        return 'Необходимо выбрать хотя бы одну функцию';
    }
    if (componentIds.length < 2) {
        return 'Для составной функции необходимо выбрать минимум 2 функции';
    }
    if (componentIds.length > 100) {
        return 'Максимальное количество компонентов: 100';
    }
    return null;
}

