import React, {
  useEffect,
  useState,
  forwardRef,
  useImperativeHandle,
  useCallback,
  useMemo,
  useRef,
} from "react";
import { Dialog } from "primereact/dialog";
import { Button } from "primereact/button";
import { InputText } from "primereact/inputtext";
import { Dropdown } from "primereact/dropdown";
import { InputNumber } from "primereact/inputnumber";
import { Toast } from "primereact/toast";
import { ProgressSpinner } from "primereact/progressspinner";
import { Badge } from "primereact/badge";
import {
  AutoComplete,
  AutoCompleteCompleteEvent,
} from "primereact/autocomplete";
import { useCandidatStore, CandidatFinis } from "../convocationStore";
import axiosInstance from "@/app/api/axiosInstance";
import { ParametrageService } from "@/demo/service/ParametrageService";

// ==================== CONSTANTES ====================

const SERIE_OPTIONS = [
  "STEG",
  "S2",
  "L'1",
  "S1",
  "L2",
  "S2A",
  "LA",
  "L-AR",
  "S3",
  "F6",
  "S1A",
  "L1B",
  "STIDD",
  "T1",
  "T2",
  "S4",
  "S5",
  "L1A",
];

const NATIONALITE_OPTIONS = [
  "SENEGAL",
  "BENIN",
  "CONGO",
  "TOGO",
  "GUINEE",
  "BURKINA FASO",
  "MAURITANIE",
  "CAMEROUN",
  "CHINE",
  "ETATS UNIS D'AMERIQUE",
  "GUINEE EQUATORIALE",
  "NIGERIA",
  "COTED'IVOIRE",
  "ITALIE",
  "FRANCE",
  "TCHAD",
  "GUINEE BISSAU",
  "CAP VERT",
  "COMORES",
  "GABON",
  "MALI",
  "REPUBLIQUE CENTRAFRICAINE",
  "MAROC",
  "ESPAGNE",
  "ALLEMAGNE",
  "R. D. CONGO",
  "NIGER",
  "SYRIE",
  "LIBERIA",
  "MADAGASCAR",
  "TUNISIE",
  "GAMBIE",
  "DJIBOUTI",
  "CANADA",
  "AFRIQUE DU SUD",
  "ALGERIE",
  "PORTUGAL",
  "GHANA",
  "SOUDAN",
  "JAPON",
  "ANGOLA",
  "EGYPTE",
  "SIERRA LEONE",
  "BURUNDI",
  "ANGLETERRE",
  "BELGIQUE",
];

const EF1_OPTIONS = ["Dessin", "Musique", "Couture"];

const EF2_OPTIONS = [
  "Espagnol",
  "Arabe Moderne",
  "Latin",
  "Allemand",
  "Italien",
  "Portugais",
  "Russe",
  "Anglais",
];

const MO1_OPTIONS = [
  "Management des organisations",
  "Espagnol",
  "Anglais",
  "Arabe Moderne",
  "Gestion comptable et financière (Etude de cas)",
  "Sciences Economiques et Sociales",
  "Portugais",
  "Allemand",
  "Génie Electrique",
  "Génie mécanique",
];

const MO2_OPTIONS = [
  "Anglais",
  "Espagnol",
  "Arabe Moderne",
  "Economie",
  "Portugais",
  "Italien",
  "Allemand",
  "Russe",
  "Latin",
];

const MO3_OPTIONS = [
  "Sciences Physiques",
  "Sciences de la vie et de la Terre",
  "Latin",
];

// ==================== CONFIG OPTIONS PAR SÉRIE ====================

interface SerieOptionConfig {
  count: number;
  labels: string[];
  required: boolean[];
  mo1Options?: { label: string; value: string }[];
  mo2Options?: { label: string; value: string }[];
  mo3Options?: { label: string; value: string }[];
}

const toOpts = (arr: string[]) => arr.map((o) => ({ label: o, value: o }));

// Toutes les matières optionnelles sont NON obligatoires (required: false)
const SERIE_OPTIONS_CONFIG: Record<string, SerieOptionConfig> = {
  S1: { count: 0, labels: [], required: [] },
  S2: { count: 0, labels: [], required: [] },
  S3: { count: 0, labels: [], required: [] },
  S4: { count: 0, labels: [], required: [] },
  S5: { count: 0, labels: [], required: [] },
  S1A: { count: 0, labels: [], required: [] },
  S2A: { count: 0, labels: [], required: [] },
  T1: { count: 0, labels: [], required: [] },
  T2: { count: 0, labels: [], required: [] },
  F6: { count: 0, labels: [], required: [] },
  LA: { count: 0, labels: [], required: [] },
  "L-AR": { count: 0, labels: [], required: [] },

  STEG: {
    count: 1,
    labels: ["Spécialité (projet)"],
    required: [false],
    mo1Options: toOpts(MO1_OPTIONS),
  },
  STIDD: {
    count: 1,
    labels: ["Enseign. de spécialité"],
    required: [false],
    mo1Options: toOpts(MO1_OPTIONS),
  },

  "L'1": {
    count: 2,
    labels: ["LV1", "LV2"],
    required: [false, false],
    mo1Options: toOpts(MO1_OPTIONS),
    mo2Options: toOpts(MO2_OPTIONS),
  },
  L1A: {
    count: 2,
    labels: ["LV1", "L.C"],
    required: [false, false],
    mo1Options: toOpts(MO1_OPTIONS),
    mo2Options: toOpts(MO2_OPTIONS),
  },

  L2: {
    count: 3,
    labels: ["LV1", "L.V.2 ou ECONOMIE", "Sciences de la Nature (P.C ou SVT)"],
    required: [false, false, false],
    mo1Options: toOpts(MO1_OPTIONS),
    mo2Options: toOpts(MO2_OPTIONS),
    mo3Options: toOpts(MO3_OPTIONS),
  },
  L1B: {
    count: 3,
    labels: ["LV1", "LV2", "L.C"],
    required: [false, false, false],
    mo1Options: toOpts(MO1_OPTIONS),
    mo2Options: toOpts(MO2_OPTIONS),
    mo3Options: toOpts(MO3_OPTIONS),
  },
};

const getSerieConfig = (serie?: string): SerieOptionConfig | null => {
  if (!serie) return null;
  if (Object.prototype.hasOwnProperty.call(SERIE_OPTIONS_CONFIG, serie)) {
    return SERIE_OPTIONS_CONFIG[serie];
  }
  return {
    count: 3,
    labels: [
      "Matière optionnelle 1",
      "Matière optionnelle 2",
      "Matière optionnelle 3",
    ],
    required: [false, false, false],
    mo1Options: toOpts(MO1_OPTIONS),
    mo2Options: toOpts(MO2_OPTIONS),
    mo3Options: toOpts(MO3_OPTIONS),
  };
};

