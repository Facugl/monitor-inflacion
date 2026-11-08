import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api/v1",
});

export interface VariacionIpc {
  periodo: string; // AAAAMM
  region: string;
  serie: string;
  indice: number;
  variacionMensualPct: number | null;
  variacionInteranualPct: number | null;
  variacionAcumuladaAnioPct: number | null;
}

export interface VariacionCategoria {
  periodo: string; // AAAAMM
  region: string;
  codigo: string; // división COICOP: "01".."12"
  categoria: string;
  indice: number;
  variacionMensualPct: number | null;
  variacionInteranualPct: number | null;
  variacionAcumuladaAnioPct: number | null;
}

export interface EvolucionSueldoReal {
  periodo: string;
  sueldoNominal: number;
  sueldoAjustadoAPrecioInicial: number;
  perdidaOGananciaPoderCompraPct: number;
}

export async function obtenerEvolucionIpc(desde: string, hasta: string): Promise<VariacionIpc[]> {
  const { data } = await api.get<VariacionIpc[]>("/ipc/evolucion", { params: { desde, hasta } });
  return data;
}

export async function obtenerResumenIpc(): Promise<VariacionIpc> {
  const { data } = await api.get<VariacionIpc>("/ipc/resumen");
  return data;
}

export async function obtenerRankingCategorias(periodo?: string): Promise<VariacionCategoria[]> {
  const { data } = await api.get<VariacionCategoria[]>("/ipc/categorias", {
    params: periodo ? { periodo } : undefined,
  });
  return data;
}

export async function sincronizarIpc(): Promise<{
  puntosSincronizados: number;
  puntosNivelGeneral: number;
  puntosCategorias: number;
  mensaje: string;
}> {
  const { data } = await api.post("/ipc/sync");
  return data;
}

export async function simularPoderCompra(
  periodoInicial: string,
  sueldoNominal: number
): Promise<EvolucionSueldoReal[]> {
  const { data } = await api.post<EvolucionSueldoReal[]>("/poder-compra", {
    periodoInicial,
    sueldoNominal,
  });
  return data;
}
