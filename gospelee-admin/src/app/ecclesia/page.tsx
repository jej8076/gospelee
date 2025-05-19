"use client"

import {useEffect, useState} from "react";
import {getCookie} from "~/lib/cookie/cookie-utils";
import useAuth from "~/lib/auth/check-auth";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {ecclesiaStatusKor} from "@/enums/ecclesia/status";
import {ecclesiaStatusStyle} from "@/app/style/ecclesia/ecclesia-status";
import Modal from "@/components/modal/modal";
import {blueButton} from "@/components/modal/modal-buttons";
import StatusSelector from "@/components/ecclesia/status-selector";

export default function Ecclesia() {
  useAuth();

  // 데이터 목록
  const [eccList, setEccList] = useState<Ecclesia[]>([]);
  // modal 에 사용될 단일 데이터 저장 변수
  const [selectedEcclesia, setSelectedEcclesia] = useState<Ecclesia | null>(null);
  // modal open or close
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);

  useEffect(() => {
    fetchEcclesias();
  }, []);

  const fetchEcclesias = async () => {
    try {
      const response = await apiFetch(`/api/ecclesia/all`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `${AuthItems.Bearer}${await getCookie(AuthItems.Authorization)}`,
        },
      });

      const res: Ecclesia[] = await response.json();
      setEccList(res);
    } catch (e) {
      console.error("Error fetching users:", e);
    }
  }

  const updateEcclesiaStatus = (ecclesiaUid: bigint, newStatus: string) => {
    // 기존 목록에서 해당 교회를 찾아 상태만 업데이트
    const updatedList = eccList.map(ecc =>
        ecc.ecclesiaUid === ecclesiaUid
            ? {...ecc, status: newStatus}
            : ecc
    );

    // 업데이트된 목록으로 상태 갱신
    setEccList(updatedList);

    // 모달의 선택된 교회 정보도 업데이트
    if (selectedEcclesia && selectedEcclesia.ecclesiaUid === ecclesiaUid) {
      setSelectedEcclesia({...selectedEcclesia, status: newStatus});
    }
  };

  const openModal = (ecc: Ecclesia) => {
    setSelectedEcclesia(ecc);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-base font-semibold leading-6 text-gray-900">에베소서 2:19–22</h1>
            <p className="mt-2 text-sm text-gray-700">
              그러므로 이제부터 너희는 외인도 아니요 나그네도 아니요 오직 성도들과 동일한 시민이요 하나님의 권속이라
            </p>
            <p className="text-sm text-gray-700">
              너희는 사도들과 선지자들의 터 위에 세우심을 입은 자라 그리스도 예수께서 친히 모퉁잇돌이 되셨느니라
            </p>
            <p className="text-sm text-gray-700">
              그의 안에서 건물마다 서로 연결하여 주 안에서 성전이 되어 가고
              너희도 성령 안에서 하나님이 거하실 처소가 되기 위하여 그리스도 예수 안에서 함께 지어져 가느니라
            </p>
          </div>
          <div className="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
            {/*<button*/}
            {/*    type="button"*/}
            {/*    className="block rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"*/}
            {/*>*/}
            {/*  Add user*/}
            {/*</button>*/}
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
                {!(eccList.length > 0) ? null : eccList.map((ecc) => (
                    <tr key={ecc.ecclesiaUid}>
                      <td className="whitespace-nowrap py-5 pl-4 pr-3 text-sm sm:pl-0">
                        <div className="flex items-center">
                          <div className="h-11 w-11 flex-shrink-0">
                            <img
                                className="h-11 w-11 rounded-full"
                                src={ecc.image || '/images/ecclesia/default_church.jpg'}
                                alt=""/>
                          </div>
                          <div className="ml-4">
                            <div className="font-medium text-gray-900">{ecc.ecclesiaName}</div>
                            <div
                                className="mt-1 text-gray-500">{ecc.churchIdentificationNumber}</div>
                          </div>
                        </div>
                      </td>
                      {/*<td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">*/}
                      {/*  <div className="text-gray-900">{u.churchIdentificationNumber}</div>*/}
                      {/*  <div className="mt-1 text-gray-500">{u.name}</div>*/}
                      {/*</td>*/}
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">{ecc.masterAccountName}</td>
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">
                        <span
                            className={ecclesiaStatusStyle(ecc.status)}>
                          {ecclesiaStatusKor(ecc.status)}
                        </span>
                      </td>
                      <td className="relative whitespace-nowrap py-5 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                        <button
                            className="text-indigo-600 hover:text-indigo-900 cursor-pointer"
                            onClick={() => openModal(ecc)}
                        >Edit
                        </button>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <Modal
            isOpen={isModalOpen}
            onClose={() => closeModal()}
            title={selectedEcclesia?.ecclesiaName}
            footer={
              <>
                {/*{grayButton("취소", closeModal)}*/}
                {blueButton("확인", closeModal)}
              </>
            }
        >
          {selectedEcclesia && (
              <div>
                <p>관리자: {selectedEcclesia.masterAccountName}</p>
                <p>교회 식별 번호: {selectedEcclesia.churchIdentificationNumber}</p>
                <div className="ecclesia-detail">
                  {/* 버튼 형태의 상태 선택기 사용 */}
                  <StatusSelector
                      ecclesiaUid={selectedEcclesia.ecclesiaUid}
                      status={selectedEcclesia.status}
                      onStatusChange={(newStatus) => {
                        // 상태가 변경됐을 때 전체 목록 업데이트
                        updateEcclesiaStatus(selectedEcclesia.ecclesiaUid, newStatus);
                      }}
                  />
                </div>
              </div>
          )}
        </Modal>
      </div>
  );
}
