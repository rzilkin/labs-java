import { useEffect, useState } from "react";
import { showError } from "./errorManager";

type FunctionInfo = { id: string; name: string; pointsCount: number };
type FunctionDetails = { id: string; name: string; pointsCount: number; xMin: number; xMax: number };

export function FunctionLibrary({ onOpened }: { onOpened: () => void }) {
  const [items, setItems] = useState<FunctionInfo[]>([]);
  const [details, setDetails] = useState<FunctionDetails | null>(null);

  async function refresh() {
    try {
      const r = await fetch(import.meta.env.VITE_API_BASE_URL + "/api/v1/ui/library");
      if (!r.ok) throw new Error(await r.text());
      setItems(await r.json());
    } catch (e) {
      showError(e);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function open(id: string) {
    try {
      const r = await fetch(
        import.meta.env.VITE_API_BASE_URL + `/api/v1/ui/library/${id}/open`,
        { method: "POST" }
      );
      if (!r.ok) throw new Error(await r.text());
      onOpened();
    } catch (e) {
      showError(e);
    }
  }

  async function del(id: string) {
    try {
      const r = await fetch(
        import.meta.env.VITE_API_BASE_URL + `/api/v1/ui/library/${id}`,
        { method: "DELETE" }
      );
      if (!r.ok) throw new Error(await r.text());

      // если удалили ту, которую смотрели — очистим details
      if (details?.id === id) setDetails(null);

      await refresh();
    } catch (e) {
      showError(e);
    }
  }

  async function showDetails(id: string) {
    try {
      const r = await fetch(import.meta.env.VITE_API_BASE_URL + `/api/v1/ui/library/${id}`);
      if (!r.ok) throw new Error(await r.text());
      setDetails(await r.json());
    } catch (e) {
      showError(e);
    }
  }

  return (
    <div style={{ border: "1px solid #333", padding: 16, borderRadius: 12, marginTop: 16 }}>
      <h2 style={{ marginTop: 0 }}>Мои функции</h2>

      <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 12 }}>
        <button onClick={refresh}>Обновить список</button>
      </div>

      {items.length === 0 ? (
        <div>Пока ничего не создано.</div>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Имя</th>
              <th>Точек</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {items.map((it) => (
              <tr key={it.id}>
                <td>{it.name}</td>
                <td>{it.pointsCount}</td>
                <td style={{ display: "flex", gap: 8 }}>
                  <button onClick={() => open(it.id)}>Открыть</button>
                  <button onClick={() => showDetails(it.id)}>Подробнее</button>
                  <button onClick={() => del(it.id)}>Удалить</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {details && (
        <div style={{ marginTop: 12, padding: 12, border: "1px solid #444", borderRadius: 10 }}>
          <div><b>Информация о функции</b></div>
          <div>Имя: {details.name}</div>
          <div>Количество точек: {details.pointsCount}</div>
          <div>Диапазон X: {details.xMin} .. {details.xMax}</div>
        </div>
      )}
    </div>
  );
}

