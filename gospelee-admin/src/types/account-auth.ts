interface AuthorityType {
  authority: string;
}

interface AuthInfoType {
  uid: number;
  name: string;
  ecclesiaUid: string;
  rrn: string | null;
  phone: string | null;
  email: string;
  role: string;
  id_token: string | null;
  pushToken: string | null;
  ecclesiaStatus: string;
  enabled: boolean;
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
  accountNonLocked: boolean;
  username: string;
  password: string;
  authorities: AuthorityType[];
}
