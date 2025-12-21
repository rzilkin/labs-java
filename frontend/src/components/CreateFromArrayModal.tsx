import { useState } from 'react';
import { Modal } from './Modal';
import { postJson } from '../api';

interface Point {
    x: number;
    y: number;
}

interface CreateFromArrayModalProps {
    isOpen: boolean;
    onClose: () => void;
    onCreated: (functionId: number, points: Point[]) => void;
    factoryKey: string;
}

export function CreateFromArrayModal({ isOpen, onClose, onCreated }: CreateFromArrayModalProps) {
    const [name, setName] = useState('');
    const [countText, setCountText] = useState('');
    const [points, setPoints] = useState<{ x: string; y: string }[]>([]);
    const [error, setError] = useState('');

    const MAX_ABS = 1e9;

    function parseNumber(value: string, field: 'X' | 'Y'): number {
        const s = value.trim();
        if (s === '') throw new Error(`${field}: пустое значение`);
        const n = Number(s);
        if (Number.isNaN(n)) throw new Error(`${field}: должно быть числом`);
        if (!Number.isFinite(n)) throw new Error(`${field}: число слишком велико`);
        if (Math.abs(n) > MAX_ABS) {
            throw new Error(`${field}: число слишком велико (|${field}| > ${MAX_ABS})`);
        }
        return n;
    }

    function buildTable() {
        try {
            const n = Number(countText);
            if (countText.trim() === '') throw new Error('Введите количество точек.');
            if (!Number.isFinite(n)) throw new Error('Количество точек должно быть числом.');
            if (!Number.isInteger(n)) throw new Error('Количество точек должно быть целым числом.');
            if (n < 2) throw new Error('Минимум 2 точки.');
            if (n > 500) throw new Error('Слишком много точек. Максимум: 500.');
            setPoints(Array.from({ length: n }, () => ({ x: '', y: '' })));
            setError('');
        } catch (e) {
            setError(e instanceof Error ? e.message : String(e));
        }
    }

    function setPoint(i: number, key: 'x' | 'y', value: string) {
        const next = points.slice();
        next[i] = { ...next[i], [key]: value };
        setPoints(next);
    }

    async function create() {
        try {
            setError('');
            if (!name.trim()) throw new Error('Введите имя функции.');
            if (points.length === 0) {
                throw new Error('Сначала нажмите «Показать таблицу» и заполните точки.');
            }

            const numericPoints: Point[] = points.map((p) => ({
                x: parseNumber(p.x, 'X'),
                y: parseNumber(p.y, 'Y'),
            }));

            // Check if x values are sorted
            for (let i = 1; i < numericPoints.length; i++) {
                if (numericPoints[i].x <= numericPoints[i - 1].x) {
                    throw new Error('Значения x должны быть строго возрастающими.');
                }
            }

            const payload = { name, points: numericPoints };
            const result = await postJson<{ summary: { id: number }; points: Point[] }>(
                '/api/v1/functions/tabulated/manual',
                payload
            );

            onCreated(result.summary.id, result.points);
            // Reset form
            setPoints([]);
            setCountText('');
            setName('');
            onClose();
        } catch (e) {
            setError(e instanceof Error ? e.message : String(e));
        }
    }

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Создать функцию из массива точек">
            <div>
                <div style={{ marginBottom: '15px' }}>
                    <input
                        placeholder="Имя функции"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        style={{ width: '100%', padding: '8px', marginBottom: '10px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                    />
                    <div style={{ display: 'flex', gap: '5px' }}>
                        <input
                            placeholder="Количество точек"
                            value={countText}
                            onChange={(e) => setCountText(e.target.value)}
                            style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                        />
                        <button onClick={buildTable} style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                            Показать таблицу
                        </button>
                    </div>
                </div>

                {error && <div style={{ color: '#ef4444', marginBottom: '10px' }}>{error}</div>}

                {points.length > 0 && (
                    <>
                        <div style={{ maxHeight: '400px', overflow: 'auto', marginBottom: '15px' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse', color: 'var(--text)' }}>
                                <thead>
                                    <tr>
                                        <th style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--muted)', backgroundColor: 'var(--card)' }}>X</th>
                                        <th style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--muted)', backgroundColor: 'var(--card)' }}>Y</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {points.map((p, i) => (
                                        <tr key={i}>
                                            <td style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--text)' }}>
                                                <input
                                                    value={p.x}
                                                    onChange={(e) => setPoint(i, 'x', e.target.value)}
                                                    placeholder="x"
                                                    style={{ width: '100%', boxSizing: 'border-box', padding: '4px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                                />
                                            </td>
                                            <td style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--text)' }}>
                                                <input
                                                    value={p.y}
                                                    onChange={(e) => setPoint(i, 'y', e.target.value)}
                                                    placeholder="y"
                                                    style={{ width: '100%', boxSizing: 'border-box', padding: '4px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                                />
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <div>
                            <button onClick={create} style={{ padding: '8px 16px', marginRight: '10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Создать
                            </button>
                            <button onClick={onClose} style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                                Отмена
                            </button>
                        </div>
                    </>
                )}
            </div>
        </Modal>
    );
}

