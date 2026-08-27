import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, within, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { SearchPage } from '../pages/SearchPage';
import { HomePage } from '../pages/HomePage';
import type { OfferResponse, ChainResponse, CatalogResponse, IngestionStatus } from '../types';

vi.mock('../api', () => ({
  fetchAllOffers: vi.fn(),
  fetchChains: vi.fn(),
  fetchCatalogs: vi.fn(),
  fetchIngestionStatus: vi.fn(),
  triggerIngestion: vi.fn(),
}));

import {
  fetchAllOffers,
  fetchChains,
  fetchCatalogs,
  fetchIngestionStatus,
  triggerIngestion,
} from '../api';

const mockFetchAllOffers = vi.mocked(fetchAllOffers);
const mockFetchChains = vi.mocked(fetchChains);
const mockFetchCatalogs = vi.mocked(fetchCatalogs);
const mockFetchIngestionStatus = vi.mocked(fetchIngestionStatus);
const mockTriggerIngestion = vi.mocked(triggerIngestion);

function makeOffer(overrides: Partial<OfferResponse> = {}): OfferResponse {
  return {
    id: 'offer-1',
    offerId: 'ext-1',
    chain: { id: 'chain-1', dealerId: 'dealer-1', name: 'Netto' },
    heading: 'Mælk 1L',
    description: 'Sødmælk',
    price: 9.95,
    prePrice: null,
    currency: 'DKK',
    catalogPage: 1,
    quantity: JSON.stringify({ unit: { symbol: 'stk' }, size: { from: 1, to: 1 } }),
    images: JSON.stringify({ view: 'https://example.com/view.jpg', thumb: 'https://example.com/thumb.jpg' }),
    runFrom: '2025-06-01',
    runTill: '2025-06-14',
    ...overrides,
  };
}

function makeChain(overrides: Partial<ChainResponse> = {}): ChainResponse {
  return {
    id: 'chain-1',
    dealerId: 'dealer-1',
    name: 'Netto',
    website: 'https://netto.dk',
    logoUrl: 'https://example.com/netto.png',
    color: 'FFD700',
    offerCount: 50,
    ...overrides,
  };
}

function makeCatalog(overrides: Partial<CatalogResponse> = {}): CatalogResponse {
  return {
    id: 'cat-1',
    catalogId: 'ext-cat-1',
    chain: { id: 'chain-1', dealerId: 'dealer-1', name: 'Netto' },
    label: 'Ugetilbud',
    catalogType: 'weekly',
    categoryIds: [],
    runFrom: '2025-06-01',
    runTill: '2025-06-14',
    offerCount: 50,
    ...overrides,
  };
}

function renderWithRouter(ui: React.ReactElement, initialEntries: string[] = ['/']) {
  return render(
    <MemoryRouter initialEntries={initialEntries}>
      {ui}
    </MemoryRouter>
  );
}

beforeEach(() => {
  vi.clearAllMocks();
});

