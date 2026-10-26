import { afterEach, expect, it, vi } from "vitest";
import { cleanup, render, screen, fireEvent } from "@testing-library/react";
import { AuthProvider } from "../auth/AuthContext";
import App from "../App";
afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  location.hash = "";
  vi.restoreAllMocks();
});
const account = {
  userId: 1,
  email: "a@example.com",
  firstName: "Jane",
  lastName: "Guest",
  activated: true,
};
const reply = (data: unknown, status = 200) =>
  new Response(JSON.stringify(data), { status });
it.each(["ADMIN", "GUEST"])("uses the authenticated %s type without forbidden role probes", async (accountType) => {
  const calls: string[] = [];
  vi.stubGlobal("fetch", vi.fn(async (path: string) => {
    calls.push(path);
    return path.endsWith("/login") ? reply({ ...account, accountType }) : reply([]);
  }));
  render(<AuthProvider><App /></AuthProvider>);
  await login();
  await screen.findByRole("button", { name: "Log out" });
  expect(calls).not.toContain("/api/v1/properties/mine");
  expect(calls).not.toContain("/api/v1/bookings");
  if (accountType === "ADMIN") {
    expect(screen.queryByRole("link", { name: "My bookings" })).toBeNull();
    expect(screen.queryByRole("link", { name: "My properties" })).toBeNull();
  } else expect(screen.getByRole("link", { name: "My bookings" })).toBeTruthy();
});
async function login() {
  fireEvent.change(screen.getByLabelText("Email"), {
    target: { value: account.email },
  });
  fireEvent.change(screen.getByLabelText("Password"), {
    target: { value: "ExamplePass1!" },
  });
  fireEvent.click(screen.getAllByRole("button", { name: "Log in" }).at(-1)!);
}
it("registers owners with exact fields, activates with a string code, and logs in", async () => {
  const calls: { path: string; body: any }[] = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(async (path, options) => {
      calls.push({
        path,
        body: options.body ? JSON.parse(options.body) : undefined,
      });
      return reply(
        path.endsWith("/register")
          ? { ...account, activated: false }
          : path.endsWith("/activate")
            ? { message: "Account activated" }
            : path.endsWith("/login")
              ? account
              : [],
      );
    }),
  );
  render(
    <AuthProvider>
      <App />
    </AuthProvider>,
  );
  fireEvent.click(screen.getByRole("button", { name: "Register" }));
  fireEvent.change(screen.getByLabelText("Account type"), {
    target: { value: "OWNER" },
  });
  for (const [label, value] of [
    ["First name", "Jane"],
    ["Last name", "Owner"],
    ["Email", account.email],
    ["Password", "ExamplePass1!"],
    ["Confirm password", "ExamplePass1!"],
    ["Contact number", "8765550100"],
  ])
    fireEvent.change(screen.getByLabelText(label), { target: { value } });
  fireEvent.click(screen.getByRole("button", { name: "Create account" }));
  await screen.findByLabelText("Six-digit activation code");
  expect(calls[0].body).toEqual({
    accountType: "OWNER",
    email: account.email,
    password: "ExamplePass1!",
    firstName: "Jane",
    lastName: "Owner",
    contactNum: "8765550100",
    gender: "NOT_SPECIFIED",
  });
  fireEvent.change(screen.getByLabelText("Six-digit activation code"), {
    target: { value: "012345" },
  });
  fireEvent.click(screen.getByRole("button", { name: "Activate account" }));
  await screen.findByText("Account activated. You can now log in.");
  expect(calls[1].body.code).toBe("012345");
});
it("selects owner navigation, displays empty state, and clears private views on logout", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (path) => reply(path.endsWith("/login") ? account : [])),
  );
  render(
    <AuthProvider>
      <App />
    </AuthProvider>,
  );
  await login();
  await screen.findByRole("link", { name: "My properties" });
  expect(screen.queryByRole("link", { name: "My bookings" })).toBeNull();
  await screen.findByText("No properties found");
  fireEvent.click(screen.getByRole("button", { name: "Log out" }));
  expect(screen.queryByRole("link", { name: "My properties" })).toBeNull();
  expect(localStorage.length).toBe(0);
  expect(sessionStorage.length).toBe(0);
});
it("selects guest navigation after 403 and offers booking history", async () => {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (path) =>
      reply(
        path.endsWith("/login")
          ? account
          : path.endsWith("/mine")
            ? { message: "Owner account required" }
            : [],
        path.endsWith("/mine") ? 403 : 200,
      ),
    ),
  );
  render(
    <AuthProvider>
      <App />
    </AuthProvider>,
  );
  await login();
  await screen.findByRole("link", { name: "My bookings" });
  location.hash = "/bookings";
  fireEvent(window, new HashChangeEvent("hashchange"));
  await screen.findByText("No bookings yet");
});
it("shows activation guidance on login 403 without inventing a role", async () => {
  vi.stubGlobal(
    "fetch",
    vi
      .fn()
      .mockResolvedValue(reply({ message: "Account must be activated" }, 403)),
  );
  render(
    <AuthProvider>
      <App />
    </AuthProvider>,
  );
  await login();
  await screen.findByRole("button", { name: "Go to account activation" });
});
it("shows a 503 and offers explicit registration retry without automatic retry", async () => {
  const fetcher = vi
    .fn()
    .mockResolvedValue(reply({ message: "Delivery failed" }, 503));
  vi.stubGlobal("fetch", fetcher);
  render(
    <AuthProvider>
      <App />
    </AuthProvider>,
  );
  fireEvent.click(screen.getByRole("button", { name: "Register" }));
  fireEvent.change(screen.getByLabelText("Account type"), {
    target: { value: "OWNER" },
  });
  for (const [label, value] of [
    ["First name", "Jane"],
    ["Last name", "Owner"],
    ["Email", account.email],
    ["Password", "ExamplePass1!"],
    ["Confirm password", "ExamplePass1!"],
    ["Contact number", "123"],
  ])
    fireEvent.change(screen.getByLabelText(label), { target: { value } });
  fireEvent.click(screen.getByRole("button", { name: "Create account" }));
  await screen.findByRole("alert");
  expect(fetcher).toHaveBeenCalledTimes(1);
  expect(
    screen.getByRole("button", { name: "Retry registration / send code" }),
  ).toBeTruthy();
});
