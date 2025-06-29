import { Book } from "./Book";


export interface BackendResponse {
  content: Book[];
  totalElements: number;
}