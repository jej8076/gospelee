"use client"

import React, {Suspense, useCallback, useEffect, useState, type JSX} from "react";
import {useRouter, useSearchParams} from "next/navigation";
import useAuth from "~/lib/auth/check-auth";
import {
  fetchPrayerCardAction,
  fetchPrayerPending,
  fetchPrayerReports,
  fetchPrayerSetting,
  fetchPrayerStats,
  fetchSavePrayerSetting,
  fetchSavePrayerTreeEvent,
  type PrayerCardAction,
} from "~/lib/api/fetch-prayer";

type Tab = "setting" | "reports" | "pending" | "stats";

const TABS: { id: Tab; name: string }[] = [
  {id: "setting", name: "설정"},
  {id: "reports", name: "신고"},
  {id: "pending", name: "승인 대기"},
  {id: "stats", name: "통계"},
];

const STATUS_LABELS: Record<string, string> = {
  PENDING: "승인 대기", ACTIVE: "노출 중", ANSWERED: "응답됨", HIDDEN: "숨김",
};

const FIELD_LABELS: Record<string, string> = {
  enabled: "기능 사용", authorMode: "작성 방식", maxLength: "최대 글자 수",
  allowedExpiryDays: "노출 기간", whoCanWrite: "작성 대상", scopeMode: "공개 범위",
  responderVisibility: "기도한 사람 공개", adminSeesAnon: "익명 작성자 확인",
  moderationMode: "검수 방식", autoHideThreshold: "자동 숨김 신고 수",
  cheerEnabled: "응원 문구", dailyDrawLimit: "하루 뽑기 횟수", summaryPush: "요약 알림",
};

const inputClass = "block w-full rounded-md bg-white px-3 py-1.5 text-sm text-gray-900 ring-1 ring-gray-300 focus:ring-2 focus:ring-indigo-600 focus:outline-none";

function Choice<T extends string>({label, hint, value, options, onChange}: {
  label: string;
  hint?: string;
  value: T;
  options: { value: T; label: string; desc?: string }[];
  onChange: (value: T) => void;
}): JSX.Element {
  return (
      <fieldset className="py-5">
        <legend className="text-sm font-semibold text-gray-900">{label}</legend>
        {hint && <p className="mt-1 text-xs text-gray-500">{hint}</p>}
        <div className="mt-3 space-y-2">
          {options.map(option => (
              <label key={option.value} className="flex cursor-pointer items-start gap-3 text-sm">
                <input type="radio" className="mt-1" checked={value === option.value}
                       onChange={() => onChange(option.value)}/>
                <span>
                  <span className="text-gray-900">{option.label}</span>
                  {option.desc && <span className="block text-xs text-gray-500">{option.desc}</span>}
                </span>
              </label>
          ))}
        </div>
      </fieldset>
  );
}

function Toggle({label, hint, checked, onChange}: {
  label: string; hint?: string; checked: boolean; onChange: (value: boolean) => void;
}): JSX.Element {
  return (
      <label className="flex cursor-pointer items-start justify-between gap-4 py-5">
        <span>
          <span className="block text-sm font-semibold text-gray-900">{label}</span>
          {hint && <span className="mt-1 block text-xs text-gray-500">{hint}</span>}
        </span>
        <input type="checkbox" className="mt-1 size-4" checked={checked}
               onChange={e => onChange(e.target.checked)}/>
      </label>
  );
}

function NumberField({label, hint, value, min, max, onChange}: {
  label: string; hint?: string; value: number; min: number; max: number; onChange: (value: number) => void;
}): JSX.Element {
  return (
      <div className="py-5">
        <label className="block text-sm font-semibold text-gray-900">{label}</label>
        {hint && <p className="mt-1 text-xs text-gray-500">{hint}</p>}
        <input type="number" className={`mt-3 max-w-40 ${inputClass}`} value={value} min={min} max={max}
               onChange={e => onChange(Number(e.target.value))}/>
      </div>
  );
}

const toInput = (value?: string | null) => (value ? value.slice(0, 16) : "");
const toServer = (value: string) => (value ? `${value}:00` : null);

