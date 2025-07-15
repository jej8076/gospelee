'use client'

import React, {useState} from 'react'
import {Field, Label, Switch} from '@headlessui/react'
import {useApiClient} from "@/hooks/useApiClient";
import {fetchInsertEcclesia} from "~/lib/api/fetch-ecclesias";
import {isEmpty} from "@/utils/validators";
import {useRouter} from "next/navigation";
import useDidMountEffect, {useOnMountEffect} from "@/hooks/useDidMountEffect";
import {getLastLoginOrElseNull} from "@/utils/user-utils";

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

  // 페이지 진입 시 동작
  useOnMountEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
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
          <h2 className="text-4xl font-semibold tracking-tight text-balance text-gray-900 sm:text-5xl">Church
            Apply</h2>
          <p className="mt-2 text-lg/8 text-gray-600">교회 정보를 입력해주세요</p>
        </div>
        <div className="mx-auto mt-16 max-w-xl sm:mt-20">
          <div className="grid grid-cols-1 gap-x-8 gap-y-6 sm:grid-cols-2">
            <div className="sm:col-span-2">
              <label htmlFor="manager-name" className="block text-sm/6 font-semibold text-gray-900">
                담당 교역자
              </label>
              <div className="mt-2.5">
                <input
                    id="manager-name"
                    name="manager-name"
                    type="text"
                    value={managerName}
                    onChange={(e) => setManagerName(e.target.value)}
                    autoComplete="organization"
                    readOnly={true}
                    className="block w-full rounded-md bg-white px-3.5 py-2 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 border border-gray-300 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600"
                />
              </div>
            </div>
            <div className="sm:col-span-2">
              <label htmlFor="church-name" className="block text-sm/6 font-semibold text-gray-900">
                교회이름
              </label>
              <div className="mt-2.5">
                <input
                    id="church-name"
                    name="church-name"
                    type="text"
                    value={churchName}
                    onChange={(e) => setChurchName(e.target.value)}
                    autoComplete="organization"
                    className="block w-full rounded-md bg-white px-3.5 py-2 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 border border-gray-300 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600"
                />
              </div>
            </div>
            <div className="sm:col-span-2">
              {/*<label htmlFor="identification-number"*/}
              {/*       className="block text-sm/6 font-semibold text-gray-900">*/}
              {/*  교회 고유번호*/}
              {/*</label>*/}
              {/*<div className="mt-2.5">*/}
              {/*  <div*/}
              {/*      className="flex rounded-md bg-white outline-1 -outline-offset-1 outline-gray-300 has-[input:focus-within]:outline-2 has-[input:focus-within]:-outline-offset-2 has-[input:focus-within]:outline-indigo-600">*/}
              {/*    <input*/}
              {/*        id="identification-number"*/}
              {/*        name="identification-number"*/}
              {/*        type="text"*/}
              {/*        value={churchIdentificationNumber}*/}
              {/*        onChange={(e) => setChurchIdentificationNumber(e.target.value)}*/}
              {/*        autoComplete="organization"*/}
              {/*        className="block w-full rounded-md bg-white px-3.5 py-2 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 border border-gray-300 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600"*/}
              {/*    />*/}
              {/*  </div>*/}
              {/*</div>*/}
              <label htmlFor="identification-number"
                     className="block text-sm/6 font-semibold text-gray-900">
                교회 전화번호
              </label>
              <div className="mt-2.5">
                <div
                    className="flex rounded-md bg-white outline-1 -outline-offset-1 outline-gray-300 has-[input:focus-within]:outline-2 has-[input:focus-within]:-outline-offset-2 has-[input:focus-within]:outline-indigo-600">
                  <input
                      id="telephone"
                      name="telephone"
                      type="tel"
                      value={telephone}
                      onChange={handleTelephoneChange}
                      placeholder="010-1234-5678"
                      autoComplete="tel"
                      className="block w-full rounded-md bg-white px-3.5 py-2 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 border border-gray-300 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600"
                  />
                </div>
              </div>
            </div>
            {/*<div className="sm:col-span-2">*/}
            {/*  <label htmlFor="message" className="block text-sm/6 font-semibold text-gray-900">*/}
            {/*    Message*/}
            {/*  </label>*/}
            {/*  <div className="mt-2.5">*/}
            {/*  <textarea*/}
            {/*      id="message"*/}
            {/*      name="message"*/}
            {/*      rows={4}*/}
            {/*      className="block w-full rounded-md bg-white px-3.5 py-2 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 border border-gray-300"*/}
            {/*      defaultValue={''}*/}
            {/*  />*/}
            {/*  </div>*/}
            {/*</div>*/}
            <Field className="flex gap-x-4 sm:col-span-2">
              <div className="flex h-6 items-center">
                <Switch
                    checked={agreed}
                    onChange={setAgreed}
                    className={`group flex w-8 flex-none cursor-pointer rounded-full p-px ring-1 ring-gray-900/5 transition-colors duration-200 ease-in-out ring-inset focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 ${
                        agreed ? 'bg-indigo-600' : 'bg-gray-200'
                    }`}
                >
                  <span className="sr-only">Agree to policies</span>
                  <span
                      aria-hidden="true"
                      className={`size-4 transform rounded-full bg-white ring-1 shadow-xs ring-gray-900/5 transition duration-200 ease-in-out ${
                          agreed ? 'translate-x-3.5' : 'translate-x-0'
                      }`}
                  />
                </Switch>
              </div>
              <Label className="text-sm/6 text-gray-600">
                개인정보 보호 정책에{' '}
                <a href="#" className="font-semibold text-indigo-600">
                  동의합니다
                </a>
                .
              </Label>
            </Field>
          </div>
          <div className="mt-10">
            <input
                type="button"
                value="신청"
                disabled={!agreed}
                className={`block w-full rounded-md px-3.5 py-2.5 text-center text-sm font-semibold shadow-xs focus-visible:outline-2 focus-visible:outline-offset-2 transition-colors ${
                    agreed
                        ? 'bg-indigo-600 text-white hover:bg-indigo-500 focus-visible:outline-indigo-600 cursor-pointer'
                        : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                }`}
                onClick={() => insertEcclesia()}
            >
            </input>
          </div>
        </div>
      </div>
  )
}