describe('SearchPage', () => {
  it('shows prompt text before any search', () => {
    mockFetchAllOffers.mockResolvedValue([]);
    renderWithRouter(<SearchPage />);
    expect(screen.getByText(/Indtast et søgeord/)).toBeInTheDocument();
  });

  it('searches and displays results in a table', async () => {
    const user = userEvent.setup();
    const offers = [
      makeOffer({ id: '1', heading: 'Mælk 1L', price: 9.95, chain: { id: 'c1', dealerId: 'd1', name: 'Netto' } }),
      makeOffer({ id: '2', heading: 'Brød', price: 19.50, chain: { id: 'c2', dealerId: 'd2', name: 'Føtex' } }),
    ];
    mockFetchAllOffers.mockResolvedValue(offers);

    renderWithRouter(<SearchPage />);

    const input = screen.getByPlaceholderText('Søg efter tilbud...');
    await user.type(input, 'mælk');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText('Mælk 1L')).toBeInTheDocument();
    });

    expect(screen.getByText('Brød')).toBeInTheDocument();
    expect(screen.getByText('2 tilbud fundet')).toBeInTheDocument();
    expect(mockFetchAllOffers).toHaveBeenCalledWith('mælk');

    const rows = screen.getAllByRole('row');
    expect(rows.length).toBe(3);
  });

  it('shows formatted price and quantity in table', async () => {
    const user = userEvent.setup();
    mockFetchAllOffers.mockResolvedValue([
      makeOffer({ price: 19.95, quantity: JSON.stringify({ unit: { symbol: 'stk' }, size: { from: 2, to: 2 }, pieces: { from: 6, to: 6 } }) }),
    ]);

    renderWithRouter(<SearchPage />);
    await user.type(screen.getByPlaceholderText('Søg efter tilbud...'), 'test');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText('19,95 kr')).toBeInTheDocument();
    });
    expect(screen.getByText('6 × 2 stk')).toBeInTheDocument();
  });

  it('excludes a chain when Fravælg button is clicked', async () => {
    const user = userEvent.setup();
    const offers = [
      makeOffer({ id: '1', heading: 'Mælk', chain: { id: 'c1', dealerId: 'd1', name: 'Netto' } }),
      makeOffer({ id: '2', heading: 'Brød', chain: { id: 'c2', dealerId: 'd2', name: 'Føtex' } }),
    ];
    mockFetchAllOffers.mockResolvedValue(offers);

    renderWithRouter(<SearchPage />);
    await user.type(screen.getByPlaceholderText('Søg efter tilbud...'), 'test');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText('Mælk')).toBeInTheDocument();
    });

    const nettoRow = screen.getByText('Mælk').closest('tr')!;
    const excludeBtn = within(nettoRow).getByTitle('Skjul Netto');
    await user.click(excludeBtn);

    expect(screen.queryByText('Mælk')).not.toBeInTheDocument();
    expect(screen.getByText('Brød')).toBeInTheDocument();

    expect(screen.getByText('Netto')).toBeInTheDocument();
    expect(screen.getByText('Vælg alle')).toBeInTheDocument();
  });

  it('re-includes all chains when Vælg alle is clicked', async () => {
    const user = userEvent.setup();
    const offers = [
      makeOffer({ id: '1', heading: 'Mælk', chain: { id: 'c1', dealerId: 'd1', name: 'Netto' } }),
      makeOffer({ id: '2', heading: 'Brød', chain: { id: 'c2', dealerId: 'd2', name: 'Føtex' } }),
    ];
    mockFetchAllOffers.mockResolvedValue(offers);

    renderWithRouter(<SearchPage />);
    await user.type(screen.getByPlaceholderText('Søg efter tilbud...'), 'test');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText('Mælk')).toBeInTheDocument();
    });

    const nettoRow = screen.getByText('Mælk').closest('tr')!;
    await user.click(within(nettoRow).getByTitle('Skjul Netto'));

    expect(screen.queryByText('Mælk')).not.toBeInTheDocument();

    await user.click(screen.getByText('Vælg alle'));

    expect(screen.getByText('Mælk')).toBeInTheDocument();
    expect(screen.getByText('Brød')).toBeInTheDocument();
  });

  it('filters offers by date', async () => {
    const user = userEvent.setup();
    const offers = [
      makeOffer({ id: '1', heading: 'Tilbud A', runFrom: '2025-06-01', runTill: '2025-06-07' }),
      makeOffer({ id: '2', heading: 'Tilbud B', runFrom: '2025-06-08', runTill: '2025-06-14' }),
    ];
    mockFetchAllOffers.mockResolvedValue(offers);

    renderWithRouter(<SearchPage />);
    await user.type(screen.getByPlaceholderText('Søg efter tilbud...'), 'test');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText('Tilbud A')).toBeInTheDocument();
    });
    expect(screen.getByText('Tilbud B')).toBeInTheDocument();

    const dateInput = screen.getByLabelText('Dato (valgfri):');
    await user.clear(dateInput);
    await user.type(dateInput, '2025-06-03');

    expect(screen.getByText('Tilbud A')).toBeInTheDocument();
    expect(screen.queryByText('Tilbud B')).not.toBeInTheDocument();
  });

  it('shows no-results message when search returns empty', async () => {
    const user = userEvent.setup();
    mockFetchAllOffers.mockResolvedValue([]);

    renderWithRouter(<SearchPage />);
    await user.type(screen.getByPlaceholderText('Søg efter tilbud...'), 'xyz');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText(/Ingen tilbud fundet/)).toBeInTheDocument();
    });
  });

  it('shows all columns in the table header', async () => {
    const user = userEvent.setup();
    mockFetchAllOffers.mockResolvedValue([makeOffer()]);

    renderWithRouter(<SearchPage />);
    await user.type(screen.getByPlaceholderText('Søg efter tilbud...'), 'test');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    await waitFor(() => {
      expect(screen.getByText('Kæde')).toBeInTheDocument();
    });
    expect(screen.getByText('Tilbud')).toBeInTheDocument();
    expect(screen.getByText('Pris')).toBeInTheDocument();
    expect(screen.getByText('Mængde')).toBeInTheDocument();
    expect(screen.getByText(/Gyldig fra/)).toBeInTheDocument();
    expect(screen.getByText(/Gyldig til/)).toBeInTheDocument();
  });
});

