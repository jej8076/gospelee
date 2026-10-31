"use client"

import {useEffect, useMemo, useRef, useState} from "react";
import {CalendarDaysIcon, ChevronLeftIcon, ChevronRightIcon} from "@heroicons/react/24/outline";

const WEEKDAYS = ["일", "월", "화", "수", "목", "금", "토"];
const YEAR_PAGE = 12;

const pad = (n: number) => String(n).padStart(2, "0");

/** Date 를 로컬 기준 yyyy-MM-dd 문자열로 바꾼다 */
export const toDateString = (d: Date): string => `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

/** yyyy-MM-dd 문자열을 로컬 Date 로 바꾼다. 형식이 틀리면 null */
const parseDateString = (value: string): Date | null => {
  const m = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!m) {
    return null;
  }
  const d = new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]));
  return d.getMonth() === Number(m[2]) - 1 ? d : null;
};

const formatKorean = (d: Date): string => `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 (${WEEKDAYS[d.getDay()]})`;

interface DatePickerProps {
  /** yyyy-MM-dd */
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

type View = "day" | "month";

export default function DatePicker({value, onChange, placeholder = "날짜를 선택하세요"}: DatePickerProps) {
  const selected = useMemo(() => parseDateString(value), [value]);
  const today = useMemo(() => new Date(), []);
  const [open, setOpen] = useState(false);
  const [view, setView] = useState<View>("day");
  // 달력에 보이는 달(그 달의 1일)
  const [cursor, setCursor] = useState(() => {
    const base = parseDateString(value) ?? new Date();
    return new Date(base.getFullYear(), base.getMonth(), 1);
  });
  // 월 선택 화면에서 보이는 연도 묶음의 시작 연도
  const [yearStart, setYearStart] = useState(() => cursor.getFullYear() - 4);
  const rootRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) {
      return;
    }
    const onPointerDown = (e: MouseEvent | TouchEvent) => {
      if (rootRef.current && !rootRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", onPointerDown);
    document.addEventListener("touchstart", onPointerDown);
    document.addEventListener("keydown", onKeyDown);
    return () => {
      document.removeEventListener("mousedown", onPointerDown);
      document.removeEventListener("touchstart", onPointerDown);
      document.removeEventListener("keydown", onKeyDown);
    };
  }, [open]);

  const openPicker = () => {
    const base = selected ?? new Date();
    const month = new Date(base.getFullYear(), base.getMonth(), 1);
    setCursor(month);
    setYearStart(month.getFullYear() - 4);
    setView("day");
    setOpen((prev) => !prev);
  };

  const moveMonth = (delta: number) => setCursor((c) => new Date(c.getFullYear(), c.getMonth() + delta, 1));

  const pick = (d: Date) => {
    onChange(toDateString(d));
    setOpen(false);
  };

  // 앞쪽 빈칸을 포함해 6주(42칸) 고정으로 그려 달이 바뀌어도 높이가 흔들리지 않게 한다
  const cells = useMemo(() => {
    const first = new Date(cursor.getFullYear(), cursor.getMonth(), 1);
    return Array.from({length: 42}, (_, i) => new Date(first.getFullYear(), first.getMonth(), 1 - first.getDay() + i));
  }, [cursor]);

  const sameDay = (a: Date | null, b: Date) => !!a && a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();

  const navButton = "flex size-8 items-center justify-center rounded-full text-gray-500 hover:bg-gray-100 cursor-pointer";

  return (
      <div ref={rootRef} className="relative">
        <button
            type="button"
            onClick={openPicker}
            aria-haspopup="dialog"
            aria-expanded={open}
            className="flex w-full items-center justify-between gap-2 rounded-md bg-white px-3 py-1.5 text-left text-base text-gray-900 border border-gray-300 hover:border-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 cursor-pointer"
        >
          <span className={selected ? "" : "text-gray-400"}>{selected ? formatKorean(selected) : placeholder}</span>
          <CalendarDaysIcon aria-hidden="true" className="size-5 shrink-0 text-gray-400"/>
        </button>

        {open && (
            <div
                role="dialog"
                aria-label="날짜 선택"
                className="absolute left-0 top-full z-30 mt-2 w-72 max-w-[calc(100vw-2rem)] rounded-xl border border-gray-200 bg-white p-3 shadow-xl"
            >
              {view === "day" ? (
                  <>
                    <div className="flex items-center justify-between pb-2">
                      <button type="button" className={navButton} onClick={() => moveMonth(-1)} aria-label="이전 달">
                        <ChevronLeftIcon className="size-4"/>
                      </button>
                      <button
                          type="button"
                          onClick={() => {
                            setYearStart(cursor.getFullYear() - 4);
                            setView("month");
                          }}
                          className="rounded-md px-2 py-1 text-sm font-semibold text-gray-900 hover:bg-gray-100 cursor-pointer"
                      >
                        {cursor.getFullYear()}년 {cursor.getMonth() + 1}월
                      </button>
                      <button type="button" className={navButton} onClick={() => moveMonth(1)} aria-label="다음 달">
                        <ChevronRightIcon className="size-4"/>
                      </button>
                    </div>

                    <div className="grid grid-cols-7 pb-1 text-center text-xs font-medium">
                      {WEEKDAYS.map((w, i) => (
                          <div key={w} className={`py-1 ${i === 0 ? "text-red-500" : i === 6 ? "text-blue-500" : "text-gray-400"}`}>{w}</div>
                      ))}
                    </div>

                    <div className="grid grid-cols-7 gap-y-1">
                      {cells.map((d) => {
                        const inMonth = d.getMonth() === cursor.getMonth();
                        const isSelected = sameDay(selected, d);
                        const isToday = sameDay(today, d);
                        const color = isSelected
                            ? "bg-indigo-600 text-white hover:bg-indigo-500"
                            : !inMonth
                                ? "text-gray-300 hover:bg-gray-50"
                                : d.getDay() === 0
                                    ? "text-red-500 hover:bg-red-50"
                                    : d.getDay() === 6
                                        ? "text-blue-500 hover:bg-blue-50"
                                        : "text-gray-800 hover:bg-gray-100";
                        return (
                            <button
                                key={toDateString(d)}
                                type="button"
                                onClick={() => pick(d)}
                                aria-label={formatKorean(d)}
                                aria-pressed={isSelected}
                                className={`mx-auto flex size-9 items-center justify-center rounded-full text-sm cursor-pointer ${color} ${isToday && !isSelected ? "ring-1 ring-indigo-400" : ""}`}
                            >
                              {d.getDate()}
                            </button>
                        );
                      })}
                    </div>

                    <div className="mt-2 flex items-center justify-between border-t border-gray-100 pt-2">
                      <button
                          type="button"
                          onClick={() => pick(new Date())}
                          className="rounded-md px-2 py-1 text-sm font-medium text-indigo-600 hover:bg-indigo-50 cursor-pointer"
                      >
                        오늘
                      </button>
                      <button
                          type="button"
                          onClick={() => setOpen(false)}
                          className="rounded-md px-2 py-1 text-sm text-gray-500 hover:bg-gray-100 cursor-pointer"
                      >
                        닫기
                      </button>
                    </div>
                  </>
              ) : (
                  <>
                    <div className="flex items-center justify-between pb-2">
                      <button type="button" className={navButton} onClick={() => setYearStart((y) => y - YEAR_PAGE)} aria-label="이전 연도">
                        <ChevronLeftIcon className="size-4"/>
                      </button>
                      <span className="text-sm font-semibold text-gray-900">{yearStart}년 – {yearStart + YEAR_PAGE - 1}년</span>
                      <button type="button" className={navButton} onClick={() => setYearStart((y) => y + YEAR_PAGE)} aria-label="다음 연도">
                        <ChevronRightIcon className="size-4"/>
                      </button>
                    </div>

                    <div className="grid grid-cols-4 gap-2 pb-3">
                      {Array.from({length: YEAR_PAGE}, (_, i) => yearStart + i).map((y) => (
                          <button
                              key={y}
                              type="button"
                              onClick={() => setCursor((c) => new Date(y, c.getMonth(), 1))}
                              className={`rounded-md py-1.5 text-sm cursor-pointer ${y === cursor.getFullYear() ? "bg-indigo-600 text-white" : "text-gray-700 hover:bg-gray-100"}`}
                          >
                            {y}
                          </button>
                      ))}
                    </div>

                    <div className="grid grid-cols-4 gap-2 border-t border-gray-100 pt-3">
                      {Array.from({length: 12}, (_, m) => m).map((m) => (
                          <button
                              key={m}
                              type="button"
                              onClick={() => {
                                setCursor((c) => new Date(c.getFullYear(), m, 1));
                                setView("day");
                              }}
                              className={`rounded-md py-1.5 text-sm cursor-pointer ${m === cursor.getMonth() ? "bg-indigo-600 text-white" : "text-gray-700 hover:bg-gray-100"}`}
                          >
                            {m + 1}월
                          </button>
                      ))}
                    </div>
                  </>
              )}
            </div>
        )}
      </div>
  );
}
