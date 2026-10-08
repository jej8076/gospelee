// 교회 검증 기한(신청일 + 14일). 서버(Ecclesia.VERIFICATION_DEADLINE_DAYS)와 같은 값을 사용한다.
export const VERIFICATION_DEADLINE_DAYS = 14;

export type VerificationState = {
  label: string;
  // 표시 색상 구분: done(검증 완료), waiting(기한 내 대기), expired(기한 초과)
  tone: "done" | "waiting" | "expired";
};

const DAY_MS = 24 * 60 * 60 * 1000;

export const getVerificationState = (
    verifiedYn: string | undefined,
    insertTime: string | undefined,
    now: Date = new Date()
): VerificationState => {
  if (verifiedYn === "Y") {
    return {label: "검증 완료", tone: "done"};
  }

  const registeredAt = insertTime ? new Date(insertTime) : null;
  if (!registeredAt || Number.isNaN(registeredAt.getTime())) {
    return {label: "미검증", tone: "waiting"};
  }

  const deadline = registeredAt.getTime() + VERIFICATION_DEADLINE_DAYS * DAY_MS;
  const remainMs = deadline - now.getTime();
  if (remainMs < 0) {
    return {label: "기한 초과", tone: "expired"};
  }

  return {label: `미검증 D-${Math.ceil(remainMs / DAY_MS)}`, tone: "waiting"};
};

export const verificationBadgeClass = (tone: VerificationState["tone"]): string => {
  const base = "inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ring-1 ring-inset";
  switch (tone) {
    case "done":
      return `${base} bg-green-50 text-green-700 ring-green-600/20`;
    case "expired":
      return `${base} bg-red-50 text-red-700 ring-red-600/20`;
    default:
      return `${base} bg-yellow-50 text-yellow-800 ring-yellow-600/20`;
  }
};
