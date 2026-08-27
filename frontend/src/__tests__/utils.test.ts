import { describe, it, expect } from 'vitest';
import {
  formatPrice,
  formatDate,
  getImageUrl,
  getZoomUrl,
  formatQuantity,
  formatPeriod,
  compareValues,
} from '../utils';

describe('formatPrice', () => {
  it('formats integer prices with comma and kr', () => {
    expect(formatPrice(10)).toBe('10,00 kr');
  });

  it('formats decimal prices correctly', () => {
    expect(formatPrice(19.95)).toBe('19,95 kr');
  });

  it('formats zero', () => {
    expect(formatPrice(0)).toBe('0,00 kr');
  });

  it('formats large numbers', () => {
    expect(formatPrice(1234.56)).toBe('1234,56 kr');
  });
});

describe('formatDate', () => {
  it('formats an ISO date to Danish locale', () => {
    const result = formatDate('2025-06-15');
    expect(result).toContain('15');
    expect(result).toContain('2025');
  });

  it('includes month name', () => {
    const result = formatDate('2025-01-01');
    expect(result).toMatch(/\w+/);
  });
});

describe('getImageUrl', () => {
  it('returns null for null input', () => {
    expect(getImageUrl(null)).toBeNull();
  });

  it('parses view key', () => {
    const images = JSON.stringify({ view: 'https://example.com/view.jpg' });
    expect(getImageUrl(images)).toBe('https://example.com/view.jpg');
  });

  it('falls back to thumb when no view', () => {
    const images = JSON.stringify({ thumb: 'https://example.com/thumb.jpg' });
    expect(getImageUrl(images)).toBe('https://example.com/thumb.jpg');
  });

  it('returns null for invalid JSON', () => {
    expect(getImageUrl('not-json')).toBeNull();
  });

  it('returns null when JSON has neither view nor thumb', () => {
    expect(getImageUrl(JSON.stringify({ other: 'x' }))).toBeNull();
  });
});

describe('getZoomUrl', () => {
  it('returns null for null input', () => {
    expect(getZoomUrl(null)).toBeNull();
  });

  it('prefers zoom key', () => {
    const images = JSON.stringify({ zoom: 'https://example.com/zoom.jpg', view: 'https://example.com/view.jpg' });
    expect(getZoomUrl(images)).toBe('https://example.com/zoom.jpg');
  });

  it('falls back to view', () => {
    const images = JSON.stringify({ view: 'https://example.com/view.jpg' });
    expect(getZoomUrl(images)).toBe('https://example.com/view.jpg');
  });

  it('falls back to thumb', () => {
    const images = JSON.stringify({ thumb: 'https://example.com/thumb.jpg' });
    expect(getZoomUrl(images)).toBe('https://example.com/thumb.jpg');
  });

  it('returns null for invalid JSON', () => {
    expect(getZoomUrl('bad')).toBeNull();
  });
});

describe('formatQuantity', () => {
  it('returns empty string for null', () => {
    expect(formatQuantity(null)).toBe('');
  });

  it('returns unit only when no size or pieces', () => {
    const q = JSON.stringify({ unit: { symbol: 'kg' } });
    expect(formatQuantity(q)).toBe('kg');
  });

  it('formats size range', () => {
    const q = JSON.stringify({ unit: { symbol: 'g' }, size: { from: 200, to: 500 } });
    expect(formatQuantity(q)).toBe('200-500 g');
  });

  it('formats single size', () => {
    const q = JSON.stringify({ unit: { symbol: 'stk' }, size: { from: 1, to: 1 } });
    expect(formatQuantity(q)).toBe('1 stk');
  });

  it('formats pieces with unit', () => {
    const q = JSON.stringify({ unit: { symbol: 'flasker' }, size: { from: 1, to: 1 }, pieces: { from: 6, to: 6 } });
    expect(formatQuantity(q)).toBe('6 × 1 flasker');
  });

  it('formats pieces range with max', () => {
    const q = JSON.stringify({ unit: { symbol: 'stk' }, size: { from: 1, to: 1 }, pieces: { from: 2, to: 3, max: 5 } });
    expect(formatQuantity(q)).toBe('2-3 × 1 stk (maks 5)');
  });

  it('returns empty string for invalid JSON', () => {
    expect(formatQuantity('bad')).toBe('');
  });
});

describe('formatPeriod', () => {
  it('formats a date range in Danish locale', () => {
    const result = formatPeriod('2025-06-01', '2025-06-14');
    expect(result).toContain('–');
    expect(result).toContain('1');
    expect(result).toContain('14');
  });
});

describe('compareValues', () => {
  it('sorts numbers ascending', () => {
    expect(compareValues(1, 2, 'asc')).toBeLessThan(0);
    expect(compareValues(2, 1, 'asc')).toBeGreaterThan(0);
    expect(compareValues(1, 1, 'asc')).toBe(0);
  });

  it('sorts numbers descending', () => {
    expect(compareValues(1, 2, 'desc')).toBeGreaterThan(0);
    expect(compareValues(2, 1, 'desc')).toBeLessThan(0);
  });

  it('sorts strings ascending', () => {
    expect(compareValues('abc', 'def', 'asc')).toBeLessThan(0);
    expect(compareValues('def', 'abc', 'asc')).toBeGreaterThan(0);
  });

  it('sorts strings descending', () => {
    expect(compareValues('abc', 'def', 'desc')).toBeGreaterThan(0);
    expect(compareValues('def', 'abc', 'desc')).toBeLessThan(0);
  });
});
