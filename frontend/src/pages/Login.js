
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Login.css";
import API_BASE_URL from "../services/api";
import { saveToken, saveUser } from "../services/auth";

function Login() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: ""
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData({
      ...formData,
      [name]: value
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/auth/login`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json"
          },
          body: JSON.stringify(formData)
        }
      );

      const responseText = await response.text();

      console.log("Login status:", response.status);
      console.log("Login response:", responseText);

      let data = null;

      if (responseText.trim() !== "") {
        try {
          data = JSON.parse(responseText);
        } catch {
          data = responseText;
        }
      }

      if (!response.ok) {
        throw new Error(
          typeof data === "string"
            ? data
            : data?.message || "Login failed"
        );
      }

      if (!data || typeof data !== "object") {
        throw new Error("Login response was empty.");
      }

      if (!data.token) {
        throw new Error(
          "Login successful, but token was not received."
        );
      }

      saveToken(data.token);
      saveUser(data);

      console.log("Login successful");

      navigate("/dashboard");

    } catch (error) {
      console.error("Login error:", error);

      setError(
        error.message || "Something went wrong."
      );

    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">

      <div className="login-card">

        <div className="login-header">
          <h1>Online Voting System</h1>
          <p>Login to your account</p>
        </div>

        {error && (
          <div className="login-error">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>

          <div className="login-form-group">
            <label htmlFor="email">
              Email
            </label>

            <input
              id="email"
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="studentId@manit.ac.in"
              required
              disabled={loading}
            />
          </div>

          <div className="login-form-group">
            <label htmlFor="password">
              Password
            </label>

            <input
              id="password"
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              required
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? "Logging in..." : "Login"}
          </button>

        </form>

        <div className="register-link">
          <span>Don't have an account?</span>

          <button
            type="button"
            onClick={() => navigate("/register")}
            disabled={loading}
          >
            Create Account
          </button>
        </div>

      </div>

    </div>
  );
}

export default Login;
