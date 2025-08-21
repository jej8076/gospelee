"use client"

import {PhotoIcon} from '@heroicons/react/24/solid'
import {useEffect, useState} from "react";
import NextImage from 'next/image';
import useAuth from "~/lib/auth/check-auth";
import {getLastLoginOrElseNull} from "@/utils/user-utils";
import {fetchUpdateAnnouncement, fetchAnnouncementById} from "~/lib/api/fetch-announcement";
import {fetchFileById} from "~/lib/api/fetch-file";
import {useApiClient} from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import {isEmpty} from "@/utils/validators";
import {useRouter, useParams} from "next/navigation";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import MarkdownEditorField from '@/components/markdown/MarkdownEditorField';

// 타입 import 추가
type Announcement = {
  id?: bigint;
  organizationType: string;
  subject: string;
  text: string;
  fileUid?: bigint | null;
  fileDetailList?: {
    id: number;
    fileId: number;
    filePath: string;
    fileOriginalName: string;
    fileSize: number;
    fileType: string;
    extension: string;
    delYn: string;
  }[];
  pushNotificationSendYn: 'Y' | 'N';
  pushNotificationIds: string;
  insertTime: string;
  updateTime: string;
};

type AuthInfoType = {
  name: string;
  // 다른 필드들...
};

export default function EditNoti() {
  useAuth();

  const params = useParams();
  const announcementId = params.id as string;

  const [userName, setUserName] = useState("");
  const [subject, setSubject] = useState("");
  const [files, setFiles] = useState<File[] | []>([]);
  const [existingFiles, setExistingFiles] = useState<{url: string, name: string, id: number}[]>([]); // 기존 파일들
  const [pushNotificationSendYn, setPushNotificationSendYn] = useState("");
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [announcement, setAnnouncement] = useState<Announcement>();
  const [originalAnnouncement, setOriginalAnnouncement] = useState<Announcement>();
  const [announcementText, setAnnouncementText] = useState<string>(''); // 마크다운 내용 저장
  const [blobFileMapping, setBlobFileMapping] = useState<{ [key: string]: string }>({}); // blob URL과 파일명 매핑
  const [isLoading, setIsLoading] = useState(true);

  const LINE = "  \n";

  const TYPE = "ECCLESIA";

  interface ImageSize {
    width: number;
    height: number;
  }

  const {callApi} = useApiClient();
  const router = useRouter();

  const [formData, setFormData] = useState({
    subject: "",
    text: "",
    pushNotificationSendYn: "N"
  });

  useEffect(() => {
    const lastLoginInfo: AuthInfoType | null = getLastLoginOrElseNull();
    setUserName(lastLoginInfo?.name ?? "");
  }, []);

  // 기존 공지사항 데이터 로드
  useEffect(() => {
    if (announcementId) {
      loadAnnouncementData();
    }

  }, [announcementId]);

  useEffect(() => {
    if (originalAnnouncement) {
      setFormData({
        subject: originalAnnouncement.subject || "",
        text: originalAnnouncement.text || "",
        pushNotificationSendYn: originalAnnouncement.pushNotificationSendYn || "N"
      });
      
      // 기존 파일들 로드
      loadExistingFiles();
    }
  }, [originalAnnouncement]);


  useDidMountEffect(() => {
    if (!isEmpty(announcement?.id)) {
      router.push("/manage/story");
      return;
    }

    if (announcement === null) {
      alert("수정 실패");
    }
  }, [announcement]);


  const loadAnnouncementData = async () => {
    try {
      setIsLoading(true);
      await callApi(
          () => fetchAnnouncementById(TYPE, announcementId),
          (data: Announcement) => {
            setOriginalAnnouncement(data);
            // 한 번에 모든 필드 업데이트
            setFormData({
              subject: data.subject || "",
              text: data.text || "",
              pushNotificationSendYn: data.pushNotificationSendYn || "N"
            });
          }
      );
    } catch (error) {
      console.error("공지사항 로드 실패:", error);
      alert("공지사항을 불러오는데 실패했습니다.");
      router.push("/manage/story");
    } finally {
      setIsLoading(false);
    }
  };

  // 기존 파일들 로드
  const loadExistingFiles = async () => {
    if (!originalAnnouncement?.fileUid || !originalAnnouncement?.fileDetailList) {
      return;
    }

    try {
      const filePromises = originalAnnouncement.fileDetailList.map(async (fileDetail) => {
        try {
          const fileUrl = await fetchFileById(Number(originalAnnouncement.fileUid), fileDetail.id);
          return {
            url: fileUrl,
            name: fileDetail.fileOriginalName,
            id: fileDetail.id
          };
        } catch (error) {
          console.error(`파일 로드 실패 (ID: ${fileDetail.id}):`, error);
          return null;
        }
      });

      const loadedFiles = await Promise.all(filePromises);
      const validFiles = loadedFiles.filter(file => file !== null) as {url: string, name: string, id: number}[];
      setExistingFiles(validFiles);
    } catch (error) {
      console.error("기존 파일 로드 실패:", error);
    }
  };

  const openModal = () => {
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const updateAnnouncement = async () => {
    // 여러 파일 업로드 지원
    const inputData: any = {
      id: announcementId,
      organizationType: TYPE,
      subject: subject || "", // subject가 비어있으면 기본값 설정
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

    console.log('수정할 데이터:', inputData);

    await callApi(() => fetchUpdateAnnouncement(inputData), setAnnouncement);
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFiles = e.target.files;
    if (selectedFiles) {
      const fileArray = Array.from(selectedFiles);
      setFiles((prevFiles) => [...prevFiles, ...fileArray]);
    }
  };

  const handleRadioChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setPushNotificationSendYn(event.target.value);
  };

  // 개별 필드 업데이트 시
  const handleTextChange = (value: string) => {
    setFormData(prev => ({...prev, text: value}));
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

  // 기존 파일 제거
  const removeExistingFile = (index: number) => {
    const fileToRemove = existingFiles[index];
    if (fileToRemove) {
      // blob URL 해제
      URL.revokeObjectURL(fileToRemove.url);
    }
    setExistingFiles((prevFiles) => prevFiles.filter((_, i) => i !== index));
  };

  const handleCancel = () => {
    // 메모리 누수 방지를 위해 blob URL 해제
    existingFiles.forEach(file => {
      URL.revokeObjectURL(file.url);
    });
    router.push("/manage/noti");
  };

  // 컴포넌트 언마운트 시 blob URL 정리
  useEffect(() => {
    return () => {
      existingFiles.forEach(file => {
        URL.revokeObjectURL(file.url);
      });
    };
  }, [existingFiles]);

  if (isLoading) {
    return (
        <div className="flex justify-center items-center h-64">
          <div className="text-lg">로딩 중...</div>
        </div>
    );
  }

  return (
      <div>
        <div className="space-y-12 px-8">
          <div className="border-b border-gray-900/10 pb-12">
            <h2 className="text-base/7 font-semibold text-gray-900">공지사항 수정하기</h2>
            <p className="mt-1 text-sm/6 text-gray-600">
              스토리를 수정하는 공간입니다.
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

                  {/* 기존 업로드된 이미지들 */}
                  {existingFiles.map((file, index) => (
                      <div
                          key={`existing-${index}`}
                          className="flex flex-col items-center justify-center space-y-2 relative">
                        <div
                            className="w-36 h-36 border rounded-lg overflow-hidden bg-gray-50 flex items-center justify-center relative">
                          <NextImage
                              src={file.url}
                              alt={file.name}
                              className="object-cover w-full h-full"
                              width={144}
                              height={144}
                          />
                          {/* 삭제 버튼 */}
                          <button
                              type="button"
                              onClick={() => removeExistingFile(index)}
                              className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600"
                          >
                            ×
                          </button>
                        </div>
                        <span className="text-xs text-gray-500 text-center max-w-36 truncate">
                          {file.name}
                        </span>
                      </div>
                  ))}

                  {/* 새로 업로드된 이미지들 */}
                  {files.map((file, index) => (
                      <div
                          key={`new-${index}`}
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
                        <span className="text-xs text-gray-500 text-center max-w-36 truncate">
                          {file.name}
                        </span>
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
                      value={originalAnnouncement?.text}
                      onChange={(e) => handleTextChange(e.target.value)}
                      rows={3}
                      className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6 border border-gray-300"
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
          <input
              type="button"
              value="Cancel"
              className="text-sm/6 font-semibold text-gray-900 cursor-pointer hover:text-gray-700"
              onClick={handleCancel}
          />
          <input
              type="button"
              value="Update"
              className="rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 cursor-pointer"
              onClick={() => openModal()}
          />
        </div>
        <Modal
            isOpen={isModalOpen}
            onClose={() => closeModal()}
            title={"공지사항을 수정합니다."}
            footer={
              <>
                {grayButton("취소", closeModal)}
                {blueButton("확인", updateAnnouncement)}
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
