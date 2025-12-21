import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { getJson, postJson } from '../api';
import { showError } from '../errorManager';

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
}

interface IntegrationModalProps {
    isOpen: boolean;
    onClose: () => void;
}

export function IntegrationModal({ isOpen, onClose }: IntegrationModalProps) {
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [selectedFunctionId, setSelectedFunctionId] = useState<number | null>(null);
    const [threads, setThreads] = useState('8');
    const [result, setResult] = useState<number | null>(null);
    const [loading, setLoading] = useState(false);
    const [calculating, setCalculating] = useState(false);

    useEffect(() => {
        if (isOpen) {
            loadFunctions();
        }
    }, [isOpen]);

    const loadFunctions = async () => {
        try {
            setLoading(true);
            const data = await getJson<FunctionSummary[]>('/api/v1/functions');
            setFunctions(data || []);
            if (data && data.length > 0) {
                setSelectedFunctionId(data[0].id);
            }
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    const handleIntegrate = async () => {
        if (!selectedFunctionId) {
            showError(new Error('Выберите функцию'));
            return;
        }

        const threadCount = parseInt(threads, 10);
        if (isNaN(threadCount) || threadCount < 1) {
            showError(new Error('Количество потоков должно быть положительным числом'));
            return;
        }

        if (threadCount > 64) {
            showError(new Error('Максимальное количество потоков: 64'));
            return;
        }

        try {
            setCalculating(true);
            setResult(null);

            const response = await postJson<{ value: number }>(`/api/v1/operations/integrate/${selectedFunctionId}`, {
                threads: threadCount,
            });

            setResult(response.value);
        } catch (e) {
            showError(e);
        } finally {
            setCalculating(false);
        }
    };

    const selectedFunction = functions.find(f => f.id === selectedFunctionId);

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Интегрирование функции">
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                {/* Function Selection */}
                <div>
                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Выберите функцию:</label>
                    {loading ? (
                        <div style={{ color: 'var(--text)' }}>Загрузка функций...</div>
                    ) : (
                        <select
                            value={selectedFunctionId || ''}
                            onChange={(e) => setSelectedFunctionId(Number(e.target.value))}
                            style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        >
                            <option value="">Выберите функцию</option>
                            {functions.map(f => (
                                <option key={f.id} value={f.id}>
                                    {f.name} (ID: {f.id}, Тип: {f.type})
                                </option>
                            ))}
                        </select>
                    )}
                </div>

                {/* Threads Configuration */}
                <div>
                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>
                        Количество потоков (1-64):
                    </label>
                    <input
                        type="number"
                        min="1"
                        max="64"
                        value={threads}
                        onChange={(e) => setThreads(e.target.value)}
                        style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                    />
                    <div style={{ color: 'var(--muted)', fontSize: '12px', marginTop: '5px' }}>
                        Рекомендуемое значение: 8 потоков
                    </div>
                </div>

                {/* Result */}
                <div>
                    <button
                        onClick={handleIntegrate}
                        disabled={!selectedFunctionId || calculating}
                        style={{
                            padding: '10px 20px',
                            background: 'var(--btn-bg)',
                            color: 'var(--btn-text)',
                            border: '1px solid var(--border)',
                            cursor: (!selectedFunctionId || calculating) ? 'not-allowed' : 'pointer',
                            opacity: (!selectedFunctionId || calculating) ? 0.5 : 1,
                        }}
                    >
                        {calculating ? 'Вычисление...' : 'Вычислить интеграл'}
                    </button>

                    {result !== null && (
                        <div
                            style={{
                                marginTop: '20px',
                                padding: '20px',
                                background: 'var(--card)',
                                border: '2px solid var(--border)',
                                borderRadius: '8px',
                                textAlign: 'center',
                            }}
                        >
                            <div style={{ color: 'var(--muted)', fontSize: '14px', marginBottom: '10px' }}>
                                Интеграл функции "{selectedFunction?.name}"
                            </div>
                            <div style={{ color: 'var(--text)', fontSize: '24px', fontWeight: 'bold' }}>
                                ∫ f(x) dx = {result.toFixed(10)}
                            </div>
                        </div>
                    )}
                </div>

                {/* Info */}
                <div style={{ padding: '15px', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '4px' }}>
                    <div style={{ color: 'var(--text)', fontSize: '14px' }}>
                        <strong>Информация:</strong>
                        <ul style={{ marginTop: '10px', paddingLeft: '20px', color: 'var(--muted)' }}>
                            <li>Интеграл вычисляется по всей области определения функции</li>
                            <li>Вычисления выполняются параллельно с использованием указанного количества потоков</li>
                            <li>Результат представляет собой приближенное значение интеграла</li>
                        </ul>
                    </div>
                </div>
            </div>
        </Modal>
    );
}

