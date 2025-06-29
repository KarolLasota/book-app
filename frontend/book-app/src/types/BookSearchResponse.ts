import { BookDto } from "./BookDto";


export interface BookSearchResponse  {
  totalItems: number;
  books: BookDto[];
}