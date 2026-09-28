
import {
  BrowserRouter,
  Routes,
  Route
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import ElectionDetails from "./pages/ElectionDetails";
import Candidates from "./pages/Candidates";
import Results from "./pages/Results";
import CreateElection from "./pages/CreateElection";
import Nominations from "./pages/Nominations";


function App() {
  return (
    <BrowserRouter>

      <Routes>

        <Route
          path="/"
          element={<Login />}
        />

        <Route
          path="/login"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        <Route
          path="/dashboard"
          element={<Dashboard />}
        />

        <Route
          path="/elections/:electionId"
          element={<ElectionDetails />}
        />

        <Route
          path="/elections/:electionId/candidates"
          element={<Candidates />}
        />

        <Route
  path="/elections/:electionId/results"
  element={<Results />}
/>
      
      <Route
  path="/election-officer/create-election"
  element={<CreateElection />}
/>
<Route
  path="/election-officer/nominations"
  element={<Nominations />}
/>


      </Routes>

    </BrowserRouter>
  );
}

export default App;

