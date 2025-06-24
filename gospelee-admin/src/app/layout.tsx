"use client"
import "./globals.css";
import {Fragment, useEffect, useState} from 'react'
import {usePathname, useRouter} from 'next/navigation'
import {Menu, Transition} from '@headlessui/react'
import {Bars3Icon, BellIcon, Cog6ToothIcon,} from '@heroicons/react/24/outline'
import {ChevronDownIcon} from '@heroicons/react/20/solid'
import {logout} from "@/utils/user-utils";
import {useMenuListStore} from "@/hooks/useMenuList";
import {getUserMenuList} from "@/utils/menu-utils";
import {layoutFadeStyle} from "@/app/style/layout/common";
import '@uiw/react-md-editor/markdown-editor.css';

const teams = [
  {id: 1, name: 'Heroicons', href: '#', initial: 'H', current: false},
  {id: 2, name: 'Tailwind Labs', href: '#', initial: 'T', current: false},
  {id: 3, name: 'Workcation', href: '#', initial: 'W', current: false},
]

const userNavigation = [
  {id: 'profile', name: 'Your profile', href: '#'},
  {id: 'signOut', name: 'Sign out', href: '#',},
]

function classNames(...classes: String[]) {
  return classes.filter(Boolean).join('  ')
}

const FADE_TIME_MS: 150 | 200 | 300 | 500 | 700 | 1000 = 200;

