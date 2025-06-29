import { useState } from "react";
import { register } from "../api/auth";
import { Link, useNavigate } from "react-router-dom";

function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();
const handleRegister = async (e: React.FormEvent) => {
  e.preventDefault();
  try {
    const res = await register(email, password);
    localStorage.setItem("token", res.data.token);
    alert("Zarejestrowano!");
    navigate("/search");
  } catch (err: any) {
    if (err.response) {
      alert(`Błąd rejestracji: ${err.response.data.message || err.response.statusText}`);
    } else {
      alert("Błąd rejestracji - brak odpowiedzi z serwera");
    }
  }
};

  return (
    <div>
    <form onSubmit={handleRegister}>
      <h2>Rejestracja</h2>
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
        minLength={8}
      />
      <button type="submit">Zarejestruj się</button>
    </form>
          <p>
            Masz konto? zaloguj się{" "}
            <Link to="/login" style={{ color: "blue", textDecoration: "underline" }}>
              Zaloguj się
            </Link>
          </p>
    </div>
    
  );
}

export default RegisterPage;
