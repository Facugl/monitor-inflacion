const MESES = [
  "ene", "feb", "mar", "abr", "may", "jun",
  "jul", "ago", "sep", "oct", "nov", "dic",
];

/** Convierte "202610" -> "oct 2026" */
export function formatearPeriodo(codigoAAAAMM: string): string {
  const anio = codigoAAAAMM.slice(0, 4);
  const mes = parseInt(codigoAAAAMM.slice(4, 6), 10);
  return `${MESES[mes - 1]} ${anio}`;
}

/** Período actual menos N meses, en formato AAAAMM. */
export function periodoHaceNMeses(n: number, desde = new Date()): string {
  const fecha = new Date(desde.getFullYear(), desde.getMonth() - n, 1);
  const anio = fecha.getFullYear();
  const mes = String(fecha.getMonth() + 1).padStart(2, "0");
  return `${anio}${mes}`;
}
