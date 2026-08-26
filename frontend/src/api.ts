import type { ChainResponse, CatalogResponse, OfferResponse, IngestionStatus } from './types';

export async function fetchChains(): Promise<ChainResponse[]> {
  const res = await fetch('/api/chains');
  if (!res.ok) throw new Error(`Failed to fetch chains: ${res.status}`);
  return res.json();
}

export async function fetchCatalogs(chainDealerId: string): Promise<CatalogResponse[]> {
  const res = await fetch(`/api/catalogs?chain=${chainDealerId}`);
  if (!res.ok) throw new Error(`Failed to fetch catalogs: ${res.status}`);
  return res.json();
}

export async function searchOffers(
  query: string,
  page: number = 0,
  size: number = 100
): Promise<{ content: OfferResponse[]; last: boolean }> {
  const res = await fetch(
    `/api/offers/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`
  );
  if (!res.ok) throw new Error(`Failed to search offers: ${res.status}`);
  return res.json();
}

export async function fetchAllOffers(query: string): Promise<OfferResponse[]> {
  const all: OfferResponse[] = [];
  let page = 0;
  while (true) {
    const data = await searchOffers(query, page, 100);
    all.push(...data.content);
    if (data.last || data.content.length === 0) break;
    page++;
  }
  return all;
}

export async function fetchIngestionStatus(): Promise<IngestionStatus> {
  const res = await fetch('/api/v1/ingestion/status');
  if (!res.ok) throw new Error(`Failed to fetch status: ${res.status}`);
  return res.json();
}

export async function triggerIngestion(): Promise<void> {
  const res = await fetch('/api/v1/ingestion/trigger', { method: 'POST' });
  if (!res.ok && res.status !== 409) throw new Error(`Failed to trigger: ${res.status}`);
}
