import { useQuery } from "@tanstack/react-query";
import { obtenerEvolucionIpc, obtenerRankingCategorias, obtenerResumenIpc } from "../api/client";
import { periodoHaceNMeses } from "../utils/periodo";

export function useEvolucionIpc(meses = 24) {
  return useQuery({
    queryKey: ["ipc", "evolucion", meses],
    queryFn: () => obtenerEvolucionIpc(periodoHaceNMeses(meses), periodoHaceNMeses(0)),
  });
}

export function useResumenIpc() {
  return useQuery({
    queryKey: ["ipc", "resumen"],
    queryFn: obtenerResumenIpc,
    retry: false,
  });
}

/** Ranking de categorías del último período sincronizado. */
export function useRankingCategorias() {
  return useQuery({
    queryKey: ["ipc", "categorias"],
    queryFn: () => obtenerRankingCategorias(),
    retry: false,
  });
}
