import React from "react";
import { Skeleton } from "primereact/skeleton";

export const LoadingSkeleton: React.FC<{ rows?: number; columns?: number }> = ({
  rows = 5,
  columns = 6,
}) => {
  return (
    <div className="p-3">
      <Skeleton width="100%" height="3rem" className="mb-3" />
      {Array.from({ length: rows }).map((_, i) => (
        <div key={i} className="flex gap-3 mb-2">
          {Array.from({ length: columns }).map((_, j) => (
            <Skeleton key={j} width={`${100 / columns}%`} height="2.5rem" />
          ))}
        </div>
      ))}
    </div>
  );
};
