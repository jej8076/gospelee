'use client'

import React, {useState} from 'react'
import {Field, Label, Switch} from '@headlessui/react'
import {useApiClient} from "@/hooks/useApiClient";
import {fetchInsertEcclesia} from "~/lib/api/fetch-ecclesias";
import {isEmpty} from "@/utils/validators";
import {useRouter} from "next/navigation";
import useDidMountEffect, {useOnMountEffect} from "@/hooks/useDidMountEffect";
import {getLastLoginOrElseNull, logout} from "@/utils/user-utils";
import {
  BuildingOfficeIcon,
  DocumentCheckIcon,
  PhoneIcon,
  UserIcon
} from '@heroicons/react/24/outline';

export default function ApplyChurch() {
  const router = useRouter();
  const {callApi} = useApiClient();

  const [ecclesia, setEcclesia] = useState<Ecclesia>();
  const [managerName, setManagerName] = useState("");
  const [churchName, setChurchName] = useState("");
  const [churchIdentificationNumber, setChurchIdentificationNumber] = useState("");
  const [telephone, setTelephone] = useState("");
  const [agreed, setAgreed] = useState(false);

  // 전화번호 입력 핸들러 (숫자만 허용)
  const handleTelephoneChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // 숫자와 하이픈만 허용
    const numericValue = value.replace(/[^0-9-]/g, '');
    setTelephone(numericValue);
  };

  const handleLogout = async () => {
    await logout(router);
  };

  // 페이지 진입 시 동작
  useOnMountEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();

    if (!isEmpty(lastLoginInfo?.ecclesiaUid)) {
      console.log("ecclesiaUid가 존재함, 페이지 이동:", ecclesia?.uid);
      router.push("/ecclesia/wait");
      return;
    }

    setManagerName(lastLoginInfo?.name ?? "");

  });

  // 신청 버튼을 눌렀을 때 이동하기 위함
  useDidMountEffect(() => {
    console.log("useDidMountEffect 실행됨, ecclesia:", ecclesia);

    if (!isEmpty(ecclesia?.uid)) {
      console.log("ecclesiaUid가 존재함, 페이지 이동:", ecclesia?.uid);
      router.push("/ecclesia/wait");
      return;
    }

    console.log("ecclesiaUid가 없음 또는 비어있음");
  }, [ecclesia]);

  const insertEcclesia = async () => {
    if (!agreed) {
      alert("개인정보 보호 정책에 동의해주세요.");
      return;
    }

    if (isEmpty(churchName)) {
      alert("교회 이름을 입력해주세요.");
      return;
    }

    console.log("신청 시작...");

    // 전화번호에서 하이픈 제거
    const cleanedTelephone = telephone.replace(/-/g, '');

    const inputData = {
      name: churchName,
      churchIdentificationNumber: churchIdentificationNumber,
      telephone: cleanedTelephone,
    };

    console.log("전송할 데이터:", inputData);

    try {
      await callApi(
          () => fetchInsertEcclesia(inputData),
          (data) => {
            console.log("API 응답 데이터:", data);
            setEcclesia(data);
            console.log("setEcclesia 호출 완료");
          }
      );
    } catch (error) {
      console.error("신청 중 오류 발생:", error);
      alert("신청 중 오류가 발생했습니다. 다시 시도해주세요.");
    }
  }

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
                    <DocumentCheckIcon className="w-10 h-10 text-white"/>
                  </div>
                </div>
              </div>
              <h1 className="text-4xl font-bold text-gray-900 sm:text-5xl mb-4">
                교회 신청
              </h1>
              <p className="text-xl text-gray-600 max-w-2xl mx-auto">
                교회 정보를 입력하여 서비스 이용을 신청해주세요.<br/>
                관리자 승인 후 서비스를 이용하실 수 있습니다.
              </p>
            </div>

            {/* Application Form Card */}
            <div
                className="bg-white rounded-2xl shadow-xl border border-gray-200 overflow-hidden mb-8">
              <div className="bg-gray-800 px-6 py-4">
                <h2 className="text-xl font-semibold text-white flex items-center">
                  <DocumentCheckIcon className="w-6 h-6 mr-2"/>
                  신청 정보 입력
                </h2>
              </div>

              <div className="p-8 space-y-8">
                {/* 담당 교역자 */}
                <div className="flex items-start space-x-4">
                  <div className="flex-shrink-0">
                    <div
                        className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                      <UserIcon className="w-6 h-6 text-gray-700"/>
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <label htmlFor="manager-name"
                           className="block text-lg font-semibold text-gray-900 mb-2">
                      담당 교역자
                    </label>
                    <input
                        id="manager-name"
                        name="manager-name"
                        type="text"
                        value={managerName}
                        onChange={(e) => setManagerName(e.target.value)}
                        autoComplete="organization"
                        readOnly={true}
                        className="block w-full rounded-lg bg-gray-50 px-4 py-3 text-base text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                {/* 교회 이름 */}
                <div className="flex items-start space-x-4">
                  <div className="flex-shrink-0">
                    <div
                        className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                      <BuildingOfficeIcon className="w-6 h-6 text-gray-700"/>
                    </div>
                  </div>
                  <div className="flex-1 min-w-0">
                    <label htmlFor="church-name"
                           className="block text-lg font-semibold text-gray-900 mb-2">
                      교회 이름
                    </label>
                    <input
                        id="church-name"
                        name="church-name"
                        type="text"
                        value={churchName}
                        onChange={(e) => setChurchName(e.target.value)}
                        autoComplete="organization"
                        placeholder="교회 이름을 입력해주세요"
                        className="block w-full rounded-lg bg-white px-4 py-3 text-base text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
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
                    <label htmlFor="telephone"
                           className="block text-lg font-semibold text-gray-900 mb-2">
                      교회 전화번호
                    </label>
                    <input
                        id="telephone"
                        name="telephone"
                        type="tel"
                        value={telephone}
                        onChange={handleTelephoneChange}
                        placeholder="010-1234-5678"
                        autoComplete="tel"
                        className="block w-full rounded-lg bg-white px-4 py-3 text-base text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                </div>

                {/* 개인정보 동의 */}
                <div className="border-t border-gray-200 pt-8">
                  <Field className="flex items-start space-x-4">
                    <div className="flex-shrink-0 pt-1">
                      <Switch
                          checked={agreed}
                          onChange={setAgreed}
                          className={`group flex w-11 h-6 flex-none cursor-pointer rounded-full p-1 ring-1 ring-gray-900/5 transition-colors duration-200 ease-in-out ring-inset focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 ${
                              agreed ? 'bg-blue-600' : 'bg-gray-200'
                          }`}
                      >
                        <span className="sr-only">Agree to policies</span>
                        <span
                            aria-hidden="true"
                            className={`size-4 transform rounded-full bg-white ring-1 shadow-sm ring-gray-900/5 transition duration-200 ease-in-out ${
                                agreed ? 'translate-x-5' : 'translate-x-0'
                            }`}
                        />
                      </Switch>
                    </div>
                    <div className="flex-1">
                      <Label className="text-base text-gray-700 leading-relaxed">
                        개인정보 보호 정책에{' '}
                        <a href="#" className="font-semibold text-blue-600 hover:text-blue-700">
                          동의합니다
                        </a>
                        .
                      </Label>
                      <p className="text-sm text-gray-500 mt-1">
                        서비스 이용을 위해 개인정보 수집 및 이용에 동의해주세요.
                      </p>
                    </div>
                  </Field>
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex justify-center">
              <button
                  type="button"
                  disabled={!agreed}
                  onClick={() => insertEcclesia()}
                  className={`px-12 py-4 text-lg font-semibold rounded-lg shadow-lg focus:outline-none focus:ring-2 focus:ring-offset-2 transition-all duration-200 ${
                      agreed
                          ? 'bg-blue-600 text-white hover:bg-blue-700 focus:ring-blue-500 transform hover:scale-105'
                          : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  }`}
              >
                교회 신청하기
              </button>
              <button
                  type="button"
                  onClick={handleLogout}
                  className="px-8 py-3 ml-8 bg-gray-600 text-white font-semibold rounded-lg shadow-lg hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors duration-200"
              >
                로그아웃
              </button>

            </div>

            {/* Help Text */}
            <div className="text-center mt-12">
              <p className="text-gray-500 text-sm">
                신청 후 관리자 승인까지 1-2일 정도 소요될 수 있습니다.<br/>
                문의사항이 있으시면 관리자에게 연락해 주세요.
              </p>
            </div>
          </div>
        </div>
      </div>
  )
}
