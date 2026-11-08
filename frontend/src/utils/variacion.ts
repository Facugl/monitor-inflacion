/*
 * Un solo lugar que decide cómo se muestra una variación porcentual, para que el
 * resumen y el ranking no se contradigan.
 *
 * El criterio de color: una suba de precios es mala noticia (rosa) y una baja es
 * buena (verde). Pero ese par es indistinguible en deuteranopía (ΔE 4,6 sobre el
 * fondo oscuro, por debajo del piso de 8), así que el color NO puede ser el único
 * portador del dato: el signo explícito y la flecha lo dejan dicho en texto.
 */

export type Polaridad = "suba" | "baja" | "neutra";

export function polaridadDe(valor: number | null | undefined): Polaridad {
  if (valor == null || valor === 0) return "neutra";
  return valor > 0 ? "suba" : "baja";
}

/** `+3.14%`, `−0.20%`, `0.00%`, o `—` si no hay dato. */
export function formatearPct(valor: number | null | undefined): string {
  if (valor == null) return "—";
  const signo = valor > 0 ? "+" : valor < 0 ? "−" : "";
  return `${signo}${Math.abs(valor).toFixed(2)}%`;
}

/** Flecha redundante con el signo, para que la polaridad no dependa del color. */
export function flechaDe(polaridad: Polaridad): string {
  if (polaridad === "suba") return "▲";
  if (polaridad === "baja") return "▼";
  return "";
}

export const COLOR_TEXTO: Record<Polaridad, string> = {
  suba: "text-rose-300",
  baja: "text-emerald-300",
  neutra: "text-slate-300",
};
