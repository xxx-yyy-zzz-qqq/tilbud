export interface ChainResponse {
  id: string;
  dealerId: string;
  name: string;
  website: string;
  logoUrl: string;
  color: string;
  offerCount: number;
}

export interface CatalogResponse {
  id: string;
  catalogId: string;
  chain: ChainSummary;
  label: string;
  catalogType: string;
  categoryIds: string[];
  runFrom: string;
  runTill: string;
  offerCount: number;
}

export interface ChainSummary {
  id: string;
  dealerId: string;
  name: string;
}

export interface OfferResponse {
  id: string;
  offerId: string;
  chain: ChainSummary;
  heading: string;
  description: string;
  price: number;
  prePrice: number | null;
  currency: string;
  catalogPage: number | null;
  quantity: string | null;
  images: string | null;
  runFrom: string;
  runTill: string;
}

export interface IngestionStatus {
  running: boolean;
  lastRun: string | null;
  lastRunResult: {
    chainsProcessed: number;
    catalogsProcessed: number;
    offersInserted: number;
    errors: number;
  } | null;
}
