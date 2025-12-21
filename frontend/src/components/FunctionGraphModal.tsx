import { useState, useEffect, useRef } from 'react';
import { Modal } from './Modal';
import { FunctionTable } from './FunctionTable';
import { getJson, postJson } from '../api';
import { showError } from '../errorManager';

interface Point {
    x: number;
    y: number;
}

interface FunctionFull {
    summary: {
        id: number;
        name: string;
        type: string;
    };
    points: Point[];
}

interface FunctionGraphModalProps {
    isOpen: boolean;
    onClose: () => void;
    functionId?: number;
}

export function FunctionGraphModal({ isOpen, onClose, functionId }: FunctionGraphModalProps) {
    const [functionData, setFunctionData] = useState<FunctionFull | null>(null);
    const [points, setPoints] = useState<Point[]>([]);
    const [loading, setLoading] = useState(false);
    const [xValue, setXValue] = useState('');
    const [calculatedValue, setCalculatedValue] = useState<number | null>(null);
    const [selectedFunctionId, setSelectedFunctionId] = useState<number | null>(functionId || null);
    const canvasRef = useRef<HTMLCanvasElement>(null);

    useEffect(() => {
        if (isOpen && selectedFunctionId) {
            loadFunction();
        }
    }, [isOpen, selectedFunctionId]);

    useEffect(() => {
        if (points.length > 0 && canvasRef.current) {
            drawGraph();
        }
    }, [points]);

    const loadFunction = async () => {
        if (!selectedFunctionId) return;
        try {
            setLoading(true);
            const data = await getJson<FunctionFull>(`/api/v1/functions/${selectedFunctionId}`);
            setFunctionData(data);
            setPoints(data.points || []);
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    const drawGraph = () => {
        const canvas = canvasRef.current;
        if (!canvas || points.length === 0) return;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        const width = canvas.width;
        const height = canvas.height;
        const padding = 40;

        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--card') || '#fff';
        ctx.fillRect(0, 0, width, height);

        // Find min/max values
        const xValues = points.map(p => p.x);
        const yValues = points.map(p => p.y);
        const minX = Math.min(...xValues);
        const maxX = Math.max(...xValues);
        const minY = Math.min(...yValues);
        const maxY = Math.max(...yValues);

        const rangeX = maxX - minX || 1;
        const rangeY = maxY - minY || 1;

        const plotWidth = width - 2 * padding;
        const plotHeight = height - 2 * padding;

        // Draw axes
        ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--border') || '#ccc';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(padding, padding);
        ctx.lineTo(padding, height - padding);
        ctx.lineTo(width - padding, height - padding);
        ctx.stroke();

        // Draw grid
        ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--border') || '#ddd';
        ctx.lineWidth = 0.5;
        for (let i = 0; i <= 10; i++) {
            const x = padding + (i / 10) * plotWidth;
            ctx.beginPath();
            ctx.moveTo(x, padding);
            ctx.lineTo(x, height - padding);
            ctx.stroke();

            const y = height - padding - (i / 10) * plotHeight;
            ctx.beginPath();
            ctx.moveTo(padding, y);
            ctx.lineTo(width - padding, y);
            ctx.stroke();
        }

        // Draw function line
        ctx.strokeStyle = getComputedStyle(document.documentElement).getPropertyValue('--btn-bg') || '#111';
        ctx.lineWidth = 2;
        ctx.beginPath();

        points.forEach((point, index) => {
            const x = padding + ((point.x - minX) / rangeX) * plotWidth;
            const y = height - padding - ((point.y - minY) / rangeY) * plotHeight;

            if (index === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });

        ctx.stroke();

        // Draw points
        ctx.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--btn-bg') || '#111';
        points.forEach(point => {
            const x = padding + ((point.x - minX) / rangeX) * plotWidth;
            const y = height - padding - ((point.y - minY) / rangeY) * plotHeight;
            ctx.beginPath();
            ctx.arc(x, y, 4, 0, 2 * Math.PI);
            ctx.fill();
        });

        // Draw labels
        ctx.fillStyle = getComputedStyle(document.documentElement).getPropertyValue('--text') || '#000';
        ctx.font = '12px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(minX.toFixed(2), padding, height - padding + 20);
        ctx.fillText(maxX.toFixed(2), width - padding, height - padding + 20);
        ctx.textAlign = 'left';
        ctx.fillText(maxY.toFixed(2), 5, padding + 5);
        ctx.fillText(minY.toFixed(2), 5, height - padding + 5);
    };

    const handleCalculateValue = async () => {
        if (!selectedFunctionId || !xValue.trim()) {
            showError(new Error('Введите значение x'));
            return;
        }

        try {
            const x = parseFloat(xValue);
            if (isNaN(x)) {
                showError(new Error('Значение x должно быть числом'));
                return;
            }

            // Calculate using interpolation (linear interpolation between points)
            if (points.length === 0) {
                showError(new Error('Функция не загружена'));
                return;
            }

            // Simple linear interpolation
            let result = 0;
            if (x <= points[0].x) {
                result = points[0].y;
            } else if (x >= points[points.length - 1].x) {
                result = points[points.length - 1].y;
            } else {
                for (let i = 0; i < points.length - 1; i++) {
                    if (x >= points[i].x && x <= points[i + 1].x) {
                        const t = (x - points[i].x) / (points[i + 1].x - points[i].x);
                        result = points[i].y + t * (points[i + 1].y - points[i].y);
                        break;
                    }
                }
            }

            setCalculatedValue(result);
        } catch (e) {
            showError(e);
        }
    };

    const handleLoad = async () => {
        try {
            const input = prompt('Введите ID функции для загрузки:');
            if (!input) return;
            const id = Number(input);
            if (isNaN(id)) {
                showError(new Error('Неверный ID функции'));
                return;
            }
            setSelectedFunctionId(id);
        } catch (e) {
            showError(e);
        }
    };

    const handleSave = () => {
        if (!functionData) {
            showError(new Error('Нет функции для сохранения'));
            return;
        }

        try {
            const name = prompt('Введите имя для сохранения функции:');
            if (!name) return;

            const data = {
                name,
                type: 'TABULATED',
                points: points,
            };

            const json = JSON.stringify(data, null, 2);
            const blob = new Blob([json], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `${name.replace(/[^a-zA-Z0-9]/g, '_')}.json`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
        } catch (e) {
            showError(e);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="График функции">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {/* Function Selection */}
                <div>
                    <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                        <input
                            type="number"
                            placeholder="ID функции"
                            value={selectedFunctionId || ''}
                            onChange={(e) => setSelectedFunctionId(e.target.value ? Number(e.target.value) : null)}
                            style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        />
                        <button
                            onClick={loadFunction}
                            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            disabled={loading || !selectedFunctionId}
                        >
                            {loading ? 'Загрузка...' : 'Загрузить'}
                        </button>
                        <button
                            onClick={handleLoad}
                            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            Выбрать из списка
                        </button>
                        <button
                            onClick={handleSave}
                            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            disabled={!functionData}
                        >
                            Сохранить
                        </button>
                    </div>
                    {functionData && (
                        <div style={{ color: 'var(--text)', marginBottom: '10px' }}>
                            Функция: {functionData.summary.name} (ID: {functionData.summary.id}, Тип: {functionData.summary.type})
                        </div>
                    )}
                </div>

                {/* Graph */}
                {points.length > 0 && (
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>График функции</h3>
                        <canvas
                            ref={canvasRef}
                            width={800}
                            height={400}
                            style={{
                                width: '100%',
                                maxWidth: '800px',
                                height: '400px',
                                border: '1px solid var(--border)',
                                borderRadius: '4px',
                                background: 'var(--card)',
                            }}
                        />
                    </div>
                )}

                {/* Calculate Value */}
                <div>
                    <h3 style={{ color: 'var(--text)' }}>Вычислить значение функции</h3>
                    <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                        <label style={{ color: 'var(--text)' }}>x =</label>
                        <input
                            type="number"
                            value={xValue}
                            onChange={(e) => setXValue(e.target.value)}
                            placeholder="Введите значение x"
                            style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        />
                        <button
                            onClick={handleCalculateValue}
                            style={{ padding: '8px 16px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}
                            disabled={!functionData}
                        >
                            Вычислить
                        </button>
                    </div>
                    {calculatedValue !== null && (
                        <div style={{ marginTop: '10px', padding: '10px', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '4px', color: 'var(--text)' }}>
                            f({xValue}) = {calculatedValue.toFixed(6)}
                        </div>
                    )}
                </div>

                {/* Points Table */}
                <div>
                    <h3 style={{ color: 'var(--text)' }}>Точки функции</h3>
                    <FunctionTable
                        points={points}
                        onPointsChange={() => {}}
                        readonly={true}
                    />
                </div>
            </div>
        </Modal>
    );
}

