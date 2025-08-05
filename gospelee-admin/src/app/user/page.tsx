"use client"
import {useEffect, useState} from "react";
import Image from 'next/image';
import useAuth from "~/lib/auth/check-auth";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchUsers} from "~/lib/api/fetch-users";
import {fetchEcclesiaRequests} from "~/lib/api/fetch-ecclesia-requests";
import InviteModal from "@/components/modal/invite-modal";

type Users = {
  name: string,
  title: string,
  department: string,
  email: string,
  role: string,
  image: string
};

type TabType = 'users' | 'requests';

export default function User() {
  useAuth();
  const {callApi} = useApiClient();
  const [user, setUsers] = useState<Users[]>([]);
  const [joinRequests, setJoinRequests] = useState<AccountEcclesiaRequest[]>([]);
  const [isInviteModalOpen, setIsInviteModalOpen] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [activeTab, setActiveTab] = useState<TabType>('users');

  const openInviteModal = () => {
    setIsInviteModalOpen(true);
  };

  const closeInviteModal = () => {
    setIsInviteModalOpen(false);
  };

  const handleApproveRequest = async (requestId: string) => {
    // TODO: API 호출로 가입 요청 승인
    console.log('승인:', requestId);
  };

  const handleRejectRequest = async (requestId: string) => {
    // TODO: API 호출로 가입 요청 거절
    console.log('거절:', requestId);
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        // 사용자 목록 로드
        await callApi(() => fetchUsers(), setUsers);

        // 가입 요청 목록 로드
        await callApi(() => fetchEcclesiaRequests(), setJoinRequests);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  const tabs = [
    {id: 'users', name: '사용자 목록', count: user.length},
    {id: 'requests', name: '가입 요청', count: joinRequests.length}
  ];

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-base font-semibold leading-6 text-gray-900">갈라디아서 3장 28절</h1>
            <p className="mt-2 text-sm text-gray-700">
              너희는 유대인이나 헬라인이나 종이나 자유인이나 남자나 여자나 다 그리스도 예수 안에서 하나이니라
            </p>
          </div>
          <div className="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
            <button
                type="button"
                className="block rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
                onClick={() => openInviteModal()}
            >
              초대하기
            </button>
          </div>
        </div>

        {/* 탭 네비게이션 */}
        <div className="mt-6 border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id as TabType)}
                    className={`whitespace-nowrap border-b-2 py-2 px-1 text-sm font-medium ${
                        activeTab === tab.id
                            ? 'border-indigo-500 text-indigo-600'
                            : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
                    }`}
                >
                  {tab.name}
                  <span className={`ml-2 rounded-full px-2.5 py-0.5 text-xs ${
                      activeTab === tab.id
                          ? 'bg-indigo-100 text-indigo-600'
                          : 'bg-gray-100 text-gray-900'
                  }`}>
                  {tab.count}
                </span>
                </button>
            ))}
          </nav>
        </div>

        {/* 사용자 목록 탭 */}
        {activeTab === 'users' && (
            <div className="mt-8 flow-root">
              <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
                  <table className="min-w-full divide-y divide-gray-300">
                    <thead>
                    <tr>
                      <th scope="col"
                          className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-0">
                        이름
                      </th>
                      <th scope="col"
                          className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                        상태
                      </th>
                      <th scope="col"
                          className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                        권한
                      </th>
                      <th scope="col" className="relative py-3.5 pl-3 pr-4 sm:pr-0">
                        <span className="sr-only">Edit</span>
                      </th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                    {isLoading ? (
                        <tr>
                          <td colSpan={4} className="text-center py-4">
                            로딩 중...
                          </td>
                        </tr>
                    ) : user.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="text-center py-4">
                            사용자가 없습니다.
                          </td>
                        </tr>
                    ) : (
                        user.map((u) => (
                            <tr key={u.email}>
                              <td className="whitespace-nowrap py-5 pl-4 pr-3 text-sm sm:pl-0">
                                <div className="flex items-center">
                                  <div className="h-11 w-11 flex-shrink-0">
                                    <Image
                                        className="h-11 w-11 rounded-full"
                                        src={u.image || '/images/users/default_user.jpg'}
                                        alt=""
                                        width={44}
                                        height={44}
                                    />
                                  </div>
                                  <div className="ml-4">
                                    <div className="font-medium text-gray-900">{u.name}</div>
                                    <div className="mt-1 text-gray-500">{u.email}</div>
                                  </div>
                                </div>
                              </td>
                              <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">
                        <span
                            className="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">
                          Active
                        </span>
                              </td>
                              <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">{u.role}</td>
                              <td className="relative whitespace-nowrap py-5 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                                <a href="#" className="text-indigo-600 hover:text-indigo-900">
                                  Edit<span className="sr-only">, {u.name}</span>
                                </a>
                              </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
        )}

        {/* 가입 요청 탭 */}
        {activeTab === 'requests' && (
            <div className="mt-8 flow-root">
              <div className="-mx-4 -my-2 overflow-x-auto sm:-mx-6 lg:-mx-8">
                <div className="inline-block min-w-full py-2 align-middle sm:px-6 lg:px-8">
                  <table className="min-w-full divide-y divide-gray-300">
                    <thead>
                    <tr>
                      <th scope="col"
                          className="py-3.5 pl-4 pr-3 text-left text-sm font-semibold text-gray-900 sm:pl-0">
                        신청자
                      </th>
                      <th scope="col"
                          className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                        신청일
                      </th>
                      <th scope="col" className="relative py-3.5 pl-3 pr-4 sm:pr-0">

                      </th>
                    </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-200 bg-white">
                    {isLoading ? (
                        <tr>
                          <td colSpan={4} className="text-center py-4">
                            로딩 중...
                          </td>
                        </tr>
                    ) : joinRequests.length === 0 ? (
                        <tr>
                          <td colSpan={4} className="text-center py-4">
                            가입 요청이 없습니다.
                          </td>
                        </tr>
                    ) : (
                        joinRequests.map((request) => (
                            <tr key={request.accountUid}>
                              <td className="whitespace-nowrap py-5 pl-4 pr-3 text-sm sm:pl-0">
                                <div className="flex items-center">
                                  <div className="h-11 w-11 flex-shrink-0">
                                    <div
                                        className="h-11 w-11 rounded-full bg-gray-300 flex items-center justify-center">
                                    <span className="text-sm font-medium text-gray-700">
                                      {request.name.charAt(0)}
                                    </span>
                                    </div>
                                  </div>
                                  <div className="ml-4">
                                    <div className="font-medium text-gray-900">{request.name}</div>
                                    <div className="mt-1 text-gray-500">{request.email}</div>
                                  </div>
                                </div>
                              </td>
                              <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">
                                {request.insertTime}
                              </td>
                              <td className="relative whitespace-nowrap py-5 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                                <div className="flex justify-start space-x-2">
                                  <button
                                      onClick={() => handleApproveRequest(request.accountUid)}
                                      className="inline-flex items-center rounded-md bg-green-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-green-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-green-600"
                                  >
                                    승인
                                  </button>
                                  <button
                                      onClick={() => handleRejectRequest(request.accountUid)}
                                      className="inline-flex items-center rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-red-600"
                                  >
                                    거절
                                  </button>
                                </div>
                              </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
        )}

        <InviteModal
            isOpen={isInviteModalOpen}
            onClose={closeInviteModal}
        />

      </div>
  );
}
