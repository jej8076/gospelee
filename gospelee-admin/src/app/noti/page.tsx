"use client"
import Link from 'next/link';
import {useLayoutEffect, useRef, useState} from 'react';

interface Person {
  name: string;
  title: string;
  email: string;
  role: string;
}

const notiList: Person[] = [
  {
    name: 'Lindsay Walton',
    title: 'Front-end Developer',
    email: 'lindsay1.walton@example.com',
    role: 'Member',
  },
  {
    name: 'Lindsay Walton',
    title: 'Front-end Developer',
    email: 'lindsay2.walton@example.com',
    role: 'Member',
  },
  {
    name: 'Lindsay Walton',
    title: 'Front-end Developer',
    email: 'lindsay3.walton@example.com',
    role: 'Member',
  },
];

function classNames(...classes: string[]): string {
  return classes.filter(Boolean).join(' ');
}

export default function Noti() {
  const checkbox = useRef<HTMLInputElement>(null);
  const [checked, setChecked] = useState(false);
  const [allSame, setAllSame] = useState(false);
  const [selectedPeople, setSelectedPeople] = useState<Person[]>([]);

  useLayoutEffect(() => {
    const isAllsame = selectedPeople.length > 0 && selectedPeople.length < notiList.length;
    setAllSame(isAllsame);
    setChecked(selectedPeople.length === notiList.length);
    if (checkbox.current) {
      checkbox.current.indeterminate = isAllsame;
    }
  }, [selectedPeople]);

  function toggleAll() {
    setSelectedPeople(checked || allSame ? [] : notiList);
    setChecked(!checked && !allSame);
    setAllSame(false);
  }

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-base font-semibold leading-6 text-gray-900">Users</h1>
            <p className="mt-2 text-sm text-gray-700">
              A list of all the users in your account including their name, title, email and role.
            </p>
          </div>
          <div className="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
            <Link
                href={"/noti/write"}
                className="block rounded-md bg-indigo-600 px-3 py-1.5 text-center text-sm font-semibold leading-6 text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600">
              공지쓰기
            </Link>
          </div>
        </div>
        <div className="mt-8 flow-root">
          <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
            <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
              <div className="relative">
                {selectedPeople.length > 0 && (
                    <div
                        className="absolute left-14 top-0 flex h-12 items-center space-x-3 bg-white sm:left-12">
                      <button
                          type="button"
                          className="inline-flex items-center rounded bg-white px-2 py-1 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:bg-white"
                      >
                        Bulk edit
                      </button>
                      <button
                          type="button"
                          className="inline-flex items-center rounded bg-white px-2 py-1 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:bg-white"
                      >
                        Delete all
                      </button>
                    </div>
                )}
                <table className="min-w-full table-fixed divide-y divide-gray-300">
                  <thead>
                  <tr>
                    <th scope="col" className="relative px-7 sm:w-12 sm:px-6">
                      <input
                          type="checkbox"
                          className="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                          ref={checkbox}
                          checked={checked}
                          onChange={toggleAll}
                      />
                    </th>
                    <th scope="col"
                        className="min-w-[12rem] py-3.5 pr-3 text-left text-sm font-semibold text-gray-900">
                      Name
                    </th>
                    <th scope="col"
                        className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                      Title
                    </th>
                    <th scope="col"
                        className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                      Email
                    </th>
                    <th scope="col"
                        className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                      Role
                    </th>
                    <th scope="col" className="relative py-3.5 pl-3 pr-4 sm:pr-3">
                      <span className="sr-only">Edit</span>
                    </th>
                  </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-200 bg-white">
                  {notiList.map((noti) => (
                      <tr key={noti.email}
                          className={selectedPeople.includes(noti) ? 'bg-gray-50' : undefined}>
                        <td className="relative px-7 sm:w-12 sm:px-6">
                          {selectedPeople.includes(noti) && (
                              <div className="absolute inset-y-0 left-0 w-0.5 bg-indigo-600"/>
                          )}
                          <input
                              type="checkbox"
                              className="absolute left-4 top-1/2 -mt-2 h-4 w-4 rounded border-gray-300 text-indigo-600 focus:ring-indigo-600"
                              value={noti.email}
                              checked={selectedPeople.includes(noti)}
                              onChange={(e) =>
                                  setSelectedPeople(
                                      e.target.checked
                                          ? [...selectedPeople, noti]
                                          : selectedPeople.filter((p) => p !== noti)
                                  )
                              }
                          />
                        </td>
                        <td
                            className={classNames(
                                'whitespace-nowrap py-4 pr-3 text-sm font-medium',
                                selectedPeople.includes(noti) ? 'text-indigo-600' : 'text-gray-900'
                            )}
                        >
                          {noti.name}
                        </td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{noti.title}</td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{noti.email}</td>
                        <td className="whitespace-nowrap px-3 py-4 text-sm text-gray-500">{noti.role}</td>
                        <td className="whitespace-nowrap py-4 pl-3 pr-4 text-right text-sm font-medium sm:pr-3">
                          <a href="#" className="text-indigo-600 hover:text-indigo-900">
                            Edit<span className="sr-only">, {noti.name}</span>
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
      </div>
  )
}
