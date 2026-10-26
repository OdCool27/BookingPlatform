import { useEffect, useState } from "react";
import { useAuth } from "./auth/AuthContext";
import AuthPage from "./pages/AuthPage";
import { Properties, PropertyDetail, PropertyEditor } from "./pages/Properties";
import { Bookings, BookingDetail } from "./pages/Bookings";
function Workspace() {
  const { session, logout } = useAuth();
  const [route, setRoute] = useState(location.hash.slice(1) || "/properties");
  useEffect(() => {
    const change = () => setRoute(location.hash.slice(1) || "/properties");
    window.addEventListener("hashchange", change);
    return () => window.removeEventListener("hashchange", change);
  }, []);
  useEffect(() => {
    document.getElementById("main")?.focus();
  }, [route]);
  const segments = route.split("/").filter(Boolean);
  let id = "";
  try {
    id = decodeURIComponent(segments[1] || "");
  } catch {
    /* Show a missing route for malformed IDs. */
  }
  let page;
  if (route === "/properties") page = <Properties />;
  else if (route === "/owner" && session!.role === "owner")
    page = <Properties owner />;
  else if (route === "/properties/new" && session!.role === "owner")
    page = <PropertyEditor />;
  else if (
    segments[0] === "properties" &&
    id &&
    segments.length === 3 &&
    segments[2] === "edit" &&
    session!.role === "owner"
  )
    page = <PropertyEditor id={id} />;
  else if (segments[0] === "properties" && id && segments.length === 2)
    page = <PropertyDetail id={id} />;
  else if (route === "/bookings" && session!.role === "guest")
    page = <Bookings />;
  else if (
    segments[0] === "bookings" &&
    id &&
    segments.length === 2 &&
    session!.role === "guest"
  )
    page = <BookingDetail id={id} />;
  else
    page = (
      <div className="panel empty">
        <h1>Page unavailable</h1>
        <p>This page does not exist or is not available to your account.</p>
        <a href="#/properties">Browse properties</a>
      </div>
    );
  return (
    <>
      <header>
        <a className="brand" href="#/properties">
          <span aria-hidden="true">⌂</span> osyntra
          <span className="brand-dot">.</span>
        </a>
        <nav aria-label="Main navigation">
          <a
            href="#/properties"
            aria-current={route === "/properties" ? "page" : undefined}
          >
            Explore
          </a>
          <a
            href={session!.role === "owner" ? "#/owner" : "#/bookings"}
            aria-current={
              route === "/owner" || route.startsWith("/bookings")
                ? "page"
                : undefined
            }
          >
            {session!.role === "owner" ? "My properties" : "My bookings"}
          </a>
        </nav>
        <div className="account">
          <span>
            {session!.account.firstName} · {session!.role}
          </span>
          <button
            onClick={() => {
              logout();
              location.hash = "";
            }}
          >
            Log out
          </button>
        </div>
      </header>
      <main id="main" tabIndex={-1} key={route}>
        {page}
      </main>
    </>
  );
}
export default function App() {
  const { session } = useAuth();
  // Logout unmounts every private view, destroying fetched data and active requests.
  return (
    <>
      <a
        href="#main"
        className="skip-link"
        onClick={(e) => {
          e.preventDefault();
          document.getElementById("main")?.focus();
        }}
      >
        Skip to content
      </a>
      {session ? (
        <Workspace key={session.account.userId} />
      ) : (
        <main id="main" tabIndex={-1}>
          <AuthPage />
        </main>
      )}
      <footer>
        Osyntra Booking Platform <span>V1 · A simpler way to stay</span>
      </footer>
    </>
  );
}
