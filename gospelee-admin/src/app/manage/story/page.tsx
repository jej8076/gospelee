"use client"

import React, {useEffect, useState} from 'react'
import useAuth from "~/lib/auth/check-auth";
import {useRouter} from "next/navigation";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchAnnouncements} from "~/lib/api/fetch-announcement";

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
  const {callApi} = useApiClient();
  const [announcement, setAnnouncement] = useState<Announcement[]>([]);

  const routeCreate = () => {
    router.push(`/manage/noti/create`)
  };

  useEffect(() => {
    callApi(fetchAnnouncements, setAnnouncement);
  }, []);

  console.log(announcement);

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
              <table className="min-w-full table-auto border-separate border-spacing-0">
                <thead>
                <tr>
                  {/*<th*/}
                  {/*    scope="col"*/}
                  {/*    className="w-1/5 sticky top-0 z-10 border-b border-gray-300 bg-white/75 px-2 sm:px-8 py-3.5 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter">*/}
                  {/*  제목*/}
                  {/*</th>*/}

                  <th
                      scope="col"
                      className="min-w-0 w-2/5 border-b border-gray-300 bg-white/75 px-2 sm:px-8 py-3.5 pr-3 pl-4 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter sm:pl-6 lg:pl-8">
                    본문
                  </th>

                  <th
                      scope="col"
                      className="min-w-0 w-1/6 border-b border-gray-300 bg-white/75 px-8 py-3.5 pr-3 pl-4 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter sm:pl-6 lg:pl-8">
                    푸시알림
                  </th>

                  <th
                      scope="col"
                      className="min-w-0 w-1/6 border-b border-gray-300 bg-white/75 px-8 py-3.5 pr-3 pl-4 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter sm:pl-6 lg:pl-8">
                    등록시간
                  </th>

                  <th
                      scope="col"
                      className="min-w-0 w-1/12 border-b border-gray-300 bg-white/75 px-8 py-3.5 text-left text-sm font-semibold text-gray-900 backdrop-blur-sm backdrop-filter">
                    수정
                  </th>
                </tr>
                </thead>
                <tbody>
                {!(announcement.length > 0) ? null : announcement.map((a) => (
                    <tr key={a.id}>
                      {/*<td className='w-1/5 border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8 truncate'>*/}
                      {/*  {a.subject}*/}
                      {/*</td>*/}
                      <td className='w-2/5 border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8'>
                        {a.text}
                      </td>
                      <td className='w-1/6 border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8'>
                        {a.pushNotificationIds == null ? "발송안함" : "발송"}
                      </td>
                      <td className='w-1/6 border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8'>
                        {a.insertTime}
                      </td>
                      <td className='w-1/12 border-b border-gray-200 py-4 pr-3 pl-4 text-sm font-medium text-gray-900 sm:pl-6 lg:pl-8'>
                        {a.pushNotificationIds == null && (
                            <a className="text-indigo-600 hover:text-indigo-900 cursor-pointer">
                              Edit
                            </a>
                        )}
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
