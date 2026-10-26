import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  // Preserve Origin: the backend validates the browser's exact configured origin.
  const target = env.API_PROXY_TARGET || "http://127.0.0.1:8080";
  const proxy = { "/api": { target }, "/health": { target } };
  return {
    plugins: [react()],
    server: { host: "127.0.0.1", port: 5173, strictPort: true, proxy },
    preview: { host: "127.0.0.1", port: 4173, strictPort: true, proxy },
    test: { environment: "jsdom" },
  };
});
