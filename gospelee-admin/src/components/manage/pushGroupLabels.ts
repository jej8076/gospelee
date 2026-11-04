export const ROLE_LABELS: Record<string, string> = {
  LAYMAN: '성도',
  TEACHER: '교사',
  COORDINATOR: '간사',
  MINISTER: '전도사',
  LICENSED_MINISTER: '강도사',
  PASTOR: '목사',
  SENIOR_PASTOR: '담임목사',
  ADMIN: '관리자',
};

export const displayName = (c: PushGroupCandidate) => c.name?.trim() || c.nickname?.trim() || `성도 ${c.uid}`;
