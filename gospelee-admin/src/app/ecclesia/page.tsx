"use client"

import {useEffect, useState} from "react";
import {ChevronRightIcon} from '@heroicons/react/20/solid'
import {getCookie} from "~/lib/cookie/cookie-utils";
import useAuth from "~/lib/auth/check-auth";

type Ecclesias = {
  name: string,
  email: string,
  role: string,
  imageUrl: string
  href: string
  lastSeen: string
  lastSeenDateTime: string
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
      await fetch(`/api/ecclesia/all`, {
        method: 'POST',
        headers: {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + await getCookie("Authorization")
        },
      })
      .then((response) => {
        return response.json();
      })
      .then((res: Ecclesias[]) => {
        setEcc(res);
      });
    } catch (e) {
      console.error("Error fetching users:", e);
    }
  }

  useEffect(() => {
    fetchEcclesias();
  }, []);

  return (
      <ul role="list" className="divide-y divide-gray-100">
        {!(ecc.length > 0) ? null : ecc.map((person) => (
            <li key={person.email} className="relative flex justify-between gap-x-6 py-5">
              <div className="flex min-w-0 gap-x-4">
                <img className="h-12 w-12 flex-none rounded-full bg-gray-50" src={person.imageUrl}
                     alt=""/>
                <div className="min-w-0 flex-auto">
                  <p className="text-sm font-semibold leading-6 text-gray-900">
                    <a href={person.href}>
                      <span className="absolute inset-x-0 -top-px bottom-0"/>
                      {person.name}
                    </a>
                  </p>
                  <p className="mt-1 flex text-xs leading-5 text-gray-500">
                    <a href={`mailto:${person.email}`}
                       className="relative truncate hover:underline">
                      {person.email}
                    </a>
                  </p>
                </div>
              </div>
              <div className="flex shrink-0 items-center gap-x-4">
                <div className="hidden sm:flex sm:flex-col sm:items-end">
                  <p className="text-sm leading-6 text-gray-900">{person.role}</p>
                  {person.lastSeen ? (
                      <p className="mt-1 text-xs leading-5 text-gray-500">
                        Last seen <time dateTime={person.lastSeenDateTime}>{person.lastSeen}</time>
                      </p>
                  ) : (
                      <div className="mt-1 flex items-center gap-x-1.5">
                        <div className="flex-none rounded-full bg-emerald-500/20 p-1">
                          <div className="h-1.5 w-1.5 rounded-full bg-emerald-500"/>
                        </div>
                        <p className="text-xs leading-5 text-gray-500">Online</p>
                      </div>
                  )}
                </div>
                <ChevronRightIcon className="h-5 w-5 flex-none text-gray-400" aria-hidden="true"/>
              </div>
            </li>
        ))}
      </ul>
  )
}
