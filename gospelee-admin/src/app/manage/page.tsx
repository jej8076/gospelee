import {
  AcademicCapIcon,
  BanknotesIcon,
  CheckBadgeIcon,
  ClockIcon,
  ReceiptRefundIcon,
  UsersIcon,
} from '@heroicons/react/24/outline'

import {FC} from 'react'

type Action = {
  title: string
  href: string
  icon: FC<React.SVGProps<SVGSVGElement>>
  iconForeground: string
  iconBackground: string
  description: string
}

const actions: Action[] = [
  {
    title: '공지사항 관리',
    href: '/manage/noti',
    icon: ClockIcon,
    iconForeground: 'text-teal-700',
    iconBackground: 'bg-teal-50',
    description: '교회의 공지사항을 관리합니다.',
  },
  {
    title: 'QT 관리',
    href: '#',
    icon: CheckBadgeIcon,
    iconForeground: 'text-purple-700',
    iconBackground: 'bg-purple-50',
    description: '성도님들에게 제공할 QT를 관리합니다',
  },
  // {
  //   title: 'Schedule a one-on-one',
  //   href: '#',
  //   icon: UsersIcon,
  //   iconForeground: 'text-sky-700',
  //   iconBackground: 'bg-sky-50',
  //   description: '',
  // },
  // {
  //   title: 'Payroll',
  //   href: '#',
  //   icon: BanknotesIcon,
  //   iconForeground: 'text-yellow-700',
  //   iconBackground: 'bg-yellow-50',
  //   description: '',
  // },
  // {
  //   title: 'Submit an expense',
  //   href: '#',
  //   icon: ReceiptRefundIcon,
  //   iconForeground: 'text-rose-700',
  //   iconBackground: 'bg-rose-50',
  //   description: '',
  // },
  // {
  //   title: 'Training',
  //   href: '#',
  //   icon: AcademicCapIcon,
  //   iconForeground: 'text-indigo-700',
  //   iconBackground: 'bg-indigo-50',
  //   description: '',
  // },
]

function classNames(...classes: (string | boolean | undefined | null)[]): string {
  return classes.filter(Boolean).join(' ')
}

export default function Noti(): JSX.Element {
  return (
      <div
          className="divide-y divide-gray-200 overflow-hidden rounded-lg bg-gray-200 shadow-sm sm:grid sm:grid-cols-2 sm:gap-px sm:divide-y-0">
        {actions.map((action, actionIdx) => (
            <div
                key={action.title}
                className={classNames(
                    actionIdx === 0 ? 'rounded-tl-lg rounded-tr-lg sm:rounded-tr-none' : '',
                    actionIdx === 1 ? 'sm:rounded-tr-lg' : '',
                    actionIdx === actions.length - 2 ? 'sm:rounded-bl-lg' : '',
                    actionIdx === actions.length - 1 ? 'rounded-br-lg rounded-bl-lg sm:rounded-bl-none' : '',
                    'group relative bg-white p-6 focus-within:ring-2 focus-within:ring-indigo-500 focus-within:ring-inset',
                )}
            >
              <div>
            <span
                className={classNames(
                    action.iconBackground,
                    action.iconForeground,
                    'inline-flex rounded-lg p-3 ring-4 ring-white',
                )}
            >
              <action.icon aria-hidden="true" className="size-6"/>
            </span>
              </div>
              <div className="mt-8">
                <h3 className="text-base font-semibold text-gray-900">
                  <a href={action.href} className="focus:outline-hidden">
                    <span aria-hidden="true" className="absolute inset-0"/>
                    {action.title}
                  </a>
                </h3>
                <p className="mt-2 text-sm text-gray-500">
                  {action.description}
                </p>
              </div>
              <span
                  aria-hidden="true"
                  className="pointer-events-none absolute top-6 right-6 text-gray-300 group-hover:text-gray-400"
              >
            <svg fill="currentColor" viewBox="0 0 24 24" className="size-6">
              <path
                  d="M20 4h1a1 1 0 00-1-1v1zm-1 12a1 1 0 102 0h-2zM8 3a1 1 0 000 2V3zM3.293 19.293a1 1 0 101.414 1.414l-1.414-1.414zM19 4v12h2V4h-2zm1-1H8v2h12V3zm-.707.293l-16 16 1.414 1.414 16-16-1.414-1.414z"/>
            </svg>
          </span>
            </div>
        ))}
      </div>
  )
}
