'use client'

import React, {useEffect, useState} from 'react'
import {fetchGetEcclesia} from "~/lib/api/fetch-ecclesias";
import {useApiClient} from "@/hooks/useApiClient";
import {getLastLoginOrElseNull, logout} from "@/utils/user-utils";
import {useRouter} from "next/navigation";
import {
  ClockIcon,
  CheckCircleIcon,
  UserIcon,
  PhoneIcon,
  BuildingOfficeIcon
} from '@heroicons/react/24/outline';

export default function WaitPage() {

  const {callApi} = useApiClient();

  const [ecclesia, setEcclesia] = useState<Ecclesia>();
  const [manager, setManager] = useState("");

  const router = useRouter();

  const handleLogout = async () => {
    await logout(router);
  };

  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setManager(lastLoginInfo?.name ?? "");
    callApi(() => fetchGetEcclesia(lastLoginInfo?.uid ?? 0), setEcclesia);
  }, []);

  return (
      <div className="min-h-screen bg-gray-50">
        {/* Background decoration */}
        <div className="absolute inset-0 overflow-hidden">
          <div
              className="absolute -top-40 -right-32 w-80 h-80 rounded-full bg-gray-200 opacity-30 blur-3xl"></div>
          <div
              className="absolute -bottom-40 -left-32 w-80 h-80 rounded-full bg-gray-300 opacity-20 blur-3xl"></div>
        </div>

        <div className="relative px-6 py-12 sm:py-16 lg:px-8">
          <div className="mx-auto max-w-3xl">
            {/* Header Section */}
            <div className="text-center mb-12">
              <div className="flex justify-center mb-6">
                <div className="relative">
                  <div
                      className="w-20 h-20 bg-gray-800 rounded-full flex items-center justify-center shadow-lg">
                    <ClockIcon className="w-10 h-10 text-white"/>
                  </div>
                  <div
                      className="absolute -top-1 -right-1 w-6 h-6 bg-gray-600 rounded-full flex items-center justify-center">
                    <div className="w-2 h-2 bg-white rounded-full animate-pulse"></div>
                  </div>
                </div>
              </div>
              <h1 className="text-4xl font-bold text-gray-900 sm:text-5xl mb-4">
                승인 대기중
              </h1>
              <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                관리자가 교회 정보를 검토하고 있습니다.<br/>
                승인이 완료되면 알림을 받으실 수 있습니다.
              </p>
            </div>

            {/* Status Card */}
            <div
                className="bg-white rounded-2xl shadow-xl border border-gray-200 overflow-hidden mb-8">
              <div className="bg-gray-800 px-6 py-4">
                <h2 className="text-xl font-semibold text-white flex items-center">
                  <CheckCircleIcon className="w-6 h-6 mr-2"/>
                  신청 정보
                </h2>
              </div>

              <div className="p-8 space-y-8">
                {/* 교회 이름 */}
                <div className="flex items-start space-x-4">
                  <div className="flex-shrink-0">
                    <div
                        className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                      <BuildingOfficeIcon className="w-6 h-6 text-gray-700"/>
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">교회 이름</h3>
                    <p className="text-2xl font-bold text-gray-800">{ecclesia?.name || '로딩 중...'}</p>
                  </div>
                </div>

                {/* 담당 교역자 */}
                <div className="flex items-start space-x-4">
                  <div className="flex-shrink-0">
                    <div
                        className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                      <UserIcon className="w-6 h-6 text-gray-700"/>
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">담당 교역자</h3>
                    <p className="text-xl text-gray-700">{manager || '정보 없음'}</p>
                  </div>
                </div>

                {/* 전화번호 */}
                <div className="flex items-start space-x-4">
                  <div className="flex-shrink-0">
                    <div
                        className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                      <PhoneIcon className="w-6 h-6 text-gray-700"/>
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-semibold text-gray-900 mb-1">전화번호</h3>
                    <p className="text-xl text-gray-700">{ecclesia?.telephone || '정보 없음'}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Progress Steps */}
            <div className="bg-white rounded-2xl shadow-xl border border-gray-200 p-8 mb-8">
              <h3 className="text-lg font-semibold text-gray-900 mb-6">승인 진행 상황</h3>
              <div className="flex items-center justify-between">
                <div className="flex flex-col items-center">
                  <div
                      className="w-10 h-10 bg-green-500 rounded-full flex items-center justify-center mb-2">
                    <CheckCircleIcon className="w-6 h-6 text-white"/>
                  </div>
                  <span className="text-sm font-medium text-green-600">신청 완료</span>
                </div>
                <div className="flex-1 h-1 bg-gray-200 mx-4 rounded-full">
                  <div className="h-1 bg-blue-500 rounded-full animate-pulse"
                       style={{width: '60%'}}></div>
                </div>
                <div className="flex flex-col items-center">
                  <div
                      className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center mb-2">
                    <ClockIcon className="w-6 h-6 text-white"/>
                  </div>
                  <span className="text-sm font-medium text-blue-600">검토 중</span>
                </div>
                <div className="flex-1 h-1 bg-gray-200 mx-4 rounded-full"></div>
                <div className="flex flex-col items-center">
                  <div
                      className="w-10 h-10 bg-gray-300 rounded-full flex items-center justify-center mb-2">
                    <CheckCircleIcon className="w-6 h-6 text-white"/>
                  </div>
                  <span className="text-sm font-medium text-gray-500">승인 완료</span>
                </div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                  type="button"
                  onClick={() => window.location.reload()}
                  className="px-8 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200"
              >
                상태 새로고침
              </button>
              <button
                  type="button"
                  onClick={handleLogout}
                  className="px-8 py-3 bg-gray-600 text-white font-semibold rounded-lg shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors duration-200"
              >
                로그아웃
              </button>
            </div>

            {/* Help Text */}
            <div className="text-center mt-12">
              <p className="text-gray-500 text-sm">
                승인 과정에서 문제가 있거나 문의사항이 있으시면<br/>
                관리자에게 연락해 주세요.
              </p>
            </div>
          </div>
        </div>
      </div>
  )
}
