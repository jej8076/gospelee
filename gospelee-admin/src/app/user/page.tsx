"use client"
import {useEffect, useState} from "react";
import Image from 'next/image';
import useAuth from "~/lib/auth/check-auth";
import {useApiClient} from "@/hooks/useApiClient";
import {fetchUsers} from "~/lib/api/fetch-users";
import InviteModal from "@/components/modal/invite-modal";

type Users = {
  name: string,
  title: string,
  department: string,
  email: string,
  role: string,
  image: string
};

// const people = [
//   {
//     name: 'Lindsay Walton',
//     title: 'Front-end Developer',
//     department: 'Optimization',
//     email: 'lindsay.walton@example.com',
//     role: 'Member',
//     image:
//         'https://images.unsplash.com/photo-1517841905240-472988babdf9?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80',
//   }
// ]

export default function User() {
  useAuth();
  const {callApi} = useApiClient();
  const [user, setUsers] = useState<Users[]>([]);
  const [isInviteModalOpen, setIsInviteModalOpen] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const openInviteModal = () => {
    setIsInviteModalOpen(true);
  };

  const closeInviteModal = () => {
    setIsInviteModalOpen(false);
  };

  useEffect(() => {
    const loadUsers = async () => {
      try {
        await callApi(() => fetchUsers(), setUsers);
      } catch (error) {
        console.error('사용자 목록 로드 실패:', error);
      } finally {
        setIsLoading(false);
      }
    };

    loadUsers();
  }, []);

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
                          {/*<td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">*/}
                          {/*  <div className="text-gray-900">{u.title}</div>*/}
                          {/*  <div className="mt-1 text-gray-500">{u.department}</div>*/}
                          {/*</td>*/}
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

        <InviteModal
            isOpen={isInviteModalOpen}
            onClose={closeInviteModal}
        />

      </div>
  );
}
