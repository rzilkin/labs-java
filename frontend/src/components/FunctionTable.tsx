interface Point {
    x: number;
    y: number | null;
}

interface FunctionTableProps {
  points: Point[];
  onPointsChange: (points: Point[]) => void;
  readonly?: boolean;
  onRemovePoint?: (index: number) => void;
}

export function FunctionTable({ points, onPointsChange, readonly = false, onRemovePoint }: FunctionTableProps) {
    const handleYChange = (index: number, value: string) => {
        if (readonly) return;
        const newPoints = [...points];
        // If empty string, set to null; otherwise parse as float
        const newY = value === '' ? null : (isNaN(parseFloat(value)) ? null : parseFloat(value));
        newPoints[index] = { ...newPoints[index], y: newY };
        onPointsChange(newPoints);
    };

    return (
        <table style={{ width: '100%', borderCollapse: 'collapse', color: 'var(--text)' }}>
            <thead>
                <tr>
          <th style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--muted)', backgroundColor: 'var(--card)' }}>Индекс</th>
          <th style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--muted)', backgroundColor: 'var(--card)' }}>x</th>
          <th style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--muted)', backgroundColor: 'var(--card)' }}>y</th>
          {!readonly && onRemovePoint && <th style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--muted)', backgroundColor: 'var(--card)' }}>Действия</th>}
                </tr>
            </thead>
            <tbody>
        {points.length === 0 ? (
          <tr>
            <td colSpan={!readonly && onRemovePoint ? 4 : 3} style={{ border: '1px solid var(--border)', padding: '8px', textAlign: 'center', color: 'var(--text)' }}>
              Нет точек
            </td>
          </tr>
        ) : (
          points.map((point, index) => (
            <tr key={index}>
              <td style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--text)' }}>{index}</td>
              <td style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--text)' }}>{point.x}</td>
              <td style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--text)' }}>
                {readonly ? (
                  point.y ?? ''
                ) : (
                  <input
                    type="number"
                    value={point.y ?? ''}
                    onChange={(e) => handleYChange(index, e.target.value)}
                    style={{ 
                      width: '100%', 
                      boxSizing: 'border-box',
                      background: 'var(--input-bg)',
                      color: 'var(--input-text)',
                      border: '1px solid var(--border)',
                      padding: '4px 8px',
                      borderRadius: '4px',
                    }}
                  />
                )}
              </td>
              {!readonly && onRemovePoint && (
                <td style={{ border: '1px solid var(--border)', padding: '8px', color: 'var(--text)' }}>
                  <button
                    onClick={() => onRemovePoint(index)}
                    style={{
                      padding: '4px 8px',
                      background: '#dc2626',
                      color: 'white',
                      border: '1px solid #dc2626',
                      borderRadius: '4px',
                      cursor: 'pointer',
                    }}
                  >
                    Удалить
                  </button>
                </td>
              )}
            </tr>
          ))
        )}
            </tbody>
        </table>
    );
}

