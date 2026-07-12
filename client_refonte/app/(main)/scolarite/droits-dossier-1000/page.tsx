"use client";

import React, { useContext, useEffect, useState, useRef } from "react";
import { UserContext } from "@/app/userContext";
import { CandidatureService } from "@/demo/service/CandidatureService";
import { Toast } from "primereact/toast";
import "primereact/resources/themes/lara-light-blue/theme.css";
import "primereact/resources/primereact.min.css";
import "primeicons/primeicons.css";
import "./styles.css"; // Import custom styles

import PaymentDialog from "./components/PaymentDialog";
import TransactionsTable from "./components/TransactionsTable";
import PaymentHeader from "./components/PaymentHeader";

interface ProgramData {
  edition?: number;
  bfem_IfEPI?: number;
  bfem_IfI?: number;
  date_end?: string;
}

declare global {
  interface Window {
    sendPaymentInfos: (
      orderNumber: string,
      amount: string,
      phoneNumber: string,
      countryCode: string,
      secureCode: string,
      agencyCode: string,
      currency: string,
      description: string,
      successUrl: string,
      failureUrl: string,
      cancelUrl: string,
      logoUrl: string,
    ) => void;
  }
}

const DroitsDossierPage: React.FC = () => {
  const { user } = useContext(UserContext);
  const toast = useRef<Toast>(null);

  const [program, setProgram] = useState<ProgramData | null>(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showPaymentDialog, setShowPaymentDialog] = useState(false);
  const [reloadTrigger, setReloadTrigger] = useState(false);
  const [scriptLoaded, setScriptLoaded] = useState(false);

  useEffect(() => {
    CandidatureService.getLastProg().then((response) => {
      setProgram(response);
    });
  }, []);

  useEffect(() => {
    const loadTransactions = async () => {
      if (!user?.acteur?.etablissement?.id || !program?.edition) return;

      setLoading(true);
      try {
        const response = await CandidatureService.getDIByEtab(
          user?.acteur?.etablissement?.code,
          Number(program?.edition),
        );
        setTransactions(response);
      } catch (error) {
        console.error("❌ Erreur chargement transactions:", error);
        toast.current?.show({
          severity: "error",
          summary: "Erreur",
          detail: "Impossible de charger les transactions",
          life: 4000,
        });
      } finally {
        setLoading(false);
      }
    };

    loadTransactions();
  }, [reloadTrigger, user, program]);

  useEffect(() => {
    const script = document.createElement("script");
    script.src =
      "https://touchpay.gutouch.net/touchpayv2/script/touchpaynr/prod_touchpay-0.0.1.js";
    script.async = true;
    script.onload = () => setScriptLoaded(true);
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  const handlePaymentSuccess = () => {
    setReloadTrigger((prev) => !prev);
    toast.current?.show({
      severity: "success",
      summary: "Succès",
      detail: "Paiement effectué avec succès",
      life: 4000,
    });
  };

  const handlePaymentError = (message: string) => {
    toast.current?.show({
      severity: "error",
      summary: "Erreur",
      detail: message || "Erreur lors du paiement",
      life: 4000,
    });
  };

  const getRemainingDays = (): number | null => {
    if (!program?.date_end) return null;
    const today = new Date().getTime();
    const endDate = new Date(program.date_end).getTime();
    return Math.max(0, Math.ceil((endDate - today) / (1000 * 60 * 60 * 24)));
  };

  const remainingDays = getRemainingDays();

  return (
    <div className="page-container">
      <Toast ref={toast} />

      <div className="max-w-7xl mx-auto">
        <div className="page-card">
          <PaymentHeader
            title="Droits de dossier à 1000 FCFA"
            remainingDays={remainingDays}
            onPay={() => setShowPaymentDialog(true)}
          />

          <div className="p-4 md:p-8">
            <TransactionsTable transactions={transactions} loading={loading} />
          </div>
        </div>
      </div>

      <PaymentDialog
        visible={showPaymentDialog}
        onHide={() => setShowPaymentDialog(false)}
        user={user}
        program={program}
        scriptLoaded={scriptLoaded}
        onSuccess={handlePaymentSuccess}
        onError={handlePaymentError}
      />
    </div>
  );
};

export default DroitsDossierPage;
