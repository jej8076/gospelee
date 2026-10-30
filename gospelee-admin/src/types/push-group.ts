type PushGroup = {
  id: number;
  name: string;
  description: string | null;
  memberCount: number;
  memberAccountUids: number[];
  insertTime: string;
};

type PushGroupCandidate = {
  uid: number;
  name: string | null;
  nickname: string | null;
  role: string;
  pushEnabled: boolean;
};

type PushGroupInput = {
  id?: number;
  name: string;
  description: string;
  memberAccountUids: number[];
};

/** 공지·주보를 등록할 때 알림을 누구에게 보낼지. '' 는 아직 고르지 않은 상태 */
type PushTarget = {
  mode: '' | 'NONE' | 'ALL' | 'GROUPS';
  groupIds: number[];
};
