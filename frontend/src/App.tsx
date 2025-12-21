import { useEffect, useState } from "react";
import { setGlobalErrorHandler } from "./errorManager";
import { ErrorModal } from "./ErrorModal";

import { CreateFromPoints } from "./CreateFromPoints";
import { CreateFromMathFunction } from "./CreateFromMathFunction";
import { FunctionLibrary } from "./FunctionLibrary";
import { FunctionStudy } from "./FunctionStudy";

type Theme = "light" | "dark";

function applyTheme(t: Theme) {
  document.documentElement.setAttribute("data-theme", t);
  localStorage.setItem("theme", t);
}

export default function App() {
  const [error, setError] = useState("");

  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem("theme");
    return saved === "dark" || saved === "light" ? saved : "dark";
  });

  useEffect(() => {
    setGlobalErrorHandler(setError);
  }, []);

  useEffect(() => {
    applyTheme(theme);
  }, [theme]);

  return (
    <div className="container">
      {error && <ErrorModal message={error} onClose={() => setError("")} />}

      <div
        className="row"
        style={{
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: 16,
        }}
      >
        <div>
          <div style={{ fontSize: 20, fontWeight: 700 }}>Tabulated Functions UI</div>
          <div style={{ color: "var(--muted)", fontSize: 13 }}>Backend ping: UI Бэк живой</div>
        </div>

        <button
          className="primary"
          onClick={() => setTheme((t) => (t === "dark" ? "light" : "dark"))}
          aria-label="Переключить тему"
        >
          {theme === "dark" ? "☀️ Светлая тема" : "🌙 Тёмная тема"}
        </button>
      </div>

      <div className="app-shell">
        <div className="left-col">
          <div className="card">
            <CreateFromPoints onError={setError} />
          </div>

          <div className="card">
            <CreateFromMathFunction />
          </div>

          <div className="card">
            <FunctionLibrary onOpened={() => {}} />
          </div>
        </div>

        <div className="right-col">
          <div className="card card-fill">
            <div className="study-body">
              <FunctionStudy />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}