// ==================== TYPES ====================

interface CandidatDialogProps {
  visible: boolean;
  onHide: () => void;
  candidatId?: string | null;
  onSuccess?: () => void;
}

export interface CandidatDialogRef {
  open: (id?: string) => void;
  close: () => void;
}

// ==================== FONCTION UTILITAIRE POUR CALCULER L'ÂGE ====================

const calculateAge = (dateNaissance: string): number | undefined => {
  if (!dateNaissance) return undefined;

  const birthDate = new Date(dateNaissance);
  if (isNaN(birthDate.getTime())) return undefined;

  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();

  if (
    monthDiff < 0 ||
    (monthDiff === 0 && today.getDate() < birthDate.getDate())
  ) {
    age--;
  }

  return age;
};

// ==================== SOUS-COMPOSANTS ====================

const EtablissementDropdown = React.memo(
  ({
    value,
    onChange,
    placeholder = "Sélectionner un établissement...",
    required = false,
    label = "Établissement",
  }: {
    value: any;
    onChange: (value: any) => void;
    placeholder?: string;
    required?: boolean;
    label?: string;
  }) => {
    const [options, setOptions] = useState<{ label: string; value: string }[]>(
      [],
    );
    const [loading, setLoading] = useState(false);

    useEffect(() => {
      const loadOptions = async () => {
        setLoading(true);
        try {
          const etablissements = await ParametrageService.getEtablissements();
          setOptions(
            (etablissements || []).map((e: any) => ({
              label: e.name || e.nom,
              value: e.id,
            })),
          );
        } catch (error) {
          console.error("Erreur chargement établissements:", error);
        } finally {
          setLoading(false);
        }
      };
      loadOptions();
    }, []);

    return (
      <div className="input-wrapper">
        <label className="input-label">{label}</label>
        <Dropdown
          value={value}
          options={options}
          onChange={(e) => onChange(e.value)}
          placeholder={placeholder}
          showClear
          loading={loading}
          virtualScrollerOptions={{ itemSize: 34, showLoader: true, delay: 0 }}
          filter
          filterBy="label"
          filterPlaceholder="Rechercher..."
          className="w-full"
          panelStyle={{ maxHeight: "350px" }}
          style={{ width: "100%" }}
        />
      </div>
    );
  },
);
EtablissementDropdown.displayName = "EtablissementDropdown";

const VilleSearch = React.memo(
  ({
    value,
    onChange,
    placeholder = "Rechercher une ville...",
    required = false,
    label = "Lieu de naissance",
  }: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    required?: boolean;
    label?: string;
  }) => {
    const [searchQuery, setSearchQuery] = useState("");
    const [filteredOptions, setFilteredOptions] = useState<any[]>([]);
    const [allOptions, setAllOptions] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const searchTimeout = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
      const loadOptions = async () => {
        setLoading(true);
        try {
          const villes = await ParametrageService.getVilles();
          setAllOptions(
            (villes || []).map((v: any) => ({
              label: v.name || v.nom,
              value: v.id,
            })),
          );
          setFilteredOptions([]);
        } catch (error) {
          console.error("Erreur chargement villes:", error);
        } finally {
          setLoading(false);
        }
      };
      loadOptions();
    }, []);

    const searchVilles = useCallback(
      (query: string) => {
        if (searchTimeout.current) clearTimeout(searchTimeout.current);
        searchTimeout.current = setTimeout(() => {
          if (!query || query.length < 2) {
            setFilteredOptions([]);
            return;
          }
          const filtered = allOptions.filter((o) =>
            o.label.toLowerCase().includes(query.toLowerCase()),
          );
          setFilteredOptions(filtered.slice(0, 30));
        }, 300);
      },
      [allOptions],
    );

    const handleSearch = (event: AutoCompleteCompleteEvent) => {
      setSearchQuery(event.query);
      searchVilles(event.query);
    };

    const displayValue = useMemo(() => {
      if (searchQuery) return searchQuery;
      if (value) return value;
      return "";
    }, [searchQuery, value]);

    return (
      <div className="input-wrapper">
        <label className="input-label">
          {label}
          {required && <span className="required-star">*</span>}
        </label>
        <AutoComplete
          value={displayValue}
          suggestions={filteredOptions}
          completeMethod={handleSearch}
          field="label"
          placeholder={placeholder}
          onChange={(e) => {
            if (typeof e.value === "string") {
              setSearchQuery(e.value);
              if (e.value.trim() === "") onChange("");
            } else if (
              e.value &&
              typeof e.value === "object" &&
              e.value.label
            ) {
              onChange(e.value.label);
              setSearchQuery(e.value.label);
            } else {
              onChange("");
              setSearchQuery("");
            }
          }}
          onSelect={(e) => {
            onChange(e.value.label);
            setSearchQuery(e.value.label);
          }}
          delay={300}
          dropdown
          forceSelection={false}
        />
        {loading && (
          <i
            className="pi pi-spin pi-spinner"
            style={{
              position: "absolute",
              right: "10px",
              top: "50%",
              transform: "translateY(-50%)",
            }}
          />
        )}
      </div>
    );
  },
);
VilleSearch.displayName = "VilleSearch";