function describeChanges(raw: string): string[] {
  try {
    const parsed = JSON.parse(raw) as Record<string, { from: unknown; to: unknown }>;
    return Object.entries(parsed).map(([key, change]) =>
        `${FIELD_LABELS[key] ?? key}: ${String(change.from)} → ${String(change.to)}`);
  } catch {
    return [raw];
  }
}

function SettingPanel(): JSX.Element {
  const [data, setData] = useState<PrayerAdminSetting | null>(null);
  const [setting, setSetting] = useState<PrayerSetting | null>(null);
  const [tree, setTree] = useState<PrayerTreeEvent | null>(null);
  const [expiryText, setExpiryText] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  const apply = (next: PrayerAdminSetting) => {
    setData(next);
    setSetting(next.setting);
    setTree(next.treeEvent);
    setExpiryText(next.setting.allowedExpiryDays.join(","));
  };

  useEffect(() => {
    fetchPrayerSetting().then(apply).catch(e => setMessage(e?.message ?? "설정을 불러오지 못했습니다."));
  }, []);

  if (!setting || !tree || !data) {
    return <div className="py-16 text-center text-sm text-gray-500">{message ?? "로딩 중..."}</div>;
  }

  const patch = (partial: Partial<PrayerSetting>) => setSetting({...setting, ...partial});

  const saveSetting = async () => {
    const days = expiryText.split(",").map(v => v.trim()).filter(Boolean).map(Number);
    if (days.length === 0 || days.some(d => !Number.isInteger(d) || d < 0)) {
      setMessage("노출 기간은 일 수를 쉼표로 구분해 적어 주세요. 예: 7,30 (0은 상시)");
      return;
    }
    setSaving(true);
    try {
      apply(await fetchSavePrayerSetting({...setting, allowedExpiryDays: days}));
      setMessage("설정을 저장했습니다.");
    } catch (e: any) {
      setMessage(e?.message ?? "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  };

  const saveTree = async () => {
    setSaving(true);
    try {
      apply(await fetchSavePrayerTreeEvent({
        ...tree, startAt: toServer(toInput(tree.startAt)), endAt: toServer(toInput(tree.endAt)),
      }));
      setMessage("트리 이벤트를 저장했습니다.");
    } catch (e: any) {
      setMessage(e?.message ?? "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  };

  return (
      <div className="mt-6 space-y-8">
        {message && <p className="rounded-md bg-indigo-50 px-4 py-3 text-sm text-indigo-800">{message}</p>}

        <section className="rounded-lg bg-white px-5 shadow-sm ring-1 ring-gray-200">
          <div className="divide-y divide-gray-100">
            <Toggle label="기도 쪽지 사용"
                    hint="켜면 우리 교회 성도에게 기도 쪽지가 공개됩니다. 꺼 두면 성도 화면에 나타나지 않습니다."
                    checked={setting.enabled} onChange={v => patch({enabled: v})}/>
            <Choice label="작성 방식" value={setting.authorMode} onChange={v => patch({authorMode: v})}
                    options={[
                      {value: "AUTHOR_CHOICE", label: "작성자가 선택", desc: "익명 또는 이름 공개를 쓸 때마다 고릅니다."},
                      {value: "ANON_ONLY", label: "익명만"},
                      {value: "NAME_ONLY", label: "이름 공개만"},
                    ]}/>
            <Choice label="기도한 사람을 작성자에게 알려 주나요?" value={setting.responderVisibility}
                    hint="이미 올라온 쪽지에는 올릴 때의 설정이 그대로 적용됩니다."
                    onChange={v => patch({responderVisibility: v})}
                    options={[
                      {value: "HIDDEN", label: "알리지 않음", desc: "기도해 준 인원 수만 보입니다."},
                      {value: "RESPONDER_CHOICE", label: "기도한 사람이 선택", desc: "이름을 알릴지 기도하는 사람이 정합니다."},
                      {value: "NAMED", label: "항상 이름 공개"},
                    ]}/>
            <Choice label="익명 작성자를 관리자가 볼 수 있나요?" value={setting.adminSeesAnon}
                    hint="이미 올라온 쪽지에는 올릴 때의 설정이 그대로 적용됩니다."
                    onChange={v => patch({adminSeesAnon: v})}
                    options={[
                      {value: "NEVER", label: "볼 수 없음"},
                      {value: "REPORTED_ONLY", label: "신고된 쪽지만"},
                      {value: "ALWAYS", label: "항상"},
                    ]}/>
            <Choice label="작성할 수 있는 성도" value={setting.whoCanWrite} onChange={v => patch({whoCanWrite: v})}
                    options={[
                      {value: "APPROVED_ONLY", label: "교회 가입이 승인된 성도만"},
                      {value: "ALL_MEMBERS", label: "가입 요청 중인 성도 포함"},
                    ]}/>
            <Choice label="공개 범위" value={setting.scopeMode} onChange={v => patch({scopeMode: v})}
                    options={[
                      {value: "CHURCH_ONLY", label: "교회 전체"},
                      {value: "GROUP_ALLOWED", label: "푸시 그룹 단위 공개도 허용"},
                    ]}/>
            <Choice label="검수 방식" value={setting.moderationMode} onChange={v => patch({moderationMode: v})}
                    options={[
                      {value: "POST_REPORT", label: "올린 뒤 신고로 관리", desc: "신고가 기준 수에 이르면 자동으로 숨깁니다."},
                      {value: "PRE_APPROVAL", label: "관리자 승인 후 노출"},
                    ]}/>
            <NumberField label="자동 숨김 신고 수" hint="1~10" min={1} max={10}
                         value={setting.autoHideThreshold} onChange={v => patch({autoHideThreshold: v})}/>
            <NumberField label="최대 글자 수" hint="50~300" min={50} max={300}
                         value={setting.maxLength} onChange={v => patch({maxLength: v})}/>
            <div className="py-5">
              <label className="block text-sm font-semibold text-gray-900">노출 기간 선택지 (일)</label>
              <p className="mt-1 text-xs text-gray-500">쉼표로 구분합니다. 0은 상시 노출입니다. 예: 7,30,0</p>
              <input className={`mt-3 max-w-xs ${inputClass}`} value={expiryText}
                     onChange={e => setExpiryText(e.target.value)}/>
            </div>
            <NumberField label="하루 뽑기 횟수" hint="0은 제한 없음" min={0} max={50}
                         value={setting.dailyDrawLimit} onChange={v => patch({dailyDrawLimit: v})}/>
            <Toggle label="응원 문구 사용" hint="기도한 뒤 정해진 응원 문구를 남길 수 있습니다."
                    checked={setting.cheerEnabled} onChange={v => patch({cheerEnabled: v})}/>
            <Toggle label="요약 알림" hint="전날 기도를 받은 작성자에게 하루 한 번 알립니다."
                    checked={setting.summaryPush} onChange={v => patch({summaryPush: v})}/>
          </div>
          <div className="flex justify-end py-4">
            <button onClick={saveSetting} disabled={saving}
                    className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-50">
              설정 저장
            </button>
          </div>
        </section>

        <section className="rounded-lg bg-white p-5 shadow-sm ring-1 ring-gray-200">
          <h2 className="text-sm font-semibold text-gray-900">크리스마스 트리 이벤트</h2>
          <p className="mt-1 text-xs text-gray-500">
            기간 동안 교회 화면에 큰 트리가 나타나고, 쪽지가 장식으로 걸립니다. 쪽지를 사용하는 중일 때만 보입니다.
          </p>
          <div className="mt-4 space-y-4">
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={tree.enabled} onChange={e => setTree({...tree, enabled: e.target.checked})}/>
              이벤트 사용
            </label>
            <div className="grid gap-4 sm:grid-cols-2">
              <label className="text-sm text-gray-700">시작
                <input type="datetime-local" className={`mt-1 ${inputClass}`} value={toInput(tree.startAt)}
                       onChange={e => setTree({...tree, startAt: e.target.value})}/>
              </label>
              <label className="text-sm text-gray-700">종료
                <input type="datetime-local" className={`mt-1 ${inputClass}`} value={toInput(tree.endAt)}
                       onChange={e => setTree({...tree, endAt: e.target.value})}/>
              </label>
            </div>
            <label className="block text-sm text-gray-700">점등률을 보여 주는 최소 쪽지 수
              <input type="number" min={1} className={`mt-1 max-w-40 ${inputClass}`} value={tree.minCards}
                     onChange={e => setTree({...tree, minCards: Number(e.target.value)})}/>
              <span className="mt-1 block text-xs text-gray-500">쪽지가 이보다 적으면 점등률은 숨깁니다.</span>
            </label>
          </div>
          <div className="mt-4 flex justify-end">
            <button onClick={saveTree} disabled={saving}
                    className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-semibold text-white hover:bg-indigo-500 disabled:opacity-50">
              이벤트 저장
            </button>
          </div>
        </section>

        <section>
          <h2 className="text-sm font-semibold text-gray-900">설정 변경 기록</h2>
          {data.history.length === 0 ? (
              <p className="mt-2 text-sm text-gray-500">변경 기록이 없습니다.</p>
          ) : (
              <ul className="mt-3 space-y-3">
                {data.history.map((item, index) => (
                    <li key={index} className="rounded-md bg-white p-3 text-sm ring-1 ring-gray-200">
                      <p className="text-xs text-gray-500">
                        {new Date(item.changedAt).toLocaleString("ko-KR")} · {item.changedByName ?? "알 수 없음"}
                      </p>
                      <ul className="mt-1 list-disc pl-5 text-gray-800">
                        {describeChanges(item.changes).map(line => <li key={line}>{line}</li>)}
                      </ul>
                    </li>
                ))}
              </ul>
          )}
        </section>
      </div>
  );
}

function CardList({kind}: { kind: "reports" | "pending" }): JSX.Element {
  const [cards, setCards] = useState<PrayerAdminCard[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      setCards(await (kind === "reports" ? fetchPrayerReports() : fetchPrayerPending()));
      setError(null);
    } catch (e: any) {
      setError(e?.message ?? "목록을 불러오지 못했습니다.");
    }
  }, [kind]);

  useEffect(() => {
    setCards(null);
    load();
  }, [load]);

  const act = async (card: PrayerAdminCard, action: PrayerCardAction, confirmText: string) => {
    if (!window.confirm(confirmText)) {
      return;
    }
    try {
      await fetchPrayerCardAction(card.id, action);
      await load();
    } catch (e: any) {
      window.alert(e?.message ?? "처리에 실패했습니다.");
    }
  };

  if (error) {
    return <div className="py-16 text-center text-sm text-red-600">{error}</div>;
  }
  if (!cards) {
    return <div className="py-16 text-center text-sm text-gray-500">로딩 중...</div>;
  }
  if (cards.length === 0) {
    return <div className="py-16 text-center text-sm text-gray-500">
      {kind === "reports" ? "신고된 쪽지가 없습니다." : "승인을 기다리는 쪽지가 없습니다."}
    </div>;
  }

  return (
      <ul className="mt-6 space-y-4">
        {cards.map(card => (
            <li key={card.id} className="rounded-lg bg-white p-5 shadow-sm ring-1 ring-gray-200">
              <div className="flex flex-wrap items-center gap-2 text-xs text-gray-500">
                <span className="rounded-full bg-gray-100 px-2 py-0.5 font-medium text-gray-700">
                  {STATUS_LABELS[card.status] ?? card.status}
                </span>
                <span>{card.displayMode === "ANON" ? "익명" : "이름 공개"}</span>
                {card.reportCount > 0 && (
                    <span className="rounded-full bg-red-50 px-2 py-0.5 font-medium text-red-700">
                      신고 {card.reportCount}건
                    </span>
                )}
                <span>{new Date(card.createdAt).toLocaleString("ko-KR")}</span>
              </div>
              <p className="mt-3 whitespace-pre-wrap rounded-md bg-gray-50 p-3 text-sm text-gray-800">{card.content}</p>
              {card.authorName && <p className="mt-3 text-xs text-gray-600">작성자 <b>{card.authorName}</b></p>}
              {card.reasons && card.reasons.length > 0 && (
                  <p className="mt-2 text-xs text-gray-500">신고 사유: {card.reasons.join(", ")}</p>
              )}
              <div className="mt-4 flex gap-2">
                {kind === "pending" ? (
                    <>
                      <button onClick={() => act(card, "approve", "이 쪽지를 승인해 노출할까요?")}
                              className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-indigo-500">승인</button>
                      <button onClick={() => act(card, "reject", "이 쪽지를 반려할까요?")}
                              className="rounded-md bg-white px-3 py-1.5 text-sm font-semibold text-gray-700 ring-1 ring-gray-300 hover:bg-gray-50">반려</button>
                    </>
                ) : card.status === "HIDDEN" ? (
                    <button onClick={() => act(card, "restore", "숨김을 풀고 다시 노출할까요? 신고는 문제 없음으로 처리됩니다.")}
                            className="rounded-md bg-white px-3 py-1.5 text-sm font-semibold text-gray-700 ring-1 ring-gray-300 hover:bg-gray-50">복원</button>
                ) : (
                    <button onClick={() => act(card, "hide", "이 쪽지를 숨길까요?")}
                            className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-red-500">숨기기</button>
                )}
              </div>
            </li>
        ))}
      </ul>
  );
}

function StatsPanel(): JSX.Element {
  const [stats, setStats] = useState<PrayerAdminStats | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchPrayerStats().then(setStats).catch(e => setError(e?.message ?? "통계를 불러오지 못했습니다."));
  }, []);

  if (error) {
    return <div className="py-16 text-center text-sm text-red-600">{error}</div>;
  }
  if (!stats) {
    return <div className="py-16 text-center text-sm text-gray-500">로딩 중...</div>;
  }

  const items: [string, number][] = [
    ["전체 쪽지", stats.totalCards], ["노출 중", stats.activeCards],
    ["응답됨", stats.answeredCards], ["숨김", stats.hiddenCards],
    ["승인 대기", stats.pendingCards], ["아직 기도받지 못한 쪽지", stats.openWithoutResponse],
    ["기도한 횟수", stats.totalResponses], ["신고 처리 대기", stats.pendingReportCards],
  ];

  return (
      <div className="mt-6">
        <dl className="grid grid-cols-2 gap-4 sm:grid-cols-4">
          {items.map(([label, value]) => (
              <div key={label} className="rounded-lg bg-white p-4 shadow-sm ring-1 ring-gray-200">
                <dt className="text-xs text-gray-500">{label}</dt>
                <dd className="mt-1 text-2xl font-semibold text-gray-900">{value}</dd>
              </div>
          ))}
        </dl>
        <p className="mt-4 text-xs text-gray-500">집계만 보여 주며 개인을 식별하는 정보는 포함하지 않습니다.</p>
      </div>
  );
}

function PrayerHub(): JSX.Element {
  useAuth();

  const router = useRouter();
  const searchParams = useSearchParams();
  const requested = searchParams.get("tab");
  const tab: Tab = TABS.some(t => t.id === requested) ? (requested as Tab) : "setting";

  return (
      <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
        <h1 className="text-base font-semibold text-gray-900">기도 쪽지</h1>
        <p className="mt-2 text-sm text-gray-700">
          성도가 기도 제목을 쪽지로 올리고, 다른 성도가 뽑아서 기도해 주는 기능을 설정하고 관리합니다.
        </p>

        <div className="mt-6 flex gap-2 overflow-x-auto border-b border-gray-200 pb-3">
          {TABS.map(t => (
              <button key={t.id}
                      onClick={() => router.replace(t.id === "setting" ? "/manage/prayer" : `/manage/prayer?tab=${t.id}`)}
                      className={`shrink-0 rounded-md px-3 py-1.5 text-sm font-medium ${
                          tab === t.id ? "bg-indigo-600 text-white"
                              : "bg-white text-gray-700 ring-1 ring-gray-300 hover:bg-gray-50"}`}>
                {t.name}
              </button>
          ))}
        </div>

        {tab === "setting" && <SettingPanel/>}
        {tab === "reports" && <CardList kind="reports"/>}
        {tab === "pending" && <CardList kind="pending"/>}
        {tab === "stats" && <StatsPanel/>}
      </div>
  );
}

export default function ManagePrayer(): JSX.Element {
  return (
      <Suspense fallback={<div className="py-16 text-center text-sm text-gray-500">로딩 중...</div>}>
        <PrayerHub/>
      </Suspense>
  );
}
