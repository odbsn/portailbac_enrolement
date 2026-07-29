import React from "react";
import { Button } from "primereact/button";

interface PaymentHeaderProps {
  title: string;
  remainingDays: number | null;
  onPay: () => void;
}

const PaymentHeader: React.FC<PaymentHeaderProps> = ({
  title,
  remainingDays,
  onPay,
}) => {
  const isOpen = remainingDays !== null && remainingDays > 0;

  return (
    <div className="payment-header">
      <div className="payment-header-content">
        <div className="payment-header-left">
          <div className="payment-header-badge">
            <div className="payment-header-icon">
              <i className="pi pi-credit-card"></i>
            </div>
            <div className="payment-header-status-wrapper">
              <span
                className={`status-badge ${
                  isOpen ? "status-badge-open" : "status-badge-closed"
                }`}
              >
                {isOpen ? "● En cours" : "● Fermé"}
              </span>
              {isOpen && remainingDays !== null && (
                <div className="payment-header-days">
                  <div className="payment-header-days-box">
                    <i className="pi pi-clock"></i>
                    <span>
                      <strong>{remainingDays}</strong> jour
                      {remainingDays > 1 ? "s" : ""} restant
                      {remainingDays > 1 ? "s" : ""}
                    </span>
                  </div>
                </div>
              )}
            </div>
          </div>

          <h1 className="payment-header-title">{title}</h1>
        </div>

        <div className="payment-header-action">
          {isOpen ? (
            <Button
              label="Effectuer un versement"
              icon="pi pi-plus-circle"
              className="btn-primary-gradient"
              onClick={onPay}
            />
          ) : (
            <div className="payment-header-closed">
              <div className="payment-header-closed-icon">
                <i className="pi pi-exclamation-triangle"></i>
              </div>
              <span className="payment-header-closed-text">
                La période d'ouverture des enrôlements est arrivée à échéance
              </span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default PaymentHeader;
