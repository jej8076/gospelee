"use client"

import {useEffect, useState} from "react";
import {getCookie} from "~/lib/cookie/cookie-utils";
import useAuth from "~/lib/auth/check-auth";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

type Ecclesias = {
  ecclesiaUid: bigint,
  churchIdentificationNumber: string,
  status: string,
  ecclesiaName: string,
  masterAccountName: string,
  insertTime: string,
  // name: string,
  // email: string,
  // role: string,
  // imageUrl: string
  // href: string
  // lastSeen: string
  // lastSeenDateTime: string
};

// const ecc = [
//   {
//     name: 'Leslie Alexander',
//     email: 'leslie.alexander@example.com',
//     role: 'Co-Founder / CEO',
//     imageUrl:
//         'https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
//     href: '#',
//     lastSeen: '3h ago',
//     lastSeenDateTime: '2023-01-23T13:23Z',
//   }
// ]


export default function Ecclesia() {
  useAuth();

  const [ecc, setEcc] = useState<Ecclesias[]>([]);

  const fetchEcclesias = async () => {
    try {
      const response = await apiFetch(`/api/ecclesia/all`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `${AuthItems.Bearer}${await getCookie(AuthItems.Authorization)}`,
        },
      });

      const res: Ecclesias[] = await response.json();
      setEcc(res);
    } catch (e) {
      console.error("Error fetching users:", e);
    }
  }

  useEffect(() => {
    fetchEcclesias();
  }, []);

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
            <button
                type="button"
                className="block rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            >
              Add user
            </button>
          </div>
        </div>
        <div className="mt-8 flow-root">
          <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
            <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
              <table className="min-w-full divide-y divide-gray-300">
                <thead>
                <tr>
                  <th scope="col"
                      className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-0">
                    교회
                  </th>
                  <th scope="col"
                      className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                    관리자
                  </th>
                  <th scope="col"
                      className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                    상태
                  </th>
                  <th scope="col" className="relative py-3.5 pl-3 pr-4 sm:pr-0">
                    <span className="sr-only">Edit</span>
                  </th>
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                {!(ecc.length > 0) ? null : ecc.map((u) => (
                    <tr key={u.ecclesiaUid}>
                      <td className="whitespace-nowrap py-5 pl-4 pr-3 text-sm sm:pl-0">
                        <div className="flex items-center">
                          <div className="h-11 w-11 flex-shrink-0">
                            <img className="h-11 w-11 rounded-full" src="" alt=""/>
                          </div>
                          <div className="ml-4">
                            <div className="font-medium text-gray-900">{u.ecclesiaName}</div>
                            <div className="mt-1 text-gray-500">{u.churchIdentificationNumber}</div>
                          </div>
                        </div>
                      </td>
                      {/*<td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">*/}
                      {/*  <div className="text-gray-900">{u.churchIdentificationNumber}</div>*/}
                      {/*  <div className="mt-1 text-gray-500">{u.name}</div>*/}
                      {/*</td>*/}
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">{u.masterAccountName}</td>
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">
                      <span
                          className="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">
                        Active
                      </span>
                      </td>
                      <td className="relative whitespace-nowrap py-5 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                        <a href="#" className="text-indigo-600 hover:text-indigo-900">Edit</a>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
  );
}
