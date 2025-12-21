import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { getJson, postJson } from '../api';
import { showError } from '../errorManager';

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
}

interface CompositeFunctionModalProps {
    isOpen: boolean;
    onClose: () => void;
    onCreated: () => void;
}

export function CompositeFunctionModal({ isOpen, onClose, onCreated }: CompositeFunctionModalProps) {
    const [name, setName] = useState('');
    const [availableFunctions, setAvailableFunctions] = useState<FunctionSummary[]>([]);
    const [selectedComponents, setSelectedComponents] = useState<number[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isOpen) {
            loadFunctions();
        }
    }, [isOpen]);

    const loadFunctions = async () => {
        try {
            setLoading(true);
            const functions = await getJson<FunctionSummary[]>('/api/v1/functions');
            // Include all function types for composition
            setAvailableFunctions(functions || []);
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    const handleToggleComponent = (id: number) => {
        setSelectedComponents(prev => {
            if (prev.includes(id)) {
                return prev.filter(x => x !== id);
            } else {
                return [...prev, id];
            }
        });
    };

    const handleCreate = async () => {
        if (!name.trim()) {
            showError(new Error('Введите имя составной функции'));
            return;
        }

        if (selectedComponents.length < 2) {
            showError(new Error('Выберите минимум 2 функции для композиции'));
            return;
        }

        try {
            await postJson('/api/v1/functions/composite', {
                name: name.trim(),
                componentIds: selectedComponents,
            });

            onCreated();
            setName('');
            setSelectedComponents([]);
            onClose();
        } catch (e) {
            showError(e);
        }
    };

    const handleMoveUp = (index: number) => {
        if (index === 0) return;
        const newComponents = [...selectedComponents];
        [newComponents[index - 1], newComponents[index]] = [newComponents[index], newComponents[index - 1]];
        setSelectedComponents(newComponents);
    };

    const handleMoveDown = (index: number) => {
        if (index === selectedComponents.length - 1) return;
        const newComponents = [...selectedComponents];
        [newComponents[index], newComponents[index + 1]] = [newComponents[index + 1], newComponents[index]];
        setSelectedComponents(newComponents);
    };

    const handleRemove = (index: number) => {
        setSelectedComponents(prev => prev.filter((_, i) => i !== index));
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Создание составной функции">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {/* Function Name */}
                <div>
                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Имя функции:</label>
                    <input
                        type="text"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="Введите имя составной функции"
                        style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                    />
                </div>

                {/* Available Functions */}
                <div>
                    <h3 style={{ color: 'var(--text)' }}>Доступные функции</h3>
                    {loading ? (
                        <div style={{ color: 'var(--text)' }}>Загрузка функций...</div>
                    ) : (
                        <div style={{ maxHeight: '300px', overflowY: 'auto', border: '1px solid var(--border)', borderRadius: '4px', padding: '10px' }}>
                            {availableFunctions.length === 0 ? (
                                <div style={{ color: 'var(--text)' }}>Нет доступных функций</div>
                            ) : (
                                availableFunctions.map(func => (
                                    <div
                                        key={func.id}
                                        style={{
                                            padding: '8px',
                                            marginBottom: '5px',
                                            border: '1px solid var(--border)',
                                            borderRadius: '4px',
                                            cursor: 'pointer',
                                            background: selectedComponents.includes(func.id) ? 'var(--btn2-bg)' : 'var(--card)',
                                            color: 'var(--text)',
                                        }}
                                        onClick={() => handleToggleComponent(func.id)}
                                    >
                                        <input
                                            type="checkbox"
                                            checked={selectedComponents.includes(func.id)}
                                            onChange={() => handleToggleComponent(func.id)}
                                            style={{ marginRight: '8px' }}
                                        />
                                        {func.name} (ID: {func.id}, Тип: {func.type})
                                    </div>
                                ))
                            )}
                        </div>
                    )}
                </div>

                {/* Selected Components */}
                {selectedComponents.length > 0 && (
                    <div>
                        <h3 style={{ color: 'var(--text)' }}>Выбранные компоненты (порядок важен)</h3>
                        <div style={{ border: '1px solid var(--border)', borderRadius: '4px', padding: '10px' }}>
                            {selectedComponents.map((id, index) => {
                                const func = availableFunctions.find(f => f.id === id);
                                return (
                                    <div
                                        key={`${id}-${index}`}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '10px',
                                            padding: '8px',
                                            marginBottom: '5px',
                                            background: 'var(--card)',
                                            border: '1px solid var(--border)',
                                            borderRadius: '4px',
                                        }}
                                    >
                                        <span style={{ color: 'var(--text)', minWidth: '30px' }}>{index + 1}.</span>
                                        <span style={{ flex: 1, color: 'var(--text)' }}>
                                            {func ? `${func.name} (ID: ${func.id})` : `ID: ${id}`}
                                        </span>
                                        <button
                                            onClick={() => handleMoveUp(index)}
                                            disabled={index === 0}
                                            style={{
                                                padding: '4px 8px',
                                                background: 'var(--btn2-bg)',
                                                color: 'var(--btn2-text)',
                                                border: '1px solid var(--border)',
                                                cursor: index === 0 ? 'not-allowed' : 'pointer',
                                                opacity: index === 0 ? 0.5 : 1,
                                            }}
                                        >
                                            ↑
                                        </button>
                                        <button
                                            onClick={() => handleMoveDown(index)}
                                            disabled={index === selectedComponents.length - 1}
                                            style={{
                                                padding: '4px 8px',
                                                background: 'var(--btn2-bg)',
                                                color: 'var(--btn2-text)',
                                                border: '1px solid var(--border)',
                                                cursor: index === selectedComponents.length - 1 ? 'not-allowed' : 'pointer',
                                                opacity: index === selectedComponents.length - 1 ? 0.5 : 1,
                                            }}
                                        >
                                            ↓
                                        </button>
                                        <button
                                            onClick={() => handleRemove(index)}
                                            style={{
                                                padding: '4px 8px',
                                                background: '#dc2626',
                                                color: 'white',
                                                border: '1px solid #dc2626',
                                                cursor: 'pointer',
                                            }}
                                        >
                                            Удалить
                                        </button>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}

                {/* Actions */}
                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                    <button
                        onClick={onClose}
                        style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                    >
                        Отмена
                    </button>
                    <button
                        onClick={handleCreate}
                        disabled={!name.trim() || selectedComponents.length < 2}
                        style={{
                            padding: '8px 16px',
                            background: 'var(--btn-bg)',
                            color: 'var(--btn-text)',
                            border: '1px solid var(--border)',
                            cursor: (!name.trim() || selectedComponents.length < 2) ? 'not-allowed' : 'pointer',
                            opacity: (!name.trim() || selectedComponents.length < 2) ? 0.5 : 1,
                        }}
                    >
                        Создать
                    </button>
                </div>
            </div>
        </Modal>
    );
}

