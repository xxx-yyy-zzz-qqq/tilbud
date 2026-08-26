import type { IngestionStatus } from '../types';

interface LoadingBannerProps {
  status: IngestionStatus | null;
}

export function LoadingBanner({ status }: LoadingBannerProps) {
  if (!status) {
    return (
      <div className="alert alert-info">
        <span className="loading loading-spinner loading-sm"></span>
        <span>Henter status...</span>
      </div>
    );
  }

  if (status.running) {
    const r = status.lastRunResult;
    return (
      <div className="alert alert-info">
        <span className="loading loading-spinner loading-sm"></span>
        <div>
          <div className="font-bold">Henter tilbud...</div>
          {r && (
            <div className="text-sm">
              {r.chainsProcessed} kæder, {r.offersInserted} tilbud hentet
              {r.errors > 0 && <span className="text-error">, {r.errors} fejl</span>}
            </div>
          )}
        </div>
      </div>
    );
  }

  if (status.lastRunResult) {
    const r = status.lastRunResult;
    const runTime = status.lastRun
      ? new Date(status.lastRun).toLocaleString('da-DK')
      : 'ukendt';
    return (
      <div className="alert alert-success">
        <div>
          <div className="text-sm">
            Sidste hentning: {runTime} — {r.chainsProcessed} kæder, {r.catalogsProcessed} kataloger, {r.offersInserted} tilbud
            {r.errors > 0 && <span className="text-error">, {r.errors} fejl</span>}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="alert alert-warning">
      <span>Ingen data hentet endnu</span>
    </div>
  );
}
