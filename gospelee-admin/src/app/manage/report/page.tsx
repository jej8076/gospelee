"use client"

import React, {useCallback, useEffect, useState, type JSX} from 'react';
import useAuth from "~/lib/auth/check-auth";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchContentReports, fetchResolveContentReport} from "~/lib/api/fetch-content-reports";
import {getLastLoginOrElseNull} from "@/utils/user-utils";

const STATUS_TABS = [
  {value: 'PENDING', label: '대기'},
  {value: 'RESOLVED', label: '조치 완료'},
  {value: 'DISMISSED', label: '기각'},
];

const REASON_LABELS: Record<string, string> = {
  SPAM: '스팸/광고',
  ABUSE: '욕설/비방',
  INAPPROPRIATE: '부적절한 내용',
  HERESY: '이단/잘못된 가르침',
  OTHER: '기타',
};

export default function ManageReport(): JSX.Element {
  useAuth();

  const {callApi} = useApiClient();
  const [status, setStatus] = useState('PENDING');
  const [reports, setReports] = useState<ContentReport[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(true);

  const load = useCallback(async (target: string) => {
    setIsLoading(true);
    try {
      await callApi(fetchContentReports, setReports, target);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    setIsAdmin(getLastLoginOrElseNull()?.role === 'ADMIN');
  }, []);

  useEffect(() => {
    if (isAdmin) {
      load(status);
    }
  }, [status, isAdmin]);

  const resolve = async (report: ContentReport, action: 'TAKEDOWN' | 'DISMISS') => {
    const message = action === 'TAKEDOWN'
        ? '이 콘텐츠를 내리시겠습니까? 같은 대상의 다른 신고도 함께 처리됩니다.'
        : '문제 없음으로 기각하시겠습니까?';
    if (!window.confirm(message)) {
      return;
    }
    try {
      await fetchResolveContentReport(report.uid, action);
      await load(status);
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
        <h1 className="text-base font-semibold text-gray-900">신고 관리</h1>
        <p className="mt-2 text-sm text-gray-700">묵상 나눔과 댓글에 접수된 신고를 검토합니다</p>

        <div className="mt-6 flex gap-2">
          {STATUS_TABS.map(tab => (
              <button
                  key={tab.value}
                  onClick={() => setStatus(tab.value)}
                  className={`rounded-md px-3 py-1.5 text-sm font-medium ${
                      status === tab.value
                          ? 'bg-indigo-600 text-white'
                          : 'bg-white text-gray-700 ring-1 ring-gray-300 hover:bg-gray-50'
                  }`}
              >
                {tab.label}
              </button>
          ))}
        </div>

        {isLoading ? (
            <div className="flex justify-center items-center h-48 text-gray-500">로딩 중...</div>
        ) : reports.length === 0 ? (
            <div className="py-16 text-center text-sm text-gray-500">신고가 없습니다.</div>
        ) : (
            <ul className="mt-6 space-y-4">
              {reports.map(report => (
                  <li key={report.uid} className="rounded-lg bg-white p-5 shadow-sm ring-1 ring-gray-200">
                    <div className="flex flex-wrap items-center gap-2 text-xs text-gray-500">
                      <span className="rounded-full bg-gray-100 px-2 py-0.5 font-medium text-gray-700">
                        {report.targetType === 'SHARE' ? '묵상 나눔' : '댓글'}
                      </span>
                      <span className="rounded-full bg-red-50 px-2 py-0.5 font-medium text-red-700">
                        {REASON_LABELS[report.reason] ?? report.reason}
                      </span>
                      {!report.targetActive && (
                          <span className="rounded-full bg-gray-100 px-2 py-0.5">이미 내려간 콘텐츠</span>
                      )}
                      <span>
                        {report.reportedAt ? new Date(report.reportedAt).toLocaleString('ko-KR') : ''}
                      </span>
                    </div>

                    <p className="mt-3 text-sm text-gray-900">
                      작성자 <b>{report.authorNickname}</b>
                      {report.reference ? ` · ${report.reference}` : ''}
                    </p>
                    <p className="mt-2 whitespace-pre-wrap rounded-md bg-gray-50 p-3 text-sm text-gray-800">
                      {report.content ?? '(내용을 찾을 수 없습니다)'}
                    </p>

                    <p className="mt-3 text-xs text-gray-500">
                      신고자 {report.reporterNickname}
                      {report.detail ? ` · ${report.detail}` : ''}
                    </p>

                    {report.status === 'PENDING' && (
                        <div className="mt-4 flex gap-2">
                          <button
                              onClick={() => resolve(report, 'TAKEDOWN')}
                              className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-semibold text-white hover:bg-red-500"
                          >
                            콘텐츠 내리기
                          </button>
                          <button
                              onClick={() => resolve(report, 'DISMISS')}
                              className="rounded-md bg-white px-3 py-1.5 text-sm font-semibold text-gray-700 ring-1 ring-gray-300 hover:bg-gray-50"
                          >
                            기각
                          </button>
                        </div>
                    )}
                  </li>
              ))}
            </ul>
        )}
      </div>
  );
}
