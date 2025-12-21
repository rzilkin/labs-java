import { useEffect, useState } from "react";
import { postJson } from "./api";
import { showError } from "./errorManager";
import Plot from "react-plotly.js";

type Point = { x: number; y: number };

type CurrentDto = {
  name: string;
  points: Point[];
  insertable: boolean;
  removable: boolean;
};

export function FunctionStudy() {
  const [data, setData] = useState<CurrentDto | null>(null);
  const [applyX, setApplyX] = useState("0");
  const [applyResult, setApplyResult] = useState<string>("");

  const [saveBase64, setSaveBase64] = useState<string>("");
  const [loadBase64, setLoadBase64] = useState<string>("");

  async function refresh() {
    try {
      const r = await fetch(import.meta.env.VITE_API_BASE_URL + "/api/v1/ui/tabulated/current");
      if (!r.ok) throw new Error(await r.text());
      setData(await r.json());
    } catch (e) {
      showError(e);
    }
  }

  useEffect(() => {
    refresh();
  }, []);

  async function doApply() {
    try {
      const x = Number(applyX);
      if (!Number.isFinite(x)) throw new Error("x должно быть числом");

      const r = await fetch(
        import.meta.env.VITE_API_BASE_URL + "/api/v1/ui/tabulated/current/apply?x=" + encodeURIComponent(String(x))
      );
      if (!r.ok) throw new Error(await r.text());
      const val = await r.text();
      setApplyResult(val);
    } catch (e) {
      showError(e);
    }
  }

  async function doSerialize() {
    try {
      const r = await fetch(import.meta.env.VITE_API_BASE_URL + "/api/v1/ui/tabulated/current/serialize");
      if (!r.ok) throw new Error(await r.text());
      setSaveBase64(await r.text());
    } catch (e) {
      showError(e);
    }
  }

  async function doDeserialize() {
    try {
      if (!loadBase64.trim()) throw new Error("Вставьте base64 для загрузки");
      const dto = await postJson<CurrentDto>("/api/v1/ui/tabulated/current/deserialize", { base64: loadBase64 });
      setData(dto);
      setApplyResult("");
      setSaveBase64("");
    } catch (e) {
      showError(e);
    }
  }

  async function doInsert() {
    try {
      const x = Number(prompt("Введите x для вставки", "0") ?? "");
      const y = Number(prompt("Введите y для вставки", "0") ?? "");
      if (!Number.isFinite(x) || !Number.isFinite(y)) throw new Error("x/y должны быть числами");

      const r = await fetch(
        import.meta.env.VITE_API_BASE_URL +
          `/api/v1/ui/tabulated/current/insert?x=${encodeURIComponent(String(x))}&y=${encodeURIComponent(String(y))}`,
        { method: "POST" }
      );
      if (!r.ok) throw new Error(await r.text());
      setData(await r.json());
    } catch (e) {
      showError(e);
    }
  }

  async function doRemove() {
    try {
      const s = prompt("Введите индекс точки для удаления (0..n-1)", "0");
      if (s == null) return;
      const idx = Number(s);
      if (!Number.isInteger(idx)) throw new Error("Индекс должен быть целым");

      const r = await fetch(
        import.meta.env.VITE_API_BASE_URL +
          `/api/v1/ui/tabulated/current/remove?index=${encodeURIComponent(String(idx))}`,
        { method: "DELETE" }
      );
      if (!r.ok) throw new Error(await r.text());
      setData(await r.json());
    } catch (e) {
      showError(e);
    }
  }

  const points = data?.points ?? [];
  const xs = points.map(p => p.x);
  const ys = points.map(p => p.y);

  return (
    <div style={{ border: "1px solid #333", padding: 16, borderRadius: 12, marginTop: 16 }}>
      <h2 style={{ marginTop: 0 }}>Изучение и изменение TabulatedFunction</h2>

      <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 12 }}>
        <button onClick={refresh}>Обновить</button>

        <button onClick={doSerialize}>Сохранить (serialize)</button>
        <button onClick={doDeserialize}>Загрузить (deserialize)</button>

        {data?.insertable && <button onClick={doInsert}>Вставка</button>}
        {data?.removable && <button onClick={doRemove}>Удалить</button>}
      </div>

      <div style={{ display: "flex", gap: 16, flexWrap: "wrap" }}>
        <div style={{ flex: "1 1 520px", minWidth: 320 }}>
          <Plot
            data={[
              { x: xs, y: ys, type: "scatter", mode: "lines+markers" } as any
            ]}
            layout={{
              title: "График",
              autosize: true,
              xaxis: { title: "x" },
              yaxis: { title: "y" },
            }}
            style={{ width: "100%", height: 420 }}
            useResizeHandler
            config={{ responsive: true }}
          />
        </div>

        <div style={{ flex: "1 1 320px", minWidth: 320 }}>
          <h3 style={{ marginTop: 0 }}>apply(x)</h3>
          <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
            <input value={applyX} onChange={e => setApplyX(e.target.value)} placeholder="x" />
            <button onClick={doApply}>Вычислить</button>
          </div>
          {applyResult && <div style={{ marginTop: 8 }}>Результат: {applyResult}</div>}

          <h3 style={{ marginTop: 16 }}>Сериализация</h3>
          <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
            <textarea readOnly value={saveBase64} placeholder="Тут появится base64 после 'Сохранить'" rows={6} />
            <textarea value={loadBase64} onChange={e => setLoadBase64(e.target.value)} placeholder="Вставьте base64 сюда и нажмите 'Загрузить'" rows={6} />
          </div>
        </div>
      </div>
    </div>
  );
}
