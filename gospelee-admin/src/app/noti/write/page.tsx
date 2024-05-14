'use client'
import QuillEditor from "@/component/QuillEditor";
import {useState} from "react";
import 'react-quill/dist/quill.snow.css';
import {Tab} from '@headlessui/react'
import {AtSymbolIcon, CodeBracketIcon, LinkIcon} from '@heroicons/react/20/solid'

function classNames(...classes: String[]) {
  return classes.filter(Boolean).join(' ')
}

export default function NotiWrite() {
  const [content, setContent] = useState('');

  const handleChange = (value: string) => {
    setContent(value);
  };

  return (
      <form action="#">
        <Tab.Group>
          {({selectedIndex}) => (
              <>
                <input
                    className="mb-12 w-full min-w-0 flex-auto px-3.5 py-2 text-3xl text-gray-900 placeholder:text-gray-400 focus:outline-none sm:text-2xl sm:leading-7"
                    placeholder="제목"
                />
                <Tab.List className="flex items-center">
                  <Tab
                      className={({selected}) =>
                          classNames(
                              selected
                                  ? 'bg-gray-100 text-gray-900 hover:bg-gray-200'
                                  : 'bg-white text-gray-500 hover:bg-gray-100 hover:text-gray-900',
                              'rounded-md border border-transparent px-3 py-1.5 text-sm font-medium'
                          )
                      }
                  >
                    Write
                  </Tab>
                  <Tab
                      className={({selected}) =>
                          classNames(
                              selected
                                  ? 'bg-gray-100 text-gray-900 hover:bg-gray-200'
                                  : 'bg-white text-gray-500 hover:bg-gray-100 hover:text-gray-900',
                              'ml-2 rounded-md border border-transparent px-3 py-1.5 text-sm font-medium'
                          )
                      }
                  >
                    Preview
                  </Tab>

                  {/* These buttons are here simply as examples and don't actually do anything. */}
                  {selectedIndex === 0 ? (
                      <div className="ml-auto flex items-center space-x-5">
                        <div className="flex items-center">
                          <button
                              type="button"
                              className="-m-2.5 inline-flex h-10 w-10 items-center justify-center rounded-full text-gray-400 hover:text-gray-500"
                          >
                            <span className="sr-only">Insert link</span>
                            <LinkIcon className="h-5 w-5" aria-hidden="true"/>
                          </button>
                        </div>
                        <div className="flex items-center">
                          <button
                              type="button"
                              className="-m-2.5 inline-flex h-10 w-10 items-center justify-center rounded-full text-gray-400 hover:text-gray-500"
                          >
                            <span className="sr-only">Insert code</span>
                            <CodeBracketIcon className="h-5 w-5" aria-hidden="true"/>
                          </button>
                        </div>
                        <div className="flex items-center">
                          <button
                              type="button"
                              className="-m-2.5 inline-flex h-10 w-10 items-center justify-center rounded-full text-gray-400 hover:text-gray-500"
                          >
                            <span className="sr-only">Mention someone</span>
                            <AtSymbolIcon className="h-5 w-5" aria-hidden="true"/>
                          </button>
                        </div>
                      </div>
                  ) : null}
                </Tab.List>
                <Tab.Panels className="mt-2">
                  <Tab.Panel className="-m-0.5 rounded-lg p-0.5">
                    <label htmlFor="comment" className="sr-only">
                      Comment
                    </label>
                    <div className="mb-12">
                      <QuillEditor value={content} onChange={handleChange} height="400px"/>
                    </div>
                  </Tab.Panel>
                  <Tab.Panel className="-m-0.5 rounded-lg p-0.5">
                    <div className="border-b">
                      <div className="mx-px mt-px px-3 pb-12 pt-2 text-sm leading-5 text-gray-800">
                        Preview content will render here.
                      </div>
                    </div>
                  </Tab.Panel>
                </Tab.Panels>
              </>
          )}
        </Tab.Group>
        <div className="mt-2 flex justify-end">
          <button
              type="submit"
              className="inline-flex items-center rounded-md bg-indigo-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
          >
            Post
          </button>
        </div>
      </form>
  )
}
