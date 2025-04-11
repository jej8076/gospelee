export const EcclesiaStatus = {
  REQUEST: 'REQ',
  REJECT: 'REJ',
  APPROVAL: 'APL',
} as const;

// EcclesiaStatus의 값들로 구성된 유니온 타입을 생성
export type EcclesiaStatusType = typeof EcclesiaStatus[keyof typeof EcclesiaStatus];

// 상태 코드와 한글 표시 매핑
export const ecclesiaStatusKor = (status: string): string => {
  const statusMap: Record<string, string> = {
    [EcclesiaStatus.REQUEST]: '요청',
    [EcclesiaStatus.REJECT]: '거절',
    [EcclesiaStatus.APPROVAL]: '승인',
  };

  return statusMap[status] || 'N/A';
};

// 모든 상태 옵션을 반환하는 함수
export const getEcclesiaStatusOptions = (): { value: EcclesiaStatusType; label: string }[] => {
  return [
    {value: EcclesiaStatus.REQUEST, label: ecclesiaStatusKor(EcclesiaStatus.REQUEST)},
    {value: EcclesiaStatus.REJECT, label: ecclesiaStatusKor(EcclesiaStatus.REJECT)},
    {value: EcclesiaStatus.APPROVAL, label: ecclesiaStatusKor(EcclesiaStatus.APPROVAL)},
  ];
};

/**
 * 문자열 상태 값을 EcclesiaStatusType으로 변환하는 함수
 * 유효하지 않은 값이 입력되면 기본값으로 REQUEST를 반환
 */
export const convertEcclesiaStatusType = (currentStatus: string): EcclesiaStatusType => {
  // 모든 가능한 상태 값 배열
  const validStatusValues: EcclesiaStatusType[] = [
    EcclesiaStatus.REQUEST,
    EcclesiaStatus.REJECT,
    EcclesiaStatus.APPROVAL
  ];

  // 입력된 값이 유효한 상태 값 중 하나인지 확인
  if (validStatusValues.includes(currentStatus as EcclesiaStatusType)) {
    return currentStatus as EcclesiaStatusType;
  }

  // 유효하지 않은 값이면 기본값으로 REQUEST 반환
  return EcclesiaStatus.REQUEST;
};