export default function MainLayout({children}: Readonly<{
  children: React.ReactNode;
}>) {

  const [fadeState, setFadeState] = useState<'hidden' | 'visible'>('hidden')
  const [sidebarOpen, setSidebarOpen] = useState(false);
  // const [isTransitioning, setIsTransitioning] = useState(false);
  const [isLoading, setLoading] = useState(false);
  const [currentChildren, setCurrentChildren] = useState(children);

  const router = useRouter();
  const pathname = usePathname()

  const menuList = useMenuListStore((state) => state.menuList);
  const setMenuList = useMenuListStore((state) => state.setMenuList);

  const currentPath = usePathname();
  const depth1 = currentPath.split('/')[1];
  const [nav, setNav] = useState(depth1);

  const handleLogout = async () => {
    await logout(router);
  };

  useEffect(() => {
    // 메뉴 노출 제어
    const fetchNavigation = async () => {
      const menuList: MenuType[] = getUserMenuList();
      setMenuList(menuList);
    };

    fetchNavigation();
  }, []);

  // 현재 페이지 정보 저장
  useEffect(() => {
    if (!isLoading) {
      setCurrentChildren(children);
    }
  }, [children, isLoading]);

  // 페이지 전환 처리
  useEffect(() => {

    // 페이지 진입 시 초기 설정
    if (!isLoading) {
      // 초기 로드 시 visible로 설정
      const timeout = setTimeout(() => {
        setFadeState('visible');
      }, 150);

      return () => clearTimeout(timeout);
    } else {
      // 페이지 전환이 완료되었을 때
      const transitionEndTimeout = setTimeout(() => {
        setCurrentChildren(children);
        setLoading(false);

        // 새 콘텐츠가 설정된 후 fade in
        const fadeInTimeout = setTimeout(() => {
          setFadeState('visible');
        }, 50);

        return () => {
          clearTimeout(fadeInTimeout);
        };
      }, FADE_TIME_MS); // fade out이 완료될 때까지의 시간

      return () => {
        clearTimeout(transitionEndTimeout);
      };
    }
  }, [pathname, isLoading, children]);

  // 커스텀 라우팅 핸들링
  const handleLinkClick = (e: React.MouseEvent<HTMLAnchorElement>, href: string): void => {
    e.preventDefault();
    // 현재 페이지에서 fade out
    setFadeState('hidden');
    setLoading(true);

    // fade out 애니메이션이 완료된 후 페이지 이동
    setTimeout(() => {
      router.push(href);
    }, FADE_TIME_MS);
  };

  const isSpecialPage = currentPath === '/login' ||
      currentPath.indexOf('/login/qr') > -1 ||
      currentPath.includes('/apply') ||
      currentPath.includes('/wait');

  return (
      <html>
      <body>
      {isSpecialPage ? (
          <div>{children}</div>
      ) : (
          <div>
            {/* Static sidebar for desktop */}
            <div className="hidden lg:fixed lg:inset-y-0 lg:z-50 lg:flex lg:w-72 lg:flex-col">
              {/* Sidebar component, swap this element with another sidebar if you like */}
              <div
                  className="flex grow flex-col gap-y-5 overflow-y-auto border-r border-gray-200 bg-white px-6 pb-4">
                <div className="flex h-16 shrink-0 items-center justify-center">
                  <img
                      className="h-8 w-auto"
                      src={'/images/logo/logo_oog.png'}
                      alt="Your Company"
                  />
                </div>
                <nav className="flex flex-1 flex-col">
                  <ul role="list" className="flex flex-1 flex-col gap-y-7">
                    <li>
                      {(!menuList || menuList.length === 0) ? "" : (
                          <ul role="list" className="-mx-2 space-y-1">
                            {menuList.map((item) => (
                                <li key={item.name} onClick={() => {
                                  setNav(item.id)
                                }}>
                                  <a
                                      href={item.href}
                                      onClick={(e) => handleLinkClick(e, item.href)}
                                      className={classNames(
                                          nav == item.id
                                              ? 'bg-gray-50 text-indigo-600'
                                              : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-50',
                                          'group flex gap-x-3 rounded-md p-2 text-sm leading-6 font-semibold'
                                      )}
                                  >
                                    <item.icon
                                        className={classNames(
                                            nav == item.id ? 'text-indigo-600' : 'text-gray-400 group-hover:text-indigo-600',
                                            'h-6 w-6 shrink-0'
                                        )}
                                        aria-hidden="true"
                                    />
                                    {item.name}
                                  </a>
                                </li>
                            ))}
                          </ul>
                      )}
                    </li>
                    <li>
                      <div className="text-xs font-semibold leading-6 text-gray-400">Your teams
                      </div>
                      <ul role="list" className="-mx-2 mt-2 space-y-1">
                        {teams.map((team) => (
                            <li key={team.name}>
                              <a
                                  href={team.href}
                                  onClick={(e) => handleLinkClick(e, team.href)}
                                  className={classNames(
                                      team.current
                                          ? 'bg-gray-50 text-indigo-600'
                                          : 'text-gray-700 hover:text-indigo-600 hover:bg-gray-50',
                                      'group flex gap-x-3 rounded-md p-2 text-sm leading-6 font-semibold'
                                  )}
                              >
                              <span
                                  className={classNames(
                                      team.current
                                          ? 'text-indigo-600 border-indigo-600'
                                          : 'text-gray-400 border-gray-200 group-hover:border-indigo-600 group-hover:text-indigo-600',
                                      'flex h-6 w-6 shrink-0 items-center justify-center rounded-lg border text-[0.625rem] font-medium bg-white'
                                  )}
                              >
                                {team.initial}
                              </span>
                                <span className="truncate">{team.name}</span>
                              </a>
                            </li>
                        ))}
                      </ul>
                    </li>
                    <li className="mt-auto">
                      <a
                          href="#"
                          className="group -mx-2 flex gap-x-3 rounded-md p-2 text-sm font-semibold leading-6 text-gray-700 hover:bg-gray-50 hover:text-indigo-600"
                      >
                        <Cog6ToothIcon
                            className="h-6 w-6 shrink-0 text-gray-400 group-hover:text-indigo-600"
                            aria-hidden="true"
                        />
                        Settings
                      </a>
                    </li>
                  </ul>
                </nav>
              </div>
            </div>

            <div className="lg:pl-72">
              <div
                  className="sticky top-0 z-40 flex h-16 shrink-0 items-center gap-x-4 border-b border-gray-200 bg-white px-4 shadow-sm sm:gap-x-6 sm:px-6 lg:px-8">
                <button type="button" className="-m-2.5 p-2.5 text-gray-700 lg:hidden"
                        onClick={() => setSidebarOpen(true)}>
                  <span className="sr-only">Open sidebar</span>
                  <Bars3Icon className="h-6 w-6" aria-hidden="true"/>
                </button>

                <div className="h-6 w-px bg-gray-200 lg:hidden" aria-hidden="true"/>

                <div className="flex flex-1 gap-x-4 self-stretch lg:gap-x-6">
                  <form className="relative flex flex-1" action="#" method="GET">
                    <label htmlFor="search-field" className="sr-only">
                      Search
                    </label>
                    {/*<MagnifyingGlassIcon*/}
                    {/*    className="pointer-events-none absolute inset-y-0 left-0 h-full w-5 text-gray-400"*/}
                    {/*    aria-hidden="true"*/}
                    {/*/>*/}
                    {/*<input*/}
                    {/*    id="search-field"*/}
                    {/*    className="block h-full w-full border-0 py-0 pl-8 pr-0 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm"*/}
                    {/*    placeholder="검색 기능은 조금만 기다려 주세요..!"*/}
                    {/*    type="search"*/}
                    {/*    name="search"*/}
                    {/*/>*/}
                  </form>
                  <div className="flex items-center gap-x-4 lg:gap-x-6">
                    <button type="button"
                            className="-m-2.5 p-2.5 text-gray-400 hover:text-gray-500">
                      <span className="sr-only">View notifications</span>
                      <BellIcon className="h-6 w-6" aria-hidden="true"/>
                    </button>

                    {/* Separator */}
                    <div className="hidden lg:block lg:h-6 lg:w-px lg:bg-gray-200"
                         aria-hidden="true"/>

                    {/* Profile dropdown */}
                    <Menu as="div" className="relative">
                      <Menu.Button className="-m-1.5 flex items-center p-1.5">
                        <span className="sr-only">Open user menu</span>
                        <img
                            className="h-8 w-8 rounded-full bg-gray-50"
                            src="/images/users/default_user.jpg"
                            alt=""
                        />
                        <span className="hidden lg:flex lg:items-center">
                          <span className="ml-4 text-sm font-semibold leading-6 text-gray-900"
                                aria-hidden="true">
                            Tom Cook
                          </span>
                          <ChevronDownIcon className="ml-2 h-5 w-5 text-gray-400"
                                           aria-hidden="true"/>
                        </span>
                      </Menu.Button>
                      <Transition
                          as={Fragment}
                          enter="transition ease-out duration-100"
                          enterFrom="transform opacity-0 scale-95"
                          enterTo="transform opacity-100 scale-100"
                          leave="transition ease-in duration-75"
                          leaveFrom="transform opacity-100 scale-100"
                          leaveTo="transform opacity-0 scale-95"
                      >
                        <Menu.Items
                            className="absolute right-0 z-10 mt-2.5 w-32 origin-top-right rounded-md bg-white py-2 shadow-lg ring-1 ring-gray-900/5 focus:outline-none">
                          {userNavigation.map((item) => (
                              <Menu.Item key={item.id}>
                                <button
                                    type="button"
                                    onClick={() => handleLogout()}
                                    className={classNames(
                                        'ui-active:bg-gray-50',
                                        'block px-3 py-1 text-sm leading-6 text-gray-900'
                                    )}
                                >
                                  {item.name}
                                </button>
                              </Menu.Item>
                          ))}
                        </Menu.Items>
                      </Transition>
                    </Menu>
                  </div>
                </div>
              </div>

              <main className="py-10">
                <div className={layoutFadeStyle(fadeState, FADE_TIME_MS)}>
                  {/* 페이지 전환 중에는 이전 페이지 컨텐츠를 유지 */}
                  {currentChildren}
                </div>
              </main>
            </div>
          </div>
      )}
      </body>
      </html>
  );
}
