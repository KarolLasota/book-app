import axiosInstance from "./axiosInstance";
import { BookSearchResponse } from "../types/BookSearchResponse";
import { BookDto } from "../types/BookDto";
import { BackendResponse } from "../types/BackendResponse";

const API_SEARCH = "/api/books/search?q=";

export const getBooks = async (
  book: string,
  page: number = 0,
  size: number = 10
): Promise<BookSearchResponse> => {
  const url = `${API_SEARCH}${book}&page=${page}&size=${size}`;
  const res = await axiosInstance.get<BookSearchResponse>(url);
  return res.data;
};

export const getBookById = async (id: string): Promise<BookDto> => {
  const res = await axiosInstance.get<BookDto>(`/api/books/${id}`);
  return res.data;
};

export const addBookToReadList = async (book: BookDto) => {
  try {
    await axiosInstance.post("/api/books", book);
    alert("Dodano książkę do przeczytanych!");
  } catch (error) {
    console.error("Błąd dodawania książki:", error);
    alert("Nie udało się dodać książki.");
  }
};

export const hasUserReadBook = async (googleBookId: string): Promise<boolean> => {
  try {
    const res = await axiosInstance.get<boolean>(`/api/books/books/read/${googleBookId}`);
    return res.data;
  } catch (error) {
    console.error("Błąd przy sprawdzaniu przeczytania:", error);
    return false;
  }
};

export const fetchReadBooks = async (
  page: number,
  size: number
): Promise<BackendResponse | null> => {
  try {
    const response = await axiosInstance.get<BackendResponse>(`/api/books/paged?page=${page}&size=${size}`);
    return response.data;
  } catch (error) {
    console.error("Błąd przy pobieraniu przeczytanych książek:", error);
    return null;
  }
};

export const deleteBook = async (id: number): Promise<void> => {
  await axiosInstance.delete(`/api/books/${id}`);
};
