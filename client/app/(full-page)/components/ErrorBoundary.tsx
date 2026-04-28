"use client";

import React, { Component, ErrorInfo, ReactNode } from "react";

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class GlobalErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    // Ignorer l'erreur spécifique de useInsertionEffect
    if (
      error.message?.includes("useInsertionEffect must not schedule updates")
    ) {
      return { hasError: false, error: null };
    }
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // Ne pas logger l'erreur de useInsertionEffect
    if (
      !error.message?.includes("useInsertionEffect must not schedule updates")
    ) {
      console.error("Uncaught error:", error, errorInfo);
    }
  }

  render() {
    if (this.state.hasError) {
      return (
        this.props.fallback || (
          <div className="flex flex-column align-items-center justify-content-center min-h-screen p-4">
            <i className="pi pi-exclamation-triangle text-6xl text-red-500 mb-4"></i>
            <h2 className="text-900 mb-2">Une erreur est survenue</h2>
            <p className="text-600 mb-4 text-center">
              {this.state.error?.message || "Erreur inattendue"}
            </p>
            <button
              onClick={() => window.location.reload()}
              className="p-button p-component p-button-rounded"
              style={{
                background: "linear-gradient(135deg, #2196f3 0%, #1565C0 100%)",
                border: "none",
                padding: "0.75rem 1.5rem",
                borderRadius: "50px",
                color: "white",
                cursor: "pointer",
              }}
            >
              <i className="pi pi-refresh mr-2"></i>
              Recharger la page
            </button>
          </div>
        )
      );
    }

    return this.props.children;
  }
}
