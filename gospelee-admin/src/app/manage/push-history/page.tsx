"use client"

import React, {useEffect, useState, type JSX} from "react";
import Link from "next/link";
import useAuth from "~/lib/auth/check-auth";
import {fetchPushHistory, fetchPushHistoryDetail} from "~/lib/api/fetch-push-history";

const CATEGORY_LABEL: Record<string, string> = {
  DIRECT: "직접 발송",
  ANNOUNCEMENT: "공지",
  BULLETIN: "주보",
};

const STATUS_LABEL: Record<string, { text: string; className: string }> = {
  CONFIRMED: {text: "확인함", className: "bg-green-50 text-green-700"},
  RECEIVED: {text: "받음", className: "bg-blue-50 text-blue-700"},
  READY: {text: "전달됨", className: "bg-gray-100 text-gray-700"},
  SENT: {text: "전달됨", className: "bg-gray-100 text-gray-700"},
  FAILED: {text: "전달 실패", className: "bg-red-50 text-red-700"},
};

const formatTime = (value: string | null) => {
  if (!value) {
    return "-";
  }
  const d = new Date(value);
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

function Stat({label, value, hint}: { label: string; value: number; hint?: string }) {
  return (
      <div className="min-w-[4.5rem]" title={hint}>
        <div className="text-xs text-gray-500">{label}</div>
        <div className="text-base font-semibold text-gray-900">{value}명</div>
      </div>
  );
}

function PushCard({push}: { push: PushHistory }) {
  const [open, setOpen] = useState(false);
  const [detail, setDetail] = useState<PushHistoryDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  const toggle = async () => {
    const next = !open;
    setOpen(next);
    if (next && !detail) {
      try {
        setDetail(await fetchPushHistoryDetail(push.pushId));
      } catch (e: any) {
        setError(e?.message ?? "상세 정보를 불러오지 못했습니다.");
      }
    }
  };

  const percent = (n: number) => (push.targetCount === 0 ? 0 : Math.round((n / push.targetCount) * 100));

  return (
      <li className="rounded-lg border border-gray-200 bg-white">
        <button type="button" onClick={toggle} className="w-full cursor-pointer p-4 text-left">
          <div className="flex flex-wrap items-center gap-x-2 gap-y-1">
            <span className="rounded bg-rose-50 px-2 py-0.5 text-xs font-medium text-rose-700">
              {CATEGORY_LABEL[push.category] ?? push.category}
            </span>
            <span className="text-xs text-gray-500">
              {formatTime(push.sentTime)}{push.senderName ? ` · ${push.senderName}` : ""}
            </span>
          </div>
          <p className="mt-2 font-semibold text-gray-900">{push.title}</p>
          <p className="mt-1 line-clamp-2 whitespace-pre-wrap text-sm text-gray-600">{push.message}</p>

          <div className="mt-4 flex flex-wrap gap-x-6 gap-y-3">
            <Stat label="발송 대상" value={push.targetCount}/>
            <Stat label="전달됨" value={push.deliveredCount} hint="알림 서버(FCM)가 접수한 수"/>
            <Stat label="받음" value={push.receivedCount} hint="앱이 켜져 있어 받은 것이 확인된 수"/>
            <Stat label="확인함" value={push.confirmedCount} hint="알림을 눌러 이동할 화면이 열린 수 (이동할 화면이 없는 알림은 앱이 열린 수)"/>
            {push.failedCount > 0 && <Stat label="전달 실패" value={push.failedCount}/>}
          </div>

          <div className="mt-3 h-2 overflow-hidden rounded-full bg-gray-100">
            <div className="flex h-full">
              <div className="bg-green-500" style={{width: `${percent(push.confirmedCount)}%`}}/>
              <div className="bg-blue-400"
                   style={{width: `${percent(push.receivedCount - push.confirmedCount)}%`}}/>
              <div className="bg-gray-300"
                   style={{width: `${percent(push.deliveredCount - push.receivedCount)}%`}}/>
            </div>
          </div>
        </button>

        {open && (
            <div className="border-t border-gray-100 px-4 py-3">
              {error && <p className="text-sm text-red-600">{error}</p>}
              {!error && !detail && <p className="text-sm text-gray-500">불러오는 중...</p>}
              {detail && (
                  <ul className="grid grid-cols-1 gap-x-6 gap-y-1 sm:grid-cols-2">
                    {detail.receivers.map((r) => {
                      const status = STATUS_LABEL[r.status] ?? {text: r.status, className: "bg-gray-100 text-gray-700"};
                      return (
                          <li key={r.accountUid} className="flex items-center justify-between py-1 text-sm">
                            <span className="text-gray-900">{r.name}</span>
                            <span className={`rounded px-2 py-0.5 text-xs font-medium ${status.className}`}>
                              {status.text}
                            </span>
                          </li>
                      );
                    })}
                  </ul>
              )}
            </div>
        )}
      </li>
  );
}

export default function PushHistoryPage(): JSX.Element {
  useAuth();

  const [items, setItems] = useState<PushHistory[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchPushHistory().then(setItems).catch((e) => setError(e?.message ?? "발송 현황을 불러오지 못했습니다."));
  }, []);

  return (
      <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
        <div className="flex items-end justify-between gap-4">
          <div>
            <h2 className="text-base/7 font-semibold text-gray-900">푸시 발송 현황</h2>
            <p className="mt-1 text-sm/6 text-gray-600">최근 30건의 알림이 성도에게 전달되고 확인되었는지 볼 수 있습니다.</p>
          </div>
          <Link href="/manage/push-send" className="text-sm font-semibold text-indigo-600 hover:text-indigo-500">
            알림 보내기
          </Link>
        </div>

        <div className="mt-4 rounded-md bg-gray-50 p-3 text-xs/5 text-gray-600">
          <b>전달됨</b>은 알림 서버가 접수한 수, <b>받음</b>은 앱이 켜져 있을 때 받은 것이 확인된 수,
          <b> 확인함</b>은 알림을 눌러 이동할 화면(공지, 주보 등)이 열린 수이며, 이동할 화면이 없는 알림은 앱이 열린 수입니다. 앱이 꺼져 있을 때 도착한 알림은 눌러서 열기 전까지
          “전달됨”으로만 표시되므로 실제 수신 인원보다 적게 집계될 수 있습니다.
        </div>

        <div className="mt-6">
          {error && <p className="text-sm text-red-600">{error}</p>}
          {!error && items === null && <p className="text-sm text-gray-500">불러오는 중...</p>}
          {items && items.length === 0 && <p className="text-sm text-gray-500">보낸 알림이 아직 없습니다.</p>}
          {items && items.length > 0 && (
              <ul className="space-y-3">
                {items.map((push) => <PushCard key={push.pushId} push={push}/>)}
              </ul>
          )}
        </div>
      </div>
  );
}
