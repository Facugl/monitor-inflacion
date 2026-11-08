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

/*
 * Dos gráficos, no uno con dos ejes.
 *
 * La mensual se mueve en el orden de 1-25% y la interanual en el de 100-290%.
 * Dibujadas en el mismo eje, la mensual queda aplastada contra el cero: una línea
 * plana ilegible. La alternativa clásica —un segundo eje Y a la derecha— es peor,
 * porque el punto donde se cruzan las dos líneas depende de cómo elegís las dos
 * escalas, no de los datos: sugiere coincidencias que no existen.
 *
 * Separarlas deja a cada serie con su propia escala y mantiene el eje X compartido,
 * que es lo que realmente hace falta para compararlas: los picos y valles siguen
 * alineados verticalmente.
 */

const EJE = "#94a3b8";
const GRILLA = "#334155";

/*
 * Pasados por el validador de paleta contra la superficie real de la tarjeta
 * (#0d1426): banda de luminosidad, piso de croma, separación en protanopía /
 * deuteranopía / tritanopía y contraste. Los sky-400 / rose-400 de Tailwind que
 * estaban antes quedaban fuera de la banda para fondo oscuro (L 0,75 y 0,72
 * contra un techo de 0,67): sobre negro "brillan" y el trazo de 2px se desdibuja.
 */
const COLOR_MENSUAL = "#0d9ce6";
const COLOR_INTERANUAL = "#f03a5f";

const ESTILO_TOOLTIP = {
  background: "#0f172a",
  border: "1px solid #334155",
  borderRadius: 8,
} as const;

/**
 * Techo redondeado para el eje Y. Sin esto Recharts parte el máximo crudo en cinco
 * y salen ticks como 0,95% / 1,9% / 2,85%, que no se leen de un vistazo.
 */
const MULTIPLOS_LINDOS = [1, 1.5, 2, 2.5, 3, 4, 5, 6, 8, 10];

function techoLindo(maximo: number): number {
  if (!Number.isFinite(maximo) || maximo <= 0) return 1;
  const magnitud = 10 ** Math.floor(Math.log10(maximo));
  const razon = maximo / magnitud;
  const multiplo = MULTIPLOS_LINDOS.find((m) => m >= razon) ?? 10;
  return multiplo * magnitud;
}

type Punto = { periodo: string; valor: number | null };

function Serie({
  titulo,
  descripcion,
  puntos,
  color,
  intervaloEjeX,
}: {
  titulo: string;
  descripcion: string;
  puntos: Punto[];
  color: string;
  intervaloEjeX: number;
}) {
  const techo = techoLindo(
    Math.max(...puntos.map((p) => p.valor ?? 0))
  );

  return (
    <div>
      <div className="flex items-baseline gap-2">
        {/* El punto de color hace de leyenda: con una sola serie por gráfico no
            hace falta una caja de leyenda, el título ya la nombra. */}
        <span
          aria-hidden
          className="size-2 rounded-full shrink-0"
          style={{ background: color }}
        />
        <h3 className="text-slate-200 text-sm font-medium">{titulo}</h3>
      </div>
      <p className="text-xs text-slate-500 mt-0.5 mb-3">{descripcion}</p>

      <ResponsiveContainer width="100%" height={220}>
        <LineChart data={puntos} margin={{ top: 5, right: 16, bottom: 0, left: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke={GRILLA} vertical={false} />
          <XAxis
            dataKey="periodo"
            stroke={EJE}
            fontSize={11}
            tickMargin={6}
            interval={intervaloEjeX}
          />
          <YAxis
            stroke={EJE}
            fontSize={11}
            width={52}
            domain={[0, techo]}
            ticks={Array.from({ length: 5 }, (_, i) => (techo / 4) * i)}
            tickFormatter={(valor: number) => `${Number(valor.toFixed(2))}%`}
          />
          <Tooltip
            cursor={{ stroke: EJE, strokeDasharray: "3 3" }}
            contentStyle={ESTILO_TOOLTIP}
            labelStyle={{ color: "#e2e8f0" }}
            formatter={((valor: unknown) => [`${Number(valor).toFixed(2)}%`, titulo]) as never}
          />
          <Line
            type="monotone"
            dataKey="valor"
            name={titulo}
            stroke={color}
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 4, strokeWidth: 0 }}
            connectNulls={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export function GraficoEvolucionIpc({ datos }: { datos: VariacionIpc[] }) {
  const etiquetas = datos.map((d) => formatearPeriodo(d.periodo));

  const mensual: Punto[] = datos.map((d, i) => ({
    periodo: etiquetas[i],
    valor: d.variacionMensualPct,
  }));
  const interanual: Punto[] = datos.map((d, i) => ({
    periodo: etiquetas[i],
    valor: d.variacionInteranualPct,
  }));

  const rango =
    etiquetas.length > 1
      ? `${etiquetas[0]} – ${etiquetas[etiquetas.length - 1]}`
      : etiquetas[0];

  // Mismo salto de ticks en los dos para que el eje X se lea como uno compartido.
  const intervaloEjeX = Math.max(Math.ceil(datos.length / 8) - 1, 0);

  return (
    <div className="rounded-2xl bg-slate-900/60 border border-white/10 p-6">
      <h2 className="text-slate-200 font-medium">Cómo viene evolucionando</h2>
      <p className="text-slate-400 text-sm mt-1 mb-6">
        {rango} · región {datos[0].region.toLowerCase()}
      </p>

      <div className="flex flex-col gap-7">
        <Serie
          titulo="Variación mensual"
          descripcion="Cuánto subieron los precios respecto del mes anterior."
          puntos={mensual}
          color={COLOR_MENSUAL}
          intervaloEjeX={intervaloEjeX}
        />
        <Serie
          titulo="Variación interanual"
          descripcion="Cuánto subieron respecto del mismo mes del año anterior."
          puntos={interanual}
          color={COLOR_INTERANUAL}
          intervaloEjeX={intervaloEjeX}
        />
      </div>
    </div>
  );
}
