import { useState, useEffect } from 'react';
import { Modal } from './Modal';
import { postJson } from '../api';

interface Point {
    x: number;
    y: number;
}

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
}

interface CreateFromFunctionModalProps {
    isOpen: boolean;
    onClose: () => void;
    onCreated: (functionId: number, points: Point[]) => void;
    factoryKey: string;
}

export function CreateFromFunctionModal({ isOpen, onClose, onCreated }: CreateFromFunctionModalProps) {
    const [name, setName] = useState('');
    const [count, setCount] = useState('10');
    const [xFrom, setXFrom] = useState('0');
    const [xTo, setXTo] = useState('10');
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [selectedFunctionId, setSelectedFunctionId] = useState<number | null>(null);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const MAX_ABS = 1e9;
    const MIN_STEP = 1e-9;

    useEffect(() => {
        if (isOpen) {
            loadFunctions();
        }
    }, [isOpen]);

    async function loadFunctions() {
        try {
            setLoading(true);
            const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/v1/functions?type=TABULATED`);
            if (!response.ok) throw new Error('Не удалось загрузить функции');
            const data = await response.json();
            setFunctions(data);
            if (data.length > 0) {
                setSelectedFunctionId(data[0].id);
            }
        } catch (e) {
            setError(e instanceof Error ? e.message : String(e));
        } finally {
            setLoading(false);
        }
    }

    function parseNumber(value: string, label: string): number {
        const s = value.trim();
        if (s === '') throw new Error(`${label}: пустое значение`);
        const n = Number(s);
        if (Number.isNaN(n)) throw new Error(`${label}: должно быть числом`);
        if (!Number.isFinite(n)) throw new Error(`${label}: число слишком велико`);
        if (Math.abs(n) > MAX_ABS) {
            throw new Error(`${label}: число слишком велико (по модулю > ${MAX_ABS})`);
        }
        return n;
    }

    async function create() {
        try {
            setError('');
            if (!name.trim()) throw new Error('Введите имя функции.');
            if (!selectedFunctionId) throw new Error('Выберите функцию-источник.');

            const n = Number(count);
            const a = parseNumber(xFrom, 'xFrom');
            const b = parseNumber(xTo, 'xTo');

            if (!Number.isFinite(n)) throw new Error('Количество точек должно быть числом.');
            if (!Number.isInteger(n)) throw new Error('Количество точек должно быть целым.');
            if (n < 2) throw new Error('Минимум 2 точки.');
            if (n > 5000) throw new Error('Максимум 5000 точек.');
            if (!(a < b)) throw new Error('xFrom должен быть меньше xTo.');

            const width = b - a;
            if (width < MIN_STEP) {
                throw new Error(`Интервал слишком маленький (xTo - xFrom < ${MIN_STEP}).`);
            }

            const payload = {
                name,
                sourceFunctionId: selectedFunctionId,
                count: n,
                from: a,
                to: b,
            };

            const result = await postJson<{ summary: { id: number }; points: Point[] }>(
                '/api/v1/functions/tabulated/from-function',
                payload
            );

            onCreated(result.summary.id, result.points);
            setName('');
            setCount('10');
            setXFrom('0');
            setXTo('10');
            onClose();
        } catch (e) {
            setError(e instanceof Error ? e.message : String(e));
        }
    }

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Создать функцию из другой функции">
            <div>
                <div style={{ marginBottom: '15px' }}>
                    <input
                        placeholder="Имя функции"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        style={{ width: '100%', padding: '8px', marginBottom: '10px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                    />
                    {loading ? (
                        <div style={{ color: 'var(--text)' }}>Загрузка функций...</div>
                    ) : (
                        <select
                            value={selectedFunctionId || ''}
                            onChange={(e) => setSelectedFunctionId(Number(e.target.value))}
                            style={{ width: '100%', padding: '8px', marginBottom: '10px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        >
                            <option value="">Выберите функцию</option>
                            {functions.map((f) => (
                                <option key={f.id} value={f.id}>
                                    {f.name} (ID: {f.id})
                                </option>
                            ))}
                        </select>
                    )}
                    <input
                        placeholder="Количество точек"
                        value={count}
                        onChange={(e) => setCount(e.target.value)}
                        style={{ width: '100%', padding: '8px', marginBottom: '10px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                    />
                    <div style={{ display: 'flex', gap: '5px', marginBottom: '10px' }}>
                        <input
                            placeholder="xFrom"
                            value={xFrom}
                            onChange={(e) => setXFrom(e.target.value)}
                            style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        />
                        <input
                            placeholder="xTo"
                            value={xTo}
                            onChange={(e) => setXTo(e.target.value)}
                            style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        />
                    </div>
                </div>

                {error && <div style={{ color: '#ef4444', marginBottom: '10px' }}>{error}</div>}

                <div>
                    <button onClick={create} style={{ padding: '8px 16px', marginRight: '10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                        Создать
                    </button>
                    <button onClick={onClose} style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                        Отмена
                    </button>
                </div>
            </div>
        </Modal>
    );
}

