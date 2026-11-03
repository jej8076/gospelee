/** 푸시 발송 이력 한 건과 상태별 인원 */
type PushHistory = {
  pushId: number;
  title: string;
  message: string;
  category: string;
  senderName: string | null;
  sentTime: string | null;
  /** 토큰이 있어 FCM 에 보낸 대상 수 */
  targetCount: number;
  /** FCM 이 접수한 수 (받음·확인 포함) */
  deliveredCount: number;
  failedCount: number;
  /** 앱이 받았다고 알려 온 수 (확인 포함) */
  receivedCount: number;
  confirmedCount: number;
};

type PushHistoryReceiver = {
  accountUid: number;
  name: string;
  /** READY(전달됨) / RECEIVED / CONFIRMED / FAILED */
  status: string;
};

type PushHistoryDetail = {
  push: PushHistory;
  receivers: PushHistoryReceiver[];
};
