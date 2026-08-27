import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SearchBar } from '../components/SearchBar';

describe('SearchBar', () => {
  it('renders the input and submit button', () => {
    render(<SearchBar onSearch={() => {}} />);
    expect(screen.getByPlaceholderText('Søg efter tilbud...')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Søg' })).toBeInTheDocument();
  });

  it('displays initialValue in the input', () => {
    render(<SearchBar onSearch={() => {}} initialValue="mælk" />);
    expect(screen.getByPlaceholderText('Søg efter tilbud...')).toHaveValue('mælk');
  });

  it('calls onSearch with trimmed value on submit', async () => {
    const onSearch = vi.fn();
    const user = userEvent.setup();
    render(<SearchBar onSearch={onSearch} />);

    const input = screen.getByPlaceholderText('Søg efter tilbud...');
    await user.type(input, '  ost  ');
    await user.click(screen.getByRole('button', { name: 'Søg' }));

    expect(onSearch).toHaveBeenCalledWith('ost');
  });

  it('calls onSearch on Enter key', async () => {
    const onSearch = vi.fn();
    const user = userEvent.setup();
    render(<SearchBar onSearch={onSearch} />);

    const input = screen.getByPlaceholderText('Søg efter tilbud...');
    await user.type(input, 'brød{Enter}');

    expect(onSearch).toHaveBeenCalledWith('brød');
  });
});