const ElegantDatePicker = React.memo(
  ({
    value,
    onChange,
    placeholder = "JJ/MM/AAAA",
    required = false,
    label = "Date de naissance",
  }: {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    required?: boolean;
    label?: string;
  }) => {
    const formatDate = (dateStr: string | null | undefined): string => {
      if (!dateStr || dateStr === "") return "";
      if (typeof dateStr === "string" && dateStr.includes("/")) {
        const parts = dateStr.split("/");
        if (parts.length === 3)
          return `${parts[0].padStart(2, "0")}/${parts[1].padStart(2, "0")}/${
            parts[2]
          }`;
      }
      if (typeof dateStr === "string" && dateStr.includes("-")) {
        const parts = dateStr.split("-");
        if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
      }
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return "";
      return `${String(date.getDate()).padStart(2, "0")}/${String(
        date.getMonth() + 1,
      ).padStart(2, "0")}/${date.getFullYear()}`;
    };

    const parseDate = (dateStr: string): Date | null => {
      if (!dateStr) return null;
      const parts = dateStr.split("/");
      if (parts.length !== 3) return null;
      const day = parseInt(parts[0], 10);
      const month = parseInt(parts[1], 10) - 1;
      const year = parseInt(parts[2], 10);
      if (isNaN(day) || isNaN(month) || isNaN(year)) return null;
      if (
        day < 1 ||
        day > 31 ||
        month < 0 ||
        month > 11 ||
        year < 1900 ||
        year > 2100
      )
        return null;
      const date = new Date(year, month, day);
      if (
        date.getDate() !== day ||
        date.getMonth() !== month ||
        date.getFullYear() !== year
      )
        return null;
      return date;
    };

    const parseAnyDate = (dateStr: string): Date | null => {
      if (!dateStr) return null;
      if (dateStr.includes("/")) return parseDate(dateStr);
      if (dateStr.includes("-")) {
        const parts = dateStr.split("-");
        if (parts.length === 3) {
          const date = new Date(
            parseInt(parts[0], 10),
            parseInt(parts[1], 10) - 1,
            parseInt(parts[2], 10),
          );
          return !isNaN(date.getTime()) ? date : null;
        }
      }
      return null;
    };

    const formatToISODate = (date: Date): string =>
      `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(
        2,
        "0",
      )}-${String(date.getDate()).padStart(2, "0")}`;

    const [isOpen, setIsOpen] = useState(false);
    const [selectedDate, setSelectedDate] = useState<Date | null>(
      parseAnyDate(value),
    );
    const [currentMonth, setCurrentMonth] = useState(
      () => parseAnyDate(value) || new Date(),
    );
    const [inputValue, setInputValue] = useState<string>(() =>
      formatDate(value),
    );
    const containerRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);
    const panelRef = useRef<HTMLDivElement>(null);
    const isTypingRef = useRef(false);

    const updateExternalDate = (date: Date | null) => {
      setSelectedDate(date);
      onChange(date ? formatToISODate(date) : "");
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      isTypingRef.current = true;
      let numbers = e.target.value
        .replace(/[^0-9/]/g, "")
        .replace(/\/{2,}/g, "/");
      let formattedValue = "";
      let parts = numbers.split("/");
      if (parts.length === 1) {
        formattedValue =
          parts[0].length >= 2
            ? `${parts[0].slice(0, 2)}/${parts[0].slice(2)}`
            : parts[0];
      } else if (parts.length === 2) {
        const jour = parts[0].slice(0, 2);
        formattedValue =
          parts[1].length >= 2
            ? `${jour}/${parts[1].slice(0, 2)}/${parts[1].slice(2)}`
            : `${jour}/${parts[1]}`;
      } else {
        formattedValue = `${parts[0].slice(0, 2)}/${parts[1].slice(
          0,
          2,
        )}/${parts.slice(2).join("").slice(0, 4)}`;
      }
      if (formattedValue.length > 10)
        formattedValue = formattedValue.slice(0, 10);
      setInputValue(formattedValue);
      if (formattedValue.length === 10) {
        const newDate = parseDate(formattedValue);
        if (newDate) {
          updateExternalDate(newDate);
          setCurrentMonth(newDate);
        }
      } else if (formattedValue === "") {
        updateExternalDate(null);
      }
      setTimeout(() => {
        isTypingRef.current = false;
      }, 100);
    };

    useEffect(() => {
      if (!isTypingRef.current) {
        const validDate = parseAnyDate(value);
        if (validDate) {
          setSelectedDate(validDate);
          setInputValue(formatDate(value));
          setCurrentMonth(validDate);
        } else {
          setSelectedDate(null);
          setInputValue("");
        }
      }
    }, [value]);

    const handleBlur = () => {
      setTimeout(() => {
        if (!containerRef.current?.contains(document.activeElement)) {
          setIsOpen(false);
          if (inputValue.length === 10) {
            const newDate = parseDate(inputValue);
            if (newDate) {
              updateExternalDate(newDate);
              setInputValue(formatDate(formatToISODate(newDate)));
            } else if (selectedDate)
              setInputValue(formatDate(formatToISODate(selectedDate)));
            else setInputValue("");
          } else if (selectedDate) {
            setInputValue(formatDate(formatToISODate(selectedDate)));
          } else setInputValue("");
        }
      }, 150);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter") {
        e.preventDefault();
        if (inputValue.length === 10) {
          const newDate = parseDate(inputValue);
          if (newDate) {
            updateExternalDate(newDate);
            setCurrentMonth(newDate);
            setIsOpen(false);
            inputRef.current?.blur();
          }
        }
      } else if (e.key === "Escape") {
        e.preventDefault();
        setIsOpen(false);
        setInputValue(
          selectedDate ? formatDate(formatToISODate(selectedDate)) : "",
        );
      } else if (e.key === "ArrowDown") {
        e.preventDefault();
        if (!isOpen) setIsOpen(true);
      } else if (e.key === "Tab") {
        setIsOpen(false);
      }
    };

    const handleDateSelect = (date: Date) => {
      updateExternalDate(date);
      setInputValue(formatDate(formatToISODate(date)));
      setCurrentMonth(date);
      setIsOpen(false);
      inputRef.current?.focus();
    };

    const generateCalendar = () => {
      const year = currentMonth.getFullYear();
      const month = currentMonth.getMonth();
      const startDay = new Date(year, month, 1).getDay();
      const daysInMonth = new Date(year, month + 1, 0).getDate();
      const adjustedStartDay = startDay === 0 ? 6 : startDay - 1;
      const days = [];
      for (let i = 0; i < adjustedStartDay; i++)
        days.push(<div key={`e-${i}`} className="calendar-day empty" />);
      for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(year, month, day);
        const isSelected =
          selectedDate && date.toDateString() === selectedDate.toDateString();
        const isToday = new Date().toDateString() === date.toDateString();
        days.push(
          <div
            key={day}
            className={`calendar-day ${isSelected ? "selected" : ""} ${
              isToday ? "today" : ""
            }`}
            onClick={() => handleDateSelect(date)}
          >
            {day}
          </div>,
        );
      }
      return days;
    };

    useEffect(() => {
      const handleClickOutside = (event: MouseEvent) => {
        if (
          containerRef.current &&
          !containerRef.current.contains(event.target as Node)
        )
          setIsOpen(false);
      };
      document.addEventListener("mousedown", handleClickOutside);
      return () =>
        document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    useEffect(() => {
      if (isOpen && inputRef.current && panelRef.current) {
        panelRef.current.style.width = `${inputRef.current.offsetWidth}px`;
      }
    }, [isOpen]);

    const monthNames = [
      "Janvier",
      "Février",
      "Mars",
      "Avril",
      "Mai",
      "Juin",
      "Juillet",
      "Août",
      "Septembre",
      "Octobre",
      "Novembre",
      "Décembre",
    ];

    return (
      <div className="elegant-date-picker" ref={containerRef}>
        {label && (
          <label className="input-label">
            {label}
            {required && <span className="required-star">*</span>}
          </label>
        )}
        <div className="date-input-wrapper">
          <input
            ref={inputRef}
            type="text"
            value={inputValue}
            onChange={handleInputChange}
            onBlur={handleBlur}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            className="date-input"
            onFocus={() => setIsOpen(true)}
          />
          <i
            className="pi pi-calendar date-icon"
            onClick={() => setIsOpen(!isOpen)}
          />
        </div>
        {isOpen && (
          <div className="date-panel" ref={panelRef}>
            <div className="calendar-header">
              <button
                type="button"
                onClick={() =>
                  setCurrentMonth(
                    new Date(
                      currentMonth.getFullYear(),
                      currentMonth.getMonth() - 1,
                      1,
                    ),
                  )
                }
                className="nav-button"
              >
                <i className="pi pi-chevron-left" />
              </button>
              <div className="month-year">
                {monthNames[currentMonth.getMonth()]}{" "}
                {currentMonth.getFullYear()}
              </div>
              <button
                type="button"
                onClick={() =>
                  setCurrentMonth(
                    new Date(
                      currentMonth.getFullYear(),
                      currentMonth.getMonth() + 1,
                      1,
                    ),
                  )
                }
                className="nav-button"
              >
                <i className="pi pi-chevron-right" />
              </button>
            </div>
            <div className="calendar-days">{generateCalendar()}</div>
            <div className="calendar-footer">
              <button
                type="button"
                className="clear-button"
                onClick={() => {
                  updateExternalDate(null);
                  setInputValue("");
                  setIsOpen(false);
                }}
              >
                Effacer
              </button>
              <button
                type="button"
                className="today-button"
                onClick={() => handleDateSelect(new Date())}
              >
                Aujourd'hui
              </button>
            </div>
          </div>
        )}
      </div>
    );
  },
);
ElegantDatePicker.displayName = "ElegantDatePicker";

