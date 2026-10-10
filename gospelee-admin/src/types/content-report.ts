type ContentReport = {
  uid: number;
  targetType: 'SHARE' | 'COMMENT';
  targetUid: number;
  reason: string;
  detail?: string | null;
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED';
  reporterNickname: string;
  authorNickname: string;
  authorUid?: number | null;
  reference?: string | null;
  content?: string | null;
  targetActive: boolean;
  reportedAt?: string | null;
};
