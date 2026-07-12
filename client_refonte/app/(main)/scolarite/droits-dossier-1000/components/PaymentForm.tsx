import React from "react";
import { InputMask } from "primereact/inputmask";
import { InputNumber } from "primereact/inputnumber";

interface PaymentFormProps {
  values: {
    nbCdtsInscrits: number;
    phoneNumber: string;
  };
  errors: any;
  touched: any;
  handleChange: (e: any) => void;
  handleBlur: (e: any) => void;
  setFieldValue: (field: string, value: any) => void;
}

const PaymentForm: React.FC<PaymentFormProps> = ({
  values,
  errors,
  touched,
  handleChange,
  handleBlur,
  setFieldValue,
}) => {
  const amount = (values.nbCdtsInscrits || 0) * 1000;

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            * Numéro de téléphone
          </label>
          <InputMask
            id="phoneNumber"
            name="phoneNumber"
            mask="999999999"
            placeholder="77 000 00 00"
            value={values.phoneNumber}
            onChange={(e) => setFieldValue("phoneNumber", e.value)}
            onBlur={handleBlur}
            className={`w-full text-sm ${
              touched.phoneNumber && errors.phoneNumber ? "p-invalid" : ""
            }`}
          />
          {touched.phoneNumber && errors.phoneNumber && (
            <small className="text-red-500 text-xs">{errors.phoneNumber}</small>
          )}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            * Nombre de candidats
          </label>
          <InputNumber
            placeholder="Ex: 5"
            id="nbCdtsInscrits"
            name="nbCdtsInscrits"
            min={1}
            value={values.nbCdtsInscrits}
            onChange={(e) => setFieldValue("nbCdtsInscrits", e.value)}
            onBlur={handleBlur}
            mode="decimal"
            useGrouping={true}
            locale="fr-FR"
            className={`w-full text-sm ${
              touched.nbCdtsInscrits && errors.nbCdtsInscrits ? "p-invalid" : ""
            }`}
          />
          {touched.nbCdtsInscrits && errors.nbCdtsInscrits && (
            <small className="text-red-500 text-xs">
              {errors.nbCdtsInscrits}
            </small>
          )}
        </div>
      </div>

      <div className="bg-green-50 rounded-lg p-4 border border-green-200">
        <div className="flex items-center justify-between">
          <span className="text-gray-700 font-medium">Montant à payer</span>
          <span className="text-3xl font-bold text-green-600">
            {amount.toLocaleString("fr-FR")} FCFA
          </span>
        </div>
        {amount === 0 && (
          <p className="text-xs text-gray-500 mt-1">
            * Le montant sera calculé automatiquement
          </p>
        )}
      </div>
    </div>
  );
};

export default PaymentForm;
