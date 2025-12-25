import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { FunctionTable } from './FunctionTable';
import { getJson, putJson, deleteRequest, postJson } from '../api';
import { showError, showSuccess } from '../errorManager';
import { validateFunctionName, validatePoints } from '../utils/validation';

interface Point {
    x: number;
    y: number | null;
}

interface FunctionFull {
    summary: {
        id: number;
        name: string;
        type: string;
    };
    points: Point[];
}

interface FunctionEditModalProps {
    isOpen: boolean;
    onClose: () => void;
    functionId: number;
    onFunctionUpdated: () => void;
    onFunctionDeleted: () => void;
}

export function FunctionEditModal({
    isOpen,
    onClose,
    functionId,
    onFunctionUpdated,
    onFunctionDeleted,
}: FunctionEditModalProps) {
    const [functionData, setFunctionData] = useState<FunctionFull | null>(null);
    const [loading, setLoading] = useState(false);
    const [editingName, setEditingName] = useState(false);
    const [newName, setNewName] = useState('');
    const [points, setPoints] = useState<Point[]>([]);

    useEffect(() => {
        if (isOpen && functionId) {
            loadFunction();
        }
    }, [isOpen, functionId]);

    const loadFunction = async () => {
        try {
            setLoading(true);
            const data = await getJson<FunctionFull>(`/api/v1/functions/${functionId}`);
            
            if (!data || !data.summary) {
                throw new Error('Не удалось загрузить данные функции');
            }
            
            setFunctionData(data);
            setPoints(data.points || []);
            setNewName(data.summary.name);
            setEditingName(false);
        } catch (e) {
            showError(e, true);
            onClose();
        } finally {
            setLoading(false);
        }
    };

    const handleRename = async () => {
        if (!functionData) {
            showError(new Error('Данные функции не загружены'), true);
            return;
        }

        const nameError = validateFunctionName(newName);
        if (nameError) {
            showError(new Error(nameError), true);
            return;
        }

        try {
            await putJson(`/api/v1/functions/${functionId}/name`, { name: newName.trim() });
            setFunctionData({ ...functionData, summary: { ...functionData.summary, name: newName.trim() } });
            setEditingName(false);
            showSuccess('Имя функции успешно изменено');
            onFunctionUpdated();
        } catch (e) {
            showError(e, true);
        }
    };

    const handleSavePoints = async () => {
        if (!functionData) {
            showError(new Error('Данные функции не загружены'), true);
            return;
        }

        // Filter out points with null y values and convert to valid points
        const validPoints = points
            .filter(p => p.y !== null && p.y !== undefined && !isNaN(p.y))
            .map(p => ({ x: p.x, y: p.y as number }));

        if (validPoints.length < 2) {
            showError(new Error('Необходимо заполнить минимум 2 точки с валидными значениями y'), true);
            return;
        }

        // Validate points using utility
        const pointsError = validatePoints(validPoints);
        if (pointsError) {
            showError(new Error(pointsError), true);
            return;
        }

        try {
            // For updating points, we'll delete and recreate the function
            // First delete the old one
            await deleteRequest(`/api/v1/functions/${functionId}`);
            
            // Create new one with updated points
            const created = await postJson<FunctionFull>('/api/v1/functions/tabulated/manual', {
                name: functionData.summary.name,
                points: validPoints,
            });
            
            if (!created || !created.summary) {
                throw new Error('Не удалось обновить функцию');
            }
            
            setFunctionData(created);
            showSuccess('Точки функции успешно обновлены');
            onFunctionUpdated();
        } catch (e) {
            showError(e, true);
            // Reload function on error to revert changes
            loadFunction();
        }
    };

    const handleDelete = async () => {
        if (!functionData) {
            showError(new Error('Данные функции не загружены'), true);
            return;
        }

        if (!confirm(`Вы уверены, что хотите удалить функцию "${functionData.summary.name}"?`)) {
            return;
        }

        try {
            await deleteRequest(`/api/v1/functions/${functionId}`);
            showSuccess(`Функция "${functionData.summary.name}" успешно удалена`);
            onFunctionDeleted();
            onClose();
        } catch (e) {
            showError(e, true);
        }
    };

    const handleAddPoint = () => {
        if (points.length === 0) {
            setPoints([{ x: 0, y: null }]);
            return;
        }

        // Add a point after the last one
        const lastPoint = points[points.length - 1];
        const newPoint: Point = { x: lastPoint.x + 1, y: null };
        setPoints([...points, newPoint]);
    };

    const handleRemovePoint = (index: number) => {
        if (points.length <= 2) {
            showError(new Error('Функция должна содержать минимум 2 точки'));
            return;
        }
        const yValue = points[index].y ?? 'не задано';
        if (confirm(`Удалить точку с x=${points[index].x}, y=${yValue}?`)) {
            const newPoints = points.filter((_, i) => i !== index);
            setPoints(newPoints);
        }
    };

    const handlePointsChange = (newPoints: Point[]) => {
        setPoints(newPoints);
    };

    if (!functionData) {
        return (
            <Modal isOpen={isOpen} onClose={onClose} title="Редактирование функции">
                <div style={{ color: 'var(--text)' }}>
                    {loading ? 'Загрузка...' : 'Функция не найдена'}
                </div>
            </Modal>
        );
    }

    const isTabulated = functionData.summary.type === 'TABULATED';

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Редактирование функции">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {/* Function Name */}
                <div>
                    <h3 style={{ color: 'var(--text)' }}>Название функции</h3>
                    {editingName ? (
                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                            <input
                                type="text"
                                value={newName}
                                onChange={(e) => setNewName(e.target.value)}
                                style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                onKeyPress={(e) => {
                                    if (e.key === 'Enter') {
                                        handleRename();
                                    }
                                }}
                                autoFocus
                            />
                            <button
                                onClick={handleRename}
                                style={{ padding: '8px 16px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}
                            >
                                Сохранить
                            </button>
                            <button
                                onClick={() => {
                                    setEditingName(false);
                                    setNewName(functionData.summary.name);
                                }}
                                style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            >
                                Отмена
                            </button>
                        </div>
                    ) : (
                        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                            <span style={{ color: 'var(--text)', fontSize: '16px', padding: '8px' }}>{functionData.summary.name}</span>
                            <button
                                onClick={() => setEditingName(true)}
                                style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            >
                                Изменить название
                            </button>
                        </div>
                    )}
                </div>

                {/* Function Points (only for tabulated functions) */}
                {isTabulated && (
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                            <h3 style={{ color: 'var(--text)', margin: 0 }}>Точки функции</h3>
                            <div style={{ display: 'flex', gap: '5px' }}>
                                <button
                                    onClick={handleAddPoint}
                                    style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                                >
                                    Добавить точку
                                </button>
                                <button
                                    onClick={handleSavePoints}
                                    style={{ padding: '5px 10px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}
                                >
                                    Сохранить изменения
                                </button>
                            </div>
                        </div>
                        <FunctionTable
                            points={points}
                            onPointsChange={handlePointsChange}
                            readonly={false}
                            onRemovePoint={handleRemovePoint}
                        />
                        <div style={{ marginTop: '10px', color: 'var(--muted)', fontSize: '12px' }}>
                            Точки можно редактировать напрямую в таблице. Используйте кнопку "Добавить точку" для добавления новых точек.
                        </div>
                    </div>
                )}

                {!isTabulated && (
                    <div style={{ color: 'var(--text)' }}>
                        Редактирование точек доступно только для табулированных функций.
                        Тип функции: {functionData.summary.type}
                    </div>
                )}

                {/* Actions */}
                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', paddingTop: '20px', borderTop: '1px solid var(--border)' }}>
                    <button
                        onClick={handleDelete}
                        style={{ padding: '8px 16px', background: '#dc2626', color: 'white', border: '1px solid #dc2626' }}
                    >
                        Удалить функцию
                    </button>
                    <button
                        onClick={onClose}
                        style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                    >
                        Закрыть
                    </button>
                </div>
            </div>
        </Modal>
    );
}

