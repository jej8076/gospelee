"use client";

import React, { useEffect, useState } from "react";
import { fetchStorageStatus } from "~/lib/api/fetch-gallery";
import { StorageStatus } from "@/types/gallery";
import { CircleStackIcon } from "@heroicons/react/24/outline";

export default function StorageUsageWidget() {
  const [status, setStatus] = useState<StorageStatus | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const loadStorage = async () => {
    try {
      const data = await fetchStorageStatus();
      setStatus(data);
    } catch (err) {
      // ignore silently if not logged in or no ecclesia
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadStorage();
  }, []);

  if (isLoading || !status) {
    return null;
  }

  const percent = Math.min(100, Math.max(0, status.usedPercentage || 0));
  let barColor = "bg-indigo-600";
  if (percent >= 90) {
    barColor = "bg-rose-500";
  } else if (percent >= 75) {
    barColor = "bg-amber-500";
  }

  return (
    <div className="rounded-xl border border-gray-200 bg-gray-50/80 p-3.5 shadow-xs">
      <div className="flex items-center justify-between text-xs font-semibold text-gray-700">
        <div className="flex items-center gap-1.5">
          <CircleStackIcon className="h-4 w-4 text-indigo-600" />
          <span>저장 공간</span>
        </div>
        <span className="text-gray-500 font-mono">{percent}%</span>
      </div>

      <div className="mt-2.5 h-2 w-full overflow-hidden rounded-full bg-gray-200">
        <div
          className={`h-full transition-all duration-500 rounded-full ${barColor}`}
          style={{ width: `${percent}%` }}
        />
      </div>

      <div className="mt-2 flex items-center justify-between text-[11px] text-gray-500 font-mono">
        <span>{status.formattedUsed || "0 B"}</span>
        <span>/ {status.formattedLimit || "10 GB"}</span>
      </div>
    </div>
  );
}
