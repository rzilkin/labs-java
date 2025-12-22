import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { getJson, postJson } from '../api';
import { showError } from '../errorManager';
import { validateThreadCount } from '../utils/validation';

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
            // Filter to only show TABULATED functions
            const tabulatedFunctions = (data || []).filter(f => f.type === 'TABULATED');
            setFunctions(tabulatedFunctions);
            if (tabulatedFunctions.length > 0) {
                setSelectedFunctionId(tabulatedFunctions[0].id);
            } else {
                setSelectedFunctionId(null);
            }
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    const handleIntegrate = async () => {
        if (!selectedFunctionId) {
            showError(new Error('Выберите табулированную функцию'), true);
            return;
        }

        // Check if selected function is tabulated
        const selectedFunction = functions.find(f => f.id === selectedFunctionId);
        if (selectedFunction && selectedFunction.type !== 'TABULATED') {
            showError(new Error('Только для табулированных функций'), true);
            return;
        }

        // Validate thread count (1-64)
        const threadError = validateThreadCount(threads, 64);
        if (threadError) {
            showError(new Error(threadError), true);
            return;
        }

        try {
            setCalculating(true);
            setResult(null);

            const response = await postJson<{ value: number }>(`/api/v1/operations/integrate/${selectedFunctionId}`, {
                threads: parseInt(threads, 10),
            });

            if (response === null || response === undefined || typeof response.value !== 'number') {
                throw new Error('Неверный ответ от сервера');
            }

            if (!isFinite(response.value)) {
                throw new Error('Результат интеграла не является конечным числом');
            }

            setResult(response.value);
        } catch (e) {
            showError(e, true);
            setResult(null);
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
                    ) : functions.length === 0 ? (
                        <div style={{ color: 'var(--text)', padding: '10px', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '4px' }}>
                            Нет табулированных функций. Создайте табулированную функцию для вычисления интеграла.
                        </div>
                    ) : (
                        <select
                            value={selectedFunctionId || ''}
                            onChange={(e) => setSelectedFunctionId(e.target.value ? Number(e.target.value) : null)}
                            style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        >
                            <option value="">Выберите табулированную функцию</option>
                            {functions.map(f => (
                                <option key={f.id} value={f.id}>
                                    {f.name} (ID: {f.id})
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
                        По умолчанию: 8 потоков
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
                                Интеграл = {result.toFixed(10)}
                            </div>
                        </div>
                    )}
                </div>

                {/* Info */}
                <div style={{ padding: '15px', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '4px' }}>
                    <div style={{ color: 'var(--text)', fontSize: '14px' }}>
                        <strong>Информация:</strong>
                        <ul style={{ marginTop: '10px', paddingLeft: '20px', color: 'var(--muted)' }}>
                            <li>Интеграл вычисляется по всей области определения табулированной функции</li>
                            <li>Вычисления выполняются параллельно с использованием указанного количества потоков (1-64)</li>
                            <li>Результат представляет собой приближенное значение определённого интеграла</li>
                            <li>Используется метод численного интегрирования с параллельной обработкой</li>
                        </ul>
                    </div>
                </div>
            </div>
        </Modal>
    );
}

