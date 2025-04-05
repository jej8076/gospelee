import {create} from "zustand/react";

type MenuListStore = {
  menuList: MenuType[];
  setMenuList: (menuList: MenuType[]) => void;
};

export const useMenuListStore = create<MenuListStore>((set) => ({
  menuList: [],
  setMenuList: (menuList) => set({menuList: menuList}),
}));