'use client'

import React, {useEffect, useState} from 'react'
import {fetchGetEcclesia} from "~/lib/api/fetch-ecclesias";
import {useApiClient} from "@/hooks/useApiClient";
import {getLastLoginOrElseNull, logout} from "@/utils/user-utils";
import {useRouter} from "next/navigation";


export default function ApplyChurch() {

  const {callApi} = useApiClient();

  const [ecclesia, setEcclesia] = useState<Ecclesia>();
  const [pastor, setPastor] = useState("");

  const router = useRouter();

  const handleLogout = async () => {
    await logout(router);
  };

  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setPastor(lastLoginInfo?.name ?? "");
    callApi(() => fetchGetEcclesia(lastLoginInfo?.ecclesiaUid ?? ""), setEcclesia);
  }, []);

  return (

      <div className="isolate bg-white px-6 py-24 sm:py-32 lg:px-8">
        <div
            aria-hidden="true"
            className="absolute inset-x-0 top-[-10rem] -z-10 transform-gpu overflow-hidden blur-3xl sm:top-[-20rem]"
        >
          <div
              style={{
                clipPath:
                    'polygon(74.1% 44.1%, 100% 61.6%, 97.5% 26.9%, 85.5% 0.1%, 80.7% 2%, 72.5% 32.5%, 60.2% 62.4%, 52.4% 68.1%, 47.5% 58.3%, 45.2% 34.5%, 27.5% 76.7%, 0.1% 64.9%, 17.9% 100%, 27.6% 76.8%, 76.1% 97.7%, 74.1% 44.1%)',
              }}
              className="relative left-1/2 -z-10 aspect-1155/678 w-[36.125rem] max-w-none -translate-x-1/2 rotate-[30deg] bg-linear-to-tr from-[#ff80b5] to-[#9089fc] opacity-30 sm:left-[calc(50%-40rem)] sm:w-[72.1875rem]"
          />
        </div>
        <div className="mx-auto max-w-2xl text-center">
          <h2 className="text-4xl font-semibold tracking-tight text-balance text-gray-900 sm:text-5xl">승인
            대기중입니다</h2>
          <p className="mt-2 text-lg/8 text-gray-600">관리자가 데이터를 검토하고 있어요</p>
        </div>
        <div className="mx-auto mt-16 max-w-xl sm:mt-20 space-y-20">
          <div className="grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="block text-2xl font-semibold text-gray-900">
                교회 <span className="text-sm ml-3 font-light">{ecclesia?.ecclesiaName}</span>
              </label>
            </div>
          </div>
          <div className="grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="block text-2xl font-semibold text-gray-900">
                담임목사 <span className="text-sm ml-3 font-light">{pastor}</span>
              </label>
            </div>
          </div>
          <div className="grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label className="block text-2xl font-semibold text-gray-900">
                교회 고유번호 <span
                  className="text-sm ml-3 font-light">{ecclesia?.churchIdentificationNumber}</span>
              </label>
            </div>
          </div>
        </div>

        {/* 로그아웃 버튼 추가 */}
        <div className="grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2 mt-20">
          <div className="sm:col-span-2 flex justify-center">
            <button
                type="button"
                onClick={() => {
                  handleLogout();
                }}
                className="rounded-md bg-gray-400 px-6 py-2.5 text-center text-sm font-semibold text-white shadow-sm hover:bg-gray-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-gray-400"
            >
              로그아웃
            </button>
          </div>
        </div>
      </div>
  )
}
