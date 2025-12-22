import { useState } from 'react';
import { Modal } from './Modal';
import { FunctionTable } from './FunctionTable';
import { FunctionListModal } from './FunctionListModal';
import { getJson, postJson } from '../api';
import { showError, showSuccess } from '../errorManager';
import { validatePoints, validateFunctionName } from '../utils/validation';

interface Point {
    x: number;
    y: number | null;
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
    const [showFunctionList, setShowFunctionList] = useState<'left' | 'right' | null>(null);

    const handleLoadClick = (operand: 'left' | 'right') => {
        setShowFunctionList(operand);
    };

    const handleFunctionSelect = async (functionId: number) => {
        const operand = showFunctionList;
        if (!operand) return;

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

            const funcData: FunctionData = { id: data.summary.id, points };
            if (operand === 'left') {
                setLeftOperand(funcData);
            } else {
                setRightOperand(funcData);
            }
            // Clear result when loading new operand
            setResult([]);
        } catch (e) {
            showError(e, true);
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
            showError(new Error('Нет функции для вставки точки'), true);
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
            showError(new Error('Функция должна содержать минимум 2 точки'), true);
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


    // Check if function supports insert/remove (tabulated functions typically do)
    const isInsertable = (operand: 'left' | 'right') => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        return funcData !== null && funcData.points.length > 0;
    };

    const isRemovable = (operand: 'left' | 'right') => {
        const funcData = operand === 'left' ? leftOperand : rightOperand;
        return funcData !== null && funcData.points.length > 2;
    };

    const handleOperation = (operation: '+' | '-' | '×' | '÷') => {
        try {
            if (!leftOperand || !rightOperand) {
                showError(new Error('Необходимо загрузить оба операнда'), true);
                return;
            }

            // Filter out null y values for validation
            const leftValidPoints = leftOperand.points.filter(p => p.y !== null && p.y !== undefined) as Array<{ x: number; y: number }>;
            const rightValidPoints = rightOperand.points.filter(p => p.y !== null && p.y !== undefined) as Array<{ x: number; y: number }>;

            // Validate points
            const leftPointsError = validatePoints(leftValidPoints);
            if (leftPointsError) {
                showError(new Error(`Левый операнд: ${leftPointsError}`), true);
                return;
            }

            const rightPointsError = validatePoints(rightValidPoints);
            if (rightPointsError) {
                showError(new Error(`Правый операнд: ${rightPointsError}`), true);
                return;
            }

            if (leftOperand.points.length !== rightOperand.points.length) {
                showError(new Error('Функции должны иметь одинаковое количество точек'), true);
                return;
            }

            // Check if X values match
            for (let i = 0; i < leftOperand.points.length; i++) {
                if (Math.abs(leftOperand.points[i].x - rightOperand.points[i].x) > 1e-9) {
                    showError(new Error('X значения функций не совпадают'), true);
                    return;
                }
            }

            // Compute operation locally
            const computedPoints: Point[] = leftOperand.points.map((leftPoint, i) => {
                const rightPoint = rightOperand.points[i];

                // Skip if either point has null y
                if (leftPoint.y === null || rightPoint.y === null) {
                    return {
                        x: leftPoint.x,
                        y: null,
                    };
                }

                let y: number;

                switch (operation) {
                    case '+':
                        y = leftPoint.y + rightPoint.y;
                        break;
                    case '-':
                        y = leftPoint.y - rightPoint.y;
                        break;
                    case '×':
                        y = leftPoint.y * rightPoint.y;
                        break;
                    case '÷':
                        if (Math.abs(rightPoint.y) < 1e-10) {
                            throw new Error(`Деление на ноль в точке x=${rightPoint.x}`);
                        }
                        y = leftPoint.y / rightPoint.y;
                        break;
                    default:
                        throw new Error(`Неизвестная операция: ${operation}`);
                }

                return {
                    x: leftPoint.x,
                    y: y,
                };
            });

            setResult(computedPoints);
        } catch (e) {
            showError(e instanceof Error ? e : new Error(String(e)), true);
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
            // Optionally close modal or keep it open
            // onClose();
        } catch (e) {
            showError(e instanceof Error ? e : new Error(String(e)), true);
        }
    };

    return (
        <>
            <FunctionListModal
                isOpen={showFunctionList !== null}
                onClose={() => setShowFunctionList(null)}
                onSelect={handleFunctionSelect}
                filterType="TABULATED"
            />
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
                            <button onClick={() => handleLoadClick('left')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Загрузить
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
                            <button onClick={() => handleLoadClick('right')} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Загрузить
                            </button>
                            {isInsertable('right') && (
                                <button onClick={() => handleInsertPoint('right')} style={{ padding: '5px 10px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}>
                                    Вставить точку
                                </button>
                            )}
                        </div>
                    </div>

                    {/* Operations */}
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Операции</h3>
                        <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap', marginBottom: '20px' }}>
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
