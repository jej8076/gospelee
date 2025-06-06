"use client"

import React from 'react'
import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";

type Person = {
  name: string
  title: string
  email: string
  role: string
}

const people: Person[] = [
  {
    name: 'Lindsay Walton',
    title: 'Front-end Developer',
    email: 'lindsay.walton@example.com',
    role: 'Member'
  },
  // 더 많은 사람들...
]

function classNames(...classes: (string | boolean | null | undefined)[]): string {
  return classes.filter(Boolean).join(' ')
}

export default function ManageNoti(): JSX.Element {
  useAuth();

  const router = useRouter();

  const routeCreate = () => {
    router.push(`/manage/noti/create`)
  };

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-base font-semibold text-gray-900">Noti</h1>
            <p className="mt-2 text-sm text-gray-700">
              공지사항을 관리합니다
            </p>
          </div>
          <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
            <button
                type="button"
                className="block rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 cursor-pointer"
                onClick={() => routeCreate()}
            >
              새로만들기
            </button>
          </div>
        </div>
        <div className="mt-8 flow-root">
          <div className="-mx-4 -my-2 sm:-mx-6 lg:-mx-8">
            <div className="inline-block min-w-full py-2 align-middle">
              <table className="min-w-full border-separate border-spacing-0">
                <thead>
                <tr>
                  <th
                      scope="col"
                      className="sticky top-0 z-10 border-b border-gray-300 bg-white/75 py-3.5 pr-3 pl-4 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter sm:pl-6 lg:pl-8"
                  >
                    Name
                  </th>
                  <th
                      scope="col"
                      className="sticky top-0 z-10 hidden border-b border-gray-300 bg-white/75 px-3 py-3.5 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter sm:table-cell"
                  >
                    Title
                  </th>
                  <th
                      scope="col"
                      className="sticky top-0 z-10 hidden border-b border-gray-300 bg-white/75 px-3 py-3.5 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter lg:table-cell"
                  >
                    Email
                  </th>
                  <th
                      scope="col"
                      className="sticky top-0 z-10 border-b border-gray-300 bg-white/75 px-3 py-3.5 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter"
                  >
                    Role
                  </th>
                  <th
                      scope="col"
                      className="sticky top-0 z-10 border-b border-gray-300 bg-white/75 py-3.5 pr-4 pl-3 backdrop-blur-sm backdrop-filter sm:pr-6 lg:pr-8"
                  >
                    <span className="sr-only">Edit</span>
                  </th>
                </tr>
                </thead>
                <tbody>
                {people.map((person, personIdx) => (
                    <tr key={person.email}>
                      <td
                          className={classNames(
                              personIdx !== people.length - 1 ? 'border-b border-gray-200' : '',
                              'py-4 pr-3 pl-4 text-sm font-medium whitespace-nowrap text-gray-900 sm:pl-6 lg:pl-8',
                          )}
                      >
                        {person.name}
                      </td>
                      <td
                          className={classNames(
                              personIdx !== people.length - 1 ? 'border-b border-gray-200' : '',
                              'hidden px-3 py-4 text-sm whitespace-nowrap text-gray-500 sm:table-cell',
                          )}
                      >
                        {person.title}
                      </td>
                      <td
                          className={classNames(
                              personIdx !== people.length - 1 ? 'border-b border-gray-200' : '',
                              'hidden px-3 py-4 text-sm whitespace-nowrap text-gray-500 lg:table-cell',
                          )}
                      >
                        {person.email}
                      </td>
                      <td
                          className={classNames(
                              personIdx !== people.length - 1 ? 'border-b border-gray-200' : '',
                              'px-3 py-4 text-sm whitespace-nowrap text-gray-500',
                          )}
                      >
                        {person.role}
                      </td>
                      <td
                          className={classNames(
                              personIdx !== people.length - 1 ? 'border-b border-gray-200' : '',
                              'relative py-4 pr-4 pl-3 text-right text-sm font-medium whitespace-nowrap sm:pr-8 lg:pr-8',
                          )}
                      >
                        <a href="#" className="text-indigo-600 hover:text-indigo-900">
                          Edit<span className="sr-only">, {person.name}</span>
                        </a>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
  )
}
