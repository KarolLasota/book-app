import { useState } from "react";
import { login } from "../api/auth";
import { Link, useNavigate } from "react-router-dom";
import { logout } from "../api/auth";

function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await login(email, password);
      localStorage.setItem("token", res.data.token);
      alert("Zalogowano!");
      navigate("/search");
    } catch (err: any) {
    if (err.response) {
      logout();
      alert(`Błąd logowania: ${err.response.data.message || err.response.statusText}`);
    } else {
           alert("Błąd logowania - brak odpowiedzi z serwera");
           }
      } 
  };

  return (
    <div>
    <form onSubmit={handleLogin}>
      <h2>Logowanie</h2>
      <input
        type="email"
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        required
      />
      <input
        type="password"
        placeholder="Hasło"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        required
      />
      <button type="submit">Zaloguj</button>
    </form>
      <p>
        Nie masz konta?{" "}
        <Link to="/register" style={{ color: "blue", textDecoration: "underline" }}>
          Zarejestruj się
        </Link>
      </p>
    </div>

    
  );
}

export default LoginPage;
