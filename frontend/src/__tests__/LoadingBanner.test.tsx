import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { LoadingBanner } from '../components/LoadingBanner';
import type { IngestionStatus } from '../types';

describe('LoadingBanner', () => {
  it('shows loading state when status is null', () => {
    render(<LoadingBanner status={null} />);
    expect(screen.getByText('Henter status...')).toBeInTheDocument();
  });

  it('shows running state', () => {
    const status: IngestionStatus = {
      running: true,
      lastRun: null,
      lastRunResult: { chainsProcessed: 5, catalogsProcessed: 10, offersInserted: 200, errors: 0 },
    };
    render(<LoadingBanner status={status} />);
    expect(screen.getByText('Henter tilbud...')).toBeInTheDocument();
    expect(screen.getByText(/5 kæder gennemgået, 200 tilbud hentet/)).toBeInTheDocument();
  });

  it('shows error count when errors > 0', () => {
    const status: IngestionStatus = {
      running: true,
      lastRun: null,
      lastRunResult: { chainsProcessed: 3, catalogsProcessed: 0, offersInserted: 0, errors: 2 },
    };
    render(<LoadingBanner status={status} />);
    expect(screen.getByText(/2 fejl/)).toBeInTheDocument();
  });

  it('shows completed state with lastRunResult', () => {
    const status: IngestionStatus = {
      running: false,
      lastRun: '2025-06-15T10:30:00Z',
      lastRunResult: { chainsProcessed: 12, catalogsProcessed: 45, offersInserted: 1234, errors: 0 },
    };
    render(<LoadingBanner status={status} chainsWithOffers={8} />);
    expect(screen.getByText(/12 kæder gennemgået, 8 med tilbud/)).toBeInTheDocument();
    expect(screen.getByText(/45 kataloger/)).toBeInTheDocument();
    expect(screen.getByText(/1\.234 tilbud/)).toBeInTheDocument();
  });

  it('shows no-data state when no lastRunResult', () => {
    const status: IngestionStatus = {
      running: false,
      lastRun: null,
      lastRunResult: null,
    };
    render(<LoadingBanner status={status} />);
    expect(screen.getByText('Ingen data hentet endnu')).toBeInTheDocument();
  });
});
