import { afterEach, describe, expect, it, vi } from "vitest";
import { api, ApiError, basicHeader, detectRole, request } from "./client";
const credentials = { email: "jane@example.com", password: "Pässword1!" };
afterEach(() => vi.unstubAllGlobals());
const response = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
describe("API boundary", () => {
  it("explains origin rejection without suggesting account activation", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(response(403, {
      code: "HTTP_403", message: "Frontend origin is not trusted",
    })));
    await expect(api.register({} as never)).rejects.toMatchObject({
      status: 403,
      code: "HTTP_403",
      message: `Frontend origin ${window.location.origin} is not trusted. Add this exact origin to the backend's TRUSTED_FRONTEND_ORIGINS environment variable and restart the backend.`,
    });
  });
  it("encodes Basic as UTF-8 without storing credentials", () => {
    const bytes = Uint8Array.from(
      atob(basicHeader(credentials).slice(6)),
      (c) => c.charCodeAt(0),
    );
    expect(new TextDecoder().decode(bytes)).toBe(
      credentials.email + ":" + credentials.password,
    );
    expect(localStorage.length).toBe(0);
    expect(sessionStorage.length).toBe(0);
  });
  it("sends only auth JSON for public login and handles a bare array", async () => {
    const fetcher = vi.fn().mockImplementation(async () => response(200, []));
    vi.stubGlobal("fetch", fetcher);
    await api.login(credentials);
    expect(fetcher.mock.calls[0][1].headers).toEqual({
      "Content-Type": "application/json",
    });
    expect(JSON.parse(fetcher.mock.calls[0][1].body)).toEqual(credentials);
    expect(await api.properties(credentials)).toEqual([]);
    expect(fetcher.mock.calls[1][1].body).toBeUndefined();
    expect(fetcher.mock.calls[1][1].headers).toEqual({
      Authorization: basicHeader(credentials),
    });
  });
  it("handles 204 deletion without JSON parsing or body", async () => {
    const fetcher = vi
      .fn()
      .mockResolvedValue(new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetcher);
    expect(await api.deleteProperty(credentials, "JA-Ab-001")).toBeUndefined();
    expect(fetcher.mock.calls[0][0]).toBe("/api/v1/properties/JA-Ab-001");
    expect(fetcher.mock.calls[0][1].body).toBeUndefined();
    expect(fetcher.mock.calls[0][1].headers["Content-Type"]).toBeUndefined();
  });
  it.each([400, 401, 403, 404, 405, 409, 413, 414, 415, 500, 503])(
    "uses HTTP %i even when error code varies",
    async (status) => {
      vi.stubGlobal(
        "fetch",
        vi
          .fn()
          .mockResolvedValue(
            response(status, { code: "ANY", message: "Specific detail" }),
          ),
      );
      await expect(request("/properties")).rejects.toMatchObject({
        status,
        code: "ANY",
        message: expect.stringContaining("Specific detail"),
      });
    },
  );
  it("handles non-JSON errors, connection loss and cancellation", async () => {
    vi.stubGlobal(
      "fetch",
      vi
        .fn()
        .mockResolvedValue(
          new Response("<html>Bad gateway</html>", { status: 500 }),
        ),
    );
    await expect(request("/properties")).rejects.toMatchObject({ status: 500 });
    vi.stubGlobal(
      "fetch",
      vi.fn().mockRejectedValue(new TypeError("Failed to fetch")),
    );
    await expect(request("/properties")).rejects.toMatchObject({ status: 0 });
    vi.stubGlobal(
      "fetch",
      vi.fn().mockRejectedValue(new DOMException("Aborted", "AbortError")),
    );
    await expect(request("/properties")).rejects.toMatchObject({
      name: "AbortError",
    });
  });
  it("classifies an empty owner collection as owner", async () => {
    const fetcher = vi.fn().mockResolvedValue(response(200, []));
    vi.stubGlobal("fetch", fetcher);
    expect(await detectRole(credentials)).toBe("owner");
    expect(fetcher).toHaveBeenCalledTimes(1);
  });
  it("probes guest access only after an explicit owner 403", async () => {
    const fetcher = vi
      .fn()
      .mockResolvedValueOnce(response(403, {}))
      .mockResolvedValueOnce(response(200, []));
    vi.stubGlobal("fetch", fetcher);
    expect(await detectRole(credentials)).toBe("guest");
    expect(fetcher.mock.calls[1][0]).toBe("/api/v1/bookings");
  });
  it.each([401, 500])("does not interpret %i as a role", async (status) => {
    const fetcher = vi.fn().mockResolvedValue(response(status, {}));
    vi.stubGlobal("fetch", fetcher);
    await expect(detectRole(credentials)).rejects.toBeInstanceOf(ApiError);
    expect(fetcher).toHaveBeenCalledTimes(1);
  });
});
