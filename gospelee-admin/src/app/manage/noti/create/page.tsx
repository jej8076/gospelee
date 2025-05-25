"use client"

import {PhotoIcon} from '@heroicons/react/24/solid'
import {useEffect, useState} from "react";
import useAuth from "~/lib/auth/check-auth";
import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {fetchInsertAnnouncement} from "~/lib/api/fetch-announcement";
import {useApiClient} from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import {isEmpty} from "@/utils/validators";
import {useRouter} from "next/navigation";

export default function CreateNoti() {
  useAuth();

  const [userName, setUserName] = useState("");
  const [subject, setSubject] = useState("");
  const [text, setText] = useState("");
  const [files, setFiles] = useState<File[] | []>([]);
  const [pushNotificationSendYn, setPushNotificationSendYn] = useState("");

  const [announcement, setAnnouncement] = useState<Announcement>();

  const {callApi} = useApiClient();
  const router = useRouter();

  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setUserName(lastLoginInfo?.name ?? "");
  }, []);

  useDidMountEffect(() => {
    debugger;
    if (!isEmpty(announcement?.id)) {
      router.push("/manage/noti");
      return;
    }

    alert("실패");
  }, [announcement]);

  const insertAnnouncement = async () => {

    // 예: 파일 input에서 선택된 파일
    // const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    // const file = fileInput?.files?.[0];

    const fileSet: File[] = files;

    /* 일단 file 1개만 개발됨 */
    const inputData = {
      // TODO 임시로 고정 값을 넣음, 교회의 공지사항이 아닐 경우엔 가변적으로
      organizationType: "ECCLESIA",
      subject: subject,
      text: text,
      file: files[0],
      pushNotificationSendYn: "Y",
    };

    await callApi(() => fetchInsertAnnouncement(inputData), setAnnouncement);
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      setFiles((prevFiles) => [...prevFiles, selectedFile]);
    }
  };

  return (
      <form>
        <div className="space-y-12 px-8">
          <div className="border-b border-gray-900/10 pb-12">
            <h2 className="text-base/7 font-semibold text-gray-900">공지사항 새로만들기</h2>
            <p className="mt-1 text-sm/6 text-gray-600">
              공지사항을 새로 만드는 공간입니다.
            </p>

            <div className="mt-10 grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
              <div className="sm:col-span-4">
                <label htmlFor="username" className="block text-sm/6 font-medium text-gray-900">
                  작성자
                </label>
                <div className="mt-2">
                  <div
                      className="flex items-center rounded-md bg-white pl-3 outline-1 -outline-offset-1 outline-gray-300 focus-within:outline-2 focus-within:-outline-offset-2 focus-within:outline-indigo-600">
                    <div
                        className="shrink-0 text-base text-gray-500 select-none sm:text-sm/6">{userName}
                    </div>
                    <input
                        id="username"
                        name="username"
                        type="text"
                        className="block min-w-0 grow py-1.5 pr-3 pl-1 text-base text-gray-900 placeholder:text-gray-400 focus:outline-none sm:text-sm/6"
                    />
                  </div>
                </div>
              </div>

              <div className="col-span-full">
                <label htmlFor="subject" className="block text-sm/6 font-medium text-gray-900">
                  제목
                </label>
                <div className="mt-2">
                  <input
                      type="text"
                      id="subject"
                      onChange={(e) => setSubject(e.target.value)}
                      className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
                      defaultValue={''}
                  />
                </div>
              </div>

              <div className="col-span-full">
                <label htmlFor="cover-photo" className="block text-sm font-medium text-gray-900">
                  Cover photo
                </label>

                <div className="mt-2 flex flex-wrap gap-4 px-6 py-4">
                  {/* 업로드 버튼 */}
                  <label
                      htmlFor="file-upload"
                      className="cursor-pointer text-center border border-dashed border-gray-300 rounded-lg p-6 hover:border-gray-400 w-36 h-36 flex flex-col justify-center items-center"
                  >
                    <PhotoIcon aria-hidden="true" className="mx-auto size-12 text-gray-300"/>
                    <span
                        className="mt-2 block text-sm font-semibold text-indigo-600 hover:text-indigo-500">
                      파일 업로드
                    </span>
                    <input
                        id="file-upload"
                        name="file-upload"
                        type="file"
                        onChange={handleFileChange}
                        className="sr-only"
                    />
                  </label>

                  {/* 업로드된 이미지들 */}
                  {files.map((file, index) => (
                      <div
                          key={index}
                          className="w-36 h-36 border rounded-lg overflow-hidden bg-gray-50 flex items-center justify-center"
                      >
                        <img
                            src={URL.createObjectURL(file)}
                            alt={`preview-${index}`}
                            className="object-cover w-full h-full"
                        />
                      </div>
                  ))}
                </div>
              </div>

              <div className="col-span-full">
                <label htmlFor="about" className="block text-sm/6 font-medium text-gray-900">
                  본문
                </label>
                <div className="mt-2">
                <textarea
                    id="about"
                    name="about"
                    onChange={(e) => setText(e.target.value)}
                    rows={3}
                    className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
                    defaultValue={''}
                />
                </div>
                {/*<p className="mt-3 text-sm/6 text-gray-600">Write a few sentences about*/}
                {/*  yourself.</p>*/}
              </div>

            </div>
          </div>

          <div className="border-b border-gray-900/10 pb-12">
            <h2 className="text-base/7 font-semibold text-gray-900">설정</h2>
            <p className="mt-1 text-sm/6 text-gray-600">

            </p>

            <div className="mt-10 space-y-10">

              <fieldset>
                <legend className="text-sm/6 font-semibold text-gray-900">푸시 알림
                </legend>
                <p className="mt-1 text-sm/6 text-gray-600">IOS/Android 앱에 푸시알림을 발송합니다.</p>
                <div className="mt-6 space-y-6">
                  <div className="flex items-center gap-x-3">
                    <input
                        defaultChecked
                        id="push-everything"
                        name="push-notifications"
                        type="radio"
                        className="relative size-4 appearance-none rounded-full border border-gray-300 bg-white before:absolute before:inset-1 before:rounded-full before:bg-white not-checked:before:hidden checked:border-indigo-600 checked:bg-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:border-gray-300 disabled:bg-gray-100 disabled:before:bg-gray-400 forced-colors:appearance-auto forced-colors:before:hidden"
                    />
                    <label htmlFor="push-everything"
                           className="block text-sm/6 font-medium text-gray-900">
                      Everything
                    </label>
                  </div>
                  <div className="flex items-center gap-x-3">
                    <input
                        id="push-email"
                        name="push-notifications"
                        type="radio"
                        className="relative size-4 appearance-none rounded-full border border-gray-300 bg-white before:absolute before:inset-1 before:rounded-full before:bg-white not-checked:before:hidden checked:border-indigo-600 checked:bg-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:border-gray-300 disabled:bg-gray-100 disabled:before:bg-gray-400 forced-colors:appearance-auto forced-colors:before:hidden"
                    />
                    <label htmlFor="push-email"
                           className="block text-sm/6 font-medium text-gray-900">
                      Same as email
                    </label>
                  </div>
                  <div className="flex items-center gap-x-3">
                    <input
                        id="push-nothing"
                        name="push-notifications"
                        type="radio"
                        className="relative size-4 appearance-none rounded-full border border-gray-300 bg-white before:absolute before:inset-1 before:rounded-full before:bg-white not-checked:before:hidden checked:border-indigo-600 checked:bg-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:border-gray-300 disabled:bg-gray-100 disabled:before:bg-gray-400 forced-colors:appearance-auto forced-colors:before:hidden"
                    />
                    <label htmlFor="push-nothing"
                           className="block text-sm/6 font-medium text-gray-900">
                      No push notifications
                    </label>
                  </div>
                </div>
              </fieldset>
            </div>
          </div>
        </div>

        <div className="mt-6 flex items-center justify-end gap-x-6">
          <input type="button" value="Cancel" className="text-sm/6 font-semibold text-gray-900"/>
          <input
              type="button"
              value="Save"
              className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
              onClick={() => insertAnnouncement()}
          />
        </div>
      </form>
  )
}
