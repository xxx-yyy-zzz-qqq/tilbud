import { useEffect, useState, useMemo, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAllOffers, fetchChains } from '../api';
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

function getImageUrl(images: string | null): string | null {
  if (!images) return null;
  try {
    const parsed = JSON.parse(images);
    return parsed.view || parsed.thumb || null;
  } catch {
    return null;
  }
}

function getZoomUrl(images: string | null): string | null {
  if (!images) return null;
  try {
    const parsed = JSON.parse(images);
    return parsed.zoom || parsed.view || parsed.thumb || null;
  } catch {
    return null;
  }
}

function formatQuantity(quantity: string | null): string {
  if (!quantity) return '';
  try {
    const q = JSON.parse(quantity);
    const unit = q.unit?.symbol || '';
    const sizeFrom = q.size?.from;
    const sizeTo = q.size?.to;
    const piecesFrom = q.pieces?.from;
    const piecesTo = q.pieces?.to;
    const piecesMax = q.pieces?.max;

    const sizeStr = sizeFrom != null
      ? sizeFrom === sizeTo ? `${sizeFrom}` : `${sizeFrom}-${sizeTo}`
      : '';
    const unitStr = sizeStr && unit ? `${sizeStr} ${unit}` : unit || '';

    if (piecesFrom != null && unitStr) {
      const piecesStr = piecesFrom === piecesTo ? `${piecesFrom}` : `${piecesFrom}-${piecesTo}`;
      const maxStr = piecesMax ? ` (maks ${piecesMax})` : '';
      return `${piecesStr} × ${unitStr}${maxStr}`;
    }
    if (unitStr) return unitStr;
    return '';
  } catch {
    return '';
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
  const initialDate = searchParams.get('date') || '';

  const [query, setQuery] = useState(initialQuery);
  const [offers, setOffers] = useState<OfferResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [sortKey, setSortKey] = useState<SortKey>('price');
  const [sortDir, setSortDir] = useState<SortDir>('asc');
  const [filterDate, setFilterDate] = useState<string>(initialDate);
  const [excludedChains, setExcludedChains] = useState<Map<string, string>>(() => {
    const params = new URLSearchParams(window.location.search);
    const exclude = params.get('exclude');
    return exclude ? new Map(exclude.split(',').map((id) => [id, ''])) : new Map();
  });
  const [zoomImage, setZoomImage] = useState<{ src: string; alt: string } | null>(null);
  const [hoverImage, setHoverImage] = useState<{ src: string; alt: string; rect: DOMRect } | null>(null);

  const handleKeyDown = useCallback((e: KeyboardEvent) => {
    if (e.key === 'Escape') { setZoomImage(null); setHoverImage(null); }
  }, []);

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);

  const sortedOffers = useMemo(() => {
    let filtered = [...offers];
    if (filterDate) {
      const d = new Date(filterDate);
      filtered = filtered.filter((o) => {
        const from = new Date(o.runFrom);
        const till = new Date(o.runTill);
        return from <= d && d <= till;
      });
    }
    if (excludedChains.size > 0) {
      filtered = filtered.filter((o) => !excludedChains.has(o.chain.dealerId));
    }
    filtered.sort((a, b) => {
      switch (sortKey) {
        case 'price': return compareValues(a.price, b.price, sortDir);
        case 'runFrom': return compareValues(a.runFrom, b.runFrom, sortDir);
        case 'runTill': return compareValues(a.runTill, b.runTill, sortDir);
        case 'chain': return compareValues(a.chain.name, b.chain.name, sortDir);
        default: return 0;
      }
    });
    return filtered;
  }, [offers, sortKey, sortDir, filterDate, excludedChains]);

  const toggleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDir((d) => (d === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortKey(key);
      setSortDir('asc');
    }
  };

  const SortIcon = ({ column }: { column: SortKey }) => {
    if (sortKey !== column) return <span className="opacity-50 ml-1.5 text-xs">▲▼</span>;
    return <span className="ml-1.5 text-sm text-primary font-bold">{sortDir === 'asc' ? '▲' : '▼'}</span>;
  };

  const thClass = (key: SortKey) =>
    `cursor-pointer select-none whitespace-nowrap ${sortKey === key ? 'text-primary font-bold' : ''}`;

  const doSearch = async (q: string) => {
    setQuery(q);
    if (!q) {
      setOffers([]);
      setSearched(false);
      return;
    }
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

  // Fetch chain names once to resolve excluded chain names from URL
  useEffect(() => {
    if (excludedChains.size > 0) {
      fetchChains().then((chainList) => {
        setExcludedChains((prev) => {
          let changed = false;
          const next = new Map(prev);
          for (const chain of chainList) {
            if (prev.has(chain.dealerId) && !prev.get(chain.dealerId)) {
              next.set(chain.dealerId, chain.name);
              changed = true;
            }
          }
          return changed ? next : prev;
        });
      }).catch(() => {});
    }
  }, []);

  const handleSearch = (q: string) => {
    const params = new URLSearchParams();
    params.set('q', q);
    if (filterDate) params.set('date', filterDate);
    if (excludedChains.size > 0) params.set('exclude', [...excludedChains.keys()].join(','));
    navigate(`/search?${params.toString()}`, { replace: true });
    doSearch(q);
  };

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="flex items-center gap-4 mb-6">
        <button className="btn btn-outline btn-sm" onClick={() => {
          const params = new URLSearchParams();
          if (filterDate) params.set('date', filterDate);
    if (excludedChains.size > 0) params.set('exclude', [...excludedChains.keys()].join(','));
          const qs = params.toString();
          navigate(qs ? `/?${qs}` : '/');
        }}>
          Hjem
        </button>
        <div className="flex-1">
          <SearchBar onSearch={handleSearch} initialValue={initialQuery} />
        </div>
        <div className="flex items-center gap-2">
          <label className="text-sm whitespace-nowrap" htmlFor="filter-date">Dato (valgfri):</label>
          <input
            id="filter-date"
            type="date"
            className="input input-bordered input-sm"
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
          />
          {filterDate && (
            <button className="btn btn-ghost btn-xs" onClick={() => setFilterDate('')}>Ryd</button>
          )}
        </div>
      </div>

      {excludedChains.size > 0 && (
        <div className="flex flex-wrap gap-2 mb-4">
          <span className="badge badge-outline gap-1 cursor-pointer hover:bg-base-200" onClick={() => setExcludedChains(new Map())}>
            Vælg alle
          </span>
          {[...excludedChains.entries()].map(([dealerId, name]) => {
            return (
              <span key={dealerId} className="badge badge-outline gap-1">
                {name || dealerId}
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
          <table className="table table-zebra w-full [&_td]:py-1 [&_th]:py-1">
            <thead>
              <tr>
                <th className="w-12 leading-tight">Fravælg<br/>Kæde</th>
                <th className="w-20 leading-tight">Hover for<br/>større billede</th>
                <th className={thClass('chain')} onClick={() => toggleSort('chain')}>
                  Kæde<SortIcon column="chain" />
                </th>
                <th>Tilbud</th>
                <th className={thClass('price')} onClick={() => toggleSort('price')}>
                  Pris<SortIcon column="price" />
                </th>
                <th>Mængde</th>
                <th className={thClass('runFrom')} onClick={() => toggleSort('runFrom')}>
                  Gyldig fra<SortIcon column="runFrom" />
                </th>
                <th className={thClass('runTill')} onClick={() => toggleSort('runTill')}>
                  Gyldig til<SortIcon column="runTill" />
                </th>
              </tr>
            </thead>
            <tbody>
              {sortedOffers.map((offer) => {
                const thumb = getImageUrl(offer.images);
                const zoom = getZoomUrl(offer.images);
                return (
                  <tr key={offer.id}>
                    <td className="text-center">
                      <button
                        className="btn btn-ghost btn-sm text-base-content/50 hover:text-error cursor-pointer px-1 min-h-0 h-auto leading-none"
                        title={`Skjul ${offer.chain.name}`}
                        onClick={() => {
                          setExcludedChains((prev) => {
                            const next = new Map(prev);
                            next.set(offer.chain.dealerId, offer.chain.name);
                            return next;
                          });
                        }}
                      >−</button>
                    </td>
                    <td>
                      {thumb ? (
                        <img
                          src={thumb}
                          alt={offer.heading}
                          className="w-12 h-12 object-cover rounded cursor-pointer hover:opacity-80 transition-opacity"
                          onMouseEnter={(e) => zoom && setHoverImage({ src: zoom, alt: offer.heading, rect: e.currentTarget.getBoundingClientRect() })}
                          onMouseLeave={() => setHoverImage(null)}
                          onClick={() => zoom && setZoomImage({ src: zoom, alt: offer.heading })}
                        />
                      ) : (
                        <div className="w-12 h-12 bg-base-200 rounded flex items-center justify-center text-xs">
                          —
                        </div>
                      )}
                    </td>
                    <td className="text-sm">{offer.chain.name}</td>
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
                    <td className="text-sm whitespace-nowrap">{formatQuantity(offer.quantity)}</td>
                    <td className="text-sm whitespace-nowrap">
                      {formatDate(offer.runFrom)}
                    </td>
                    <td className="text-sm whitespace-nowrap">
                      {formatDate(offer.runTill)}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      )}

      {zoomImage && (
        <dialog className="modal modal-open" onClick={() => setZoomImage(null)}>
          <div className="modal-box max-w-2xl p-2" onClick={(e) => e.stopPropagation()}>
            <img
              src={zoomImage.src}
              alt={zoomImage.alt}
              className="w-full rounded"
            />
            <div className="modal-action">
              <button className="btn btn-sm" onClick={() => setZoomImage(null)}>Luk</button>
            </div>
          </div>
          <form method="dialog" className="modal-backdrop">
            <button onClick={() => setZoomImage(null)}>luk</button>
          </form>
        </dialog>
      )}

      {hoverImage && (
        <div
          className="fixed z-50 pointer-events-none shadow-xl rounded-lg overflow-hidden bg-base-100 border border-base-300"
          style={{
            top: hoverImage.rect.top,
            left: hoverImage.rect.right + 12 > window.innerWidth - 320
              ? hoverImage.rect.left - 320 - 12
              : hoverImage.rect.right + 12,
          }}
        >
          <img
            src={hoverImage.src}
            alt={hoverImage.alt}
            className="w-72 h-72 object-contain"
          />
        </div>
      )}
    </div>
  );
}
