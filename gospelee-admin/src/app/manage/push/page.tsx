"use client"

import React, {Suspense, useState, type JSX} from "react";
import {useRouter, useSearchParams} from "next/navigation";
import useAuth from "~/lib/auth/check-auth";
import PushHistoryList from "@/components/manage/push/PushHistoryList";
import PushGroupList from "@/components/manage/push/PushGroupList";
import PushSendPanel from "@/components/manage/push/PushSendPanel";

type PushTab = "history" | "groups";

const TABS: { id: PushTab; name: string }[] = [
  {id: "history", name: "발송 현황"},
  {id: "groups", name: "푸시 그룹"},
];

function PushHub(): JSX.Element {
  useAuth();

  const router = useRouter();
  const searchParams = useSearchParams();
  const tab: PushTab = searchParams.get("tab") === "groups" ? "groups" : "history";

  // 예전 '푸시 알림 보내기' 주소(?compose=1)로 들어오면 패널을 바로 연다
  const [composeOpen, setComposeOpen] = useState(searchParams.get("compose") === "1");
  const [historyKey, setHistoryKey] = useState(0);

  const selectTab = (next: PushTab) => {
    router.replace(next === "history" ? "/manage/push" : `/manage/push?tab=${next}`);
  };

  // 알림을 보내면 방금 보낸 알림이 보이도록 발송 현황으로 옮기고 새로 불러온다
  const handleSent = () => {
    setHistoryKey((key) => key + 1);
    if (tab !== "history") {
      selectTab("history");
    }
  };

  return (
      <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-base font-semibold text-gray-900">푸시 알림</h1>
            <p className="mt-2 text-sm text-gray-700">
              성도에게 알림을 보내고, 잘 전달되고 확인되었는지 보고, 알림을 받을 그룹을 관리합니다.
            </p>
          </div>
          <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
            <button
                type="button"
                onClick={() => setComposeOpen(true)}
                className="block cursor-pointer rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            >
              새 알림 보내기
            </button>
          </div>
        </div>

        <div className="mt-6 border-b border-gray-200">
          <nav className="-mb-px flex gap-x-8" aria-label="푸시 알림 메뉴">
            {TABS.map((t) => (
                <button
                    key={t.id}
                    type="button"
                    onClick={() => selectTab(t.id)}
                    aria-current={tab === t.id ? "page" : undefined}
                    className={`cursor-pointer border-b-2 px-1 py-3 text-sm font-medium whitespace-nowrap ${
                        tab === t.id
                            ? "border-indigo-500 text-indigo-600"
                            : "border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700"
                    }`}
                >
                  {t.name}
                </button>
            ))}
          </nav>
        </div>

        <div className="mt-6">
          {tab === "history" ? <PushHistoryList refreshKey={historyKey}/> : <PushGroupList/>}
        </div>

        <PushSendPanel open={composeOpen} onClose={() => setComposeOpen(false)} onSent={handleSent}/>
      </div>
  );
}

export default function PushPage(): JSX.Element {
  // useSearchParams 는 Suspense 안에서 써야 정적 빌드가 통과한다
  return (
      <Suspense fallback={null}>
        <PushHub/>
      </Suspense>
  );
}
