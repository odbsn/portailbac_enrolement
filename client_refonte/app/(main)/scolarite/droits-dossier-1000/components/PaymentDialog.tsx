import React, { useRef } from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { InputMask } from "primereact/inputmask";
import { InputNumber } from "primereact/inputnumber";
import { useFormik } from "formik";
import * as Yup from "yup";
import axiosInstance from "@/app/api/axiosInstance";
import { Toast } from "primereact/toast";

interface PaymentDialogProps {
  visible: boolean;
  onHide: () => void;
  user: any;
  program: any;
  scriptLoaded: boolean;
  onSuccess: () => void;
  onError: (message: string) => void;
}

interface PaymentFormValues {
  nbCdtsInscrits: number;
  phoneNumber: string;
}

const PaymentDialog: React.FC<PaymentDialogProps> = ({
  visible,
  onHide,
  user,
  program,
  scriptLoaded,
  onSuccess,
  onError,
}) => {
  const toast = useRef<Toast>(null);

  const formik = useFormik<PaymentFormValues>({
    initialValues: {
      nbCdtsInscrits: 0,
      phoneNumber: "",
    },
    validationSchema: Yup.object({
      nbCdtsInscrits: Yup.number()
        .required("Le nombre de candidats est requis")
        .moreThan(0, "Le nombre doit être supérieur à 0")
        .integer("Le nombre doit être un entier"),
      phoneNumber: Yup.string()
        .required("Le numéro de téléphone est requis")
        .min(9, "Numéro invalide")
        .max(9, "Numéro invalide"),
    }),
    onSubmit: async (values, { setSubmitting, resetForm }) => {
      const amount = values.nbCdtsInscrits * 1000;

      try {
        const response = await axiosInstance.post(
          `/payment-FAEB3/createPayment/${
            user?.acteur?.etablissement?.code
          }/${Number(program?.edition)}`,
          {
            nbCdtsInscrits: values.nbCdtsInscrits,
            montantAVerser: amount,
            phoneNumber: values.phoneNumber,
            montantVerser: amount,
          },
        );

        const {
          orderNumber,
          amount: paymentAmount,
          phoneNumber,
        } = response.data;

        const newWin = window.open("", "_blank", "width=500,height=700");
        if (!newWin) {
          alert("Veuillez autoriser les popups pour ce site.");
          return;
        }

        newWin.document.write(`
          <!DOCTYPE html>
          <html>
            <head>
              <title>Paiement en ligne</title>
              <style>
                body { font-family: 'Inter', -apple-system, sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; background: #f8fafc; }
                #touchpay-container { width: 100%; max-width: 500px; padding: 20px; }
                .loader { text-align: center; padding: 60px 20px; background: white; border-radius: 16px; box-shadow: 0 4px 24px rgba(0,0,0,0.06); }
                .loader i { font-size: 48px; color: #2563eb; animation: spin 1s linear infinite; display: block; margin-bottom: 20px; }
                .loader p { color: #64748b; font-size: 16px; margin: 0; }
                @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
              </style>
            </head>
            <body>
              <div id="touchpay-container">
                <div class="loader">
                  <i class="pi pi-spinner"></i>
                  <p>Chargement du module de paiement sécurisé...</p>
                </div>
              </div>
              <script src="https://touchpay.gutouch.net/touchpayv2/script/touchpaynr/prod_touchpay-0.0.1.js"><\/script>
              <script>
                window.onload = function() {
                  if (typeof window.sendPaymentInfos !== "function") {
                    alert("Le widget de paiement n'a pas pu être chargé.");
                    return;
                  }
                  window.sendPaymentInfos(
                    "${orderNumber}",
                    "ODB26571",
                    "cBbFbOecN700AnZX6SvSJoYAhVOBusnStLx90ULZ6jaNi6ZD0C",
                    "odb.sn",
                    "https://portailbac.ucad.sn/scolarite/droits-dossier-1000",
                    "https://portailbac.ucad.sn/scolarite/droits-dossier-1000",
                    "${paymentAmount}",
                    "Dakar",
                    "",
                    "",
                    "",
                    "${phoneNumber}"
                  );
                };
              <\/script>
            </body>
          </html>
        `);
        newWin.document.close();

        resetForm();
        onHide();
        onSuccess();
      } catch (error: any) {
        console.error("❌ Erreur de paiement:", error);
        onError(error?.response?.data?.message || "Erreur lors du paiement");
      } finally {
        setSubmitting(false);
      }
    },
  });

  const amount = (formik.values.nbCdtsInscrits || 0) * 1000;

  return (
    <>
      <Toast ref={toast} />
      <Dialog
        visible={visible}
        header={
          <div className="dialog-header">
            <div className="dialog-header-icon">
              <i className="pi pi-wallet"></i>
            </div>
            <div>
              <h3 className="dialog-header-title">Paiement des droits</h3>
              <p className="dialog-header-subtitle">Office du Bac</p>
            </div>
          </div>
        }
        modal
        className="payment-dialog"
        onHide={onHide}
        style={{ width: "95vw", maxWidth: "520px" }}
        contentClassName="p-0"
        headerClassName="p-4"
        footer={
          <div className="dialog-footer">
            <Button
              label="Annuler"
              icon="pi pi-times"
              className="btn-cancel"
              onClick={onHide}
            />
            <Button
              label="Payer maintenant"
              icon="pi pi-lock"
              className="btn-pay"
              onClick={() => formik.handleSubmit()}
              loading={formik.isSubmitting}
              disabled={
                !formik.values.nbCdtsInscrits || !formik.values.phoneNumber
              }
            />
          </div>
        }
      >
        <div className="dialog-body">
          {/* Info Cards */}
          <div className="info-cards">
            <div className="info-card info-card-blue">
              <div className="info-card-header">
                <i className="pi pi-mobile"></i>
                <span>Paiement mobile</span>
              </div>
              <p className="info-card-text">Wave • Orange Money • Free Money</p>
            </div>
            <div className="info-card info-card-green">
              <div className="info-card-header">
                <i className="pi pi-shield"></i>
                <span>Sécurisé</span>
              </div>
              <p className="info-card-text">Transaction cryptée</p>
            </div>
          </div>

          {/* Form */}
          <form className="payment-form">
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">
                  Numéro de téléphone
                  <span className="required">*</span>
                </label>
                <InputMask
                  id="phoneNumber"
                  name="phoneNumber"
                  mask="999999999"
                  placeholder="77 000 00 00"
                  value={formik.values.phoneNumber}
                  onChange={(e) => formik.setFieldValue("phoneNumber", e.value)}
                  onBlur={formik.handleBlur}
                  className={`form-input ${
                    formik.touched.phoneNumber && formik.errors.phoneNumber
                      ? "form-input-error"
                      : ""
                  }`}
                />
                {formik.touched.phoneNumber && formik.errors.phoneNumber && (
                  <div className="form-error">
                    <i className="pi pi-exclamation-circle"></i>
                    {formik.errors.phoneNumber}
                  </div>
                )}
              </div>

              <div className="form-group">
                <label className="form-label">
                  Nombre de candidats
                  <span className="required">*</span>
                </label>
                <InputNumber
                  placeholder="Ex: 5"
                  id="nbCdtsInscrits"
                  name="nbCdtsInscrits"
                  min={1}
                  value={formik.values.nbCdtsInscrits}
                  onChange={(e) =>
                    formik.setFieldValue("nbCdtsInscrits", e.value)
                  }
                  onBlur={formik.handleBlur}
                  mode="decimal"
                  useGrouping={true}
                  locale="fr-FR"
                  className={`form-input ${
                    formik.touched.nbCdtsInscrits &&
                    formik.errors.nbCdtsInscrits
                      ? "form-input-error"
                      : ""
                  }`}
                />
                {formik.touched.nbCdtsInscrits &&
                  formik.errors.nbCdtsInscrits && (
                    <div className="form-error">
                      <i className="pi pi-exclamation-circle"></i>
                      {formik.errors.nbCdtsInscrits}
                    </div>
                  )}
              </div>
            </div>

            {/* Amount Display */}
            <div className="amount-card">
              <div className="amount-card-content">
                <div>
                  <p className="amount-card-label">Montant à payer</p>
                  <div className="amount-card-value">
                    <span className="amount-card-number">
                      {amount.toLocaleString("fr-FR")}
                    </span>
                    <span className="amount-card-currency">FCFA</span>
                  </div>
                </div>
                {amount > 0 && (
                  <div className="amount-card-status">
                    <i className="pi pi-check-circle"></i>
                    Prêt
                  </div>
                )}
              </div>
              {amount === 0 && (
                <p className="amount-card-hint">
                  <i className="pi pi-info-circle"></i>
                  Saisissez le nombre de candidats
                </p>
              )}
            </div>

            {/* Security Notice */}
            <div className="security-notice">
              <span>
                <i className="pi pi-lock"></i> Paiement sécurisé
              </span>
              <span className="divider">|</span>
              <span>
                <i className="pi pi-shield"></i> TouchPay
              </span>
              <span className="divider">|</span>
              <span>
                <i className="pi pi-check-circle"></i> 100% fiable
              </span>
            </div>
          </form>
        </div>
      </Dialog>
    </>
  );
};

export default PaymentDialog;
