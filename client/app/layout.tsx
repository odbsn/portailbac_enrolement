"use client";

import { LayoutProvider } from "../layout/context/layoutcontext";
import { PrimeReactProvider } from "primereact/api";
import { GlobalErrorBoundary } from "./(full-page)/components/ErrorBoundary";
import ClientOnly from "./(full-page)/components/ClientOnly";
import "primereact/resources/themes/lara-light-blue/theme.css";
import "primereact/resources/primereact.css";
import "primereact/resources/primereact.min.css";
import "primeflex/primeflex.css";
import "primeicons/primeicons.css";
import "../styles/layout/layout.scss";
import "../styles/demo/Demos.scss";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>
        <GlobalErrorBoundary>
          <ClientOnly>
            <PrimeReactProvider
              value={{
                ripple: true,
                inputStyle: "outlined",
              }}
            >
              <LayoutProvider>{children}</LayoutProvider>
            </PrimeReactProvider>
          </ClientOnly>
        </GlobalErrorBoundary>
      </body>
    </html>
  );
}
