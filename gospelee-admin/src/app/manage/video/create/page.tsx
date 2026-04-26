"use client"

import {useEffect, useState} from "react";
import useAuth from "~/lib/auth/check-auth";
import {fetchCreateYoutubeVideo} from "~/lib/api/fetch-youtube";
import {useApiClient} from "@/hooks/useApiClient";
import useDidMountEffect from "@/hooks/useDidMountEffect";
import {isEmpty} from "@/utils/validators";
import {useRouter} from "next/navigation";
import Modal from "@/components/modal/modal";
import {blueButton, grayButton} from "@/components/modal/modal-buttons";
import FormLayout from "@/components/manage/FormLayout";
import FormField from "@/components/manage/FormField";

export default function CreateVideo() {
  useAuth();

  const router = useRouter();
  const {callApi} = useApiClient();

  const [videoId, setVideoId] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [thumbnailUrl, setThumbnailUrl] = useState("");
  const [channelTitle, setChannelTitle] = useState("");
  const [publishedAt, setPublishedAt] = useState("");
  const [sortOrder, setSortOrder] = useState(0);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [createdVideo, setCreatedVideo] = useState<YoutubeVideo>();

  // 썸네일 URL 자동 생성
  useEffect(() => {
    if (videoId) {
      setThumbnailUrl(`https://img.youtube.com/vi/${videoId}/hqdefault.jpg`);
    }
  }, [videoId]);

  useDidMountEffect(() => {
    if (!isEmpty(createdVideo?.id)) {
      router.push("/manage/video");
      return;
    }
    alert("실패");
  }, [createdVideo]);

  const handleSubmit = () => {
    if (!videoId.trim() || !title.trim()) {
      alert("Video ID와 제목은 필수입니다.");
      return;
    }
    setIsModalOpen(true);
  };

  const handleCancel = () => {
    router.push("/manage/video");
  };

  const insertVideo = async () => {
    const inputData: Partial<YoutubeVideo> = {
      videoId,
      title,
      description,
      thumbnailUrl,
      channelTitle,
      publishedAt,
      isActive: true,
      sortOrder,
    };

    await callApi(() => fetchCreateYoutubeVideo(inputData), setCreatedVideo);
  };

  return (
      <>
        <FormLayout
            title="영상 새로만들기"
            description="YouTube 영상 정보를 등록합니다."
            onCancel={handleCancel}
            onSubmit={handleSubmit}
            submitText="저장"
            cancelText="취소"
        >
          <div className="grid grid-cols-1 gap-x-6 gap-y-8 sm:grid-cols-6">
            <FormField label="Video ID" required className="sm:col-span-4">
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="text"
                    value={videoId}
                    onChange={(e) => setVideoId(e.target.value)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                    placeholder="YouTube Video ID (예: dQw4w9WgXcQ)"
                />
              </div>
            </FormField>

            <FormField label="제목" required>
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                    placeholder="영상 제목을 입력하세요"
                />
              </div>
            </FormField>

            <FormField label="설명">
              <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={4}
                  className="block w-full rounded-md bg-white px-3 py-1.5 text-base text-gray-900 outline-1 -outline-offset-1 outline-gray-300 placeholder:text-gray-400 focus:outline-2 focus:-outline-offset-2 focus:outline-indigo-600 sm:text-sm/6"
                  placeholder="영상 설명을 입력하세요"
              />
            </FormField>

            <FormField label="채널명" className="sm:col-span-4">
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="text"
                    value={channelTitle}
                    onChange={(e) => setChannelTitle(e.target.value)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                    placeholder="채널명을 입력하세요"
                />
              </div>
            </FormField>

            <FormField label="게시일" className="sm:col-span-3">
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="date"
                    value={publishedAt}
                    onChange={(e) => setPublishedAt(e.target.value)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                />
              </div>
            </FormField>

            <FormField label="정렬 순서" className="sm:col-span-2">
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="number"
                    value={sortOrder}
                    onChange={(e) => setSortOrder(parseInt(e.target.value) || 0)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                    placeholder="0"
                />
              </div>
            </FormField>

            <FormField label="썸네일 URL" className="sm:col-span-4">
              <div
                  className="flex rounded-md bg-white shadow-sm ring-1 ring-inset ring-gray-300 focus-within:ring-2 focus-within:ring-inset focus-within:ring-indigo-600">
                <input
                    type="text"
                    value={thumbnailUrl}
                    onChange={(e) => setThumbnailUrl(e.target.value)}
                    className="block flex-1 border-0 bg-transparent py-1.5 pl-3 pr-3 text-gray-900 placeholder:text-gray-400 focus:ring-0 sm:text-sm/6"
                    placeholder="Video ID 입력 시 자동 생성됩니다"
                />
              </div>
            </FormField>

            {thumbnailUrl && (
                <div className="col-span-full">
                  <p className="text-sm font-medium text-gray-700 mb-2">썸네일 미리보기</p>
                  <img
                      src={thumbnailUrl}
                      alt="썸네일 미리보기"
                      className="rounded-lg shadow-sm max-w-sm"
                      onError={(e) => {
                        (e.target as HTMLImageElement).style.display = 'none';
                      }}
                  />
                </div>
            )}
          </div>
        </FormLayout>

        <Modal
            isOpen={isModalOpen}
            onClose={() => setIsModalOpen(false)}
            title="영상을 등록합니다."
            footer={
              <>
                {grayButton("취소", () => setIsModalOpen(false))}
                {blueButton("확인", insertVideo)}
              </>
            }
        >
          <div className="text-sm text-gray-600">
            작성한 영상 정보를 등록하시겠습니까?
          </div>
        </Modal>
      </>
  );
}
