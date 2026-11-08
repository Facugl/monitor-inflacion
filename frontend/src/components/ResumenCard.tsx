import { formatearPeriodo } from "../utils/periodo";
import type { VariacionIpc } from "../api/client";

function Metric({ label, value }: { label: string; value: number | null }) {
  const positivo = value != null && value >= 0;
  return (
    <div className="flex-1 min-w-[140px] rounded-xl bg-white/5 border border-white/10 p-4">
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p className={`text-2xl font-semibold mt-1 ${positivo ? "text-rose-400" : "text-emerald-400"}`}>
        {value != null ? `${value > 0 ? "+" : ""}${value.toFixed(2)}%` : "—"}
      </p>
    </div>
  );
}

export function ResumenCard({ resumen }: { resumen: VariacionIpc }) {
  return (
    <div className="rounded-2xl bg-slate-900/60 border border-white/10 p-6">
      <p className="text-slate-400 text-sm">
        Dato del mes · {formatearPeriodo(resumen.periodo)} · región {resumen.region.toLowerCase()}
      </p>
      <div className="flex flex-wrap gap-4 mt-4">
        <Metric label="Variación mensual" value={resumen.variacionMensualPct} />
        <Metric label="Variación interanual" value={resumen.variacionInteranualPct} />
        <Metric label="Acumulada en el año" value={resumen.variacionAcumuladaAnioPct} />
      </div>
    </div>
  );
}
