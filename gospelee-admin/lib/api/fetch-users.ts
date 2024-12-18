import {fetchData} from "~/lib/api/common";

export type Users = {
  name: string;
  title: string;
  department: string;
  email: string;
  role: string;
  image: string;
};

export const fetchUsers = async (setUsers: (data: Users[]) => void) => {
  await fetchData<Users[]>("/api/account/getAccount", "POST", setUsers);
};
