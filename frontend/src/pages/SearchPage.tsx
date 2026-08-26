import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { fetchAllOffers } from '../api';
import { SearchBar } from '../components/SearchBar';
import type { OfferResponse } from '../types';

function formatPrice(kr: number): string {
  return `${kr.toFixed(2)} kr`.replace('.', ',');
}

function formatPeriod(from: string, till: string): string {
  const f = new Date(from);
  const t = new Date(till);
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short' };
  return `${f.toLocaleDateString('da-DK', opts)} – ${t.toLocaleDateString('da-DK', opts)}`;
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

export function SearchPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialQuery = searchParams.get('q') || '';

  const [query, setQuery] = useState(initialQuery);
  const [offers, setOffers] = useState<OfferResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const doSearch = async (q: string) => {
    setQuery(q);
    setLoading(true);
    setSearched(true);
    try {
      const results = await fetchAllOffers(q);
      setOffers(results);
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
                <th>Pris</th>
                <th>Gyldig</th>
                <th>Kæde</th>
              </tr>
            </thead>
            <tbody>
              {offers.map((offer) => {
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
                      {formatPeriod(offer.runFrom, offer.runTill)}
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
