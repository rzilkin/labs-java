import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { getJson } from '../api';
import { showError } from '../errorManager';

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
}

interface FunctionListModalProps {
    isOpen: boolean;
    onClose: () => void;
    onSelect: (functionId: number) => void;
    filterType?: string; // Optional filter by type (e.g., 'TABULATED')
}

export function FunctionListModal({ isOpen, onClose, onSelect, filterType }: FunctionListModalProps) {
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [loading, setLoading] = useState(false);
    const [selectedId, setSelectedId] = useState<number | null>(null);

    useEffect(() => {
        if (isOpen) {
            loadFunctions();
        } else {
            setSelectedId(null);
        }
    }, [isOpen]);

    const loadFunctions = async () => {
        try {
            setLoading(true);
            const data = await getJson<FunctionSummary[]>('/api/v1/functions');
            let filtered = data || [];

            if (filterType) {
                filtered = filtered.filter(f => f.type === filterType);
            }

            setFunctions(filtered);
        } catch (e) {
            showError(e, true);
        } finally {
            setLoading(false);
        }
    };

    const handleSelect = () => {
        if (selectedId !== null) {
            onSelect(selectedId);
            onClose();
            setSelectedId(null);
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Выберите функцию" zIndex={2000}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {loading ? (
                    <div style={{ color: 'var(--text)', textAlign: 'center', padding: '20px' }}>
                        Загрузка функций...
                    </div>
                ) : functions.length === 0 ? (
                    <div style={{ color: 'var(--text)', textAlign: 'center', padding: '20px' }}>
                        {filterType ? `Нет функций типа ${filterType}` : 'Нет доступных функций'}
                    </div>
                ) : (
                    <div style={{ maxHeight: '400px', overflowY: 'auto', border: '1px solid var(--border)', borderRadius: '4px' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', color: 'var(--text)' }}>
                            <thead>
                                <tr>
                                    <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>ID</th>
                                    <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>Название</th>
                                    <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>Тип</th>
                                </tr>
                            </thead>
                            <tbody>
                                {functions.map((func) => (
                                    <tr
                                        key={func.id}
                                        onClick={() => setSelectedId(func.id)}
                                        style={{
                                            cursor: 'pointer',
                                            backgroundColor: selectedId === func.id ? 'var(--btn2-bg)' : 'transparent',
                                        }}
                                    >
                                        <td style={{ border: '1px solid var(--border)', padding: '8px' }}>{func.id}</td>
                                        <td style={{ border: '1px solid var(--border)', padding: '8px' }}>{func.name}</td>
                                        <td style={{ border: '1px solid var(--border)', padding: '8px' }}>{func.type}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                    <button
                        onClick={onClose}
                        style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                    >
                        Отмена
                    </button>
                    <button
                        onClick={handleSelect}
                        disabled={selectedId === null}
                        style={{
                            padding: '8px 16px',
                            background: 'var(--btn-bg)',
                            color: 'var(--btn-text)',
                            border: '1px solid var(--border)',
                            cursor: selectedId === null ? 'not-allowed' : 'pointer',
                            opacity: selectedId === null ? 0.5 : 1,
                        }}
                    >
                        Выбрать
                    </button>
                </div>
            </div>
        </Modal>
    );
}

