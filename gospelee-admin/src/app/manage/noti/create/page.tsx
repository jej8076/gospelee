"use client"

import {PhotoIcon} from '@heroicons/react/24/solid'
import {useEffect, useState} from "react";
import NextImage from 'next/image';
import useAuth from "~/lib/auth/check-auth";
import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {fetchInsertAnnouncement} from "~/lib/api/fetch-announcement";
import {useApiClient} from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import {isEmpty} from "@/utils/validators";
import {useRouter} from "next/navigation";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";

export default function CreateNoti() {
  useAuth();

  const [userName, setUserName] = useState("");
  const [subject, setSubject] = useState("");
  const [files, setFiles] = useState<File[] | []>([]);
  const [pushNotificationSendYn, setPushNotificationSendYn] = useState("");
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [isGoBackModalOpen, setIsGoBackModalOpen] = useState<boolean>(false);
  const [announcement, setAnnouncement] = useState<Announcement>();
  const [announcementText, setAnnouncementText] = useState<string>(''); // 마크다운 내용 저장
  const [blobFileMapping, setBlobFileMapping] = useState<{ [key: string]: string }>({}); // blob URL과 파일명 매핑

  const LINE = "  \n";

  interface ImageSize {
    width: number;
    height: number;
  }

  const {callApi} = useApiClient();
  const router = useRouter();

  const openModal = () => {
    if (pushNotificationSendYn === "") {
      alert("푸시 알림 발송 여부를 선택해주세요");
      return;
    }
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const openGoBackModal = () => {
    setIsGoBackModalOpen(true);
  };

  const closeGoBackModal = () => {
    setIsGoBackModalOpen(false);
  };

  const goBack = () => {
    router.push("/manage/noti");
    return;
  }

  const handleRadioChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setPushNotificationSendYn(event.target.value);
  };

  const handleMarkdownImage = (index: number, file: File) => {
    // ![대체 텍스트](이미지_URL_또는_경로 "선택적_제목")
    const blobUrl = URL.createObjectURL(file);
    const previewText = "preview-" + index;

    // blob URL과 파일명 매핑 저장
    setBlobFileMapping(prev => ({
      ...prev,
      [blobUrl]: file.name
    }));

    getImageDimensionsFromBlobUrl(blobUrl)
    .then(imageSize => {
      const markdownImage: string = `<img src="${blobUrl}" width="${imageSize.width}" height="${imageSize.height}" alt="${previewText}">`;
      setAnnouncementText(prevContent => prevContent + LINE + markdownImage);
    })
    .catch(error => console.error(error));

  };

  const getImageDimensionsFromBlobUrl: (blobUrl: string) => Promise<ImageSize> = (blobUrl: string) => {
    return new Promise((resolve, reject) => {

      const img = new Image();

      img.onload = () => {
        const width = img.width;
        const height = img.height;
        const imageSize: ImageSize = {width: width, height: height};
        resolve(imageSize);
      };

      img.onerror = (error) => {
        URL.revokeObjectURL(blobUrl); // 오류 발생 시에도 해제
        reject(new Error("Blob URL에서 이미지를 로드하는 중 오류가 발생했습니다."));
      };

      img.src = blobUrl; // blobUrl을 이미지 소스로 사용
    });
  }

  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setUserName(lastLoginInfo?.name ?? "");
  }, []);

  useDidMountEffect(() => {
    if (!isEmpty(announcement?.id)) {
      router.push("/manage/noti");
      return;
    }

    alert("실패");
  }, [announcement]);

  const insertAnnouncement = async () => {

    // 여러 파일 업로드 지원
    const inputData: any = {
      // TODO 임시로 고정 값을 넣음, 교회의 공지사항이 아닐 경우엔 가변적으로
      organizationType: "ECCLESIA",
      subject: subject || "공지사항", // subject가 비어있으면 기본값 설정
      text: announcementText,
      pushNotificationSendYn: pushNotificationSendYn,
    };

    // 파일이 있을 때만 추가
    if (files && files.length > 0) {
      inputData.files = files;
    }

    // blob 매핑이 있을 때만 추가
    if (Object.keys(blobFileMapping).length > 0) {
      inputData.blobFileMapping = blobFileMapping;
    }

    console.log('전송할 데이터:', inputData);

    await callApi(() => fetchInsertAnnouncement(inputData), setAnnouncement);
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = e.target.files;
    if (selectedFiles) {
      const fileArray = Array.from(selectedFiles);
      setFiles((prevFiles) => [...prevFiles, ...fileArray]);
    }
  };

  const removeFile = (index: number) => {
    setFiles((prevFiles) => prevFiles.filter((_, i) => i !== index));

    // 해당 파일과 관련된 blob URL 매핑도 제거
    const fileToRemove = files[index];
    if (fileToRemove) {
      setBlobFileMapping(prev => {
        const newMapping = {...prev};
        Object.keys(newMapping).forEach(blobUrl => {
          if (newMapping[blobUrl] === fileToRemove.name) {
            delete newMapping[blobUrl];
          }
        });
        return newMapping;
      });
    }
  };

  return (
      <div>
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

              {/*<div className="col-span-full">*/}
              {/*  <label htmlFor="subject" className="block text-sm/6 font-medium text-gray-900">*/}
              {/*    제목*/}
              {/*  </label>*/}
              {/*  <div className="mt-2">*/}
              {/*    <input*/}
              {/*        type="text"*/}
              {/*        id="subject"*/}
              {/*        onChange={(e) => setSubject(e.target.value)}*/}
              {/*        className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"*/}
              {/*        defaultValue={''}*/}
              {/*    />*/}
              {/*  </div>*/}
              {/*</div>*/}

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
                        multiple
                        accept="image/*"
                        onChange={handleFileChange}
                        className="sr-only"
                    />
                  </label>

                  {/* 업로드된 이미지들 */}
                  {files.map((file, index) => (
                      <div
                          key={index}
                          className="flex flex-col items-center justify-center space-y-2 relative">
                        <div
                            className="w-36 h-36 border rounded-lg overflow-hidden bg-gray-50 flex items-center justify-center relative">
                          <NextImage
                              src={URL.createObjectURL(file)}
                              alt={`preview-${index}`}
                              className="object-cover w-full h-full"
                              width={144}
                              height={144}
                          />
                          {/* 삭제 버튼 */}
                          <button
                              type="button"
                              onClick={() => removeFile(index)}
                              className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600"
                          >
                            ×
                          </button>
                        </div>
                        {/*<div className="flex space-x-2">*/}
                        {/*  <input*/}
                        {/*      type="button"*/}
                        {/*      value="적용"*/}
                        {/*      className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 cursor-pointer"*/}
                        {/*      onClick={() => handleMarkdownImage(index, file)}*/}
                        {/*  />*/}
                        {/*</div>*/}
                      </div>
                  ))}
                </div>
              </div>

              <div className="col-span-full">
                <label htmlFor="about" className="block text-sm/6 font-medium text-gray-900">
                  본문
                </label>
                <div className="mt-2">
                  {/*<MarkdownEditorField*/}
                  {/*    value={announcementText} // 기존에 저장된 내용이 있다면 전달*/}
                  {/*    onMarkdownChange={setAnnouncementText} // 에디터 내용 변경 시 상태 업데이트*/}
                  {/*/>*/}
                  <textarea
                      id="about"
                      name="about"
                      onChange={(e) => setAnnouncementText(e.target.value)}
                      rows={3}
                      className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
                      defaultValue={''}
                  />
                </div>
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
                <p className="mt-1 text-sm/6 text-gray-600">앱 푸시 알림을 발송할 지 선택해 주세요</p>
                <div className="mt-6 space-y-6">
                  <div className="flex items-center gap-x-3">
                    <input
                        id="push-everyone"
                        name="push-notifications"
                        type="radio"
                        value="Y" // 이 라디오 버튼이 선택될 때의 값
                        checked={pushNotificationSendYn === "Y"} // 현재 상태에 따라 체크 여부 결정
                        onChange={handleRadioChange} // 변경 이벤트 핸들러
                        className="relative size-4 appearance-none rounded-full border border-gray-300 bg-white before:absolute before:inset-1 before:rounded-full before:bg-white not-checked:before:hidden checked:border-indigo-600 checked:bg-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:border-gray-300 disabled:bg-gray-100 disabled:before:bg-gray-400 forced-colors:appearance-auto forced-colors:before:hidden"
                    />
                    <label htmlFor="push-everyone"
                           className="block text-sm/6 font-medium text-gray-900">
                      등록 교인 전체발송
                    </label>
                  </div>
                  <div className="flex items-center gap-x-3">
                    <input
                        id="push-nothing"
                        name="push-notifications"
                        type="radio"
                        value="N" // 이 라디오 버튼이 선택될 때의 값
                        checked={pushNotificationSendYn === "N"} // 현재 상태에 따라 체크 여부 결정
                        onChange={handleRadioChange} // 변경 이벤트 핸들러
                        className="relative size-4 appearance-none rounded-full border border-gray-300 bg-white before:absolute before:inset-1 before:rounded-full before:bg-white not-checked:before:hidden checked:border-indigo-600 checked:bg-indigo-600 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 disabled:border-gray-300 disabled:bg-gray-100 disabled:before:bg-gray-400 forced-colors:appearance-auto forced-colors:before:hidden"
                    />
                    <label htmlFor="push-nothing"
                           className="block text-sm/6 font-medium text-gray-900">
                      발송안함
                    </label>
                  </div>
                </div>
              </fieldset>
            </div>
          </div>
        </div>

        <div className="mt-6 mr-6 flex items-center justify-end gap-x-6">
          <input type="button" value="뒤로가기" className="text-sm/6 font-semibold text-gray-900"
                 onClick={() => openGoBackModal()}/>
          <input
              type="button"
              value="저장"
              className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 cursor-pointer"
              onClick={() => openModal()}
          />
        </div>
        <Modal
            isOpen={isModalOpen}
            onClose={() => closeModal()}
            title={"공지사항을 등록합니다."}
            footer={
              <>
                {grayButton("취소", closeModal)}
                {blueButton("확인", insertAnnouncement)}
              </>
            }
        >
          {
            <div></div>
          }
        </Modal>
        <Modal
            isOpen={isGoBackModalOpen}
            onClose={() => closeGoBackModal()}
            title={"공지사항 작성을 그만합니다."}
            footer={
              <>
                {grayButton("취소", closeGoBackModal)}
                {blueButton("확인", goBack)}
              </>
            }
        >
          {
            <div></div>
          }
        </Modal>
      </div>
  )
}
