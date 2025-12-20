import { useEffect, useState } from "react";
import { postJson } from "./api";
import { showError } from "./errorManager";

export function CreateFromMathFunction() {
  const [name, setName] = useState("");
  const [count, setCount] = useState("10");
  const [xFrom, setXFrom] = useState("0");
  const [xTo, setXTo] = useState("10");

  const [options, setOptions] = useState<string[]>([]);
  const [selected, setSelected] = useState("");

  const [result, setResult] = useState("");
  const MAX_ABS = 1e9;
  const MIN_STEP = 1e-9;

  function parseNumber(value: string, label: string): number {
      const s = value.trim();
      if (s === "") throw new Error(`${label}: пустое значение`);

      const n = Number(s);

      if (Number.isNaN(n)) throw new Error(`${label}: должно быть числом`);
      if (!Number.isFinite(n)) throw new Error(`${label}: не гоняем, вводим разумные значения`);

      if (Math.abs(n) > MAX_ABS) {
        throw new Error(`${label}: не гоняем, вводим разумные значения (по модулю > ${MAX_ABS})`);
      }

      return n;
  }

  useEffect(() => {
    fetch(import.meta.env.VITE_API_BASE_URL + "/api/v1/ui/math-functions")
      .then(r => r.json())
      .then((arr: string[]) => {
        setOptions(arr);
        setSelected(arr[0] ?? "");
      })
      .catch(showError);
  }, []);

  async function create() {
      try {
        const n = Number(count);
        const a = parseNumber(xFrom, "xFrom");
        const b = parseNumber(xTo, "xTo");

        if (!name.trim()) throw new Error("Введите имя функции.");
        if (!selected) throw new Error("Выберите MathFunction.");
        if (!Number.isFinite(n)) throw new Error("Количество точек должно быть числом.");
        if (!Number.isInteger(n)) throw new Error("Количество точек должно быть целым.");
        if (n < 2) throw new Error("Минимум 2 точки.");
        if (n > 5000) throw new Error("Максимум 5000 точек.");

        if (!(a < b)) throw new Error("xFrom должен быть меньше xTo.");

        const width = b - a;
        if (width < MIN_STEP) {
          throw new Error(`Интервал слишком маленький (xTo - xFrom < ${MIN_STEP}).`);
        }

        const payload = {
          name,
          mathFunctionKey: selected,
          count: n,
          xFrom: a,
          xTo: b,
          factoryKey: "default",
        };

        const r = await postJson<{ name: string; pointsCount: number }>(
          "/api/v1/ui/tabulated/from-math-function",
          payload
        );

        setResult(`Создано: ${r.name}, точек: ${r.pointsCount}`);
      } catch (e) {
        showError(e);
      }
  }


  return (
    <div style={{ border: "1px solid #333", padding: 16, borderRadius: 12, marginTop: 16 }}>
      <h2 style={{ marginTop: 0 }}>Создание TabulatedFunction из MathFunction</h2>

      <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
        <input placeholder="Имя функции" value={name} onChange={(e) => setName(e.target.value)} />

        <select value={selected} onChange={(e) => setSelected(e.target.value)}>
          {options.map((x) => (
            <option key={x} value={x}>{x}</option>
          ))}
        </select>

        <input placeholder="count" value={count} onChange={(e) => setCount(e.target.value)} />
        <input placeholder="xFrom" value={xFrom} onChange={(e) => setXFrom(e.target.value)} />
        <input placeholder="xTo" value={xTo} onChange={(e) => setXTo(e.target.value)} />

        <button onClick={create}>Создать</button>
      </div>

      {result && <div style={{ marginTop: 12 }}>{result}</div>}
    </div>
  );
}
