import { useState } from 'react';
import { Modal } from './Modal';
import { FunctionTable } from './FunctionTable';
import { CreateFromArrayModal } from './CreateFromArrayModal';
import { CreateFromFunctionModal } from './CreateFromFunctionModal';
import { postJson } from '../api';

interface Point {
    x: number;
    y: number;
}

interface FunctionData {
    id: number;
    points: Point[];
}

interface OperationsModalProps {
    isOpen: boolean;
    onClose: () => void;
    factoryKey: string;
}

export function OperationsModal({ isOpen, onClose, factoryKey }: OperationsModalProps) {
    const [leftOperand, setLeftOperand] = useState<FunctionData | null>(null);
    const [rightOperand, setRightOperand] = useState<FunctionData | null>(null);
    const [result, setResult] = useState<Point[]>([]);

    const [showCreateArray, setShowCreateArray] = useState<'left' | 'right' | null>(null);
    const [showCreateFunction, setShowCreateFunction] = useState<'left' | 'right' | null>(null);

    const handleCreateFromArray = (operand: 'left' | 'right') => {
        setShowCreateArray(operand);
    };

    const handleCreateFromFunction = (operand: 'left' | 'right') => {
        setShowCreateFunction(operand);
    };

    const handleCreated = (operand: 'left' | 'right', functionId: number, points: Point[]) => {
        const funcData: FunctionData = { id: functionId, points };
        if (operand === 'left') {
            setLeftOperand(funcData);
        } else {
            setRightOperand(funcData);
        }
        setShowCreateArray(null);
        setShowCreateFunction(null);
    };

    const handleLoad = async (operand: 'left' | 'right') => {
        try {
            const input = prompt('Введите ID функции для загрузки:');
            if (!input) return;
            const id = Number(input);
            if (isNaN(id)) {
                alert('Неверный ID функции');
                return;
            }

            const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/v1/functions/${id}`);
            if (!response.ok) {
                throw new Error('Не удалось загрузить функцию');
            }
            const data = await response.json();

            if (data.points && Array.isArray(data.points)) {
                const points: Point[] = data.points.map((p: { x: number; y: number }) => ({
                    x: p.x,
                    y: p.y,
                }));
                const funcData: FunctionData = { id: data.summary.id, points };
                if (operand === 'left') {
                    setLeftOperand(funcData);
                } else {
                    setRightOperand(funcData);
                }
            } else {
                alert('Функция не является табулированной');
            }
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    const handleSave = async (operand: 'left' | 'right') => {
        try {
            const funcData = operand === 'left' ? leftOperand : rightOperand;
            if (!funcData) {
                alert('Нет функции для сохранения');
                return;
            }

            const name = prompt('Введите имя для сохранения функции:');
            if (!name) return;

            const payload = { name, points: funcData.points };
            const result = await postJson<{ summary: { id: number } }>(
                '/api/v1/functions/tabulated/manual',
                payload
            );
            alert(`Функция сохранена с ID: ${result.summary.id}`);
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    const handlePointsChange = (operand: 'left' | 'right', newPoints: Point[]) => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        if (!funcData) return;

        // Update Y values only (X values are readonly)
        const updatedPoints = funcData.points.map((p, i) => ({
            x: p.x,
            y: i < newPoints.length ? newPoints[i].y : p.y,
        }));

        const updatedFuncData = { ...funcData, points: updatedPoints };
        if (operand === 'left') {
            setLeftOperand(updatedFuncData);
        } else {
            setRightOperand(updatedFuncData);
        }
    };

    const handleInsertPoint = (operand: 'left' | 'right') => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        if (!funcData) {
            alert('Нет функции для вставки точки');
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
        const newPoints = [...funcData.points, newPoint].sort((a, b) => a.x - b.x);

        const updatedFuncData = { ...funcData, points: newPoints };
        if (operand === 'left') {
            setLeftOperand(updatedFuncData);
        } else {
            setRightOperand(updatedFuncData);
        }
    };

    const handleRemovePoint = (operand: 'left' | 'right', index: number) => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        if (!funcData) return;

        if (funcData.points.length <= 2) {
            alert('Функция должна содержать минимум 2 точки');
            return;
        }

        if (confirm(`Удалить точку x=${funcData.points[index].x}, y=${funcData.points[index].y}?`)) {
            const newPoints = funcData.points.filter((_, i) => i !== index);
            const updatedFuncData = { ...funcData, points: newPoints };
            if (operand === 'left') {
                setLeftOperand(updatedFuncData);
            } else {
                setRightOperand(updatedFuncData);
            }
        }
    };

    const handleSaveJSON = (operand: 'left' | 'right') => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        if (!funcData) {
            alert('Нет функции для сохранения');
            return;
        }

        try {
            const name = prompt('Введите имя файла:');
            if (!name) return;

            const data = {
                name: `function_${operand}`,
                type: 'TABULATED',
                points: funcData.points,
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
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    const handleLoadJSON = async (operand: 'left' | 'right') => {
        try {
            const input = document.createElement('input');
            input.type = 'file';
            input.accept = '.json';
            input.onchange = async (e) => {
                const file = (e.target as HTMLInputElement).files?.[0];
                if (!file) return;

                try {
                    const text = await file.text();
                    const data = JSON.parse(text);

                    if (!data || !Array.isArray(data.points)) {
                        throw new Error('Неверный формат файла');
                    }

                    const points: Point[] = data.points.map((p: any) => ({
                        x: p.x,
                        y: p.y,
                    }));

                    if (points.length < 2) {
                        throw new Error('Функция должна содержать минимум 2 точки');
                    }

                    // Create a temporary ID for loaded function
                    const funcData: FunctionData = { id: 0, points };
                    if (operand === 'left') {
                        setLeftOperand(funcData);
                    } else {
                        setRightOperand(funcData);
                    }
                } catch (e) {
                    alert(e instanceof Error ? e.message : String(e));
                }
            };
            input.click();
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    // Check if function supports insert/remove (tabulated functions typically do)
    const isInsertable = (operand: 'left' | 'right') => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        return funcData !== null && funcData.points.length > 0;
    };

    const isRemovable = (operand: 'left' | 'right') => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        return funcData !== null && funcData.points.length > 2;
    };

    const handleOperation = async (operation: '+' | '-' | '×' | '÷') => {
        try {
            if (!leftOperand || !rightOperand) {
                alert('Необходимо загрузить оба операнда');
                return;
            }

            if (leftOperand.points.length !== rightOperand.points.length) {
                alert('Функции должны иметь одинаковое количество точек');
                return;
            }

            // Check if X values match
            for (let i = 0; i < leftOperand.points.length; i++) {
                if (Math.abs(leftOperand.points[i].x - rightOperand.points[i].x) > 1e-9) {
                    alert('X значения функций не совпадают');
                    return;
                }
            }

            const opMap: Record<string, string> = {
                '+': 'sum',
                '-': 'subtract',
                '×': 'multiplication',
                '÷': 'division',
            };

            const op = opMap[operation];
            const payload = {
                leftId: leftOperand.id,
                rightId: rightOperand.id,
            };

            const resultData = await postJson<{ summary: { id: number }; points: Point[] }>(
                `/api/v1/operations/${op}`,
                payload
            );

            setResult(resultData.points || []);
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    return (
        <>
            <Modal isOpen={isOpen} onClose={onClose} title="Поэлементные операции">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {/* Left Operand */}
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Левый операнд</h3>
                        <FunctionTable
                            points={leftOperand?.points || []}
                            onPointsChange={(points) => handlePointsChange('left', points)}
                            readonly={false}
                            onRemovePoint={isRemovable('left') ? (index) => handleRemovePoint('left', index) : undefined}
                        />
                        <div style={{ marginTop: '10px', display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                            <button onClick={() => handleCreateFromArray('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Создать из массива
                            </button>
                            <button onClick={() => handleCreateFromFunction('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Создать из другой функции
                            </button>
                            <button onClick={() => handleLoad('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Загрузить
                            </button>
                            <button onClick={() => handleLoadJSON('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Загрузить JSON
                            </button>
                            <button onClick={() => handleSave('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Сохранить в БД
                            </button>
                            <button onClick={() => handleSaveJSON('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Сохранить JSON
                            </button>
                            {isInsertable('left') && (
                                <button onClick={() => handleInsertPoint('left')} style={{ padding: '5px 10px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}>
                                    Вставить точку
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Right Operand */}
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Правый операнд</h3>
                        <FunctionTable
                            points={rightOperand?.points || []}
                            onPointsChange={(points) => handlePointsChange('right', points)}
                            readonly={false}
                            onRemovePoint={isRemovable('right') ? (index) => handleRemovePoint('right', index) : undefined}
                        />
                        <div style={{ marginTop: '10px', display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                            <button onClick={() => handleCreateFromArray('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Создать из массива
                            </button>
                            <button onClick={() => handleCreateFromFunction('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Создать из другой функции
                            </button>
                            <button onClick={() => handleLoad('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Загрузить
                            </button>
                            <button onClick={() => handleLoadJSON('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Загрузить JSON
                            </button>
                            <button onClick={() => handleSave('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Сохранить в БД
                            </button>
                            <button onClick={() => handleSaveJSON('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Сохранить JSON
                            </button>
                            {isInsertable('right') && (
                                <button onClick={() => handleInsertPoint('right')} style={{ padding: '5px 10px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}>
                                    Вставить точку
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Result */}
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Результат</h3>
                        <FunctionTable points={result} onPointsChange={() => { }} readonly={true} />
                        <div style={{ marginTop: '10px', display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                            <button onClick={() => handleOperation('+')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                +
                            </button>
                            <button onClick={() => handleOperation('-')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                −
                            </button>
                            <button onClick={() => handleOperation('×')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                ×
                            </button>
                            <button onClick={() => handleOperation('÷')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                ÷
                            </button>
                        </div>
                    </div>
                </div>
            </Modal>

            <CreateFromArrayModal
                isOpen={showCreateArray === 'left'}
                onClose={() => setShowCreateArray(null)}
                onCreated={(id, points) => handleCreated('left', id, points)}
                factoryKey={factoryKey}
            />
            <CreateFromArrayModal
                isOpen={showCreateArray === 'right'}
                onClose={() => setShowCreateArray(null)}
                onCreated={(id, points) => handleCreated('right', id, points)}
                factoryKey={factoryKey}
            />
            <CreateFromFunctionModal
                isOpen={showCreateFunction === 'left'}
                onClose={() => setShowCreateFunction(null)}
                onCreated={(id, points) => handleCreated('left', id, points)}
                factoryKey={factoryKey}
            />
            <CreateFromFunctionModal
                isOpen={showCreateFunction === 'right'}
                onClose={() => setShowCreateFunction(null)}
                onCreated={(id, points) => handleCreated('right', id, points)}
                factoryKey={factoryKey}
            />
        </>
    );
}
