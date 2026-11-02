/** 공지·주보 등록 없이 직접 푸시를 보내는 요청. groupIds 가 비어 있으면 교회 성도 전체 */
type PushDirectInput = {
  title: string;
  message: string;
  groupIds: number[];
};

type PushDirectResult = {
  pushId: number;
  targetCount: number;
  sentCount: number;
  failedCount: number;
  noTokenCount: number;
};
