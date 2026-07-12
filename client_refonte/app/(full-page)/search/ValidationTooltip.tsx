import React from "react";

interface ValidationTooltipProps {
  message: string;
  isVisible: boolean;
  className?: string;
}

const ValidationTooltip = ({ message, isVisible, className = "" }: ValidationTooltipProps) => {
  if (!isVisible || !message) return null;

  return (
    <div className={`mt-1 ${className}`}>
      <div className="flex align-items-start gap-2 bg-white border-1 border-300 border-round-lg p-2 shadow-2 transition-all transition-duration-200">
        <div className="bg-yellow-500 border-circle flex align-items-center justify-content-center flex-shrink-0"
             style={{ width: '1.25rem', height: '1.25rem' }}>
          <i className="pi pi-info text-white text-xs"></i>
        </div>
        <p className="text-sm text-700 line-height-3 m-0">{message}</p>
      </div>
    </div>
  );
};

export default ValidationTooltip;