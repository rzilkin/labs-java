import { useState } from "react";
import { postJson } from "./api";

type PointInput = { x: string; y: string };
type PointNumber = { x: number; y: number };

export function CreateFromPoints({ onError }: { onError: (m: string) => void }) {
  const [name, setName] = useState("");
  const [countText, setCountText] = useState("");
  const [points, setPoints] = useState<PointInput[]>([]);
  const [result, setResult] = useState("");

  const MAX_ABS = 1e9;

  function parseNumber(value: string, field: "X" | "Y"): number {
    const s = value.trim();

    if (s === "") throw new Error(`${field}: пустое значение`);

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

      if (countText.trim() === "") throw new Error("Введите количество точек.");
      if (!Number.isFinite(n)) throw new Error("Количество точек должно быть числом.");
      if (!Number.isInteger(n)) throw new Error("Количество точек должно быть целым числом.");
      if (n < 2) throw new Error("Минимум 2 точки.");
      if (n > 500) throw new Error("Слишком много точек. Максимум: 500.");

      // пересоздаём таблицу
      setPoints(Array.from({ length: n }, () => ({ x: "", y: "" })));
      setResult("");
    } catch (e) {
      onError(e instanceof Error ? e.message : String(e));
    }
  }

  function setPoint(i: number, key: "x" | "y", value: string) {
    const next = points.slice();
    next[i] = { ...next[i], [key]: value };
    setPoints(next);
  }

  async function create() {
    try {
      if (!name.trim()) throw new Error("Введите имя функции.");

      // ✅ если таблицу не строили / сбросили — создавать нельзя
      if (points.length === 0) {
        throw new Error("Сначала нажмите «Показать таблицу» и заполните точки.");
      }

      const numericPoints: PointNumber[] = points.map((p) => ({
        x: parseNumber(p.x, "X"),
        y: parseNumber(p.y, "Y"),
      }));

      const payload = { name, factoryKey: "default", points: numericPoints };

      const r = await postJson<{ name: string; pointsCount: number }>(
        "/api/v1/ui/tabulated/from-points",
        payload
      );

      setResult(`Создано: ${r.name}, точек: ${r.pointsCount}`);

      // ✅ после успешного создания — скрываем таблицу и чистим ввод
      setPoints([]);       // таблица исчезнет
      setCountText("");    // сбросить количество
      // setName("");      // если хочешь очищать имя тоже — раскомментируй
    } catch (e) {
      onError(e instanceof Error ? e.message : String(e));
    }
  }

  return (
    <div style={{ border: "1px solid #333", padding: 16, borderRadius: 12 }}>
      <h2 style={{ marginTop: 0 }}>Создание TabulatedFunction из точек</h2>

      <div style={{ display: "flex", gap: 8, marginBottom: 12, flexWrap: "wrap" }}>
        <input
          placeholder="Имя функции"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <input
          placeholder="Количество точек"
          value={countText}
          onChange={(e) => setCountText(e.target.value)}
        />
        <button onClick={buildTable}>Показать таблицу</button>
      </div>

      {points.length > 0 && (
        <>
          <table>
            <thead>
              <tr>
                <th>X</th>
                <th>Y</th>
              </tr>
            </thead>
            <tbody>
              {points.map((p, i) => (
                <tr key={i}>
                  <td>
                    <input
                      value={p.x}
                      onChange={(e) => setPoint(i, "x", e.target.value)}
                      placeholder="x"
                    />
                  </td>
                  <td>
                    <input
                      value={p.y}
                      onChange={(e) => setPoint(i, "y", e.target.value)}
                      placeholder="y"
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <button style={{ marginTop: 12 }} onClick={create}>
            Создать
          </button>
        </>
      )}

      {result && <div style={{ marginTop: 12 }}>{result}</div>}
    </div>
  );
}


