export type SortDir = 'asc' | 'desc';

export function formatPrice(kr: number): string {
  return `${kr.toFixed(2)} kr`.replace('.', ',');
}

export function formatDate(iso: string): string {
  const d = new Date(iso);
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short', year: 'numeric' };
  return d.toLocaleDateString('da-DK', opts);
}

export function getImageUrl(images: string | null): string | null {
  if (!images) return null;
  try {
    const parsed = JSON.parse(images);
    return parsed.view || parsed.thumb || null;
  } catch {
    return null;
  }
}

export function getZoomUrl(images: string | null): string | null {
  if (!images) return null;
  try {
    const parsed = JSON.parse(images);
    return parsed.zoom || parsed.view || parsed.thumb || null;
  } catch {
    return null;
  }
}

export function formatQuantity(quantity: string | null): string {
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

export function formatPeriod(from: string, till: string): string {
  const f = new Date(from);
  const t = new Date(till);
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'short' };
  return `${f.toLocaleDateString('da-DK', opts)} – ${t.toLocaleDateString('da-DK', opts)}`;
}

export function compareValues(a: number | string, b: number | string, dir: SortDir): number {
  const mul = dir === 'asc' ? 1 : -1;
  if (typeof a === 'number' && typeof b === 'number') return (a - b) * mul;
  return String(a).localeCompare(String(b), 'da') * mul;
}
