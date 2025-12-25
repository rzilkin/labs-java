import { useState, useEffect, useRef } from 'react';
import { Modal } from './Modal';
import { getJson } from '../api';
import { showError } from '../errorManager';
import { validateNumber, validateRange } from '../utils/validation';

interface Point {
    x: number;
    y: number;
}

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
}

interface FunctionFull {
    summary: {
        id: number;
        name: string;
        type: string;
    };
    points: Point[];
    analyticExpression?: string;
    components?: number[];
}

interface FunctionGraphModalProps {
    isOpen: boolean;
    onClose: () => void;
}

// Cache for loaded component functions (to avoid re-fetching)
const componentCache = new Map<number, FunctionFull>();

export function FunctionGraphModal({ isOpen, onClose }: FunctionGraphModalProps) {
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [selectedFunctionId, setSelectedFunctionId] = useState<number | null>(null);
    const [functionData, setFunctionData] = useState<FunctionFull | null>(null);
    const [graphPoints, setGraphPoints] = useState<Point[]>([]);
    const [loading, setLoading] = useState(false);
    const [compositeLoading, setCompositeLoading] = useState(false);

    // Graph parameters for ANALYTIC and COMPOSITE functions
    const [xFrom, setXFrom] = useState('-10');
    const [xTo, setXTo] = useState('10');

    // Value calculation
    const [xValue, setXValue] = useState('');
    const [calculatedValue, setCalculatedValue] = useState<number | null>(null);

    const canvasRef = useRef<HTMLCanvasElement>(null);

    useEffect(() => {
        if (isOpen) {
            loadFunctions();
        } else {
            // Сброс состояния при закрытии модального окна
            setFunctions([]);
            setSelectedFunctionId(null);
            setFunctionData(null);
            setGraphPoints([]);
            setXFrom('-10');
            setXTo('10');
            setXValue('');
            setCalculatedValue(null);
        }
    }, [isOpen]);

    useEffect(() => {
        if (isOpen && selectedFunctionId) {
            loadFunction();
        }
    }, [isOpen, selectedFunctionId]);

    useEffect(() => {
        if (graphPoints.length > 0 && canvasRef.current) {
            drawGraph();
        }
    }, [graphPoints]);

    const loadFunctions = async () => {
        try {
            const data = await getJson<FunctionSummary[]>('/api/v1/functions');
            setFunctions(data || []);
        } catch (e) {
            showError(e);
        }
    };

    const loadFunction = async () => {
        if (!selectedFunctionId) return;
        try {
            setLoading(true);
            const data = await getJson<FunctionFull>(`/api/v1/functions/${selectedFunctionId}`);
            setFunctionData(data);

            // For TABULATED functions, immediately display existing points
            if (data.summary.type === 'TABULATED' && data.points && data.points.length > 0) {
                setGraphPoints(data.points);
            } else {
                setGraphPoints([]);
            }
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    // Cotangent function: ctg(x) = 1/tan(x)
    const ctg = (x: number): number => {
        const tanVal = Math.tan(x);
        if (Math.abs(tanVal) < 1e-15) {
            return NaN; // tan(x) = 0 означает ctg не определен
        }
        return 1.0 / tanVal;
    };

    // Safe expression evaluator for ANALYTIC functions
    const safeEval = (expression: string, x: number): number => {
        try {
            // Replace x with the actual value
            let expr = expression.replace(/x/g, `(${x})`);

            // Support common math functions including cotangent
            const mathContext: { [key: string]: any } = {
                Math: Math,
                sin: Math.sin,
                cos: Math.cos,
                tan: Math.tan,
                tg: Math.tan,     // Альтернативное название для tan
                ctg: ctg,         // Котангенс
                cot: ctg,         // Альтернативное название для ctg
                asin: Math.asin,
                acos: Math.acos,
                atan: Math.atan,
                exp: Math.exp,
                log: Math.log,
                ln: Math.log,
                sqrt: Math.sqrt,
                abs: Math.abs,
                pow: Math.pow,
                floor: Math.floor,
                ceil: Math.ceil,
                round: Math.round,
                PI: Math.PI,
                E: Math.E,
            };

            // Create a safe evaluation function
            // Replace ^ with ** for exponentiation
            expr = expr.replace(/\^/g, '**');

            // Use Function constructor with limited scope
            const func = new Function(...Object.keys(mathContext), `return ${expr}`);
            return func(...Object.values(mathContext));
        } catch (e) {
            throw new Error(`Ошибка вычисления выражения: ${e instanceof Error ? e.message : String(e)}`);
        }
    };

    // Определение, содержит ли выражение функции с ограниченной областью определения
    const detectRestrictedFunction = (expression: string | undefined): 'tan' | 'ctg' | null => {
        if (!expression) return null;
        const lowerExpr = expression.toLowerCase();
        // Проверяем ctg/cot первым, так как они более специфичны
        if (/\b(ctg|cot)\s*\(/.test(lowerExpr)) {
            return 'ctg';
        }
        // Проверяем tan/tg
        if (/\b(tan|tg)\s*\(/.test(lowerExpr)) {
            return 'tan';
        }
        return null;
    };

    // Получение безопасного диапазона для функций с ограниченной областью определения
    const getSafeRange = (restrictedFunc: 'tan' | 'ctg' | null): { from: string; to: string } | null => {
        if (!restrictedFunc) return null;

        const epsilon = 0.01;
        const halfPi = Math.PI / 2;
        const pi = Math.PI;

        if (restrictedFunc === 'tan') {
            // tan не определен при x = π/2 + πn
            // Безопасный диапазон: (-π/2 + ε, π/2 - ε)
            return {
                from: (-halfPi + epsilon).toFixed(4),
                to: (halfPi - epsilon).toFixed(4)
            };
        } else if (restrictedFunc === 'ctg') {
            // ctg не определен при x = πn (т.е. 0, π, -π, ...)
            // Безопасный диапазон: (ε, π - ε)
            return {
                from: epsilon.toFixed(4),
                to: (pi - epsilon).toFixed(4)
            };
        }
        return null;
    };

    const updateGraphForAnalytic = async () => {
        if (!functionData || functionData.summary.type !== 'ANALYTIC') return;

        try {
            // Определяем, нужен ли безопасный диапазон для ограниченных функций
            const restrictedFunc = detectRestrictedFunction(functionData.analyticExpression);
            const safeRange = getSafeRange(restrictedFunc);

            // Используем безопасный диапазон для ограниченных функций или пользовательский диапазон
            const actualXFrom = (restrictedFunc && safeRange) ? safeRange.from : xFrom;
            const actualXTo = (restrictedFunc && safeRange) ? safeRange.to : xTo;

            // Validate range
            const rangeError = validateRange(actualXFrom, actualXTo);
            if (rangeError) {
                showError(new Error(rangeError), true);
                return;
            }

            const from = parseFloat(actualXFrom);
            const to = parseFloat(actualXTo);

            // Validate from >= to
            if (from >= to) {
                showError(new Error('Значение "x от" должно быть меньше "x до"'), true);
                return;
            }

            setLoading(true);

            // Call backend to get function with generated points
            const response = await getJson<FunctionFull>(
                `/api/v1/functions/${selectedFunctionId}?from=${from}&to=${to}`
            );

            // Debug logging
            console.log('Response from server:', response);
            console.log('Points array:', response.points);
            console.log('Points length:', response.points?.length);

            // Check if response has points array
            if (response && Array.isArray(response.points)) {
                if (response.points.length > 0) {
                    // Фильтруем NaN значения для функций с ограниченной областью определения
                    const validPoints = response.points.filter(p =>
                        isFinite(p.x) && isFinite(p.y) && !isNaN(p.x) && !isNaN(p.y)
                    );

                    if (validPoints.length > 0) {
                        setGraphPoints(validPoints);
                    } else {
                        showError(new Error('Не удалось сгенерировать точки. Проверьте выражение функции или диапазон.'), true);
                    }
                } else {
                    // Backend returned empty points array (error occurred on server)
                    showError(new Error('Не удалось сгенерировать точки. Проверьте выражение функции или диапазон.'), true);
                }
            } else {
                // Response structure is invalid or points field is missing
                console.error('Invalid response structure:', response);
                showError(new Error('Не удалось сгенерировать точки. Проверьте выражение функции или диапазон.'), true);
            }
        } catch (e) {
            console.error('Error updating graph for analytic function:', e);
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    const drawGraph = () => {
        const canvas = canvasRef.current;
        if (!canvas || graphPoints.length === 0) return;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        const width = canvas.width;
        const height = canvas.height;
        const padding = 50;

        ctx.clearRect(0, 0, width, height);

        // Background
        const bgColor = getComputedStyle(document.documentElement).getPropertyValue('--card') || '#fff';
        ctx.fillStyle = bgColor;
        ctx.fillRect(0, 0, width, height);

        // Find min/max values
        const xValues = graphPoints.map(p => p.x);
        const yValues = graphPoints.map(p => p.y);
        const minX = Math.min(...xValues);
        const maxX = Math.max(...xValues);
        const minY = Math.min(...yValues);
        const maxY = Math.max(...yValues);

        const rangeX = maxX - minX || 1;
        const rangeY = maxY - minY || 1;

        // Better axis scaling with proper padding
        const xPadding = rangeX * 0.05;
        const yPadding = rangeY * 0.1;
        const plotMinX = minX - xPadding;
        const plotMaxX = maxX + xPadding;
        const plotMinY = minY - yPadding;
        const plotMaxY = maxY + yPadding;
        const plotRangeX = plotMaxX - plotMinX || 1;
        const plotRangeY = plotMaxY - plotMinY || 1;

        const plotWidth = width - 2 * padding;
        const plotHeight = height - 2 * padding;

        // Draw grid
        const borderColor = getComputedStyle(document.documentElement).getPropertyValue('--border') || '#ddd';
        ctx.strokeStyle = borderColor;
        ctx.lineWidth = 0.5;

        // Vertical grid lines
        for (let i = 0; i <= 10; i++) {
            const x = padding + (i / 10) * plotWidth;
            ctx.beginPath();
            ctx.moveTo(x, padding);
            ctx.lineTo(x, height - padding);
            ctx.stroke();
        }

        // Horizontal grid lines
        for (let i = 0; i <= 10; i++) {
            const y = height - padding - (i / 10) * plotHeight;
            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();
        }

        // Draw axes with proper scaling
        ctx.strokeStyle = borderColor;
        ctx.lineWidth = 2;

        // Y axis (x = 0 or at left edge if 0 is outside range)
        const yAxisX = padding + ((0 - plotMinX) / plotRangeX) * plotWidth;
        if (yAxisX >= padding && yAxisX <= width - padding) {
            ctx.beginPath();
            ctx.moveTo(yAxisX, padding);
            ctx.lineTo(yAxisX, height - padding);
            ctx.stroke();
        }

        // X axis (y = 0 or at bottom edge if 0 is outside range)
        const xAxisY = height - padding - ((0 - plotMinY) / plotRangeY) * plotHeight;
        if (xAxisY >= padding && xAxisY <= height - padding) {
            ctx.beginPath();
            ctx.moveTo(padding, xAxisY);
            ctx.lineTo(width - padding, xAxisY);
            ctx.stroke();
        }

        // Draw function line with proper scaling
        const lineColor = getComputedStyle(document.documentElement).getPropertyValue('--btn-bg') || '#111';
        ctx.strokeStyle = lineColor;
        ctx.lineWidth = 2;
        ctx.beginPath();

        let firstPoint = true;
        for (const point of graphPoints) {
            const x = padding + ((point.x - plotMinX) / plotRangeX) * plotWidth;
            const y = height - padding - ((point.y - plotMinY) / plotRangeY) * plotHeight;

            if (firstPoint) {
                ctx.moveTo(x, y);
                firstPoint = false;
            } else {
                ctx.lineTo(x, y);
            }
        }

        ctx.stroke();

        // Draw axis labels
        const textColor = getComputedStyle(document.documentElement).getPropertyValue('--text') || '#000';
        ctx.fillStyle = textColor;
        ctx.font = '12px sans-serif';
        ctx.textAlign = 'center';

        // X axis labels with proper scaling
        for (let i = 0; i <= 5; i++) {
            const x = padding + (i / 5) * plotWidth;
            const value = plotMinX + (i / 5) * plotRangeX;
            ctx.fillText(value.toFixed(2), x, height - padding + 20);
        }

        // Y axis labels with proper scaling
        ctx.textAlign = 'right';
        for (let i = 0; i <= 5; i++) {
            const y = height - padding - (i / 5) * plotHeight;
            const value = plotMinY + (i / 5) * plotRangeY;
            ctx.fillText(value.toFixed(2), padding - 10, y + 4);
        }
    };

    // Linear interpolation for tabulated functions
    const interpolateTabulated = (x: number, points: Point[]): number => {
        if (points.length === 0) {
            throw new Error('Нет точек для интерполяции');
        }

        // Sort points by x
        const sortedPoints = [...points].sort((a, b) => a.x - b.x);

        // Extrapolation: x < min
        if (x < sortedPoints[0].x) {
            if (sortedPoints.length === 1) {
                return sortedPoints[0].y;
            }
            // Linear extrapolation using first two points
            const p1 = sortedPoints[0];
            const p2 = sortedPoints[1];
            const slope = (p2.y - p1.y) / (p2.x - p1.x);
            return p1.y + slope * (x - p1.x);
        }

        // Extrapolation: x > max
        if (x > sortedPoints[sortedPoints.length - 1].x) {
            if (sortedPoints.length === 1) {
                return sortedPoints[0].y;
            }
            // Linear extrapolation using last two points
            const last = sortedPoints.length - 1;
            const p1 = sortedPoints[last - 1];
            const p2 = sortedPoints[last];
            const slope = (p2.y - p1.y) / (p2.x - p1.x);
            return p2.y + slope * (x - p2.x);
        }

        // Interpolation: find the segment
        for (let i = 0; i < sortedPoints.length - 1; i++) {
            if (x >= sortedPoints[i].x && x <= sortedPoints[i + 1].x) {
                const p1 = sortedPoints[i];
                const p2 = sortedPoints[i + 1];
                if (p2.x === p1.x) {
                    return p1.y; // Vertical line
                }
                const t = (x - p1.x) / (p2.x - p1.x);
                return p1.y + t * (p2.y - p1.y);
            }
        }

        // Fallback
        return sortedPoints[0].y;
    };

    // Load a component function (with caching)
    const loadComponentFunction = async (componentId: number): Promise<FunctionFull> => {
        // Check cache first
        if (componentCache.has(componentId)) {
            return componentCache.get(componentId)!;
        }

        // Fetch from server
        const componentData = await getJson<FunctionFull>(`/api/v1/functions/${componentId}`);
        componentCache.set(componentId, componentData);
        return componentData;
    };

    // Evaluate a single component function at value y
    const evaluateComponent = (component: FunctionFull, y: number): number => {
        if (component.summary.type === 'ANALYTIC' && component.analyticExpression) {
            return safeEval(component.analyticExpression, y);
        } else if (component.summary.type === 'TABULATED' && component.points && component.points.length > 0) {
            return interpolateTabulated(y, component.points);
        } else if (component.summary.type === 'COMPOSITE') {
            // For nested composite, we would need to recursively evaluate
            // For simplicity, if it has pre-computed points, use interpolation
            if (component.points && component.points.length > 0) {
                return interpolateTabulated(y, component.points);
            }
            throw new Error(`Композитная функция "${component.summary.name}" не имеет вычисленных точек`);
        } else {
            throw new Error(`Не удалось вычислить компонент "${component.summary.name}" типа ${component.summary.type}`);
        }
    };

    // Generate graph for COMPOSITE function
    const updateGraphForComposite = async () => {
        if (!functionData || functionData.summary.type !== 'COMPOSITE') return;
        if (!functionData.components || functionData.components.length === 0) {
            showError(new Error('Композитная функция не содержит компонентов'), true);
            return;
        }

        // Validate range
        const rangeError = validateRange(xFrom, xTo);
        if (rangeError) {
            showError(new Error(rangeError), true);
            return;
        }

        const from = parseFloat(xFrom);
        const to = parseFloat(xTo);

        if (from >= to) {
            showError(new Error('Значение "x от" должно быть меньше "x до"'), true);
            return;
        }

        setCompositeLoading(true);
        setGraphPoints([]);

        try {
            // Load all component functions
            const componentIds = functionData.components;
            const components: FunctionFull[] = [];

            for (const componentId of componentIds) {
                try {
                    const component = await loadComponentFunction(componentId);
                    components.push(component);
                } catch (e) {
                    throw new Error(`Не удалось загрузить компонент с ID ${componentId}: ${e instanceof Error ? e.message : String(e)}`);
                }
            }

            // Generate 200 x values
            const pointCount = 200;
            const step = (to - from) / (pointCount - 1);
            const points: Point[] = [];
            let errorCount = 0;

            for (let i = 0; i < pointCount; i++) {
                const x = from + i * step;
                let y = x; // Start with y = x

                try {
                    // Apply each component function sequentially
                    for (const component of components) {
                        y = evaluateComponent(component, y);

                        // Check for NaN or Infinity
                        if (!isFinite(y) || isNaN(y)) {
                            throw new Error('Результат не является конечным числом');
                        }
                    }

                    points.push({ x, y });
                } catch {
                    // Skip points that cause errors
                    errorCount++;
                }
            }

            if (points.length === 0) {
                showError(new Error('Не удалось вычислить композитную функцию. Проверьте компоненты и диапазон.'), true);
                return;
            }

            if (errorCount > 0) {
                console.warn(`Пропущено ${errorCount} точек из-за ошибок вычисления`);
            }

            setGraphPoints(points);
        } catch (e) {
            console.error('Ошибка вычисления композитной функции:', e);
            showError(new Error(`Не удалось вычислить композитную функцию: ${e instanceof Error ? e.message : String(e)}`), true);
        } finally {
            setCompositeLoading(false);
        }
    };

    // Calculate single value for COMPOSITE function
    const evaluateCompositeAtX = async (x: number): Promise<number> => {
        if (!functionData || functionData.summary.type !== 'COMPOSITE') {
            throw new Error('Функция не является композитной');
        }
        if (!functionData.components || functionData.components.length === 0) {
            throw new Error('Композитная функция не содержит компонентов');
        }

        let y = x;

        for (const componentId of functionData.components) {
            const component = await loadComponentFunction(componentId);
            y = evaluateComponent(component, y);

            if (!isFinite(y) || isNaN(y)) {
                throw new Error('Результат не является конечным числом');
            }
        }

        return y;
    };

    const handleCalculateValue = async () => {
        if (!functionData) {
            showError(new Error('Выберите функцию'), true);
            return;
        }

        if (!xValue.trim()) {
            showError(new Error('Введите значение x'), true);
            return;
        }

        try {
            const xError = validateNumber(xValue, 'Значение x');
            if (xError) {
                showError(new Error(xError), true);
                return;
            }

            const x = parseFloat(xValue);

            let result: number;

            if (functionData.summary.type === 'ANALYTIC' && functionData.analyticExpression) {
                result = safeEval(functionData.analyticExpression, x);
            } else if (functionData.summary.type === 'TABULATED' && functionData.points) {
                result = interpolateTabulated(x, functionData.points);
            } else if (functionData.summary.type === 'COMPOSITE') {
                // For COMPOSITE: if we have pre-computed points, use interpolation
                // Otherwise, compute through component chain
                if (graphPoints.length > 0) {
                    result = interpolateTabulated(x, graphPoints);
                } else {
                    // Evaluate through components
                    setLoading(true);
                    try {
                        result = await evaluateCompositeAtX(x);
                    } finally {
                        setLoading(false);
                    }
                }
            } else {
                showError(new Error('Не удалось вычислить значение для данного типа функции'), true);
                return;
            }

            setCalculatedValue(result);
        } catch (e) {
            showError(e);
            setCalculatedValue(null);
        }
    };

    const getDomainDisplay = () => {
        if (!functionData || !functionData.points || functionData.points.length === 0) return null;

        const xValues = functionData.points.map(p => p.x);
        const minX = Math.min(...xValues);
        const maxX = Math.max(...xValues);

        return `Область: [${minX.toFixed(2)} .. ${maxX.toFixed(2)}]`;
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="График функции">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {/* Function Selection */}
                <div>
                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>
                        Выберите функцию:
                    </label>
                    <select
                        value={selectedFunctionId || ''}
                        onChange={(e) => {
                            const id = e.target.value ? parseInt(e.target.value) : null;
                            setSelectedFunctionId(id);
                            setFunctionData(null);
                            setGraphPoints([]);
                            setCalculatedValue(null);
                        }}
                        style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                    >
                        <option value="">-- Выберите функцию --</option>
                        {functions.map(func => (
                            <option key={func.id} value={func.id}>
                                {func.name} (ID: {func.id}, Тип: {func.type})
                            </option>
                        ))}
                    </select>
                    {functionData && (
                        <div style={{ color: 'var(--text)', marginTop: '8px', fontSize: '14px' }}>
                            Функция: {functionData.summary.name} (ID: {functionData.summary.id}, Тип: {functionData.summary.type})
                        </div>
                    )}
                </div>

                {/* Graph Parameters - Only for ANALYTIC functions */}
                {functionData && functionData.summary.type === 'ANALYTIC' && (() => {
                    const restrictedFunc = detectRestrictedFunction(functionData.analyticExpression);
                    const safeRange = getSafeRange(restrictedFunc);

                    return (
                        <div>
                            <h3 style={{ color: 'var(--text)', marginTop: 0, marginBottom: '15px' }}>Параметры графика</h3>

                            {/* Предупреждение для функций с ограниченной областью определения */}
                            {restrictedFunc && (
                                <div style={{
                                    padding: '12px',
                                    marginBottom: '15px',
                                    background: 'rgba(255, 193, 7, 0.1)',
                                    border: '1px solid rgba(255, 193, 7, 0.5)',
                                    borderRadius: '4px',
                                    color: 'var(--text)',
                                    fontSize: '14px'
                                }}>
                                    ⚠️ Диапазон ограничен областью определения функции {restrictedFunc === 'tan' ? 'tan' : 'ctg'}.
                                    <br />
                                    <span style={{ color: 'var(--muted)', fontSize: '12px' }}>
                                        {restrictedFunc === 'tan'
                                            ? 'tan(x) не определён при x = π/2 + πn'
                                            : 'ctg(x) не определён при x = πn (0, ±π, ±2π, ...)'
                                        }
                                    </span>
                                </div>
                            )}

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '10px', alignItems: 'end' }}>
                                <div>
                                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontSize: '14px' }}>
                                        x от:
                                    </label>
                                    <input
                                        type="number"
                                        value={restrictedFunc && safeRange ? safeRange.from : xFrom}
                                        onChange={(e) => !restrictedFunc && setXFrom(e.target.value)}
                                        disabled={!!restrictedFunc}
                                        step="any"
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            background: restrictedFunc ? 'var(--muted)' : 'var(--input-bg)',
                                            color: 'var(--input-text)',
                                            border: '1px solid var(--border)',
                                            opacity: restrictedFunc ? 0.7 : 1
                                        }}
                                    />
                                </div>
                                <div>
                                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontSize: '14px' }}>
                                        x до:
                                    </label>
                                    <input
                                        type="number"
                                        value={restrictedFunc && safeRange ? safeRange.to : xTo}
                                        onChange={(e) => !restrictedFunc && setXTo(e.target.value)}
                                        disabled={!!restrictedFunc}
                                        step="any"
                                        style={{
                                            width: '100%',
                                            padding: '8px',
                                            background: restrictedFunc ? 'var(--muted)' : 'var(--input-bg)',
                                            color: 'var(--input-text)',
                                            border: '1px solid var(--border)',
                                            opacity: restrictedFunc ? 0.7 : 1
                                        }}
                                    />
                                </div>
                                <button
                                    onClick={() => {
                                        // Для ограниченных функций используем безопасный диапазон
                                        if (restrictedFunc && safeRange) {
                                            setXFrom(safeRange.from);
                                            setXTo(safeRange.to);
                                        }
                                        updateGraphForAnalytic();
                                    }}
                                    disabled={loading}
                                    style={{
                                        padding: '8px 16px',
                                        background: 'var(--btn-bg)',
                                        color: 'var(--btn-text)',
                                        border: '1px solid var(--border)',
                                        whiteSpace: 'nowrap',
                                        opacity: loading ? 0.6 : 1,
                                        cursor: loading ? 'not-allowed' : 'pointer'
                                    }}
                                >
                                    {loading ? 'Загрузка...' : 'Обновить график'}
                                </button>
                            </div>
                        </div>
                    );
                })()}

                {/* Graph Parameters for COMPOSITE functions */}
                {functionData && functionData.summary.type === 'COMPOSITE' && (
                    <div>
                        <h3 style={{ color: 'var(--text)', marginTop: 0, marginBottom: '15px' }}>Параметры графика композитной функции</h3>

                        {/* Info about components */}
                        {functionData.components && functionData.components.length > 0 && (
                            <div style={{
                                padding: '12px',
                                marginBottom: '15px',
                                background: 'rgba(59, 130, 246, 0.1)',
                                border: '1px solid rgba(59, 130, 246, 0.5)',
                                borderRadius: '4px',
                                color: 'var(--text)',
                                fontSize: '14px'
                            }}>
                                ℹ️ Композитная функция из {functionData.components.length} компонент(ов).
                                <br />
                                <span style={{ color: 'var(--muted)', fontSize: '12px' }}>
                                    Вычисление: для каждого x применяются компоненты последовательно.
                                </span>
                            </div>
                        )}

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '10px', alignItems: 'end' }}>
                            <div>
                                <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontSize: '14px' }}>
                                    x от:
                                </label>
                                <input
                                    type="number"
                                    value={xFrom}
                                    onChange={(e) => setXFrom(e.target.value)}
                                    step="any"
                                    style={{
                                        width: '100%',
                                        padding: '8px',
                                        background: 'var(--input-bg)',
                                        color: 'var(--input-text)',
                                        border: '1px solid var(--border)'
                                    }}
                                />
                            </div>
                            <div>
                                <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontSize: '14px' }}>
                                    x до:
                                </label>
                                <input
                                    type="number"
                                    value={xTo}
                                    onChange={(e) => setXTo(e.target.value)}
                                    step="any"
                                    style={{
                                        width: '100%',
                                        padding: '8px',
                                        background: 'var(--input-bg)',
                                        color: 'var(--input-text)',
                                        border: '1px solid var(--border)'
                                    }}
                                />
                            </div>
                            <button
                                onClick={updateGraphForComposite}
                                disabled={compositeLoading}
                                style={{
                                    padding: '8px 16px',
                                    background: 'var(--btn-bg)',
                                    color: 'var(--btn-text)',
                                    border: '1px solid var(--border)',
                                    whiteSpace: 'nowrap',
                                    opacity: compositeLoading ? 0.6 : 1,
                                    cursor: compositeLoading ? 'not-allowed' : 'pointer'
                                }}
                            >
                                {compositeLoading ? 'Вычисление...' : 'Обновить график'}
                            </button>
                        </div>
                    </div>
                )}

                {/* Domain display for TABULATED functions */}
                {functionData && functionData.summary.type === 'TABULATED' && getDomainDisplay() && (
                    <div style={{
                        padding: '12px',
                        background: 'var(--card)',
                        border: '1px solid var(--border)',
                        borderRadius: '4px',
                        color: 'var(--text)',
                        fontSize: '14px',
                        fontWeight: 500
                    }}>
                        {getDomainDisplay()}
                    </div>
                )}

                {/* Graph */}
                {graphPoints.length > 0 && (
                    <div>
                        <h3 style={{ color: 'var(--text)', marginTop: 0, marginBottom: '15px' }}>График функции</h3>
                        <canvas
                            ref={canvasRef}
                            width={800}
                            height={500}
                            style={{
                                width: '100%',
                                maxWidth: '800px',
                                height: '500px',
                                border: '1px solid var(--border)',
                                borderRadius: '4px',
                                background: 'var(--card)',
                            }}
                        />
                    </div>
                )}

                {/* Calculate Value */}
                {functionData && (
                    <div>
                        <h3 style={{ color: 'var(--text)', marginTop: 0, marginBottom: '15px' }}>Вычислить значение функции</h3>
                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                            <label style={{ color: 'var(--text)', whiteSpace: 'nowrap' }}>f(</label>
                            <input
                                type="number"
                                value={xValue}
                                onChange={(e) => {
                                    setXValue(e.target.value);
                                    setCalculatedValue(null);
                                }}
                                placeholder="x"
                                step="any"
                                style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                            />
                            <label style={{ color: 'var(--text)', whiteSpace: 'nowrap' }}>) =</label>
                            <button
                                onClick={handleCalculateValue}
                                style={{ padding: '8px 16px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}
                            >
                                Вычислить
                            </button>
                        </div>
                        {calculatedValue !== null && (
                            <div style={{
                                marginTop: '10px',
                                padding: '12px',
                                background: 'var(--card)',
                                border: '1px solid var(--border)',
                                borderRadius: '4px',
                                color: 'var(--text)',
                                fontSize: '16px',
                                fontFamily: 'monospace'
                            }}>
                                f({xValue}) = {calculatedValue.toFixed(6)}
                            </div>
                        )}
                    </div>
                )}

                {!functionData && selectedFunctionId && loading && (
                    <div style={{ color: 'var(--text)', textAlign: 'center', padding: '20px' }}>
                        Загрузка функции...
                    </div>
                )}
            </div>
        </Modal>
    );
}
