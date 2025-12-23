import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { FunctionTable } from './FunctionTable';
import { FunctionListModal } from './FunctionListModal';
import { getJson, postJson } from '../api';
import { showError, showSuccess } from '../errorManager';
import { validateFunctionName, validatePoints } from '../utils/validation';

interface Point {
    x: number;
    y: number | null;
}

interface DifferentiationModalProps {
    isOpen: boolean;
    onClose: () => void;
    factoryKey: 'array' | 'linked-list';
}

export function DifferentiationModal({ isOpen, onClose, factoryKey }: DifferentiationModalProps) {
    const [sourceFunction, setSourceFunction] = useState<Point[]>([]);
    const [result, setResult] = useState<Point[]>([]);
    const [showFunctionList, setShowFunctionList] = useState(false);

    // Сброс состояния при закрытии модального окна
    useEffect(() => {
        if (!isOpen) {
            setSourceFunction([]);
            setResult([]);
            setShowFunctionList(false);
        }
    }, [isOpen]);

    const handleLoadClick = () => {
        setShowFunctionList(true);
    };

    const handleFunctionSelect = async (functionId: number) => {
        try {
            const data = await getJson<{ summary: { id: number }; points: Point[] }>(`/api/v1/functions/${functionId}`);

            if (!data.points || !Array.isArray(data.points)) {
                showError(new Error('Функция не является табулированной'), true);
                return;
            }

            const points: Point[] = data.points.map((p: { x: number; y: number | null }) => ({
                x: p.x,
                y: p.y ?? null,
            }));

            setSourceFunction(points);
            setResult([]);
        } catch (e) {
            showError(e, true);
        }
    };

    const computeNumericalDerivative = (points: Point[]): Point[] => {
        // Filter out points with null y values
        const validPoints = points.filter(p => p.y !== null && p.y !== undefined);

        if (validPoints.length < 2) {
            throw new Error('Необходимо минимум 2 точки с валидными значениями y');
        }

        const n = validPoints.length;
        const derivative: Point[] = [];

        // First point: forward difference
        const dx1 = validPoints[1].x - validPoints[0].x;
        if (Math.abs(dx1) < 1e-10) {
            throw new Error('Интервалы между точками слишком малы');
        }
        const dy1 = (validPoints[1].y as number) - (validPoints[0].y as number);
        derivative.push({
            x: validPoints[0].x,
            y: dy1 / dx1,
        });

        // Inner points: central difference
        for (let i = 1; i < n - 1; i++) {
            const dxPrev = validPoints[i].x - validPoints[i - 1].x;
            const dxNext = validPoints[i + 1].x - validPoints[i].x;

            if (Math.abs(dxPrev) < 1e-10 || Math.abs(dxNext) < 1e-10) {
                throw new Error('Интервалы между точками слишком малы');
            }

            // Central difference: (y[i+1] - y[i-1]) / (x[i+1] - x[i-1])
            const dy = (validPoints[i + 1].y as number) - (validPoints[i - 1].y as number);
            const dx = validPoints[i + 1].x - validPoints[i - 1].x;
            derivative.push({
                x: validPoints[i].x,
                y: dy / dx,
            });
        }

        // Last point: backward difference
        const dxLast = validPoints[n - 1].x - validPoints[n - 2].x;
        if (Math.abs(dxLast) < 1e-10) {
            throw new Error('Интервалы между точками слишком малы');
        }
        const dyLast = (validPoints[n - 1].y as number) - (validPoints[n - 2].y as number);
        derivative.push({
            x: validPoints[n - 1].x,
            y: dyLast / dxLast,
        });

        return derivative;
    };

    const handleInsertPoint = () => {
        if (sourceFunction.length === 0) {
            showError(new Error('Сначала создайте или загрузите функцию'), true);
            return;
        }

        const xStr = prompt('Введите значение x:');
        if (!xStr) return;
        const x = parseFloat(xStr);
        if (isNaN(x)) {
            showError(new Error('x должно быть числом'), true);
            return;
        }

        const yStr = prompt('Введите значение y:');
        if (!yStr) return;
        const y = parseFloat(yStr);
        if (isNaN(y)) {
            showError(new Error('y должно быть числом'), true);
            return;
        }

        // Insert point maintaining sorted order
        const newPoint: Point = { x, y: y ?? null };
        const newPoints = [...sourceFunction, newPoint].sort((a, b) => a.x - b.x);
        setSourceFunction(newPoints);
        setResult([]);
    };

    const handleRemovePoint = (index: number) => {
        if (sourceFunction.length <= 2) {
            showError(new Error('Функция должна содержать минимум 2 точки'), true);
            return;
        }

        if (confirm(`Удалить точку x=${sourceFunction[index].x}, y=${sourceFunction[index].y}?`)) {
            const newPoints = sourceFunction.filter((_, i) => i !== index);
            setSourceFunction(newPoints);
            setResult([]);
        }
    };

    const handleDifferentiate = () => {
        try {
            // Filter out null y values for validation
            const validPoints = sourceFunction.filter(p => p.y !== null && p.y !== undefined) as Array<{ x: number; y: number }>;

            if (validPoints.length < 2) {
                showError(new Error('Нужно минимум 2 точки с валидными значениями y'), true);
                return;
            }

            // Validate points
            const pointsError = validatePoints(validPoints);
            if (pointsError) {
                showError(new Error(pointsError), true);
                return;
            }

            // Compute derivative using numerical differentiation locally
            // The factory selection (array vs linked-list) is noted but doesn't affect
            // the numerical differentiation algorithm itself
            const derivative = computeNumericalDerivative(sourceFunction);

            setResult(derivative);
        } catch (e) {
            showError(e instanceof Error ? e : new Error('Ошибка при дифференцировании'), true);
        }
    };

    const handleSaveResult = async () => {
        if (result.length === 0) {
            showError(new Error('Нет результата для сохранения'), true);
            return;
        }

        try {
            const name = prompt('Введите имя для новой функции:');
            if (!name) return;

            // Validate name
            const nameError = validateFunctionName(name);
            if (nameError) {
                showError(new Error(nameError), true);
                return;
            }

            // Filter out null y values and convert to valid points
            const validPoints = result
                .filter(p => p.y !== null && p.y !== undefined && !isNaN(p.y as number))
                .map(p => ({ x: p.x, y: p.y as number }));

            if (validPoints.length < 2) {
                showError(new Error('Результат должен содержать минимум 2 валидные точки'), true);
                return;
            }

            // Validate points
            const pointsError = validatePoints(validPoints);
            if (pointsError) {
                showError(new Error(pointsError), true);
                return;
            }

            // Save to backend
            const created = await postJson<{ summary: { id: number; name: string } }>(
                '/api/v1/functions/tabulated/manual',
                {
                    name: name.trim(),
                    points: validPoints,
                }
            );

            if (!created || !created.summary) {
                throw new Error('Не удалось сохранить функцию');
            }

            showSuccess(`Функция "${created.summary.name}" успешно создана (ID: ${created.summary.id})`);
        } catch (e) {
            showError(e instanceof Error ? e : new Error(String(e)), true);
        }
    };

    return (
        <>
            <FunctionListModal
                isOpen={showFunctionList}
                onClose={() => setShowFunctionList(false)}
                onSelect={handleFunctionSelect}
                filterType="TABULATED"
            />
            <Modal isOpen={isOpen} onClose={onClose} title="Дифференцирование">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {/* Source Function */}
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Исходная функция</h3>
                        <div style={{ fontSize: '12px', color: 'var(--muted)', marginBottom: '8px' }}>
                            Фабрика: {factoryKey === 'array' ? 'Массив' : 'Связный список'}
                        </div>
                        <FunctionTable
                            points={sourceFunction}
                            onPointsChange={setSourceFunction}
                            readonly={false}
                            onRemovePoint={sourceFunction.length > 2 ? handleRemovePoint : undefined}
                        />
                        <div style={{ marginTop: '10px', display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                            <button
                                onClick={handleLoadClick}
                                style={{
                                    padding: '5px 10px',
                                    background: 'var(--btn2-bg)',
                                    color: 'var(--btn2-text)',
                                    border: '1px solid var(--border)',
                                }}
                            >
                                Загрузить
                            </button>
                            {sourceFunction.length > 0 && (
                                <button
                                    onClick={handleInsertPoint}
                                    style={{
                                        padding: '5px 10px',
                                        background: 'var(--btn-bg)',
                                        color: 'var(--btn-text)',
                                        border: '1px solid var(--border)',
                                    }}
                                >
                                    Вставить точку
                                </button>
                            )}
                            <button
                                onClick={handleDifferentiate}
                                style={{
                                    padding: '5px 10px',
                                    background: 'var(--btn-bg)',
                                    color: 'var(--btn-text)',
                                    border: '1px solid var(--border)',
                                }}
                            >
                                Дифференцировать
                            </button>
                        </div>
                    </div>

                    {/* Result */}
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Результат</h3>
                        <FunctionTable points={result} onPointsChange={() => { }} readonly={true} />
                        {result.length > 0 && (
                            <div style={{ marginTop: '10px' }}>
                                <button
                                    onClick={handleSaveResult}
                                    style={{
                                        padding: '8px 16px',
                                        background: 'var(--btn-bg)',
                                        color: 'var(--btn-text)',
                                        border: '1px solid var(--border)',
                                    }}
                                >
                                    Сохранить результат как новую функцию
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </Modal>
        </>
    );
}
