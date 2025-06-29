import React, { useEffect, useState } from "react";
import { Book } from "../types/Book";
import { fetchReadBooks, deleteBook  } from "../api/book";
import { refreshToken as apiRefreshToken } from "../api/auth";
import { useNavigate, Link } from "react-router-dom";
import { logout } from "../api/auth";
import { JWTService } from "../services/JWTService";


const ReadBooksPage: React.FC = () => {
  const [books, setBooks] = useState<Book[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const pageSize = 5;
  const totalPages = Math.ceil(totalElements / pageSize);
  const navigate = useNavigate();
  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError(null);

      let token = localStorage.getItem("token");

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
        const data = await fetchReadBooks(0, 5); 
        setBooks(data?.content || []);
      } catch (e) {
        setError("Błąd podczas pobierania książek");
      }

      setLoading(false);
    };

    loadData();
  }, []);


const handleDelete = async (id: number) => {
  try {
    await deleteBook(id);

    setBooks((prevBooks) => {
      const updatedBooks = prevBooks.filter((book) => book.id !== id);
      if (updatedBooks.length === 0 && currentPage > 0) {
        setCurrentPage((p) => p - 1);
      }

      return updatedBooks;
    });

    setTotalElements((prevTotal) => prevTotal - 1);
  } catch (e) {
    console.error("Błąd podczas usuwania książki", e);
    alert("Nie udało się usunąć książki");
  }
};

  return (
    <div>
    <Link to="/search">← Powrót do wyszukiwania</Link> {}
      <h1>Przeczytane książki</h1>

      {loading && <p>Ładowanie...</p>}
      {error && <p style={{ color: "red" }}>{error}</p>}

      {!loading && !error && (
        <>
          <table>
            <thead>
              <tr>
                <th>Miniaturka</th>
                <th>Tytuł</th>
                <th>Autorzy</th>
                <th>Opis</th>
                <th>Usuń z kolekcji</th>
              </tr>
            </thead>
            <tbody>
              {books.map((book) => (
                <tr key={book.id}>
                  <td>
                    <img src={book.thumbnail} alt={book.title} width={50} />
                  </td>
                  <td>{book.title}</td>
                  <td>{book.authors}</td>
                  <td>{book.description.slice(0, 100)}...</td>
                  <td>
                    <button onClick={() => handleDelete(book.id)}>Usuń</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ marginTop: "1rem" }}>
            <button
              onClick={() => setCurrentPage((p) => Math.max(p - 1, 0))}
              disabled={currentPage === 0}
            >
              Poprzednia
            </button>

            <span style={{ margin: "0 1rem" }}>
              Strona {currentPage + 1} z {totalPages}
            </span>

            <button
              onClick={() =>
                setCurrentPage((p) => Math.min(p + 1, totalPages - 1))
              }
              disabled={currentPage >= totalPages - 1}
            >
              Następna
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default ReadBooksPage;