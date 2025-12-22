import { useState, useEffect, useRef } from 'react';
import { Modal } from './Modal';
import { getJson } from '../api';
import { showError } from '../errorManager';
import { validateNumber, validateRange, validatePointCount } from '../utils/validation';

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

export function FunctionGraphModal({ isOpen, onClose }: FunctionGraphModalProps) {
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [selectedFunctionId, setSelectedFunctionId] = useState<number | null>(null);
    const [functionData, setFunctionData] = useState<FunctionFull | null>(null);
    const [graphPoints, setGraphPoints] = useState<Point[]>([]);
    const [loading, setLoading] = useState(false);
    
    // Graph parameters
    const [xFrom, setXFrom] = useState('-10');
    const [xTo, setXTo] = useState('10');
    const [numPoints, setNumPoints] = useState('100');
    
    // Value calculation
    const [xValue, setXValue] = useState('');
    const [calculatedValue, setCalculatedValue] = useState<number | null>(null);
    
    const canvasRef = useRef<HTMLCanvasElement>(null);

    useEffect(() => {
        if (isOpen) {
            loadFunctions();
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
            // Generate initial graph points
            updateGraph(data);
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    // Safe expression evaluator for ANALYTIC functions
    const safeEval = (expression: string, x: number): number => {
        try {
            // Replace x with the actual value
            let expr = expression.replace(/x/g, `(${x})`);
            
            // Support common math functions
            const mathContext: { [key: string]: any } = {
                Math: Math,
                sin: Math.sin,
                cos: Math.cos,
                tan: Math.tan,
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

    const updateGraph = (data: FunctionFull | null = functionData) => {
        if (!data) {
            setGraphPoints([]);
            return;
        }

        try {
            // Validate number of points
            const countError = validatePointCount(numPoints);
            if (countError) {
                showError(new Error(countError), true);
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
            const count = parseInt(numPoints);

            const points: Point[] = [];
            const step = (to - from) / (count - 1);

            if (data.summary.type === 'ANALYTIC' && data.analyticExpression) {
                // Generate points from analytic expression
                for (let i = 0; i < count; i++) {
                    const x = from + i * step;
                    try {
                        const y = safeEval(data.analyticExpression, x);
                        points.push({ x, y });
                    } catch (e) {
                        // Skip invalid points
                        console.warn(`Ошибка вычисления в точке x=${x}:`, e);
                    }
                }
            } else if (data.summary.type === 'TABULATED' && data.points && data.points.length > 0) {
                // Generate points using interpolation/extrapolation
                for (let i = 0; i < count; i++) {
                    const x = from + i * step;
                    try {
                        const y = interpolateTabulated(x, data.points);
                        points.push({ x, y });
                    } catch (e) {
                        console.warn(`Ошибка интерполяции в точке x=${x}:`, e);
                    }
                }
            } else if (data.summary.type === 'COMPOSITE') {
                // For composite functions, we'd need to evaluate components
                // For now, use points if available
                if (data.points && data.points.length > 0) {
                    for (let i = 0; i < count; i++) {
                        const x = from + i * step;
                        try {
                            const y = interpolateTabulated(x, data.points);
                            points.push({ x, y });
                        } catch (e) {
                            console.warn(`Ошибка интерполяции в точке x=${x}:`, e);
                        }
                    }
                } else {
                    showError(new Error('Составная функция не имеет точек для отображения'));
                    return;
                }
            } else {
                showError(new Error('Не удалось построить график для данного типа функции'));
                return;
            }

            setGraphPoints(points);
        } catch (e) {
            showError(e);
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

    const handleCalculateValue = () => {
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
            } else if (functionData.summary.type === 'COMPOSITE' && functionData.points) {
                result = interpolateTabulated(x, functionData.points);
            } else {
                showError(new Error('Не удалось вычислить значение для данного типа функции'));
                return;
            }

            setCalculatedValue(result);
        } catch (e) {
            showError(e);
            setCalculatedValue(null);
        }
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

                {/* Graph Parameters */}
                {functionData && (
                    <div>
                        <h3 style={{ color: 'var(--text)', marginTop: 0, marginBottom: '15px' }}>Параметры графика</h3>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr auto', gap: '10px', alignItems: 'end' }}>
                            <div>
                                <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontSize: '14px' }}>
                                    x от:
                                </label>
                                <input
                                    type="number"
                                    value={xFrom}
                                    onChange={(e) => setXFrom(e.target.value)}
                                    step="any"
                                    style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
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
                                    style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                />
                            </div>
                            <div>
                                <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontSize: '14px' }}>
                                    Количество точек:
                                </label>
                                <input
                                    type="number"
                                    value={numPoints}
                                    onChange={(e) => setNumPoints(e.target.value)}
                                    min="2"
                                    max="10000"
                                    style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                />
                            </div>
                            <button
                                onClick={() => updateGraph()}
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
