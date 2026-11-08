import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { simularPoderCompra, type EvolucionSueldoReal } from "../api/client";
import { formatearPeriodo, periodoHaceNMeses } from "../utils/periodo";

const formatoMoneda = new Intl.NumberFormat("es-AR", {
  style: "currency",
  currency: "ARS",
  maximumFractionDigits: 0,
});

export function CalculadoraPoderCompra() {
  const [periodoInicial, setPeriodoInicial] = useState(periodoHaceNMeses(12));
  const [sueldoNominal, setSueldoNominal] = useState("800000");

  const mutation = useMutation({
    mutationFn: () => simularPoderCompra(periodoInicial, Number(sueldoNominal)),
  });

  const resultado: EvolucionSueldoReal[] | undefined = mutation.data;
  const ultimo = resultado?.at(-1);

  return (
    <div className="rounded-2xl bg-slate-900/60 border border-white/10 p-6">
      <h2 className="text-slate-200 font-medium mb-1">¿Cuánto perdiste contra la inflación?</h2>
      <p className="text-slate-400 text-sm mb-4">
        Ingresá desde cuándo cobrás ese sueldo sin aumentos y te mostramos qué pasó con tu poder de compra real.
      </p>

      <form
        className="flex flex-wrap gap-3 items-end"
        onSubmit={(e) => {
          e.preventDefault();
          mutation.mutate();
        }}
      >
        <label className="flex flex-col text-sm text-slate-300">
          Período inicial (AAAAMM)
          <input
            className="mt-1 rounded-lg bg-slate-800 border border-white/10 px-3 py-2 text-slate-100 w-40"
            value={periodoInicial}
            onChange={(e) => setPeriodoInicial(e.target.value)}
            pattern="\d{6}"
            required
          />
        </label>

        <label className="flex flex-col text-sm text-slate-300">
          Sueldo nominal ($)
          <input
            className="mt-1 rounded-lg bg-slate-800 border border-white/10 px-3 py-2 text-slate-100 w-40"
            type="number"
            min="1"
            value={sueldoNominal}
            onChange={(e) => setSueldoNominal(e.target.value)}
            required
          />
        </label>

        <button
          type="submit"
          className="rounded-lg bg-sky-500 hover:bg-sky-400 transition-colors px-4 py-2 text-sm font-medium text-slate-900"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Calculando…" : "Calcular"}
        </button>
      </form>

      {mutation.isError && (
        <p className="text-rose-400 text-sm mt-4">
          No se pudo calcular. Verificá que el período tenga datos sincronizados.
        </p>
      )}

      {ultimo && (
        <div className="mt-6 grid gap-4 sm:grid-cols-2">
          <div className="rounded-xl bg-white/5 border border-white/10 p-4">
            <p className="text-xs uppercase tracking-wide text-slate-400">
              Hoy, tus {formatoMoneda.format(Number(sueldoNominal))} valen (a precios de {formatearPeriodo(periodoInicial)})
            </p>
            <p className="text-2xl font-semibold mt-1 text-slate-100">
              {formatoMoneda.format(ultimo.sueldoAjustadoAPrecioInicial)}
            </p>
          </div>
          <div className="rounded-xl bg-white/5 border border-white/10 p-4">
            <p className="text-xs uppercase tracking-wide text-slate-400">Pérdida de poder de compra</p>
            <p className="text-2xl font-semibold mt-1 text-rose-400">
              {ultimo.perdidaOGananciaPoderCompraPct.toFixed(2)}%
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
