"use client"

import React, {useCallback, useEffect, useState, type JSX} from 'react';
import useAuth from "~/lib/auth/check-auth";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchChangeMinistryStatus, fetchMinistries} from "~/lib/api/fetch-ministries";
import {getLastLoginOrElseNull} from "@/utils/user-utils";

const formatBytes = (bytes: number): string => {
  if (bytes >= 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)}GB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
};

export default function ManageMinistry(): JSX.Element {
  useAuth();

  const {callApi} = useApiClient();
  const [keyword, setKeyword] = useState('');
  const [ministries, setMinistries] = useState<MinistryRow[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(true);

  const load = useCallback(async (target: string) => {
    setIsLoading(true);
    try {
      await callApi(fetchMinistries, setMinistries, target);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    setIsAdmin(getLastLoginOrElseNull()?.role === 'ADMIN');
  }, []);

  useEffect(() => {
    if (isAdmin) {
      load('');
    }
  }, [isAdmin]);

  const changeStatus = async (ministry: MinistryRow) => {
    const suspend = ministry.status === 'ACTIVE';
    const message = suspend
        ? `[${ministry.name}] 모임을 정지하시겠습니까?\n멤버는 읽기만 가능하고 글쓰기·초대·푸시가 막힙니다.`
        : `[${ministry.name}] 모임의 정지를 해제하시겠습니까?`;
    if (!window.confirm(message)) {
      return;
    }
    try {
      const updated = await fetchChangeMinistryStatus(ministry.id, suspend);
      setMinistries(prev => prev.map(m => (m.id === updated.id ? updated : m)));
    } catch (error: any) {
      window.alert(error?.message ?? '처리에 실패했습니다.');
    }
  };

  if (!isAdmin) {
    return (
        <div className="px-4 sm:px-6 lg:px-8 py-16 text-center text-gray-500">
          관리자만 접근할 수 있습니다.
        </div>
    );
  }

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <h1 className="text-base font-semibold text-gray-900">모임 관리</h1>
        <p className="mt-2 text-sm text-gray-700">
          앱에서 만들어진 모임 목록입니다. 대기 신고가 쌓인 모임은 신고 관리에서 공지를 내리거나 모임을 정지할 수 있습니다.
        </p>

        <form
            className="mt-6 flex gap-2"
            onSubmit={e => {
              e.preventDefault();
              load(keyword);
            }}
        >
          <input
              value={keyword}
              onChange={e => setKeyword(e.target.value)}
              placeholder="모임 이름 또는 대표 이름"
              className="w-64 rounded-md border-0 px-3 py-1.5 text-sm text-gray-900 ring-1 ring-gray-300 focus:ring-2 focus:ring-indigo-600"
          />
          <button
              type="submit"
              className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-indigo-500"
          >
            검색
          </button>
        </form>

        {isLoading ? (
            <div className="flex justify-center items-center h-48 text-gray-500">로딩 중...</div>
        ) : ministries.length === 0 ? (
            <div className="py-16 text-center text-sm text-gray-500">모임이 없습니다.</div>
        ) : (
            <div className="mt-6 overflow-x-auto rounded-lg bg-white shadow-sm ring-1 ring-gray-200">
              <table className="min-w-full divide-y divide-gray-200 text-sm">
                <thead className="bg-gray-50 text-left text-xs font-semibold text-gray-600">
                <tr>
                  <th className="px-4 py-3">모임</th>
                  <th className="px-4 py-3">대표</th>
                  <th className="px-4 py-3 text-right">멤버</th>
                  <th className="px-4 py-3 text-right">사용량</th>
                  <th className="px-4 py-3">만든 날</th>
                  <th className="px-4 py-3 text-right">대기 신고</th>
                  <th className="px-4 py-3">상태</th>
                  <th className="px-4 py-3"></th>
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                {ministries.map(ministry => (
                    <tr key={ministry.id}>
                      <td className="px-4 py-3 font-medium text-gray-900">{ministry.name}</td>
                      <td className="px-4 py-3 text-gray-700">{ministry.ownerName || '-'}</td>
                      <td className="px-4 py-3 text-right text-gray-700">{ministry.memberCount}명</td>
                      <td className="px-4 py-3 text-right text-gray-700">
                        {formatBytes(ministry.storageUsedBytes)} / {formatBytes(ministry.storageLimitBytes)}
                      </td>
                      <td className="px-4 py-3 text-gray-500">
                        {ministry.createdAt ? new Date(ministry.createdAt).toLocaleDateString('ko-KR') : '-'}
                      </td>
                      <td className="px-4 py-3 text-right">
                        {ministry.pendingReports > 0 ? (
                            <a href="/manage/report" className="font-semibold text-red-600 hover:underline">
                              {ministry.pendingReports}건
                            </a>
                        ) : (
                            <span className="text-gray-400">-</span>
                        )}
                      </td>
                      <td className="px-4 py-3">
                        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                            ministry.status === 'ACTIVE'
                                ? 'bg-green-50 text-green-700'
                                : 'bg-red-50 text-red-700'
                        }`}>
                          {ministry.status === 'ACTIVE' ? '운영 중' : '정지됨'}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <button
                            onClick={() => changeStatus(ministry)}
                            className={ministry.status === 'ACTIVE'
                                ? 'rounded-md bg-red-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-red-500'
                                : 'rounded-md bg-white px-3 py-1.5 text-xs font-semibold text-gray-700 ring-1 ring-gray-300 hover:bg-gray-50'}
                        >
                          {ministry.status === 'ACTIVE' ? '정지' : '정지 해제'}
                        </button>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
        )}
      </div>
  );
}
