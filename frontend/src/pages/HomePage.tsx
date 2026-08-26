import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchChains, fetchCatalogs, fetchIngestionStatus, triggerIngestion } from '../api';
import { LoadingBanner } from '../components/LoadingBanner';
import { SearchBar } from '../components/SearchBar';
import type { ChainResponse, CatalogResponse, IngestionStatus } from '../types';

interface ChainWithCatalog extends ChainResponse {
  catalog: CatalogResponse | null;
  catalogCount: number;
}

function formatPeriod(from: string, till: string): string {
  const f = new Date(from);
  const t = new Date(till);
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short' };
  return `${f.toLocaleDateString('da-DK', opts)} – ${t.toLocaleDateString('da-DK', opts)}`;
}

export function HomePage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<IngestionStatus | null>(null);
  const [chains, setChains] = useState<ChainWithCatalog[]>([]);
  const [loading, setLoading] = useState(true);

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
    navigate(`/search?q=${encodeURIComponent(query)}`);
  };

  const handleRefetch = async () => {
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
      // already showing status
    }
  };

  return (
    <div className="max-w-5xl mx-auto p-6">
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-3xl font-bold">Tilbud</h1>
        <button className="btn btn-outline btn-sm" onClick={handleRefetch}>
          Hent igen
        </button>
      </div>

      <LoadingBanner status={status} chainsWithOffers={chains.filter(c => c.offerCount > 0).length} />

      <div className="mt-4 mb-6">
        <SearchBar onSearch={handleSearch} />
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      ) : chains.length === 0 ? (
        <div className="text-center py-12 text-base-content/60">
          Ingen kæder fundet
        </div>
      ) : (
        <table className="table table-zebra w-full [&_td]:py-0.5 [&_th]:py-0.5">
          <thead>
            <tr>
              <th className="w-16"></th>
              <th>Navn</th>
              <th>Tilbudsperiode</th>
              <th className="text-right">Kataloger</th>
              <th className="text-right">Antal tilbud</th>
            </tr>
          </thead>
          <tbody>
            {chains.map((chain) => (
              <tr key={chain.dealerId} className="leading-none">
                <td className="relative overflow-visible">
                  {chain.logoUrl ? (
                    <img
                      src={chain.logoUrl}
                      alt={chain.name}
                      className="w-16 h-16 object-contain rounded transition-transform duration-200 hover:scale-[2.5] hover:absolute hover:z-10"
                    />
                  ) : (
                    <div
                      className="w-16 h-16 rounded flex items-center justify-center text-lg font-bold text-white transition-transform duration-200 hover:scale-[2.5] hover:absolute hover:z-10"
                      style={{ backgroundColor: `#${chain.color || '999'}` }}
                    >
                      {chain.name.charAt(0)}
                    </div>
                  )}
                </td>
                <td className="font-medium">{chain.name}</td>
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