const InputField = React.memo(
  ({
    label,
    field,
    type = "text",
    options,
    placeholder,
    required,
    value,
    onValueChange,
  }: {
    label: string;
    field: string;
    type?: string;
    options?: any[];
    placeholder?: string;
    required?: boolean;
    value: any;
    onValueChange: (field: string, value: any) => void;
  }) => {
    const getInput = useCallback(() => {
      switch (type) {
        case "date":
          return (
            <ElegantDatePicker
              value={value}
              onChange={(v: string) => onValueChange(field, v)}
              placeholder={placeholder}
              required={required}
              label=""
            />
          );
        case "number":
          return (
            <InputNumber
              value={value as number}
              onValueChange={(e) => onValueChange(field, e.value)}
              placeholder={placeholder}
            />
          );
        case "dropdown":
          return (
            <Dropdown
              value={value}
              options={options}
              onChange={(e) => onValueChange(field, e.value)}
              placeholder="Sélectionner..."
              showClear
              filter
              filterBy="label"
              filterPlaceholder="Rechercher..."
              virtualScrollerOptions={{
                itemSize: 34,
                showLoader: true,
                delay: 250,
              }}
            />
          );
        case "etablissement":
          return (
            <EtablissementDropdown
              value={value}
              onChange={(v) => onValueChange(field, v)}
              placeholder={placeholder}
              required={required}
              label=""
            />
          );
        case "ville":
          return (
            <InputText
              value={(value as string) || ""}
              onChange={(e) => onValueChange(field, e.target.value)}
              placeholder={placeholder}
            />
          );
        default:
          return (
            <InputText
              value={(value as string) || ""}
              onChange={(e) => onValueChange(field, e.target.value)}
              placeholder={placeholder}
            />
          );
      }
    }, [type, value, options, placeholder, field, onValueChange, required]);

    return (
      <div className="input-wrapper">
        <label className="input-label">
          {label}
          {required && <span className="required-star">*</span>}
        </label>
        {getInput()}
      </div>
    );
  },
);
InputField.displayName = "InputField";

const FormCard = React.memo(
  ({
    children,
    title,
    icon,
  }: {
    children: React.ReactNode;
    title: string;
    icon: string;
  }) => (
    <div className="form-card">
      <div className="form-card-header">
        <div className="form-card-icon">
          <i className={`pi ${icon}`} />
        </div>
        <h3 className="form-card-title">{title}</h3>
      </div>
      <div className="form-card-body">{children}</div>
    </div>
  ),
);
FormCard.displayName = "FormCard";

const FormRow = React.memo(({ children }: { children: React.ReactNode }) => (
  <div className="form-row">{children}</div>
));
FormRow.displayName = "FormRow";

const FormCol = React.memo(
  ({ children, size = 4 }: { children: React.ReactNode; size?: number }) => (
    <div className={`form-col col-${size}`}>{children}</div>
  ),
);
FormCol.displayName = "FormCol";

// ==================== COMPOSANT PRINCIPAL ====================

