import Plot from "react-plotly.js";

export type PlotPoint = { x: number; y: number };

export function FunctionPlot({ points }: { points: PlotPoint[] }) {
  const xs = points.map(p => p.x);
  const ys = points.map(p => p.y);

  return (
    <Plot
      data={[
        {
          x: xs,
          y: ys,
          type: "scatter",
          mode: "lines+markers",
        } as any,
      ]}
      layout={{
        title: "График функции",
        autosize: true,
        xaxis: { title: "x" },
        yaxis: { title: "y" },
      }}
      style={{ width: "100%", height: 400 }}
      useResizeHandler
      config={{ responsive: true }}
    />
  );
}
