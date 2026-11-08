import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { formatearPeriodo } from "../utils/periodo";
import type { VariacionIpc } from "../api/client";

export function GraficoEvolucionIpc({ datos }: { datos: VariacionIpc[] }) {
  const puntos = datos.map((d) => ({
    periodo: formatearPeriodo(d.periodo),
    mensual: d.variacionMensualPct,
    interanual: d.variacionInteranualPct,
  }));

  return (
    <div className="rounded-2xl bg-slate-900/60 border border-white/10 p-6">
      <h2 className="text-slate-200 font-medium mb-4">Variación mensual del IPC (%)</h2>
      <ResponsiveContainer width="100%" height={320}>
        <LineChart data={puntos} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
          <XAxis dataKey="periodo" stroke="#94a3b8" fontSize={12} interval={Math.ceil(puntos.length / 12)} />
          <YAxis stroke="#94a3b8" fontSize={12} unit="%" />
          <Tooltip
            contentStyle={{ background: "#0f172a", border: "1px solid #334155", borderRadius: 8 }}
            labelStyle={{ color: "#e2e8f0" }}
            formatter={((value: unknown) => [`${Number(value).toFixed(2)}%`, "Variación"]) as never}
          />
          <Line type="monotone" dataKey="mensual" name="Mensual" stroke="#38bdf8" strokeWidth={2} dot={false} />
          <Line type="monotone" dataKey="interanual" name="Interanual" stroke="#fb7185" strokeWidth={2} dot={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
