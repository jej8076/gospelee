import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {FolderIcon, HomeIcon, UsersIcon,} from '@heroicons/react/24/outline'

// @formatter:off
const dashBoardMenu: MenuType = {name: '대시보드', id: 'main', href: '/main', icon: HomeIcon};
const ecclesiaMenu: MenuType = {name: '에클레시아', id: 'ecclesia', href: '/ecclesia', icon: FolderIcon};
const userMenu: MenuType = {name: '성도님들', id: 'user', href: '/user', icon: UsersIcon};
const notiMenu: MenuType = {name: '공지관리', id: 'noti', href: '/noti', icon: UsersIcon};
// @formatter:on

export const getUserMenuList = (): MenuType[] => {
  const lastLoginInfo: AuthInfoType = getLastLoginOrElseNull();
  if (lastLoginInfo == null) {
    return [];
  }

  const nav = [];
  if (lastLoginInfo.role === "ADMIN") {
    nav.push(dashBoardMenu);
    nav.push(ecclesiaMenu);
    nav.push(userMenu);
    nav.push(notiMenu);
  } else if (lastLoginInfo.role === "SENIOR_PASTOR" || lastLoginInfo.role === "PASTOR") {
    nav.push(dashBoardMenu);
    nav.push(userMenu);
    nav.push(notiMenu);
  } else {
    console.log("ROLE : " + lastLoginInfo.role);
  }

  return nav;
};
