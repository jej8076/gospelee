"use client"
import "./globals.css";
import {Fragment, useEffect, useState} from 'react'
import {usePathname} from 'next/navigation'
import {Button, Dialog, Menu, Transition} from '@headlessui/react'
import Link from "next/link";
import {
  Bars3Icon,
  BellIcon,
  Cog6ToothIcon,
  HomeIcon,
  UsersIcon,
  CalendarIcon,
  ChartPieIcon,
  XMarkIcon,
  DocumentDuplicateIcon,
  FolderIcon,
} from '@heroicons/react/24/outline'
import {ChevronDownIcon, MagnifyingGlassIcon} from '@heroicons/react/20/solid'
import {expireCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {useRouter} from 'next/navigation';
import {AppRouterInstance} from "next/dist/shared/lib/app-router-context.shared-runtime";

const navigation = [
  {name: '대시보드', id: 'main', href: '/main', icon: HomeIcon},
  {name: '에클레시아', id: 'ecclesia', href: '/ecclesia', icon: FolderIcon},
  {name: '성도님들', id: 'user', href: '/user', icon: UsersIcon},
  {name: '공지관리', id: 'noti', href: '/noti', icon: UsersIcon},
]

const teams = [
  {id: 1, name: 'Heroicons', href: '#', initial: 'H', current: false},
  {id: 2, name: 'Tailwind Labs', href: '#', initial: 'T', current: false},
  {id: 3, name: 'Workcation', href: '#', initial: 'W', current: false},
]

const logout = async (router: AppRouterInstance) => {
  try {
    await expireCookie(AuthItems.Authorization);
    await router.push('/login');
  } catch (error) {
    console.error('Error expiring cookie:', error);
  }
}

const userNavigation = [
  {id: 'profile', name: 'Your profile', href: '#'},
  {id: 'signOut', name: 'Sign out', href: '#',},
]

function classNames(...classes: String[]) {
  return classes.filter(Boolean).join('  ')
}

export default function MainLayout({children}: Readonly<{
  children: React.ReactNode;
}>) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const currentPath = usePathname();
  const depth1 = currentPath.split('/')[1];
  const [nav, setNav] = useState(depth1);

  const router = useRouter();
  const handleLogout = async () => {
    await logout(router);
  };

  return (
      <html>
      <body>
      {currentPath == '/login' || currentPath.startsWith('/apply') ? (
          <div>{children}</div>
      ) : (
          <div>
            {/* Static sidebar for desktop */}
            <div className="hidden lg:fixed lg:inset-y-0 lg:z-50 lg:flex lg:w-72 lg:flex-col">
              {/* Sidebar component, swap this element with another sidebar if you like */}
              <div
                  className="flex grow flex-col gap-y-5 overflow-y-auto border-r border-gray-200 bg-white px-6 pb-4">
                <div className="flex h-16 shrink-0 items-center">
                  <img
                      className="h-8 w-auto"
                      src="https://tailwindui.com/img/logos/mark.svg?color=indigo&shade=600"
                      alt="Your Company"
                  />
                </div>
                <nav className="flex flex-1 flex-col">
                  <ul role="list" className="flex flex-1 flex-col gap-y-7">
                    <li>
                      <ul role="list" className="-mx-2 space-y-1">
                        {navigation.map((item) => (
                            <li key={item.name} onClick={() => {
                              setNav(item.id)
                            }}>
                              <Link
                                  href={item.href}
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
                              </Link>
                            </li>
                        ))}
                      </ul>
                    </li>
                    <li>
                      <div className="text-xs font-semibold leading-6 text-gray-400">Your teams
                      </div>
                      <ul role="list" className="-mx-2 mt-2 space-y-1">
                        {teams.map((team) => (
                            <li key={team.name}>
                              <Link
                                  href={team.href}
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
                              </Link>
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
                    <MagnifyingGlassIcon
                        className="pointer-events-none absolute inset-y-0 left-0 h-full w-5 text-gray-400"
                        aria-hidden="true"
                    />
                    <input
                        id="search-field"
                        className="block h-full w-full border-0 py-0 pl-8 pr-0 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm"
                        placeholder="Search..."
                        type="search"
                        name="search"
                    />
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
                            src=""
                            alt=""
                        />
                        <span className="hidden lg:flex lg:items-center">
                      <span className="ml-4 text-sm font-semibold leading-6 text-gray-900"
                            aria-hidden="true">
                        Tom Cook
                      </span>
                      <ChevronDownIcon className="ml-2 h-5 w-5 text-gray-400" aria-hidden="true"/>
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
                <div className="px-4 sm:px-6 lg:px-8">{children}</div>
              </main>
            </div>
          </div>
      )}
      </body>
      </html>
  );
}
