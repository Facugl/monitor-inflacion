import { formatearPeriodo } from "../utils/periodo";
import { COLOR_TEXTO, flechaDe, formatearPct, polaridadDe } from "../utils/variacion";
import type { VariacionCategoria } from "../api/client";

/** Ancho mínimo visible para que una barra de valor chico no desaparezca. */
const ANCHO_MINIMO_PCT = 1.5;

function Fila({
  dato,
  maximo,
  nivelGeneralPct,
}: {
  dato: VariacionCategoria;
  maximo: number;
  nivelGeneralPct: number | null;
}) {
  const valor = dato.variacionMensualPct;
  const polaridad = polaridadDe(valor);

  const anchoPct =
    valor == null || maximo === 0
      ? 0
      : Math.max((Math.abs(valor) / maximo) * 100, ANCHO_MINIMO_PCT);

  // Criterio de color y encoding redundante: ver utils/variacion.ts
  const colorBarra = polaridad === "baja" ? "bg-emerald-400/80" : "bg-rose-400/80";

  const detalle = [
    `${dato.categoria} (división ${dato.codigo})`,
    `Mensual: ${formatearPct(valor)}`,
    `Interanual: ${formatearPct(dato.variacionInteranualPct)}`,
    `Acumulada en el año: ${formatearPct(dato.variacionAcumuladaAnioPct)}`,
  ].join("\n");

  return (
    <div
      title={detalle}
      className="grid grid-cols-[1fr_auto] sm:grid-cols-[minmax(0,16rem)_1fr_auto] items-center gap-x-3 gap-y-1 py-1.5 px-2 -mx-2 rounded-lg hover:bg-white/5 transition-colors"
    >
      <p className="text-sm text-slate-300 truncate" title={dato.categoria}>
        {dato.categoria}
      </p>

      {/* Pista de la barra. En mobile pasa a ocupar el ancho completo debajo del nombre. */}
      <div className="relative col-span-2 sm:col-span-1 h-2.5 order-last sm:order-none">
        <div
          className={`h-full rounded-[4px] ${colorBarra}`}
          style={{ width: `${anchoPct}%` }}
        />
        {nivelGeneralPct != null && maximo > 0 && (
          <div
            aria-hidden
            className="absolute top-[-3px] bottom-[-3px] w-px bg-slate-400/60"
            style={{ left: `${Math.min((nivelGeneralPct / maximo) * 100, 100)}%` }}
          />
        )}
      </div>

      <p
        className={`text-sm font-medium tabular-nums text-right whitespace-nowrap ${COLOR_TEXTO[polaridad]}`}
      >
        {/* A diferencia del resumen, acá la flecha va solo en las bajas: casi todas
            las filas son subas, así que marcar la excepción se lee mejor que repetir
            doce veces el mismo ▲. El signo del número sigue estando en las dos. */}
        {polaridad === "baja" && (
          <>
            <span aria-hidden className="text-[0.7rem]">
              {flechaDe(polaridad)}{" "}
            </span>
            <span className="sr-only">bajó</span>
          </>
        )}
        {formatearPct(valor)}
      </p>
    </div>
  );
}

export function RankingCategorias({
  datos,
  nivelGeneralPct,
}: {
  datos: VariacionCategoria[];
  nivelGeneralPct?: number | null;
}) {
  if (datos.length === 0) return null;

  const periodo = datos[0].periodo;
  const maximo = Math.max(
    ...datos.map((d) => Math.abs(d.variacionMensualPct ?? 0)),
    nivelGeneralPct ?? 0
  );

  return (
    <div className="rounded-2xl bg-slate-900/60 border border-white/10 p-6">
      <div className="flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1 mb-1">
        <h2 className="text-slate-200 font-medium">Qué aumentó más</h2>
        <p className="text-xs text-slate-500">{formatearPeriodo(periodo)}</p>
      </div>
      <p className="text-slate-400 text-sm mb-5">
        Variación mensual por división, de mayor a menor.
        {nivelGeneralPct != null && (
          <> La línea marca el nivel general ({formatearPct(nivelGeneralPct)}).</>
        )}
      </p>

      <div className="flex flex-col gap-0.5">
        {datos.map((dato) => (
          <Fila
            key={dato.codigo}
            dato={dato}
            maximo={maximo}
            nivelGeneralPct={nivelGeneralPct ?? null}
          />
        ))}
      </div>

      <p className="text-xs text-slate-500 mt-5">
        Divisiones COICOP Argentina 2019 · interanual y acumulada al pasar el mouse
      </p>
    </div>
  );
}
