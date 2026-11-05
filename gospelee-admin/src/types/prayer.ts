type PrayerSetting = {
  enabled: boolean;
  authorMode: 'ANON_ONLY' | 'NAME_ONLY' | 'AUTHOR_CHOICE';
  maxLength: number;
  allowedExpiryDays: number[];
  whoCanWrite: 'APPROVED_ONLY' | 'ALL_MEMBERS';
  scopeMode: 'CHURCH_ONLY' | 'GROUP_ALLOWED';
  responderVisibility: 'HIDDEN' | 'NAMED' | 'RESPONDER_CHOICE';
  adminSeesAnon: 'NEVER' | 'REPORTED_ONLY' | 'ALWAYS';
  moderationMode: 'POST_REPORT' | 'PRE_APPROVAL';
  autoHideThreshold: number;
  cheerEnabled: boolean;
  dailyDrawLimit: number;
  summaryPush: boolean;
};

type PrayerTreeEvent = {
  enabled: boolean;
  startAt?: string | null;
  endAt?: string | null;
  minCards: number;
};

type PrayerSettingHistory = {
  changedByName?: string | null;
  changedAt: string;
  /** {"필드": {"from": ..., "to": ...}} 형태의 JSON 문자열 */
  changes: string;
};

type PrayerAdminSetting = {
  setting: PrayerSetting;
  treeEvent: PrayerTreeEvent;
  history: PrayerSettingHistory[];
};

type PrayerAdminCard = {
  id: number;
  content: string;
  displayMode: 'ANON' | 'NAME';
  status: 'PENDING' | 'ACTIVE' | 'ANSWERED' | 'HIDDEN';
  reportCount: number;
  responseCount: number;
  createdAt: string;
  /** 설정이 허용한 경우에만 내려온다 */
  authorName?: string | null;
  reasons?: string[] | null;
};

type PrayerAdminStats = {
  totalCards: number;
  activeCards: number;
  answeredCards: number;
  hiddenCards: number;
  pendingCards: number;
  openWithoutResponse: number;
  totalResponses: number;
  pendingReportCards: number;
};
