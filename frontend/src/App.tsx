import { useMutation, useQueryClient } from "@tanstack/react-query";
import { sincronizarIpc } from "./api/client";
import { useEvolucionIpc, useRankingCategorias, useResumenIpc } from "./hooks/useIpc";
import { ResumenCard } from "./components/ResumenCard";
import { GraficoEvolucionIpc } from "./components/GraficoEvolucionIpc";
import { RankingCategorias } from "./components/RankingCategorias";
import { CalculadoraPoderCompra } from "./components/CalculadoraPoderCompra";

function App() {
  const queryClient = useQueryClient();
  const resumen = useResumenIpc();
  const evolucion = useEvolucionIpc(24);
  const categorias = useRankingCategorias();

  const sync = useMutation({
    mutationFn: sincronizarIpc,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["ipc"] });
    },
  });

  const sinDatosTodavia = resumen.isError;

  return (
    <div className="min-h-screen bg-[#0b1020] text-slate-100">
      <div className="max-w-5xl mx-auto px-4 py-10">
        <header className="flex flex-wrap items-start justify-between gap-4 mb-8">
          <div>
            <h1 className="text-2xl sm:text-3xl font-semibold">Monitor de Inflación Argentina</h1>
            <p className="text-slate-400 mt-1 text-sm">
              Datos oficiales del INDEC (API pública), sincronizados a demanda.
            </p>
          </div>
          <button
            onClick={() => sync.mutate()}
            disabled={sync.isPending}
            className="rounded-lg bg-white/10 hover:bg-white/20 transition-colors px-4 py-2 text-sm font-medium"
          >
            {sync.isPending ? "Sincronizando…" : "Sincronizar con INDEC"}
          </button>
        </header>

        {sinDatosTodavia && (
          <div className="rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 p-4 mb-6 text-sm">
            Todavía no hay datos cargados. Hacé click en <strong>"Sincronizar con INDEC"</strong> para traer la serie.
          </div>
        )}

        <div className="grid gap-6">
          {resumen.data && <ResumenCard resumen={resumen.data} />}
          {evolucion.data && evolucion.data.length > 0 && <GraficoEvolucionIpc datos={evolucion.data} />}
          {categorias.data && (
            <RankingCategorias
              datos={categorias.data}
              nivelGeneralPct={resumen.data?.variacionMensualPct ?? null}
            />
          )}
          <CalculadoraPoderCompra />
        </div>

        <footer className="text-slate-500 text-xs mt-10 text-center">
          Hecho por{" "}
          <a className="underline hover:text-slate-300" href="https://github.com/Facugl" target="_blank" rel="noreferrer">
            Facundo Luna
          </a>{" "}
          · Fuente:{" "}
          <a className="underline hover:text-slate-300" href="https://api.indec.gob.ar" target="_blank" rel="noreferrer">
            API-INDEC
          </a>
        </footer>
      </div>
    </div>
  );
}

export default App;
