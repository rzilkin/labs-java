import { useState, useRef } from 'react';
import { Modal } from './Modal';
import { FunctionTable } from './FunctionTable';

interface Point {
    x: number;
    y: number;
}

interface DifferentiationModalProps {
    isOpen: boolean;
    onClose: () => void;
    factoryKey: 'array' | 'linked-list';
}

export function DifferentiationModal({ isOpen, onClose, factoryKey }: DifferentiationModalProps) {
    const [sourceFunction, setSourceFunction] = useState<Point[]>([]);
    const [result, setResult] = useState<Point[]>([]);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleCreate = () => {
        try {
            const countStr = prompt('Введите количество точек (минимум 2):');
            if (!countStr) return;

            const count = parseInt(countStr, 10);
            if (isNaN(count) || count < 2) {
                alert('Количество точек должно быть числом не менее 2');
                return;
            }

            if (count > 500) {
                alert('Максимум 500 точек');
                return;
            }

            const newPoints: Point[] = Array.from({ length: count }, (_, i) => ({
                x: i,
                y: 0,
            }));

            setSourceFunction(newPoints);
            setResult([]);
        } catch (e) {
            alert(e instanceof Error ? e.message : 'Ошибка при создании функции');
        }
    };

    const handleLoad = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        try {
            const text = await file.text();
            const data = JSON.parse(text);

            if (!data || !Array.isArray(data.points)) {
                throw new Error('Неверный формат файла. Ожидается объект с полем points');
            }

            const points: Point[] = data.points.map((p: any) => {
                if (typeof p.x !== 'number' || typeof p.y !== 'number') {
                    throw new Error('Точки должны содержать числовые поля x и y');
                }
                return { x: p.x, y: p.y };
            });

            if (points.length < 2) {
                throw new Error('Функция должна содержать минимум 2 точки');
            }

            // Check if x values are sorted
            for (let i = 1; i < points.length; i++) {
                if (points[i].x <= points[i - 1].x) {
                    throw new Error('Значения x должны быть строго возрастающими');
                }
            }

            setSourceFunction(points);
            setResult([]);
        } catch (e) {
            alert(e instanceof Error ? e.message : 'Ошибка при загрузке файла');
        } finally {
            // Reset file input
            if (event.target) {
                event.target.value = '';
            }
        }
    };

    const handleSave = () => {
        try {
            if (sourceFunction.length === 0) {
                alert('Нет функции для сохранения');
                return;
            }

            const name = prompt('Введите имя функции:');
            if (!name) return;

            const data = {
                name,
                type: 'TABULATED',
                points: sourceFunction,
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
            alert(e instanceof Error ? e.message : 'Ошибка при сохранении функции');
        }
    };

    const computeNumericalDerivative = (points: Point[]): Point[] => {
        const n = points.length;
        const derivative: Point[] = [];

        if (n < 2) {
            return [];
        }

        // First point: forward difference
        if (n >= 2) {
            const dx = points[1].x - points[0].x;
            if (Math.abs(dx) < 1e-10) {
                throw new Error('Интервалы между точками слишком малы');
            }
            const dy = points[1].y - points[0].y;
            derivative.push({
                x: points[0].x,
                y: dy / dx,
            });
        }

        // Inner points: central difference
        for (let i = 1; i < n - 1; i++) {
            const dxPrev = points[i].x - points[i - 1].x;
            const dxNext = points[i + 1].x - points[i].x;

            if (Math.abs(dxPrev) < 1e-10 || Math.abs(dxNext) < 1e-10) {
                throw new Error('Интервалы между точками слишком малы');
            }

            // Central difference: (y[i+1] - y[i-1]) / (x[i+1] - x[i-1])
            const dy = points[i + 1].y - points[i - 1].y;
            const dx = points[i + 1].x - points[i - 1].x;
            derivative.push({
                x: points[i].x,
                y: dy / dx,
            });
        }

        // Last point: backward difference
        if (n >= 2) {
            const dx = points[n - 1].x - points[n - 2].x;
            if (Math.abs(dx) < 1e-10) {
                throw new Error('Интервалы между точками слишком малы');
            }
            const dy = points[n - 1].y - points[n - 2].y;
            derivative.push({
                x: points[n - 1].x,
                y: dy / dx,
            });
        }

        return derivative;
    };

    const handleInsertPoint = () => {
        if (sourceFunction.length === 0) {
            alert('Сначала создайте или загрузите функцию');
            return;
        }

        const xStr = prompt('Введите значение x:');
        if (!xStr) return;
        const x = parseFloat(xStr);
        if (isNaN(x)) {
            alert('x должно быть числом');
            return;
        }

        const yStr = prompt('Введите значение y:');
        if (!yStr) return;
        const y = parseFloat(yStr);
        if (isNaN(y)) {
            alert('y должно быть числом');
            return;
        }

        // Insert point maintaining sorted order
        const newPoint: Point = { x, y };
        const newPoints = [...sourceFunction, newPoint].sort((a, b) => a.x - b.x);
        setSourceFunction(newPoints);
        setResult([]);
    };

    const handleRemovePoint = (index: number) => {
        if (sourceFunction.length <= 2) {
            alert('Функция должна содержать минимум 2 точки');
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
            if (sourceFunction.length < 2) {
                alert('Нужно минимум 2 точки');
                return;
            }

            // Check if x values are sorted
            for (let i = 1; i < sourceFunction.length; i++) {
                if (sourceFunction[i].x <= sourceFunction[i - 1].x) {
                    alert('Значения x должны быть строго возрастающими');
                    return;
                }
            }

            // Compute derivative using numerical differentiation
            // The factory selection (array vs linked-list) is noted but doesn't affect
            // the numerical differentiation algorithm itself
            const derivative = computeNumericalDerivative(sourceFunction);

            setResult(derivative);
        } catch (e) {
            alert(e instanceof Error ? e.message : 'Ошибка при дифференцировании');
        }
    };

    return (
        <>
            <input
                ref={fileInputRef}
                type="file"
                accept=".json,application/json"
                style={{ display: 'none' }}
                onChange={handleFileChange}
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
                                onClick={handleCreate}
                                style={{
                                    padding: '5px 10px',
                                    background: 'var(--btn2-bg)',
                                    color: 'var(--btn2-text)',
                                    border: '1px solid var(--border)',
                                }}
                            >
                                Создать
                            </button>
                            <button
                                onClick={handleLoad}
                                style={{
                                    padding: '5px 10px',
                                    background: 'var(--btn2-bg)',
                                    color: 'var(--btn2-text)',
                                    border: '1px solid var(--border)',
                                }}
                            >
                                Загрузить
                            </button>
                            <button
                                onClick={handleSave}
                                style={{
                                    padding: '5px 10px',
                                    background: 'var(--btn2-bg)',
                                    color: 'var(--btn2-text)',
                                    border: '1px solid var(--border)',
                                }}
                            >
                                Сохранить
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
                    </div>
                </div>
            </Modal>
        </>
    );
}
