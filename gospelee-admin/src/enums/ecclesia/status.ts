export const EcclesiaStatus = {
  REQUEST: 'REQ',
  REJECT: 'REJ',
  APPROVAL: 'APL',
} as const;

export const ecclesiaStatusKor = (status: string): string => {
  const statusMap: Record<string, string> = {
    [EcclesiaStatus.REQUEST]: '요청',
    [EcclesiaStatus.REJECT]: '거절',
    [EcclesiaStatus.APPROVAL]: '승인',
  };

  return statusMap[status] || 'N/A';
}
