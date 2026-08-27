import { useEffect, useState, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchChains, fetchCatalogs, fetchIngestionStatus, triggerIngestion } from '../api';
import { LoadingBanner } from '../components/LoadingBanner';
import { SearchBar } from '../components/SearchBar';
import type { ChainResponse, CatalogResponse, IngestionStatus } from '../types';
import { formatPeriod } from '../utils';

interface ChainWithCatalog extends ChainResponse {
  catalog: CatalogResponse | null;
  catalogCount: number;
}

export function HomePage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<IngestionStatus | null>(null);
  const [chains, setChains] = useState<ChainWithCatalog[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterDate, setFilterDate] = useState<string>(() => {
    const params = new URLSearchParams(window.location.search);
    return params.get('date') || '';
  });
  const [excludedChains, setExcludedChains] = useState<Map<string, string>>(new Map());

  const loadChains = useCallback(async () => {
    setLoading(true);
    try {
      const chainList = await fetchChains();
      const withCatalogs: ChainWithCatalog[] = await Promise.all(
        chainList.map(async (chain) => {
          const catalogs = await fetchCatalogs(chain.dealerId);
          const catalog = catalogs.length > 0 ? catalogs[0] : null;
          return { ...chain, catalog, catalogCount: catalogs.length } as ChainWithCatalog;
        })
      );
      withCatalogs.sort((a, b) => {
        if (a.offerCount === 0 && b.offerCount === 0) return a.name.localeCompare(b.name, 'da');
        if (a.offerCount === 0) return 1;
        if (b.offerCount === 0) return -1;
        if (b.offerCount !== a.offerCount) return b.offerCount - a.offerCount;
        return a.name.localeCompare(b.name, 'da');
      });
      setChains(withCatalogs);
    } catch (err) {
      console.error('Failed to load chains:', err);
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;

    const poll = async () => {
      try {
        const s = await fetchIngestionStatus();
        setStatus(s);
        if (!s.running) {
          clearInterval(interval);
          loadChains();
        }
      } catch {
        // keep polling
      }
    };

    poll();
    interval = setInterval(poll, 2000);
    return () => clearInterval(interval);
  }, [loadChains]);

  const handleSearch = (query: string) => {
    const params = new URLSearchParams();
    if (query) params.set('q', query);
    if (filterDate) params.set('date', filterDate);
    navigate(`/search?${params.toString()}`);
  };

  const handleRefetch = async () => {
    setStatus((prev) => prev ? { ...prev, running: true } : { running: true, lastRun: null, lastRunResult: null });
    try {
      await triggerIngestion();
      setLoading(true);
      const interval = setInterval(async () => {
        const s = await fetchIngestionStatus();
        setStatus(s);
        if (!s.running) {
          clearInterval(interval);
          loadChains();
        }
      }, 2000);
    } catch {
      setStatus((prev) => prev ? { ...prev, running: false } : null);
    }
  };

  const filteredChains = useMemo(() => {
    let result = chains;
    if (filterDate) {
      const d = new Date(filterDate);
      result = result.filter((chain) => {
        if (!chain.catalog) return false;
        const from = new Date(chain.catalog.runFrom);
        const till = new Date(chain.catalog.runTill);
        return from <= d && d <= till;
      });
    }
    return result.filter((chain) => !excludedChains.has(chain.dealerId));
  }, [chains, filterDate, excludedChains]);

  return (
    <div className="max-w-5xl mx-auto p-6">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-3xl font-bold">Tilbud</h1>
        <button className="btn btn-outline btn-sm" onClick={handleRefetch}>
          Hent igen
        </button>
      </div>

      <LoadingBanner status={status} chainsWithOffers={chains.filter(c => c.offerCount > 0).length} />

      <div className="mt-4 mb-6 flex items-center gap-4">
        <div className="flex-1">
          <SearchBar onSearch={handleSearch} disabled={!status || status.running} />
        </div>
        <div className="flex items-center gap-2">
          <label className="text-sm whitespace-nowrap" htmlFor="filter-date">Dato (valgfri):</label>
          <input
            id="filter-date"
            type="date"
            className="input input-bordered input-sm"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
            disabled={!status || status.running}
          />
          {filterDate && (
            <button className="btn btn-ghost btn-xs" onClick={() => setFilterDate('')} disabled={!status || status.running}>Ryd</button>
          )}
        </div>
      </div>

      {excludedChains.size > 0 && (
        <div className="flex flex-wrap gap-2 mb-4">
          <span className="badge badge-outline gap-1 cursor-pointer hover:bg-base-200" onClick={() => setExcludedChains(new Map())}>
            Vælg alle
          </span>
          {[...excludedChains.entries()].map(([dealerId]) => {
            const chain = chains.find((c) => c.dealerId === dealerId);
            const name = chain?.name || excludedChains.get(dealerId) || dealerId;
            return (
              <span key={dealerId} className="badge badge-outline gap-1">
                {name}
                <button
                  className="text-xs cursor-pointer"
                  onClick={() => {
                    setExcludedChains((prev) => {
                      const next = new Map(prev);
                      next.delete(dealerId);
                      return next;
                    });
                  }}
                >×</button>
              </span>
            );
          })}
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-12">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      ) : filteredChains.length === 0 ? (
        <div className="text-center py-12 text-base-content/60">
          Ingen kæder fundet
        </div>
      ) : (
        <table className="table table-zebra w-full [&_td]:py-1 [&_th]:py-1">
          <thead>
            <tr>
              <th className="w-12">Fravælg</th>
              <th>Kæde</th>
              <th>Tilbudsperiode</th>
              <th className="text-right">Kataloger</th>
              <th className="text-right">Antal tilbud</th>
            </tr>
          </thead>
          <tbody>
            {filteredChains.map((chain) => (
              <tr key={chain.dealerId} className="leading-none overflow-visible">
                <td className="text-center">
                  <button
                    className="btn btn-ghost btn-sm text-base-content/50 hover:text-error cursor-pointer px-1 min-h-0 h-auto leading-none"
                    title={`Skjul ${chain.name}`}
                    onClick={() => {
                      setExcludedChains((prev) => {
                        const next = new Map(prev);
                        next.set(chain.dealerId, chain.name);
                        return next;
                      });
                    }}
                  >−</button>
                </td>
                <td className="relative overflow-visible">
                  <div className="flex items-center gap-2">
                    {chain.logoUrl ? (
                      <img
                        src={chain.logoUrl}
                        alt={chain.name}
                        className="w-16 h-16 object-contain rounded transition-transform duration-200 hover:scale-[2.5] origin-left"
                      />
                    ) : (
                      <div
                        className="w-16 h-16 rounded flex items-center justify-center text-lg font-bold text-white transition-transform duration-200 hover:scale-[2.5] origin-left"
                        style={{ backgroundColor: `#${chain.color || '999'}` }}
                      >
                        {chain.name.charAt(0)}
                      </div>
                    )}
                    <span className="font-medium">{chain.name}</span>
                  </div>
                </td>
                <td>
                  {chain.catalog
                    ? formatPeriod(chain.catalog.runFrom, chain.catalog.runTill)
                    : '—'}
                </td>
                <td className="text-right">{chain.catalogCount}</td>
                <td className="text-right">{chain.offerCount}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