describe('HomePage', () => {
  const chains: ChainResponse[] = [
    makeChain({ dealerId: 'd1', name: 'Netto', offerCount: 50, logoUrl: 'https://example.com/netto.png' }),
    makeChain({ dealerId: 'd2', name: 'Føtex', offerCount: 30, logoUrl: 'https://example.com/føtex.png' }),
    makeChain({ dealerId: 'd3', name: 'Aldi', offerCount: 0, logoUrl: '' }),
  ];

  const catalogs: Record<string, CatalogResponse[]> = {
    d1: [makeCatalog({ catalogId: 'cat-1', runFrom: '2025-06-01', runTill: '2025-06-14' })],
    d2: [makeCatalog({ catalogId: 'cat-2', runFrom: '2025-06-08', runTill: '2025-06-21' })],
    d3: [],
  };

  function setupHomePage() {
    mockFetchChains.mockResolvedValue(chains);
    mockFetchCatalogs.mockImplementation(async (dealerId: string) => catalogs[dealerId] || []);
    mockFetchIngestionStatus.mockResolvedValue({
      running: false,
      lastRun: '2025-06-15T10:30:00Z',
      lastRunResult: { chainsProcessed: 12, catalogsProcessed: 45, offersInserted: 1234, errors: 0 },
    });
    mockTriggerIngestion.mockResolvedValue();
  }

  it('renders the page title and loading banner', async () => {
    setupHomePage();
    renderWithRouter(<HomePage />);

    expect(screen.getByText('Tilbud')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText(/12 kæder gennemgået/)).toBeInTheDocument();
    });
  });

  it('displays chains in a table with names and logos', async () => {
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Netto')).toBeInTheDocument();
    });
    expect(screen.getByText('Føtex')).toBeInTheDocument();
    expect(screen.getByText('Aldi')).toBeInTheDocument();

    const nettoImg = screen.getByAltText('Netto');
    expect(nettoImg).toHaveAttribute('src', 'https://example.com/netto.png');

    const føtexImg = screen.getByAltText('Føtex');
    expect(føtexImg).toHaveAttribute('src', 'https://example.com/føtex.png');
  });

  it('shows catalog period for chains with catalogs', async () => {
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Netto')).toBeInTheDocument();
    });

    const rows = screen.getAllByRole('row');
    const nettoRow = rows.find((r) => within(r).queryByText('Netto'));
    expect(nettoRow).toBeDefined();
    const periodCell = within(nettoRow!).getAllByRole('cell')[2];
    expect(periodCell.textContent).toMatch(/\d/);
  });

  it('shows "—" for chains without catalogs', async () => {
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Aldi')).toBeInTheDocument();
    });

    const rows = screen.getAllByRole('row');
    const aldiRow = rows.find((r) => within(r).queryByText('Aldi'));
    expect(aldiRow).toBeDefined();
    const periodCell = within(aldiRow!).getAllByRole('cell')[2];
    expect(periodCell.textContent).toBe('—');
  });

  it('excludes a chain when Fravælg is clicked', async () => {
    const user = userEvent.setup();
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Netto')).toBeInTheDocument();
    });

    const rows = screen.getAllByRole('row');
    const nettoRow = rows.find((r) => within(r).queryByText('Netto') && within(r).queryByText('50'))!;
    await user.click(within(nettoRow).getByTitle('Skjul Netto'));

    const tableRows = screen.getAllByRole('row');
    const tableBody = tableRows.filter((r) => r.closest('tbody'));
    const chainCells = tableBody.flatMap((r) => within(r).getAllByRole('cell').map((c) => c.textContent));
    expect(chainCells.join(' ')).not.toContain('Netto');
    expect(screen.getByText('Føtex')).toBeInTheDocument();
    expect(screen.getByText('Aldi')).toBeInTheDocument();

    expect(screen.getByText('Vælg alle')).toBeInTheDocument();
  });

  it('re-includes all chains when Vælg alle is clicked', async () => {
    const user = userEvent.setup();
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Netto')).toBeInTheDocument();
    });

    const rows = screen.getAllByRole('row');
    const nettoRow = rows.find((r) => within(r).queryByText('Netto') && within(r).queryByText('50'))!;
    await user.click(within(nettoRow).getByTitle('Skjul Netto'));

    await user.click(screen.getByText('Vælg alle'));

    const tableRows = screen.getAllByRole('row');
    const tableBody = tableRows.filter((r) => r.closest('tbody'));
    const chainCells = tableBody.flatMap((r) => within(r).getAllByRole('cell').map((c) => c.textContent));
    expect(chainCells.join(' ')).toContain('Netto');
  });

  it('filters chains by date', async () => {
    const user = userEvent.setup();
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Netto')).toBeInTheDocument();
    });

    const dateInput = screen.getByLabelText('Dato (valgfri):');
    await user.clear(dateInput);
    await user.type(dateInput, '2025-06-03');

    expect(screen.getByText('Netto')).toBeInTheDocument();
    expect(screen.queryByText('Føtex')).not.toBeInTheDocument();
  });

  it('shows catalog and offer counts', async () => {
    setupHomePage();
    renderWithRouter(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Netto')).toBeInTheDocument();
    });

    const rows = screen.getAllByRole('row');
    const nettoRow = rows.find((r) => within(r).queryByText('Netto'));
    const cells = within(nettoRow!).getAllByRole('cell');
    expect(cells[3].textContent).toBe('1');
    expect(cells[4].textContent).toBe('50');
  });
});
