import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {FolderIcon, HomeIcon, UsersIcon, QuestionMarkCircleIcon} from '@heroicons/react/24/outline'

// @formatter:off
const dashBoardMenu: MenuType = {name: '대시보드', id: 'main', href: '/main', icon: HomeIcon};
const ecclesiaMenu: MenuType = {name: '에클레시아', id: 'ecclesia', href: '/ecclesia', icon: FolderIcon};
const userMenu: MenuType = {name: '교인관리', id: 'user', href: '/user', icon: UsersIcon};
const notiMenu: MenuType = {name: '관리', id: 'manage', href: '/manage', icon: UsersIcon};
const supportMenu: MenuType = {name: '고객지원', id: 'support', href: '/support', icon: QuestionMarkCircleIcon};
// @formatter:on

export const getUserMenuList = (): MenuType[] => {
  const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
  if (lastLoginInfo == null) {
    return [];
  }

  const nav = [];
  if (lastLoginInfo.role === "ADMIN") {
    nav.push(dashBoardMenu);
    nav.push(ecclesiaMenu);
    nav.push(userMenu);
    nav.push(notiMenu);
    nav.push(supportMenu);
  } else if (lastLoginInfo.role === "SENIOR_PASTOR" || lastLoginInfo.role === "PASTOR") {
    nav.push(dashBoardMenu);
    nav.push(userMenu);
    nav.push(notiMenu);
    nav.push(supportMenu);
  } else {
    console.log("ROLE : " + lastLoginInfo.role);
  }

  return nav;
};