const CandidatDialog = forwardRef<CandidatDialogRef, CandidatDialogProps>(
  ({ visible, onHide, candidatId: externalId, onSuccess }, ref) => {
    const toast = useRef<Toast>(null);
    const { createCandidat, updateCandidat } = useCandidatStore();
    const [isLoading, setIsLoading] = useState(false);
    const [isEditing, setIsEditing] = useState(false);

    const [formData, setFormData] = useState<Partial<CandidatFinis>>({
      prenoms: "",
      nom: "",
      dateNaissance: "",
      lieuNaissance: "",
      nationalite: "",
      numeroTable: "",
      jury: "",
      serie: "",
      numeroDossier: "",
      sexe: "",
      age: undefined,
      eps: "",
      etablissement: undefined,
      centreExamen: undefined,
      centreActEPS: undefined,
      mo1: "",
      mo2: "",
      mo3: "",
      ef1: "",
      ef2: "",
      centreEcrit: undefined,
      centreEcritParticulier: undefined,
      typeCandidat: "",
      codeEtatCivil: "",
      libEtatCivil: "",
      anneeActe: "",
      refActeNaissance: "",
      telephone: "",
      handicap: "",
    });

    // Calcul automatique de l'âge lorsque la date de naissance change
    useEffect(() => {
      if (formData.dateNaissance) {
        const age = calculateAge(formData.dateNaissance);
        setFormData((prev) => ({ ...prev, age }));
      } else {
        setFormData((prev) => ({ ...prev, age: undefined }));
      }
    }, [formData.dateNaissance]);

    // ── Options mémorisées ─────────────────────────────────────────
    const serieOptionsMemo = useMemo(
      () => SERIE_OPTIONS.map((opt) => ({ label: opt, value: opt })),
      [],
    );
    const nationaliteOptionsMemo = useMemo(
      () =>
        NATIONALITE_OPTIONS.map((opt) => ({
          label: opt.charAt(0) + opt.slice(1).toLowerCase(),
          value: opt,
        })),
      [],
    );
    const ef1OptionsMemo = useMemo(
      () => EF1_OPTIONS.map((opt) => ({ label: opt, value: opt })),
      [],
    );
    const ef2OptionsMemo = useMemo(
      () => EF2_OPTIONS.map((opt) => ({ label: opt, value: opt })),
      [],
    );
    const sexeOptionsMemo = useMemo(
      () => [
        { label: "M", value: "M" },
        { label: "F", value: "F" },
      ],
      [],
    );
    const typeCandidatOptionsMemo = useMemo(
      () => [
        { label: "Régulier/Officiel", value: "Officiel" },
        { label: "Individuel/Libre", value: "Indiv./Libre" },
      ],
      [],
    );
    const typeEpsMemo = useMemo(
      () => [
        { label: "Apte", value: "A" },
        { label: "Inapte", value: "I" },
      ],
      [],
    );

    const currentSerieConfig = useMemo(
      () => getSerieConfig(formData.serie),
      [formData.serie],
    );

    const handleInputChange = useCallback(
      (field: keyof CandidatFinis, value: any) => {
        setFormData((prev) => {
          const next = { ...prev, [field]: value };
          if (field === "serie") {
            next.mo1 = "";
            next.mo2 = "";
            next.mo3 = "";
          }
          return next;
        });
      },
      [],
    );

    useEffect(() => {
      if (visible && externalId) loadCandidatData(externalId);
      else if (visible && !externalId) {
        resetForm();
        setIsEditing(false);
      }
    }, [visible, externalId]);

    const loadCandidatData = async (id: string) => {
      setIsLoading(true);
      try {
        const response = await axiosInstance.get(`/candidats/${id}`);
        const data = response.data;
        const getEtablissementId = (etab: any) => {
          if (!etab) return undefined;
          if (typeof etab === "string") return etab;
          if (etab.id) return etab.id;
          if (etab.name) return etab.name;
          return undefined;
        };
        setFormData({
          ...data,
          dateNaissance: data.dateNaissance || "",
          etablissement: getEtablissementId(data.etablissement),
          centreActEPS: getEtablissementId(data.centreActEPS),
          centreEcrit: getEtablissementId(data.centreEcrit),
          centreExamen: getEtablissementId(data.centreExamen),
          centreEcritParticulier: getEtablissementId(
            data.centreEcritParticulier,
          ),
        });
        setIsEditing(true);
      } catch (error) {
        console.error("Erreur chargement:", error);
        toast.current?.show({
          severity: "error",
          summary: "Erreur",
          detail: "Impossible de charger les données",
          life: 5000,
        });
      } finally {
        setIsLoading(false);
      }
    };

    const resetForm = () => {
      setFormData({
        prenoms: "",
        nom: "",
        dateNaissance: "",
        lieuNaissance: "",
        nationalite: "",
        numeroTable: "",
        jury: "",
        serie: "",
        numeroDossier: "",
        sexe: "",
        age: undefined,
        eps: "",
        etablissement: undefined,
        centreExamen: undefined,
        centreActEPS: undefined,
        mo1: "",
        mo2: "",
        mo3: "",
        ef1: "",
        ef2: "",
        centreEcrit: undefined,
        centreEcritParticulier: undefined,
        typeCandidat: "",
        codeEtatCivil: "",
        libEtatCivil: "",
        anneeActe: "",
        refActeNaissance: "",
        telephone: "",
        handicap: "",
      });
    };

    const validateForm = useCallback((): boolean => {
      const requiredFields = [
        { field: "nom", message: "Le nom est obligatoire" },
        { field: "prenoms", message: "Les prénoms sont obligatoires" },
        {
          field: "dateNaissance",
          message: "La date de naissance est obligatoire",
        },
        {
          field: "lieuNaissance",
          message: "Le lieu de naissance est obligatoire",
        },
        { field: "nationalite", message: "La nationalité est obligatoire" },
        { field: "numeroTable", message: "Le numéro de table est obligatoire" },
        { field: "jury", message: "Le jury est obligatoire" },
        { field: "serie", message: "La série est obligatoire" },
        { field: "sexe", message: "Le sexe est obligatoire" },
        { field: "eps", message: "L'EPS est obligatoire" },
        { field: "etablissement", message: "L'établissement est obligatoire" },
        { field: "centreActEPS", message: "Le centre EPS est obligatoire" },
        { field: "centreEcrit", message: "Le centre écrit est obligatoire" },
        {
          field: "typeCandidat",
          message: "Le type de candidat est obligatoire",
        },
        {
          field: "codeEtatCivil",
          message: "Le code état civil est obligatoire",
        },
        {
          field: "libEtatCivil",
          message: "Le libellé état civil est obligatoire",
        },
        { field: "anneeActe", message: "L'année d'acte est obligatoire" },
        {
          field: "refActeNaissance",
          message: "La référence d'acte de naissance est obligatoire",
        },
      ];

      for (const { field, message } of requiredFields) {
        const value = formData[field as keyof CandidatFinis];
        if (!value || (typeof value === "string" && !value.trim())) {
          toast.current?.show({
            severity: "warn",
            summary: "Champ requis",
            detail: message,
            life: 3000,
          });
          return false;
        }
      }

      if (formData.age && (formData.age <= 0 || formData.age > 120)) {
        toast.current?.show({
          severity: "warn",
          summary: "Valeur invalide",
          detail: "L'âge doit être compris entre 1 et 120 ans",
          life: 3000,
        });
        return false;
      }

      if (formData.anneeActe) {
        const annee = parseInt(formData.anneeActe);
        if (isNaN(annee) || annee < 1900 || annee > new Date().getFullYear()) {
          toast.current?.show({
            severity: "warn",
            summary: "Valeur invalide",
            detail: "L'année d'acte doit être valide",
            life: 3000,
          });
          return false;
        }
      }

      return true;
    }, [formData]);

    const handleSubmit = useCallback(async () => {
      if (!validateForm()) return;
      setIsLoading(true);
      try {
        const extractId = (value: any) => {
          if (!value) return undefined;
          if (typeof value === "string") return value;
          if (value.id) return value.id;
          if (value.name) return value.name;
          return undefined;
        };

        const formatDateForBackend = (
          dateStr: string | undefined,
        ): string | undefined => {
          if (!dateStr) return undefined;
          // Si déjà au format DD/MM/YYYY, on laisse tel quel
          if (dateStr.includes("/")) return dateStr;
          // Conversion de YYYY-MM-DD → DD/MM/YYYY
          const parts = dateStr.split("-");
          if (parts.length === 3) return `${parts[2]}/${parts[1]}/${parts[0]}`;
          return dateStr;
        };

        const submitData = {
          ...formData,
          dateNaissance: formatDateForBackend(formData.dateNaissance),
          etablissement: extractId(formData.etablissement),
          centreExamen: extractId(formData.centreExamen),
          centreActEPS: extractId(formData.centreActEPS),
          centreEcrit: extractId(formData.centreEcrit),
          centreEcritParticulier: extractId(formData.centreEcritParticulier),
        };

        let result;
        if (isEditing && externalId)
          result = await updateCandidat(externalId, submitData);
        else result = await createCandidat(submitData);

        if (result) {
          toast.current?.show({
            severity: "success",
            summary: "Succès",
            detail: isEditing ? "Candidat modifié" : "Candidat créé",
            life: 3000,
          });
          onSuccess?.();
          onHide();
          resetForm();
        }
      } catch (error) {
        console.error("Erreur lors de l'enregistrement:", error);
        toast.current?.show({
          severity: "error",
          summary: "Erreur",
          detail: "Erreur lors de l'enregistrement",
          life: 5000,
        });
      } finally {
        setIsLoading(false);
      }
    }, [
      validateForm,
      formData,
      isEditing,
      externalId,
      updateCandidat,
      createCandidat,
      onSuccess,
      onHide,
    ]);

    useImperativeHandle(ref, () => ({
      open: (id?: string) => {
        if (id) loadCandidatData(id);
        else {
          resetForm();
          setIsEditing(false);
        }
      },
      close: () => {
        onHide();
        resetForm();
      },
    }));

    const dialogHeader = useMemo(
      () => (
        <div className="premium-header">
          <div className="premium-header-content">
            <div className="premium-header-icon">
              <i
                className={`pi ${
                  isEditing ? "pi-pencil" : "pi-user-plus"
                } text-white text-xl`}
              />
            </div>
            <div className="premium-header-text">
              <h2 className="premium-header-title">
                {isEditing ? "Modification du candidat" : "Nouveau candidat"}
              </h2>
              <p className="premium-header-subtitle">
                {isEditing
                  ? "Mettez à jour les informations"
                  : "Saisissez les informations du candidat"}
              </p>
            </div>
            <div className="premium-header-badge">
              <Badge
                value={isEditing ? "Édition" : "Nouveau"}
                severity={isEditing ? "warning" : "info"}
              />
            </div>
          </div>
        </div>
      ),
      [isEditing],
    );

    const dialogFooter = useMemo(
      () => (
        <div className="premium-footer single-step-footer">
          <div className="footer-right">
            <Button
              label="Annuler"
              icon="pi pi-times"
              onClick={onHide}
              className="p-button-outlined p-button-secondary"
            />
            <Button
              label={isEditing ? "Mettre à jour" : "Créer le candidat"}
              icon="pi pi-check"
              onClick={handleSubmit}
              loading={isLoading}
              className="premium-btn-primary"
            />
          </div>
        </div>
      ),
      [onHide, handleSubmit, isLoading, isEditing],
    );

    if (isLoading && !isEditing) {
      return (
        <>
          <Toast ref={toast} />
          <Dialog
            visible={visible}
            header={dialogHeader}
            footer={dialogFooter}
            onHide={onHide}
            style={{ width: "95vw", maxWidth: "1400px", height: "85vh" }}
            className="premium-candidat-dialog"
            modal
            closable={!isLoading}
          >
            <div className="loading-container">
              <ProgressSpinner />
            </div>
          </Dialog>
        </>
      );
    }

    const renderMatiereOptionnelles = () => {
      const config = currentSerieConfig;
      if (!config || config.count === 0) {
        if (formData.serie) {
          return (
            <div className="no-options-notice">
              <i className="pi pi-info-circle" />
              <span>
                La série <strong>{formData.serie}</strong> ne comporte pas de
                matières optionnelles.
              </span>
            </div>
          );
        }
        return null;
      }

      const moFields = ["mo1", "mo2", "mo3"] as const;
      const moOptionsList = [
        config.mo1Options ?? [],
        config.mo2Options ?? [],
        config.mo3Options ?? [],
      ];
      const colSize = config.count === 1 ? 12 : config.count === 2 ? 6 : 4;

      return (
        <FormCard title="Matières optionnelles" icon="pi-book">
          <FormRow>
            {Array.from({ length: config.count }).map((_, i) => (
              <FormCol key={moFields[i]} size={colSize}>
                <InputField
                  label={config.labels[i] ?? `Option ${i + 1}`}
                  field={moFields[i]}
                  type="dropdown"
                  required={false}
                  options={moOptionsList[i]}
                  placeholder="Sélectionner (optionnel)"
                  value={formData[moFields[i]]}
                  onValueChange={handleInputChange}
                />
              </FormCol>
            ))}
          </FormRow>
        </FormCard>
      );
    };

    return (
      <>
        <Toast ref={toast} />
        <Dialog
          visible={visible}
          header={dialogHeader}
          footer={dialogFooter}
          onHide={onHide}
          style={{ width: "95vw", maxWidth: "1400px", minHeight: "98vh" }}
          className="premium-candidat-dialog"
          modal
          closable={!isLoading}
        >
          <div className="premium-dialog-body single-step-body">
            <div className="form-scroll-area single-step-scroll">
              {/* ══════════════ SECTION IDENTITÉ ══════════════ */}
              <FormCard title="Identité du candidat" icon="pi-id-card">
                <FormRow>
                  <FormCol size={3}>
                    <InputField
                      label="Prénom(s)"
                      field="prenoms"
                      placeholder="Prénom(s)"
                      required
                      value={formData.prenoms}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Nom"
                      field="nom"
                      placeholder="Nom"
                      required
                      value={formData.nom}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Date naissance"
                      field="dateNaissance"
                      type="date"
                      required
                      value={formData.dateNaissance}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={3}>
                    <InputField
                      label="Lieu naissance"
                      field="lieuNaissance"
                      type="ville"
                      placeholder="Ville..."
                      required
                      value={formData.lieuNaissance}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Nationalité"
                      field="nationalite"
                      type="dropdown"
                      required
                      options={nationaliteOptionsMemo}
                      placeholder="Sélectionner..."
                      value={formData.nationalite}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                </FormRow>

                <FormRow>
                  <FormCol size={2}>
                    <InputField
                      label="N° dossier"
                      field="numeroDossier"
                      placeholder="N° dossier"
                      value={formData.numeroDossier}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Sexe"
                      field="sexe"
                      type="dropdown"
                      required
                      options={sexeOptionsMemo}
                      value={formData.sexe}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Âge (auto-calculé)"
                      field="age"
                      type="number"
                      required
                      placeholder="Âge calculé automatiquement"
                      value={formData.age}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Téléphone"
                      field="telephone"
                      placeholder="Numéro"
                      value={formData.telephone}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="N° table"
                      field="numeroTable"
                      required
                      placeholder="N° table"
                      value={formData.numeroTable}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Série"
                      field="serie"
                      type="dropdown"
                      options={serieOptionsMemo}
                      required
                      value={formData.serie}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                </FormRow>

                <FormRow>
                  <FormCol size={2}>
                    <InputField
                      label="Jury"
                      field="jury"
                      required
                      placeholder="Jury"
                      value={formData.jury}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Type candidat"
                      field="typeCandidat"
                      type="dropdown"
                      required
                      options={typeCandidatOptionsMemo}
                      value={formData.typeCandidat}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="EPS"
                      field="eps"
                      type="dropdown"
                      required
                      options={typeEpsMemo}
                      value={formData.eps}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Code état civil"
                      field="codeEtatCivil"
                      placeholder="Code"
                      required
                      value={formData.codeEtatCivil}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Centre état civil"
                      field="libEtatCivil"
                      required
                      placeholder="Centre d'état civil"
                      value={formData.libEtatCivil}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={2}>
                    <InputField
                      label="Année acte"
                      field="anneeActe"
                      placeholder="Année"
                      required
                      value={formData.anneeActe}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={3}>
                    <InputField
                      label="Réf. acte naissance"
                      field="refActeNaissance"
                      placeholder="Référence"
                      required
                      value={formData.refActeNaissance}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={9}>
                    <InputField
                      label="Informations handicap"
                      field="handicap"
                      placeholder="Description du handicap (optionnel)"
                      value={formData.handicap}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                </FormRow>
              </FormCard>

              {/* ══════════════ SECTION CENTRES ══════════════ */}
              <FormCard title="Centres d'examen" icon="pi-building">
                <FormRow>
                  <FormCol size={6}>
                    <InputField
                      label="Établissement"
                      field="etablissement"
                      type="etablissement"
                      required
                      placeholder="Sélectionner..."
                      value={formData.etablissement}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={6}>
                    <InputField
                      label="Centre EPS"
                      field="centreActEPS"
                      type="etablissement"
                      required
                      placeholder="Sélectionner..."
                      value={formData.centreActEPS}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={6}>
                    <InputField
                      label="Centre écrit"
                      field="centreEcrit"
                      type="etablissement"
                      required
                      placeholder="Sélectionner..."
                      value={formData.centreEcrit}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={6}>
                    <InputField
                      label="Centre écrit particulier"
                      field="centreEcritParticulier"
                      type="etablissement"
                      placeholder="Sélectionner (optionnel)"
                      value={formData.centreEcritParticulier}
                      onValueChange={handleInputChange}
                    />
                    <div className="field-description">
                      <i
                        className="pi pi-info-circle"
                        style={{ fontSize: "0.65rem", marginRight: "0.25rem" }}
                      />
                      Optionnel — uniquement si centre distinct
                    </div>
                  </FormCol>
                </FormRow>
              </FormCard>

              {/* Matières optionnelles */}
              {renderMatiereOptionnelles()}

              {/* Matières facultatives */}
              <FormCard title="Matières facultatives" icon="pi-flag">
                <FormRow>
                  <FormCol size={6}>
                    <InputField
                      label="EF1"
                      field="ef1"
                      type="dropdown"
                      options={ef1OptionsMemo}
                      placeholder="Sélectionner (optionnel)"
                      value={formData.ef1}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                  <FormCol size={6}>
                    <InputField
                      label="EF2"
                      field="ef2"
                      type="dropdown"
                      options={ef2OptionsMemo}
                      placeholder="Sélectionner (optionnel)"
                      value={formData.ef2}
                      onValueChange={handleInputChange}
                    />
                  </FormCol>
                </FormRow>
              </FormCard>
            </div>
          </div>
        </Dialog>

        <style jsx global>{`
          .premium-candidat-dialog .p-dialog-header {
            padding: 0;
          }
          .premium-candidat-dialog .p-dialog-content {
            padding: 0;
            overflow: hidden;
          }
          .premium-candidat-dialog .p-dialog {
            max-height: 85vh;
          }

          .premium-header {
            position: relative;
            overflow: hidden;
            border-radius: 12px 12px 0 0;
          }
          .premium-header-content {
            position: relative;
            display: flex;
            align-items: center;
            gap: 0.75rem;
            padding: 1rem 1.25rem;
            z-index: 1;
            color: #000;
          }
          .premium-header-icon {
            width: 2.5rem;
            height: 2.5rem;
            background: #000;
            border-radius: 0.75rem;
            display: flex;
            align-items: center;
            justify-content: center;
          }
          .premium-header-text {
            flex: 1;
          }
          .premium-header-title {
            font-size: 1.1rem;
            font-weight: 700;
            color: #000;
            margin: 0;
          }
          .premium-header-subtitle {
            font-size: 0.7rem;
            color: #475569;
            margin: 0.15rem 0 0 0;
          }

          .premium-footer {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 1rem;
            padding: 0.75rem 1rem;
            background: #f8fafc;
            border-top: 1px solid #e2e8f0;
            border-radius: 0 0 12px 12px;
          }
          .single-step-footer {
            justify-content: flex-end;
          }
          .footer-right {
            display: flex;
            gap: 0.75rem;
          }
          .premium-btn-primary {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            border: none;
            padding: 0.5rem 1.25rem;
            font-weight: 600;
            border-radius: 8px;
            transition: all 0.2s;
          }
          .premium-btn-primary:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
          }

          .premium-dialog-body {
            display: flex;
            flex-direction: column;
            height: 100%;
            background: #ffffff;
          }
          .single-step-body {
            height: calc(95vh - 120px);
            max-height: calc(95vh - 120px);
          }

          .form-scroll-area {
            flex: 1;
            padding: 1rem 1.25rem;
            overflow-y: auto;
            background: #ffffff;
          }
          .single-step-scroll {
            overflow-y: auto;
          }

          .form-card {
            background: white;
            border-radius: 10px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
            margin-bottom: 1rem;
            overflow: hidden;
          }
          .form-card-header {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.75rem 1rem;
            background: #fafbfc;
            border-bottom: 1px solid #f1f5f9;
          }
          .form-card-icon {
            width: 1.5rem;
            height: 1.5rem;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.85rem;
          }
          .form-card-title {
            font-size: 0.875rem;
            font-weight: 600;
            color: #1e293b;
            margin: 0;
          }
          .form-card-body {
            padding: 1rem;
          }

          .form-row {
            display: flex;
            flex-wrap: wrap;
            margin: -0.5rem;
          }
          .form-col {
            padding: 0.5rem;
          }
          .col-2 {
            width: 16.666%;
          }
          .col-3 {
            width: 25%;
          }
          .col-4 {
            width: 33.333%;
          }
          .col-6 {
            width: 50%;
          }
          .col-9 {
            width: 75%;
          }
          .col-12 {
            width: 100%;
          }

          .input-wrapper {
            margin-bottom: 0;
            position: relative;
          }
          .input-label {
            display: block;
            font-size: 0.65rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.4px;
            color: #475569 !important;
            margin-bottom: 0.25rem;
          }
          .required-star {
            color: #ef4444;
            margin-left: 0.2rem;
          }
          .w-full {
            width: 100%;
          }

          .p-inputtext,
          .p-dropdown,
          .p-inputnumber {
            width: 100%;
            height: 34px !important;
            min-height: 34px !important;
            padding: 0.35rem 0.625rem !important;
            font-size: 0.8rem;
            border: 1.5px solid #e2e8f0;
            border-radius: 7px;
            transition: all 0.2s;
            background: white;
          }
          .p-inputtext:focus,
          .p-dropdown.p-focus,
          .p-inputnumber:focus-within {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }
          .p-dropdown {
            display: flex;
            align-items: center !important;
          }
          .p-dropdown .p-dropdown-label {
            border: none !important;
            box-shadow: none !important;
            background: transparent !important;
            display: flex !important;
            align-items: center !important;
            padding: 0.35rem 0.625rem !important;
            color: #1e293b !important;
            font-size: 0.8rem !important;
          }
          .p-dropdown .p-dropdown-label.p-placeholder {
            color: #94a3b8 !important;
          }
          .p-dropdown .p-dropdown-trigger {
            width: 2rem;
            height: 32px !important;
            border: none;
            background: transparent !important;
          }
          .p-inputnumber {
            align-items: center !important;
          }
          .p-inputnumber input,
          .p-inputnumber .p-inputnumber-input {
            border: none !important;
            box-shadow: none !important;
            background: transparent !important;
            padding: 0.35rem 0.625rem !important;
            font-size: 0.8rem !important;
          }
          .p-dropdown-panel {
            border-radius: 8px;
            margin-top: 0.2rem;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
          }

          .no-options-notice {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.625rem 1rem;
            margin-bottom: 0.875rem;
            background: #f0f9ff;
            border: 1px solid #bae6fd;
            border-radius: 8px;
            color: #0369a1;
            font-size: 0.8rem;
          }

          /* Date picker compact */
          .elegant-date-picker {
            position: relative;
            width: 100%;
          }
          .date-input-wrapper {
            position: relative;
            width: 100%;
          }
          .date-input {
            width: 100%;
            height: 34px;
            padding: 0.35rem 2.25rem 0.35rem 0.625rem;
            font-size: 0.8rem;
            border: 1.5px solid #e2e8f0;
            border-radius: 7px;
            transition: all 0.2s;
            background: white;
            font-family: inherit;
            color: #1e293b !important;
          }
          .date-input:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }
          .date-input::placeholder {
            color: #94a3b8 !important;
          }
          .date-icon {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            color: #94a3b8;
            cursor: pointer;
            transition: color 0.2s;
            font-size: 0.875rem;
          }
          .date-icon:hover {
            color: #3b82f6;
          }
          .date-panel {
            position: absolute;
            top: calc(100% + 6px);
            left: 0;
            background: white;
            border-radius: 10px;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
            border: 1px solid #e2e8f0;
            z-index: 1000;
            overflow: hidden;
          }
          .calendar-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0.5rem 0.75rem;
            background: #f8fafc;
            border-bottom: 1px solid #e2e8f0;
          }
          .month-year {
            font-weight: 600;
            font-size: 0.8rem;
            color: #1e293b;
          }
          .nav-button {
            background: transparent;
            border: none;
            cursor: pointer;
            padding: 0.2rem 0.4rem;
            border-radius: 5px;
            transition: all 0.2s;
            color: #64748b;
          }
          .nav-button:hover {
            background: #f1f5f9;
            color: #3b82f6;
          }
          .calendar-days {
            display: grid;
            grid-template-columns: repeat(7, 1fr);
            padding: 0.5rem;
            gap: 2px;
          }
          .calendar-day {
            text-align: center;
            padding: 0.35rem;
            font-size: 0.78rem;
            cursor: pointer;
            border-radius: 6px;
            transition: all 0.2s;
            color: #334155;
            font-weight: 500;
          }
          .calendar-day:hover:not(.empty) {
            background: #f1f5f9;
          }
          .calendar-day.selected {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: white;
            font-weight: 600;
          }
          .calendar-day.today {
            background: #e0f2fe;
            color: #0369a1;
            font-weight: 600;
          }
          .calendar-day.empty {
            cursor: default;
            background: transparent;
          }
          .calendar-footer {
            display: flex;
            justify-content: space-between;
            padding: 0.5rem 0.75rem;
            border-top: 1px solid #e2e8f0;
            background: #f8fafc;
          }
          .clear-button,
          .today-button {
            padding: 0.25rem 0.625rem;
            font-size: 0.7rem;
            border-radius: 5px;
            border: 1px solid #e2e8f0;
            background: white;
            cursor: pointer;
            transition: all 0.2s;
            font-weight: 500;
          }
          .clear-button {
            color: #ef4444;
          }
          .clear-button:hover {
            background: #fef2f2;
            border-color: #ef4444;
          }
          .today-button {
            color: #3b82f6;
          }
          .today-button:hover {
            background: #eff6ff;
            border-color: #3b82f6;
          }

          .loading-container {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 3rem;
            min-height: 300px;
          }
          .field-description {
            font-size: 0.65rem;
            color: #64748b;
            margin-top: 0.2rem;
          }

          @keyframes slideDown {
            from {
              opacity: 0;
              transform: translateY(-8px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }
          @keyframes slideIn {
            from {
              opacity: 0;
              transform: translateX(15px);
            }
            to {
              opacity: 1;
              transform: translateX(0);
            }
          }

          @media (max-width: 1024px) {
            .col-2,
            .col-3,
            .col-4,
            .col-6 {
              width: 50%;
            }
          }
          @media (max-width: 768px) {
            .top-tabs {
              flex-direction: column;
            }
            .top-tab {
              justify-content: flex-start;
              padding: 0.5rem 1rem;
            }
            .col-2,
            .col-3,
            .col-4,
            .col-6,
            .col-9,
            .col-12 {
              width: 100%;
            }
            .premium-footer {
              flex-direction: column;
            }
            .footer-left,
            .footer-right {
              width: 100%;
              justify-content: center;
            }
          }
        `}</style>
      </>
    );
  },
);

CandidatDialog.displayName = "CandidatDialog";
export default CandidatDialog;
