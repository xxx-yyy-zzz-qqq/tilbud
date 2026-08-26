import { useEffect, useState, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAllOffers } from '../api';
import { SearchBar } from '../components/SearchBar';
import type { OfferResponse } from '../types';

type SortKey = 'price' | 'runFrom' | 'runTill' | 'chain';
type SortDir = 'asc' | 'desc';

function formatPrice(kr: number): string {
  return `${kr.toFixed(2)} kr`.replace('.', ',');
}

function formatDate(iso: string): string {
  const d = new Date(iso);
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' };
  return d.toLocaleDateString('da-DK', opts);
}

function getThumbUrl(images: string | null): string | null {
  if (!images) return null;
  try {
    const parsed = JSON.parse(images);
    return parsed.thumb || null;
  } catch {
    return null;
  }
}

function dedupOffers(offers: OfferResponse[]): OfferResponse[] {
  const seen = new Set<string>();
  return offers.filter((o) => {
    const key = `${o.heading}|${o.price}|${o.runFrom}|${o.runTill}|${o.chain.dealerId}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

function compareValues(a: number | string, b: number | string, dir: SortDir): number {
  const mul = dir === 'asc' ? 1 : -1;
  if (typeof a === 'number' && typeof b === 'number') return (a - b) * mul;
  return String(a).localeCompare(String(b), 'da') * mul;
}

export function SearchPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';

  const [query, setQuery] = useState(initialQuery);
  const [offers, setOffers] = useState<OfferResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [sortKey, setSortKey] = useState<SortKey>('price');
  const [sortDir, setSortDir] = useState<SortDir>('asc');

  const sortedOffers = useMemo(() => {
    const copy = [...offers];
    copy.sort((a, b) => {
      switch (sortKey) {
        case 'price': return compareValues(a.price, b.price, sortDir);
        case 'runFrom': return compareValues(a.runFrom, b.runFrom, sortDir);
        case 'runTill': return compareValues(a.runTill, b.runTill, sortDir);
        case 'chain': return compareValues(a.chain.name, b.chain.name, sortDir);
        default: return 0;
      }
    });
    return copy;
  }, [offers, sortKey, sortDir]);

  const toggleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('asc');
    }
  };

  const SortIcon = ({ column }: { column: SortKey }) => {
    if (sortKey !== column) return <span className="opacity-30 ml-1 text-xs">↕</span>;
    return <span className="ml-1 text-xs">{sortDir === 'asc' ? '▲' : '▼'}</span>;
  };

  const thClass = (key: SortKey) =>
    `cursor-pointer select-none whitespace-nowrap ${sortKey === key ? 'text-primary font-bold' : ''}`;

  const doSearch = async (q: string) => {
    setQuery(q);
    setLoading(true);
    setSearched(true);
    try {
      const results = await fetchAllOffers(q);
      setOffers(dedupOffers(results));
    } catch (err) {
      console.error('Search failed:', err);
      setOffers([]);
    }
    setLoading(false);
  };

  useEffect(() => {
    if (initialQuery) {
      doSearch(initialQuery);
    }
  }, []);

  const handleSearch = (q: string) => {
    navigate(`/search?q=${encodeURIComponent(q)}`, { replace: true });
    doSearch(q);
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="flex items-center gap-4 mb-6">
        <button className="btn btn-outline btn-sm" onClick={() => navigate('/')}>
          Hjem
        </button>
        <div className="flex-1">
          <SearchBar onSearch={handleSearch} initialValue={initialQuery} />
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-12">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      ) : !searched ? (
        <div className="text-center py-12 text-base-content/60">
          Indtast et søgeord for at finde tilbud
        </div>
      ) : offers.length === 0 ? (
        <div className="text-center py-12 text-base-content/60">
          Ingen tilbud fundet for "{query}"
        </div>
      ) : (
        <>
          <div className="mb-4 text-sm text-base-content/60">
            {offers.length} tilbud fundet
          </div>
          <table className="table table-zebra w-full">
            <thead>
              <tr>
                <th className="w-16"></th>
                <th>Tilbud</th>
                <th className={thClass('price')} onClick={() => toggleSort('price')}>
                  Pris<SortIcon column="price" />
                </th>
                <th className={thClass('runFrom')} onClick={() => toggleSort('runFrom')}>
                  Gyldig fra<SortIcon column="runFrom" />
                </th>
                <th className={thClass('runTill')} onClick={() => toggleSort('runTill')}>
                  Gyldig til<SortIcon column="runTill" />
                </th>
                <th className={thClass('chain')} onClick={() => toggleSort('chain')}>
                  Kæde<SortIcon column="chain" />
                </th>
              </tr>
            </thead>
            <tbody>
              {sortedOffers.map((offer) => {
                const thumb = getThumbUrl(offer.images);
                return (
                  <tr key={offer.id}>
                    <td>
                      {thumb ? (
                        <img
                          src={thumb}
                          alt={offer.heading}
                          className="w-12 h-12 object-cover rounded"
                        />
                      ) : (
                        <div className="w-12 h-12 bg-base-200 rounded flex items-center justify-center text-xs">
                          —
                        </div>
                      )}
                    </td>
                    <td>
                      <div className="font-medium">{offer.heading}</div>
                    </td>
                    <td>
                      <span className="font-bold">{formatPrice(offer.price)}</span>
                      {offer.prePrice && (
                        <span className="text-sm line-through text-base-content/50 ml-2">
                          {formatPrice(offer.prePrice)}
                        </span>
                      )}
                    </td>
                    <td className="text-sm whitespace-nowrap">
                      {formatDate(offer.runFrom)}
                    </td>
                    <td className="text-sm whitespace-nowrap">
                      {formatDate(offer.runTill)}
                    </td>
                    <td className="text-sm">{offer.chain.name}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}
