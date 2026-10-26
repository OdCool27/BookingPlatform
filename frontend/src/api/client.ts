import type {
  Account,
  Booking,
  Credentials,
  Dates,
  Property,
  PropertyInput,
  Registration,
  Role,
} from "../types/api";
export class ApiError extends Error {
  status: number;
  code: string;
  constructor(status: number, message: string, code = "") {
    super(message);
    this.status = status;
    this.code = code;
  }
}
export function basicHeader(credentials: Credentials) {
  // btoa accepts bytes, not Unicode. Encode UTF-8 first, including non-ASCII passwords.
  return (
    "Basic " +
    btoa(
      Array.from(
        new TextEncoder().encode(
          credentials.email + ":" + credentials.password,
        ),
        (b) => String.fromCharCode(b),
      ).join(""),
    )
  );
}
const messages: Record<number, string> = {
  400: "Check the entered values.",
  401: "Authentication failed. Please log in again.",
  403: "The server refused this request.",
  404: "This resource could not be found.",
  405: "API integration error: method not supported.",
  409: "This action conflicts with existing data.",
  413: "The request is too large.",
  414: "The request path is too long.",
  415: "API integration error: JSON content type required.",
  500: "The server could not complete this request.",
  503: "Email delivery is unavailable. Please try again explicitly later.",
};
const base = import.meta.env.VITE_API_BASE || "/api/v1";
if (!base.startsWith("/") || base.startsWith("//") || base.endsWith("/"))
  throw new Error(
    "VITE_API_BASE must be a relative path without a trailing slash",
  );
export async function request<T>(
  path: string,
  options: {
    method?: string;
    body?: unknown;
    credentials?: Credentials;
    signal?: AbortSignal;
  } = {},
): Promise<T> {
  const headers: Record<string, string> = {};
  if (options.credentials)
    headers.Authorization = basicHeader(options.credentials);
  const body =
    options.body === undefined ? undefined : JSON.stringify(options.body);
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
    if (new TextEncoder().encode(body).length > 65536)
      throw new ApiError(413, messages[413]);
  }
  let response: Response;
  try {
    response = await fetch(base + path, {
      method: options.method || "GET",
      headers,
      body,
      signal: options.signal,
      credentials: "omit",
      cache: "no-store",
    });
  } catch (error) {
    if (
      error &&
      typeof error === "object" &&
      "name" in error &&
      error.name === "AbortError"
    )
      throw error;
    throw new ApiError(
      0,
      "Could not reach the server. Check your connection and the backend.",
    );
  }
  if (response.status === 204) return undefined as T;
  // Status drives behavior even when a proxy returns HTML instead of standard error JSON.
  let data: unknown;
  try {
    data = await response.json();
  } catch {
    if (response.ok)
      throw new ApiError(
        0,
        "The server returned an unreadable response. The operation may have completed.",
      );
  }
  if (!response.ok) {
    const error = data as { message?: string; code?: string } | undefined;
    // Origin validation happens before account/activation checks. Explain this configuration
    // failure directly instead of sending the user to an unrelated activation workflow.
    if (response.status === 403 && error?.message === "Frontend origin is not trusted") {
      throw new ApiError(
        403,
        `Frontend origin ${window.location.origin} is not trusted. Add this exact origin to the backend's TRUSTED_FRONTEND_ORIGINS environment variable and restart the backend.`,
        error.code,
      );
    }
    throw new ApiError(
      response.status,
      (messages[response.status] || "Request failed.") +
        (error?.message ? " " + error.message : ""),
      error?.code,
    );
  }
  return data as T;
}
export const api = {
  login: (body: Credentials) =>
    request<Account>("/auth/login", { method: "POST", body }),
  register: (body: Registration) =>
    request<Account>("/auth/register", { method: "POST", body }),
  activate: (body: Credentials & { code: string }) =>
    request<{ message: string }>("/auth/activate", { method: "POST", body }),
  properties: (credentials: Credentials, suffix = "", signal?: AbortSignal) =>
    request<Property[]>("/properties" + suffix, { credentials, signal }),
  property: (credentials: Credentials, id: string, signal?: AbortSignal) =>
    request<Property>("/properties/" + encodeURIComponent(id), {
      credentials,
      signal,
    }),
  saveProperty: (credentials: Credentials, body: PropertyInput, id?: string) =>
    request<Property>(
      "/properties" + (id ? "/" + encodeURIComponent(id) : ""),
      { credentials, body, method: id ? "PUT" : "POST" },
    ),
  deleteProperty: (credentials: Credentials, id: string) =>
    request<void>("/properties/" + encodeURIComponent(id), {
      credentials,
      method: "DELETE",
    }),
  bookings: (credentials: Credentials, signal?: AbortSignal) =>
    request<Booking[]>("/bookings", { credentials, signal }),
  booking: (credentials: Credentials, id: string, signal?: AbortSignal) =>
    request<Booking>("/bookings/" + encodeURIComponent(id), {
      credentials,
      signal,
    }),
  createBooking: (
    credentials: Credentials,
    body: Dates & { propertyId: string },
  ) => request<Booking>("/bookings", { credentials, body, method: "POST" }),
  changeDates: (credentials: Credentials, id: string, body: Dates) =>
    request<Booking>("/bookings/" + encodeURIComponent(id), {
      credentials,
      body,
      method: "PUT",
    }),
};
export async function detectRole(credentials: Credentials): Promise<Role> {
  // Only an explicit 403 permits the guest probe. Outages and expired credentials are not roles.
  try {
    await api.properties(credentials, "/mine");
    return "owner";
  } catch (error) {
    if (!(error instanceof ApiError) || error.status !== 403) throw error;
  }
  await api.bookings(credentials);
  return "guest";
}
