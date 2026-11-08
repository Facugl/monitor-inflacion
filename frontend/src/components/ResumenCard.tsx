import { formatearPeriodo } from "../utils/periodo";
import { COLOR_TEXTO, flechaDe, formatearPct, polaridadDe } from "../utils/variacion";
import type { VariacionIpc } from "../api/client";

function Metric({ label, value }: { label: string; value: number | null }) {
  const polaridad = polaridadDe(value);

  return (
    <div className="flex-1 min-w-[140px] rounded-xl bg-white/5 border border-white/10 p-4">
      <p className="text-xs uppercase tracking-wide text-slate-400">{label}</p>
      <p
        className={`text-2xl font-semibold mt-1 flex items-baseline gap-1.5 tabular-nums ${COLOR_TEXTO[polaridad]}`}
      >
        {/* El signo y la flecha hacen explícita la polaridad; el color solo la refuerza. */}
        {polaridad !== "neutra" && (
          <>
            <span aria-hidden className="text-base">
              {flechaDe(polaridad)}
            </span>
            <span className="sr-only">{polaridad === "suba" ? "subió" : "bajó"}</span>
          </>
        )}
        {formatearPct(value)}
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
