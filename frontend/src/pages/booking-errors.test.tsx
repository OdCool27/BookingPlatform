import { afterEach, expect, it, vi } from 'vitest';
import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { api, ApiError } from '../api/client';
import { PropertyDetail } from './Properties';

const fixtureAuth = vi.hoisted(() => ({ session: { role: 'guest', credentials: { email: 'fixture@example.com', password: 'TestOnly1!' } } }));
vi.mock('../auth/AuthContext', () => ({ useAuth: () => fixtureAuth }));
afterEach(() => { cleanup(); vi.restoreAllMocks(); });

async function prepare() {
  vi.spyOn(api, 'property').mockResolvedValue({ propertyId: 'JA-Exact-001', propertyName: 'Test stay', description: '', address: { street: 'Main', suite: '', city: 'Kingston', state: 'Kingston', postalCode: '', country: 'Jamaica' }, latitude: 18, longitude: -76, houseSize: 100, landSize: 200, baseRate: 100 });
  render(<PropertyDetail id="JA-Exact-001" />);
  await screen.findByText('Book your stay');
  fireEvent.change(screen.getByLabelText('Arrival'), { target: { value: '2030-01-03' } });
  fireEvent.change(screen.getByLabelText('Departure'), { target: { value: '2030-01-05' } });
}

it('blocks invalid date order and requires confirmation before creating', async () => {
  const create = vi.spyOn(api, 'createBooking');
  const confirm = vi.spyOn(window, 'confirm').mockReturnValue(false);
  await prepare();
  fireEvent.change(screen.getByLabelText('Departure'), { target: { value: '2030-01-02' } });
  fireEvent.click(screen.getByRole('button', { name: 'Confirm booking' }));
  expect(screen.getByRole('alert').textContent).toContain('Departure must be after arrival');
  expect(confirm).not.toHaveBeenCalled(); expect(create).not.toHaveBeenCalled();
  fireEvent.change(screen.getByLabelText('Departure'), { target: { value: '2030-01-05' } });
  fireEvent.click(screen.getByRole('button', { name: 'Confirm booking' }));
  expect(confirm).toHaveBeenCalledOnce(); expect(create).not.toHaveBeenCalled();
});

it('shows a booking conflict without inventing success', async () => {
  vi.spyOn(window, 'confirm').mockReturnValue(true);
  const create = vi.spyOn(api, 'createBooking').mockRejectedValue(new ApiError(409, 'Property is not available for these dates'));
  await prepare(); fireEvent.click(screen.getByRole('button', { name: 'Confirm booking' }));
  expect((await screen.findByRole('alert')).textContent).toContain('not available');
  expect(create).toHaveBeenCalledOnce();
  expect(screen.queryByRole('link', { name: 'Check booking history' })).toBeNull();
});

it('prevents immediate resubmission after an ambiguous network failure', async () => {
  vi.spyOn(window, 'confirm').mockReturnValue(true);
  const create = vi.spyOn(api, 'createBooking').mockRejectedValue(new ApiError(0, 'Connection lost'));
  await prepare(); fireEvent.click(screen.getByRole('button', { name: 'Confirm booking' }));
  await screen.findByRole('link', { name: 'Check booking history' });
  expect(screen.queryByRole('button', { name: 'Confirm booking' })).toBeNull();
  expect(create).toHaveBeenCalledOnce();
});
