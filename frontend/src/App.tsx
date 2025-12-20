import { useEffect, useState } from "react";
import { setGlobalErrorHandler, showError } from "./errorManager";
import { ErrorModal } from "./ErrorModal";
import { CreateFromPoints } from "./CreateFromPoints";
import { CreateFromMathFunction } from "./CreateFromMathFunction";
import { getText } from "./api";

export default function App() {
  const [error, setError] = useState("");
  const [ping, setPing] = useState("...");

  useEffect(() => {
    setGlobalErrorHandler(setError);
    getText("/api/v1/ui/ping")
      .then(setPing)
      .catch(showError);
  }, []);

  return (
    <div style={{ padding: 20 }}>
      {error && <ErrorModal message={error} onClose={() => setError("")} />}

      <div style={{ marginBottom: 12, opacity: 0.8 }}>
        Backend ping: {ping}
      </div>

      {/* 1) Создание из точек */}
      <CreateFromPoints onError={setError} />

      {/* 2) Создание из MathFunction */}
      <CreateFromMathFunction />
    </div>
  );
}



