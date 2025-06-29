import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BookDto } from "../types/BookDto";
import { getBooks, getBookById, addBookToReadList, hasUserReadBook, } from "../api/book";
import { logout } from "../api/auth";
import { refreshToken as apiRefreshToken } from "../api/auth";
import { JWTService } from '../services/JWTService';
import { JwtPayload } from "jwt-decode";


const SearchPage: React.FC = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState<string>("");
  const[searchBook, setSearchBook] = useState("");
  const [books, setBooks] = useState<BookDto[]>([]);
  const [totalItems, setTotalItems] = useState<number>(0);
  const [currentPage, setCurrentPage] = useState(0);
  const [inputValue, setInputValue] = useState("");
  const pageSize = 10;
  const [selectedBook, setSelectedBook] = useState<BookDto | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [isRead, setIsRead] = useState<boolean>(false);

const handleLogout = async () => {
  try {
    await logout();
  } catch (error) {
    console.error("Błąd wylogowania", error);
  }
  localStorage.removeItem("token");
  navigate("/login");
};

useEffect(() => {
  const fetchBooks = async () => {
    let token = localStorage.getItem("token");
    const decoded = JWTService.decodeJwt<JwtPayload>(token!);
    setEmail(decoded.sub ?? "Brak emaila");
    if (searchBook.trim() === "") return;

      if (!token || JWTService.isTokenExpired(token)) {
        try {
          const res = await apiRefreshToken();
          token = res.data.token;
          localStorage.setItem("token", token);
        } catch {
          await logout();
          navigate("/login");
          return;
        }
      }
    try {


      const response = await getBooks(searchBook, currentPage, pageSize);
      setBooks(response.books);
      setTotalItems(response.totalItems);
    } catch (error) {
      console.error("Błąd podczas pobierania książek:", error);
      alert("Nie udało się pobrać książek.");
    }
  };

  fetchBooks();
}, [searchBook, currentPage, pageSize,  navigate]);





  const handleShowBook = async (id: string) => {
  try {
    const bookDetails = await getBookById(id);
    setSelectedBook(bookDetails);
    setShowModal(true);

     const wasRead = await hasUserReadBook(bookDetails.googleBookId);
     setIsRead(wasRead);
  } catch (error:any) {
    
    console.error("Błąd pobierania szczegółów książki:", error.response.data.message);
    alert("Nie udało się pobrać szczegółów książki.");
  }
};

  const handleNextPage = () => {
    setCurrentPage((prev) => prev + 1);
  };

  const handlePrevPage = () => {
    if (currentPage > 0) setCurrentPage((prev) => prev - 1);
  };

  const handleSearchClick = () => {
  setCurrentPage(0);      
  setSearchBook(inputValue);
};

const handleAddBook = async (book: BookDto) => {
  try {
    await addBookToReadList(book);
    setIsRead(true); 
  } catch (error) {
    console.error("Błąd dodawania książki:", error);
    alert("Nie udało się dodać książki.");
  }
};



  return (
    <div>
      <h2>Witaj, {email}!</h2>
      <div style={{ whiteSpace: "pre-line" }}>
  <button onClick={handleLogout}>Wyloguj</button>

</div>
<p>
  <Link to="/read-books">
  Przejdź do przeczytanych książek
</Link>
</p>

      
      <input
        type="text"
        value={inputValue}
        onChange={(e) => setInputValue(e.target.value)}
        required
      />
      <button onClick={handleSearchClick}>Wyszukaj</button>
<table>
  <thead>
    <tr>
      <th>Okładka</th>
      <th>Tytuł</th>
      <th>Autorzy</th>
      <th>Opis</th>
      <th>Więcej informacji</th>
    </tr>
  </thead>
  <tbody>
    {books.map((book) => (
      <tr key={book.googleBookId}>
        <td>
          <img src={book.thumbnail} alt={book.title} width={50} />
        </td>
        <td>{book.title}</td>
        <td>{book.authors}</td>
        <td>{book.description.substring(0, 100)}...</td>
        <td>
        <button onClick={() => handleShowBook(book.googleBookId)}>Pokaż</button>
      </td>
      </tr>
    ))}
  </tbody>
</table>
{showModal && selectedBook && (
  <div
    style={{
      position: "fixed",
      top: "20%",
      left: "50%",
      transform: "translateX(-50%)",
      backgroundColor: "white",
      border: "1px solid black",
      padding: "20px",
      zIndex: 1000,
      maxWidth: "500px",
      boxShadow: "0 0 10px rgba(0,0,0,0.5)"
    }}
  >
    <h3>{selectedBook.title}</h3>
    <p><strong>Autorzy:</strong> {selectedBook.authors}</p>
    <img src={selectedBook.thumbnail} alt={selectedBook.title} style={{ maxWidth: "100%" }} /> 
    <div style={{ maxHeight: '300px', overflowY: 'auto', marginTop: '10px' }}>
      {selectedBook.description}
    </div>
    <button onClick={() => setShowModal(false)}>Zamknij</button>
    <button onClick={() => handleAddBook(selectedBook)} disabled={isRead}>
    {isRead ? "Już przeczytana" : "Dodaj do przeczytanych"}
</button>
  </div>
)}



      <div style={{ marginTop: "20px" }}>
  <button onClick={handlePrevPage} disabled={currentPage === 0}>
    Poprzednia
  </button>
  <span style={{ margin: "0 10px" }}>
    Strona {currentPage + 1} / {Math.ceil(totalItems / pageSize)}
  </span>
  <button
    onClick={handleNextPage}
    disabled={currentPage + 1 >= Math.ceil(totalItems / pageSize)}
  >
    Następna
  </button>
</div>

    </div>
    
    
  );
};



export default SearchPage;
