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
import { useCandidatStore, CandidatFinis } from "../convocationStore";
import axiosInstance from "@/app/api/axiosInstance";
import { ParametrageService } from "@/demo/service/ParametrageService";

// Constantes déplacées en dehors du composant pour éviter les recréations
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

const SECTIONS = [
  {
    id: "general",
    label: "Informations générales",
    icon: "pi-id-card",
    color: "#3b82f6",
  },
  {
    id: "academic",
    label: "Centres et matières additionnelles",
    icon: "pi-building",
    color: "#3b82f6",
  },
];

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
    // Formatage de la date pour l'affichage (quel que soit le format d'entrée)
    const formatDate = (dateStr: string | null | undefined): string => {
      if (!dateStr || dateStr === "") return "";

      // Si c'est déjà au format DD/MM/YYYY
      if (typeof dateStr === "string" && dateStr.includes("/")) {
        const parts = dateStr.split("/");
        if (parts.length === 3) {
          const day = parts[0].padStart(2, "0");
          const month = parts[1].padStart(2, "0");
          const year = parts[2];
          return `${day}/${month}/${year}`;
        }
      }

      // Si c'est au format YYYY-MM-DD
      if (typeof dateStr === "string" && dateStr.includes("-")) {
        const parts = dateStr.split("-");
        if (parts.length === 3) {
          const year = parts[0];
          const month = parts[1];
          const day = parts[2];
          return `${day}/${month}/${year}`;
        }
      }

      // Si c'est un objet Date
      const date = new Date(dateStr);
      if (isNaN(date.getTime())) return "";
      const day = String(date.getDate()).padStart(2, "0");
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const year = date.getFullYear();
      return `${day}/${month}/${year}`;
    };

    // Parsing de la date (DD/MM/YYYY -> Date)
    const parseDate = (dateStr: string): Date | null => {
      if (!dateStr || dateStr === "") return null;
      const parts = dateStr.split("/");
      if (parts.length === 3) {
        const day = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10) - 1;
        const year = parseInt(parts[2], 10);

        if (isNaN(day) || isNaN(month) || isNaN(year)) return null;
        if (day < 1 || day > 31) return null;
        if (month < 0 || month > 11) return null;
        if (year < 1900 || year > 2100) return null;

        const date = new Date(year, month, day);
        if (
          date.getDate() !== day ||
          date.getMonth() !== month ||
          date.getFullYear() !== year
        ) {
          return null;
        }
        return date;
      }
      return null;
    };

    // Convertir n'importe quel format de date en Date
    const parseAnyDate = (dateStr: string): Date | null => {
      if (!dateStr || dateStr === "") return null;

      // Si c'est au format DD/MM/YYYY
      if (dateStr.includes("/")) {
        return parseDate(dateStr);
      }

      // Si c'est au format YYYY-MM-DD
      if (dateStr.includes("-")) {
        const parts = dateStr.split("-");
        if (parts.length === 3) {
          const year = parseInt(parts[0], 10);
          const month = parseInt(parts[1], 10) - 1;
          const day = parseInt(parts[2], 10);
          const date = new Date(year, month, day);
          return !isNaN(date.getTime()) ? date : null;
        }
      }

      return null;
    };

    // Formater Date en YYYY-MM-DD pour l'API
    const formatToISODate = (date: Date): string => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    };

    const [isOpen, setIsOpen] = useState(false);
    const [selectedDate, setSelectedDate] = useState<Date | null>(
      parseAnyDate(value),
    );
    const [currentMonth, setCurrentMonth] = useState(() => {
      const validDate = parseAnyDate(value);
      return validDate || new Date();
    });
    const [inputValue, setInputValue] = useState<string>(() => {
      return formatDate(value);
    });
    const containerRef = useRef<HTMLDivElement>(null);
    const inputRef = useRef<HTMLInputElement>(null);
    const panelRef = useRef<HTMLDivElement>(null);
    const isTypingRef = useRef(false);

    // Mettre à jour la date externe (envoie YYYY-MM-DD)
    const updateExternalDate = (date: Date | null) => {
      setSelectedDate(date);
      if (date) {
        onChange(formatToISODate(date));
      } else {
        onChange("");
      }
    };

    // Gestion de la saisie manuelle avec auto-formatage
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      isTypingRef.current = true;
      let rawValue = e.target.value;

      // Ne garder que les chiffres et les slashs
      let numbers = rawValue.replace(/[^0-9/]/g, "");
      numbers = numbers.replace(/\/{2,}/g, "/");

      let formattedValue = "";
      let parts = numbers.split("/");

      if (parts.length === 1) {
        const num = parts[0];
        if (num.length >= 2) {
          formattedValue = `${num.slice(0, 2)}/${num.slice(2)}`;
        } else {
          formattedValue = num;
        }
      } else if (parts.length === 2) {
        const jour = parts[0].slice(0, 2);
        const mois = parts[1];
        formattedValue = `${jour}/${mois}`;
        if (mois.length >= 2) {
          formattedValue = `${jour}/${mois.slice(0, 2)}/${mois.slice(2)}`;
        }
      } else if (parts.length >= 3) {
        const jour = parts[0].slice(0, 2);
        const mois = parts[1].slice(0, 2);
        const annee = parts.slice(2).join("").slice(0, 4);
        formattedValue = `${jour}/${mois}/${annee}`;
      }

      if (formattedValue.length > 10) {
        formattedValue = formattedValue.slice(0, 10);
      }

      setInputValue(formattedValue);

      if (formattedValue.length === 10) {
        const newDate = parseDate(formattedValue);
        if (newDate && !isNaN(newDate.getTime())) {
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

    // Synchroniser l'affichage avec la valeur externe
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

    // Gestion de la perte de focus
    const handleBlur = () => {
      setTimeout(() => {
        if (!containerRef.current?.contains(document.activeElement)) {
          setIsOpen(false);
          if (inputValue.length === 10) {
            const newDate = parseDate(inputValue);
            if (newDate && !isNaN(newDate.getTime())) {
              updateExternalDate(newDate);
              setInputValue(formatDate(formatToISODate(newDate)));
            } else if (selectedDate) {
              setInputValue(formatDate(formatToISODate(selectedDate)));
            } else {
              setInputValue("");
            }
          } else if (selectedDate) {
            setInputValue(formatDate(formatToISODate(selectedDate)));
          } else {
            setInputValue("");
          }
        }
      }, 150);
    };

    // Gestion des touches clavier
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
      switch (e.key) {
        case "Enter":
          e.preventDefault();
          if (inputValue.length === 10) {
            const newDate = parseDate(inputValue);
            if (newDate && !isNaN(newDate.getTime())) {
              updateExternalDate(newDate);
              setCurrentMonth(newDate);
              setIsOpen(false);
              inputRef.current?.blur();
            }
          }
          break;
        case "Escape":
          e.preventDefault();
          setIsOpen(false);
          setInputValue(
            selectedDate ? formatDate(formatToISODate(selectedDate)) : "",
          );
          break;
        case "ArrowDown":
          e.preventDefault();
          if (!isOpen) {
            setIsOpen(true);
          }
          break;
        case "Tab":
          setIsOpen(false);
          break;
        case "/":
          e.preventDefault();
          const currentValue = inputValue;
          if (currentValue.length === 2 && !currentValue.includes("/")) {
            setInputValue(`${currentValue}/`);
          } else if (
            currentValue.length === 5 &&
            currentValue.split("/").length === 2
          ) {
            setInputValue(`${currentValue}/`);
          }
          break;
      }
    };

    // Gestion de la sélection dans le calendrier
    const handleDateSelect = (date: Date) => {
      updateExternalDate(date);
      setInputValue(formatDate(formatToISODate(date)));
      setCurrentMonth(date);
      setIsOpen(false);
      inputRef.current?.focus();
    };

    // Navigation mois
    const prevMonth = () => {
      setCurrentMonth(
        new Date(currentMonth.getFullYear(), currentMonth.getMonth() - 1, 1),
      );
    };

    const nextMonth = () => {
      setCurrentMonth(
        new Date(currentMonth.getFullYear(), currentMonth.getMonth() + 1, 1),
      );
    };

    // Génération du calendrier
    const generateCalendar = () => {
      const year = currentMonth.getFullYear();
      const month = currentMonth.getMonth();
      const firstDayOfMonth = new Date(year, month, 1);
      const startDay = firstDayOfMonth.getDay();
      const daysInMonth = new Date(year, month + 1, 0).getDate();

      const calendarDays = [];

      let adjustedStartDay = startDay === 0 ? 6 : startDay - 1;

      for (let i = 0; i < adjustedStartDay; i++) {
        calendarDays.push(
          <div key={`empty-${i}`} className="calendar-day empty"></div>,
        );
      }

      for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(year, month, day);
        const isSelected =
          selectedDate &&
          date.getDate() === selectedDate.getDate() &&
          date.getMonth() === selectedDate.getMonth() &&
          date.getFullYear() === selectedDate.getFullYear();

        const isToday = new Date().toDateString() === date.toDateString();

        calendarDays.push(
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

      return calendarDays;
    };

    const calendarDays = generateCalendar();

    // Gestion du clic en dehors
    useEffect(() => {
      const handleClickOutside = (event: MouseEvent) => {
        if (
          containerRef.current &&
          !containerRef.current.contains(event.target as Node)
        ) {
          setIsOpen(false);
        }
      };

      document.addEventListener("mousedown", handleClickOutside);
      return () =>
        document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    // Ajustement de la largeur du panel
    useEffect(() => {
      if (isOpen && inputRef.current && panelRef.current) {
        const inputWidth = inputRef.current.offsetWidth;
        panelRef.current.style.width = `${inputWidth}px`;
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
              <button type="button" onClick={prevMonth} className="nav-button">
                <i className="pi pi-chevron-left" />
              </button>
              <div className="month-year">
                {monthNames[currentMonth.getMonth()]}{" "}
                {currentMonth.getFullYear()}
              </div>
              <button type="button" onClick={nextMonth} className="nav-button">
                <i className="pi pi-chevron-right" />
              </button>
            </div>

            <div className="calendar-days">{calendarDays}</div>

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
                onClick={() => {
                  const today = new Date();
                  handleDateSelect(today);
                }}
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

// Composant InputField mémorisé pour éviter les re-rendus inutiles
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
              onChange={(newValue: string) => onValueChange(field, newValue)}
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

const CandidatDialog = forwardRef<CandidatDialogRef, CandidatDialogProps>(
  ({ visible, onHide, candidatId: externalId, onSuccess }, ref) => {
    const toast = useRef<Toast>(null);
    const { createCandidat, updateCandidat } = useCandidatStore();
    const [isLoading, setIsLoading] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [activeSection, setActiveSection] = useState("general");
    const [etablissementOptions, setEtablissementOptions] = useState<
      { label: string; value: string }[]
    >([]);
    const [villeOptions, setVilleOptions] = useState<
      { label: string; value: string }[]
    >([]);

    const [formData, setFormData] = useState<Partial<CandidatFinis>>({
      prenoms: "",
      nom: "",
      dateNaissance: "",
      lieuNaissance: "",
      nationalite: "",
      numeroTable: "",
      jury: "",
      serie: "",
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
      typeCandidat: "",
      codeEtatCivil: "",
      libEtatCivil: "",
      anneeActe: "",
      refActeNaissance: "",
      telephone: "",
      handicap: "",
    });

    // Optimisation des options avec useMemo
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

    const mo1OptionsMemo = useMemo(
      () => MO1_OPTIONS.map((opt) => ({ label: opt, value: opt })),
      [],
    );

    const mo2OptionsMemo = useMemo(
      () => MO2_OPTIONS.map((opt) => ({ label: opt, value: opt })),
      [],
    );

    const mo3OptionsMemo = useMemo(
      () => MO3_OPTIONS.map((opt) => ({ label: opt, value: opt })),
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

    // Fonctions de navigation optimisées avec useCallback
    const goToPreviousSection = useCallback(() => {
      const currentIndex = SECTIONS.findIndex(
        (section) => section.id === activeSection,
      );
      if (currentIndex > 0) {
        setActiveSection(SECTIONS[currentIndex - 1].id);
      }
    }, [activeSection]);

    const goToNextSection = useCallback(() => {
      const currentIndex = SECTIONS.findIndex(
        (section) => section.id === activeSection,
      );
      if (currentIndex < SECTIONS.length - 1) {
        setActiveSection(SECTIONS[currentIndex + 1].id);
      }
    }, [activeSection]);

    // Fonction handleInputChange optimisée
    const handleInputChange = useCallback(
      (field: keyof CandidatFinis, value: any) => {
        setFormData((prev) => ({ ...prev, [field]: value }));
      },
      [],
    );

    // Chargement des options au montage uniquement
    useEffect(() => {
      const loadOptions = async () => {
        try {
          const etablissements = await ParametrageService.getEtablissements();
          const villes = await ParametrageService.getVilles();

          setEtablissementOptions(
            (etablissements || []).map((e: any) => ({
              label: e.name || e.nom,
              value: e.id,
            })),
          );

          setVilleOptions(
            (villes || []).map((v: any) => ({
              label: v.name || v.nom,
              value: v.id,
            })),
          );
        } catch (error) {
          console.error("Erreur chargement options:", error);
          toast.current?.show({
            severity: "error",
            summary: "Erreur",
            detail: "Impossible de charger les options",
            life: 5000,
          });
        }
      };
      loadOptions();
    }, []);

    // Gestion de la visibilité et chargement des données
    useEffect(() => {
      if (visible && externalId) {
        loadCandidatData(externalId);
      } else if (visible && !externalId) {
        resetForm();
        setIsEditing(false);
      }
    }, [visible, externalId]);

    const loadCandidatData = async (id: string) => {
      setIsLoading(true);
      try {
        const response = await axiosInstance.get(`/candidats/${id}`);
        const data = response.data;

        // Correction: extraction correcte des IDs
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
        typeCandidat: "",
        codeEtatCivil: "",
        libEtatCivil: "",
        anneeActe: "",
        refActeNaissance: "",
        telephone: "",
        handicap: "",
      });
      setActiveSection("general");
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
        { field: "age", message: "L'âge est obligatoire" },
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
        const anneeActuelle = new Date().getFullYear();
        if (isNaN(annee) || annee < 1900 || annee > anneeActuelle) {
          toast.current?.show({
            severity: "warn",
            summary: "Valeur invalide",
            detail:
              "L'année d'acte doit être valide (entre 1900 et l'année actuelle)",
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
        const extractEtablissementId = (value: any) => {
          if (!value) return undefined;
          if (typeof value === "string") return value;
          if (value.id) return value.id;
          if (value.name) return value.name;
          return undefined;
        };

        const submitData = {
          ...formData,
          dateNaissance: formData.dateNaissance || undefined,
          etablissement: extractEtablissementId(formData.etablissement),
          centreExamen: extractEtablissementId(formData.centreExamen),
          centreActEPS: extractEtablissementId(formData.centreActEPS),
          centreEcrit: extractEtablissementId(formData.centreEcrit),
        };

        let result;
        if (isEditing && externalId) {
          result = await updateCandidat(externalId, submitData);
        } else {
          result = await createCandidat(submitData);
        }

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
          <div className="premium-header-bg" />
          <div className="premium-header-content">
            <div className="premium-header-icon">
              <i
                className={`pi ${
                  isEditing ? "pi-pencil" : "pi-user-plus"
                } text-white text-2xl`}
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
        <div className="premium-footer">
          <div className="footer-left">
            <Button
              label="Précédent"
              icon="pi pi-chevron-left"
              onClick={goToPreviousSection}
              disabled={activeSection === SECTIONS[0].id}
              className="p-button-outlined p-button-secondary"
            />
            <Button
              label="Suivant"
              icon="pi pi-chevron-right"
              iconPos="right"
              onClick={goToNextSection}
              disabled={activeSection === SECTIONS[SECTIONS.length - 1].id}
              className="p-button-outlined p-button-secondary"
            />
          </div>
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
      [
        goToPreviousSection,
        goToNextSection,
        activeSection,
        onHide,
        handleSubmit,
        isLoading,
        isEditing,
      ],
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
            style={{ width: "90vw", maxWidth: "1200px" }}
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

    return (
      <>
        <Toast ref={toast} />
        <Dialog
          visible={visible}
          header={dialogHeader}
          footer={dialogFooter}
          onHide={onHide}
          style={{ width: "90vw", maxWidth: "1200px" }}
          className="premium-candidat-dialog"
          modal
          closable={!isLoading}
        >
          <div className="premium-dialog-body">
            <div className="top-tabs-container">
              <div className="top-tabs">
                {SECTIONS.map((section) => (
                  <button
                    key={section.id}
                    onClick={() => setActiveSection(section.id)}
                    className={`top-tab ${
                      activeSection === section.id ? "active" : ""
                    }`}
                    style={{
                      borderBottomColor:
                        activeSection === section.id
                          ? section.color
                          : "transparent",
                      color:
                        activeSection === section.id
                          ? section.color
                          : "#64748b",
                    }}
                  >
                    <i className={`pi ${section.icon} top-tab-icon`} />
                    <div className="top-tab-text">
                      <span className="top-tab-label">{section.label}</span>
                    </div>
                  </button>
                ))}
              </div>
            </div>

            <div className="form-scroll-area">
              {activeSection === "general" && (
                <>
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
                          placeholder="Lieu de naissance"
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
                          placeholder="Sélectionner une nationalité"
                          value={formData.nationalite}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                    </FormRow>

                    <div className="py-4"></div>

                    <FormRow>
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
                          label="Âge"
                          field="age"
                          type="number"
                          required
                          placeholder="Âge"
                          value={formData.age}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                      <FormCol size={2}>
                        <InputField
                          label="Téléphone"
                          field="telephone"
                          placeholder="Numéro de téléphone"
                          value={formData.telephone}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                      <FormCol size={2}>
                        <InputField
                          label="Numéro table"
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
                    </FormRow>

                    <div className="py-4"></div>

                    <FormRow>
                      <FormCol size={3}>
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
                      <FormCol size={3}>
                        <InputField
                          label="Centre d'état civil"
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
                    </FormRow>

                    <div className="py-4"></div>

                    <FormRow>
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
                          placeholder="Description du handicap"
                          value={formData.handicap}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                    </FormRow>
                  </FormCard>
                </>
              )}

              {activeSection === "academic" && (
                <>
                  <FormCard title="Centres d'examen" icon="pi-building">
                    <FormRow>
                      <FormCol size={6}>
                        <InputField
                          label="Etablissement"
                          field="etablissement"
                          type="dropdown"
                          required
                          options={etablissementOptions}
                          value={formData.etablissement}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                      <FormCol size={6}>
                        <InputField
                          label="Centre EPS"
                          field="centreActEPS"
                          type="dropdown"
                          required
                          options={etablissementOptions}
                          value={formData.centreActEPS}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                      <FormCol size={12}>
                        <InputField
                          label="Centre écrit"
                          field="centreEcrit"
                          type="dropdown"
                          required
                          options={etablissementOptions}
                          value={formData.centreEcrit}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                    </FormRow>
                  </FormCard>

                  <FormCard title="Matières optionnelles" icon="pi-book">
                    <FormRow>
                      <FormCol size={4}>
                        <InputField
                          label="Matière optionnelle 1"
                          field="mo1"
                          type="dropdown"
                          required
                          options={mo1OptionsMemo}
                          placeholder="Sélectionner une matière"
                          value={formData.mo1}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                      <FormCol size={4}>
                        <InputField
                          label="Matière optionnelle 2"
                          field="mo2"
                          type="dropdown"
                          required
                          options={mo2OptionsMemo}
                          placeholder="Sélectionner une matière"
                          value={formData.mo2}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                      <FormCol size={4}>
                        <InputField
                          label="Matière optionnelle 3"
                          field="mo3"
                          type="dropdown"
                          required
                          options={mo3OptionsMemo}
                          placeholder="Sélectionner une matière"
                          value={formData.mo3}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                    </FormRow>
                  </FormCard>

                  <FormCard title="Matières facultatives" icon="pi-flag">
                    <FormRow>
                      <FormCol size={6}>
                        <InputField
                          label="EF1"
                          field="ef1"
                          type="dropdown"
                          options={ef1OptionsMemo}
                          placeholder="Sélectionner une matière"
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
                          placeholder="Sélectionner une matière"
                          value={formData.ef2}
                          onValueChange={handleInputChange}
                        />
                      </FormCol>
                    </FormRow>
                  </FormCard>
                </>
              )}
            </div>
          </div>
        </Dialog>

        <style jsx global>{`
          .premium-candidat-dialog .p-dialog-header {
            padding: 0;
          }
          .premium-candidat-dialog .p-dialog-content {
            padding: 0;
          }

          .premium-header {
            position: relative;
            overflow: hidden;
            border-radius: 12px 12px 0 0;
            color: #000;
          }
          .premium-header-bg {
            position: absolute;
            inset: 0;
          }
          .premium-header-content {
            position: relative;
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 1.5rem;
            z-index: 1;
            color: #000;
          }
          .premium-header-icon {
            width: 3rem;
            height: 3rem;
            background: #000;
            border-radius: 1rem;
            display: flex;
            align-items: center;
            justify-content: center;
            backdrop-filter: blur(10px);
          }
          .premium-header-text {
            flex: 1;
            color: #000;
          }
          .premium-header-title {
            font-size: 1.25rem;
            font-weight: 700;
            color: white;
            margin: 0;
            color: #000;
          }
          .premium-header-subtitle {
            font-size: 0.75rem;
            color: #000;
            margin: 0.25rem 0 0 0;
          }
          .premium-header-badge .p-badge {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            padding: 0.25rem 0.75rem;
          }

          .premium-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 1rem;
            padding: 1rem 1.5rem;
            background: #f8fafc;
            border-top: 1px solid #e2e8f0;
            border-radius: 0 0 12px 12px;
          }

          .footer-left {
            display: flex;
            gap: 0.5rem;
          }

          .footer-right {
            display: flex;
            gap: 1rem;
          }

          .premium-btn-primary {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            border: none;
            padding: 0.625rem 1.5rem;
            font-weight: 600;
            border-radius: 10px;
            transition: all 0.2s;
          }
          .premium-btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
          }

          .premium-dialog-body {
            display: flex;
            flex-direction: column;
            min-height: 600px;
            max-height: 75vh;
            background: #ffffff;
          }

          .top-tabs-container {
            width: 100%;
            background: #ffffff;
            border-bottom: 2px solid #f1f5f9;
          }

          .top-tabs {
            display: flex;
            width: 100%;
            gap: 0;
          }

          .top-tab {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.75rem;
            padding: 1rem 1.5rem;
            border: none;
            border-bottom: 3px solid transparent;
            background: transparent;
            cursor: pointer;
            transition: all 0.2s ease;
            text-align: left;
            font-size: 0.875rem;
            font-weight: 500;
          }

          .top-tab:hover {
            background: #f8fafc;
          }

          .top-tab.active {
            background: #ffffff;
            border-bottom-width: 3px;
            border-bottom-style: solid;
          }

          .top-tab-icon {
            font-size: 1.125rem;
            transition: transform 0.2s;
          }

          .top-tab:hover .top-tab-icon {
            transform: scale(1.1);
          }

          .top-tab-text {
            display: flex;
            flex-direction: column;
          }

          .top-tab-label {
            font-size: 0.875rem;
            font-weight: 600;
          }

          .form-scroll-area {
            flex: 1;
            padding: 1.5rem;
            overflow-y: auto;
            background: #ffffff;
          }

          .form-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
            margin-bottom: 1.5rem;
            overflow: hidden;
          }
          .form-card-header {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            padding: 0.875rem 1.25rem;
            background: #fafbfc;
            border-bottom: 1px solid #f1f5f9;
          }
          .form-card-icon {
            width: 2rem;
            height: 2rem;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 10px;
            font-size: 1rem;
          }
          .form-card-title {
            font-size: 1rem;
            font-weight: 600;
            color: #1e293b;
            margin: 0;
          }
          .form-card-body {
            padding: 1.25rem;
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
          .col-12 {
            width: 100%;
          }

          .input-wrapper {
            margin-bottom: 0;
          }
          .input-label {
            display: block;
            font-size: 0.7rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #0a0a0a;
            margin-bottom: 0.375rem;
          }
          .required-star {
            color: #ef4444;
            margin-left: 0.25rem;
          }

          .p-dropdown .p-dropdown-label,
          .p-inputnumber input,
          .p-inputnumber .p-inputnumber-input {
            border: none !important;
            box-shadow: none !important;
            background: transparent !important;
            display: flex !important;
            align-items: center !important;
            line-height: 1.2 !important;
            padding: 0.5rem 0.75rem !important;
          }

          .p-dropdown .p-dropdown-label.p-placeholder {
            display: flex !important;
            align-items: center !important;
          }

          .p-dropdown,
          .p-inputnumber {
            border: 1.5px solid #e2e8f0 !important;
            border-radius: 8px;
            display: flex;
            align-items: center;
            height: 40px;
          }

          .p-dropdown .p-dropdown-trigger,
          .p-inputnumber .p-inputnumber-button {
            border: none !important;
            background: transparent !important;
          }

          .p-dropdown {
            align-items: center !important;
          }

          .p-inputtext {
            display: flex !important;
            align-items: center !important;
          }

          .p-inputnumber {
            align-items: center !important;
          }

          .p-inputnumber .p-inputnumber-input {
            display: flex !important;
            align-items: center !important;
          }

          .p-inputtext,
          .p-dropdown,
          .p-inputnumber {
            width: 100%;
            height: 40px !important;
            min-height: 40px !important;
            padding: 0.5rem 0.75rem !important;
            font-size: 0.875rem;
            border: 1.5px solid #e2e8f0;
            border-radius: 8px;
            transition: all 0.2s;
            background: white;
          }

          .p-dropdown .p-dropdown-trigger {
            width: 2.5rem;
            height: 37px !important;
            border: none;
          }

          .p-inputnumber .p-inputnumber-button {
            height: 37px !important;
            border: none;
          }

          .p-inputnumber .p-inputnumber-button-group {
            height: 37px !important;
          }

          .p-dropdown {
            display: flex;
            align-items: center;
          }

          .p-inputtext:focus,
          .p-dropdown.p-focus,
          .p-inputnumber:focus-within {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }

          .p-inputtext:hover,
          .p-dropdown:hover,
          .p-inputnumber:hover {
            border-color: #cbd5e1;
          }

          .p-dropdown-panel {
            border-radius: 10px;
            margin-top: 0.25rem;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
          }

          .loading-container {
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 3rem;
            min-height: 400px;
          }

          /* Styles pour ElegantDatePicker */
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
            height: 40px;
            padding: 0.5rem 2.5rem 0.5rem 0.75rem;
            font-size: 0.875rem;
            border: 1.5px solid #e2e8f0;
            border-radius: 8px;
            transition: all 0.2s;
            background: white;
            font-family: inherit;
          }

          .date-input:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }

          .date-icon {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            color: #94a3b8;
            cursor: pointer;
            transition: color 0.2s;
            font-size: 1rem;
          }

          .date-icon:hover {
            color: #3b82f6;
          }

          .date-panel {
            position: absolute;
            top: calc(100% + 8px);
            left: 0;
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1),
              0 8px 10px -6px rgba(0, 0, 0, 0.02);
            border: 1px solid #e2e8f0;
            z-index: 1000;
            animation: slideDown 0.2s ease;
            overflow: hidden;
          }

          @keyframes slideDown {
            from {
              opacity: 0;
              transform: translateY(-10px);
            }
            to {
              opacity: 1;
              transform: translateY(0);
            }
          }

          .calendar-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0.75rem 1rem;
            background: linear-gradient(135deg, #f8fafc, #ffffff);
            border-bottom: 1px solid #e2e8f0;
          }

          .month-year {
            font-weight: 600;
            font-size: 0.875rem;
            color: #1e293b;
          }

          .nav-button {
            background: transparent;
            border: none;
            cursor: pointer;
            padding: 0.25rem 0.5rem;
            border-radius: 6px;
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
            padding: 0.75rem;
            gap: 4px;
            background: white;
          }

          .calendar-day {
            text-align: center;
            padding: 0.5rem;
            font-size: 0.875rem;
            cursor: pointer;
            border-radius: 8px;
            transition: all 0.2s;
            color: #334155;
            font-weight: 500;
          }

          .calendar-day:hover:not(.empty) {
            background: #f1f5f9;
            transform: scale(1.05);
          }

          .calendar-day.selected {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: white;
            font-weight: 600;
            transform: scale(1);
          }

          .calendar-day.today {
            background: #e0f2fe;
            color: #0369a1;
            font-weight: 600;
            position: relative;
          }

          .calendar-day.today::after {
            content: "";
            position: absolute;
            bottom: 2px;
            left: 50%;
            transform: translateX(-50%);
            width: 4px;
            height: 4px;
            background: #0369a1;
            border-radius: 50%;
          }

          .calendar-day.empty {
            cursor: default;
            background: transparent;
          }

          .calendar-footer {
            display: flex;
            justify-content: space-between;
            padding: 0.75rem 1rem;
            border-top: 1px solid #e2e8f0;
            background: #f8fafc;
          }

          .clear-button,
          .today-button {
            padding: 0.375rem 0.75rem;
            font-size: 0.75rem;
            border-radius: 6px;
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

          @media (max-width: 1024px) {
            .col-2,
            .col-3,
            .col-4,
            .col-6 {
              width: 50%;
            }
          }

          .p-inputtext,
          .p-dropdown .p-dropdown-label,
          .p-inputnumber input,
          .p-inputnumber .p-inputnumber-input,
          .date-input,
          input,
          textarea {
            color: #1e293b !important;
          }

          /* Pour le placeholder */
          .p-inputtext::placeholder,
          .p-dropdown .p-dropdown-label.p-placeholder,
          .date-input::placeholder,
          input::placeholder {
            color: #94a3b8 !important;
          }

          /* Pour les dropdowns quand une valeur est sélectionnée */
          .p-dropdown .p-dropdown-label {
            color: #1e293b !important;
          }

          /* Pour le composant DatePicker */
          .date-input {
            color: #1e293b !important;
          }

          /* Pour les labels */
          .input-label {
            color: #475569 !important;
          }

          /* Pour le texte dans les champs désactivés */
          .p-inputtext:disabled,
          .p-dropdown:disabled .p-dropdown-label,
          .date-input:disabled {
            color: #94a3b8 !important;
          }

          @media (max-width: 768px) {
            .top-tabs {
              flex-direction: column;
            }

            .top-tab {
              justify-content: flex-start;
              padding: 0.75rem 1rem;
            }

            .top-tab-text .top-tab-desc {
              display: none;
            }

            .col-2,
            .col-3,
            .col-4,
            .col-6,
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

          @keyframes slideIn {
            from {
              opacity: 0;
              transform: translateX(20px);
            }
            to {
              opacity: 1;
              transform: translateX(0);
            }
          }
          .form-card {
            animation: slideIn 0.3s ease-out;
          }
        `}</style>
      </>
    );
  },
);

CandidatDialog.displayName = "CandidatDialog";
export default CandidatDialog;
