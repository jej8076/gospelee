"use client"
import {useEffect, useState} from "react";

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

  const [people, setPeople] = useState<Users[]>([]);

  const fetchUsers = async () => {
    try {
      await fetch("http://localhost:8008/api/account/all", {
        method: 'POST',
        headers: {
          "Content-Type": "application/json",
          "id_token": "eyJraWQiOiI5ZjI1MmRhZGQ1ZjIzM2Y5M2QyZmE1MjhkMTJmZWEiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJiYzlhYzBmZDRjZDE3YTg1OGM5NzFmNmQ0YWVkZTMwNSIsInN1YiI6IjMyNTI5MjcyMTIiLCJhdXRoX3RpbWUiOjE3MTUxNDc5ODIsImlzcyI6Imh0dHBzOi8va2F1dGgua2FrYW8uY29tIiwibmlja25hbWUiOiLsoJXsnZjsp4QiLCJleHAiOjE3MTUxOTExODIsImlhdCI6MTcxNTE0Nzk4MiwiZW1haWwiOiJqZWpAa2FrYW8uY29tIn0.dqQPzXdHJqsucWaNy3BvqgHf8b8GprS3KM-z62y_HdEPtSseTJAN4_MveOQBhRpSw0tVBI6_jKNm9n9Ih8mcDqENjXAmdotGHahuYbXlFj5afMlUuuIALz5xq7O4F1lX2nKuCODixpzf_V2XOp4X1CayZAISQVXZOdFhwO4IILJTUm0FnM3QDhddTj4z4Z3g9yiQeu5B2gsYDECWlXJbhbbrBVO9PoeT1ojjkiR9wRYl3J9SSriOGQ9W8a4M4_0TSmGcGpBSqdxkpYVDL1de6V_lm3T1SrHLvU4qHbIYnZ2bvLP7aiw1bi2pH7Wuk2eat83WIszzH0kjK7QGDJmdFQ"
        },
      })
      .then((response) => {
        return response.json();
      })
      .then((res: Users[]) => {
        setPeople(res);
      });
    } catch (e) {
      console.error("Error fetching users:", e);
    }
  }

  useEffect(() => {
    fetchUsers();
  }, []);

  return (
      <div className="px-4 sm:px-6 lg:px-8">
        <div className="sm:flex sm:items-center">
          <div className="sm:flex-auto">
            <h1 className="text-base font-semibold leading-6 text-gray-900">Users</h1>
            <p className="mt-2 text-sm text-gray-700">
              A list of all the users in your account including their name, title, email and role.
            </p>
          </div>
          <div className="mt-4 sm:ml-16 sm:mt-0 sm:flex-none">
            <button
                type="button"
                className="block rounded-md bg-indigo-600 px-3 py-2 text-center text-sm font-semibold text-white shadow-sm hover:bg-indigo-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600"
            >
              Add user
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
                    Name
                  </th>
                  <th scope="col"
                      className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                    Title
                  </th>
                  <th scope="col"
                      className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                    Status
                  </th>
                  <th scope="col"
                      className="px-3 py-3.5 text-left text-sm font-semibold text-gray-900">
                    Role
                  </th>
                  <th scope="col" className="relative py-3.5 pl-3 pr-4 sm:pr-0">
                    <span className="sr-only">Edit</span>
                  </th>
                </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                {people.map((person) => (
                    <tr key={person.email}>
                      <td className="whitespace-nowrap py-5 pl-4 pr-3 text-sm sm:pl-0">
                        <div className="flex items-center">
                          <div className="h-11 w-11 flex-shrink-0">
                            <img className="h-11 w-11 rounded-full" src={person.image} alt=""/>
                          </div>
                          <div className="ml-4">
                            <div className="font-medium text-gray-900">{person.name}</div>
                            <div className="mt-1 text-gray-500">{person.email}</div>
                          </div>
                        </div>
                      </td>
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">
                        <div className="text-gray-900">{person.title}</div>
                        <div className="mt-1 text-gray-500">{person.department}</div>
                      </td>
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">
                      <span
                          className="inline-flex items-center rounded-md bg-green-50 px-2 py-1 text-xs font-medium text-green-700 ring-1 ring-inset ring-green-600/20">
                        Active
                      </span>
                      </td>
                      <td className="whitespace-nowrap px-3 py-5 text-sm text-gray-500">{person.role}</td>
                      <td className="relative whitespace-nowrap py-5 pl-3 pr-4 text-right text-sm font-medium sm:pr-0">
                        <a href="#" className="text-indigo-600 hover:text-indigo-900">
                          Edit<span className="sr-only">, {person.name}</span>
                        </a>
                      </td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
  );
}
