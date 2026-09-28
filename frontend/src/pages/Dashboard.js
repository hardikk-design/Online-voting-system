
import {
  useCallback,
  useEffect,
  useState
} from "react";

import { useNavigate } from "react-router-dom";

import {
  getToken,
  getUser,
  logout
} from "../services/auth";

import API_BASE_URL from "../services/api";

import "./Dashboard.css";
import Notifications from "./Notification";

function Dashboard() {

  const navigate = useNavigate();

  const [elections, setElections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Election Officer state
  const [eoAssignment, setEoAssignment] = useState(null);
  const [pendingResults, setPendingResults] = useState([]);
  const [eoLoading, setEoLoading] = useState(true);
  const [eoError, setEoError] = useState("");
  const [releasingResult, setReleasingResult] = useState(null);

  const user = getUser();


  /*
   * FETCH ELECTIONS
   */
  useEffect(() => {

    const fetchElections = async () => {

      try {

        const token = getToken();

        if (!token) {
          navigate("/login");
          return;
        }

        const response = await fetch(
          `${API_BASE_URL}/api/elections`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );

        const responseText =
          await response.text();

        let data = null;

        if (responseText.trim() !== "") {

          try {
            data = JSON.parse(responseText);
          } catch {
            data = responseText;
          }

        }

        if (!response.ok) {

          if (
            response.status === 401 ||
            response.status === 403
          ) {
            logout();
            navigate("/login");
            return;
          }

          throw new Error(
            typeof data === "string"
              ? data
              : "Failed to load elections"
          );
        }

        setElections(
          Array.isArray(data)
            ? data
            : []
        );

      } catch (error) {

        console.error(
          "Election fetch error:",
          error
        );

        setError(error.message);

      } finally {

        setLoading(false);

      }

    };

    fetchElections();

  }, [navigate]);


  /*
   * FETCH ELECTION OFFICER INFORMATION
   */
  const fetchEOAssignment = useCallback(async () => {

    try {

      const token = getToken();

      if (!token) {
        logout();
        navigate("/login");
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/election-officer/my-assignment`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      if (
        response.status === 401 ||
        response.status === 403
      ) {
        logout();
        navigate("/login");
        return;
      }

      if (!response.ok) {

        throw new Error(
          "Failed to check Election Officer assignment."
        );

      }

      const data = await response.json();


      /*
       * Backend response:
       *
       * {
       *   currentAssignment: {...},
       *   pendingResults: [...]
       * }
       */

      setEoAssignment(
        data?.currentAssignment || null
      );

      setPendingResults(
        Array.isArray(data?.pendingResults)
          ? data.pendingResults
          : []
      );

    } catch (error) {

      console.error(
        "EO assignment error:",
        error
      );

      setEoError(
        error.message ||
        "Unable to check Election Officer assignment."
      );

    } finally {

      setEoLoading(false);

    }

  }, [navigate]);


  /*
   * CHECK ELECTION OFFICER ASSIGNMENT
   */
  useEffect(() => {

    fetchEOAssignment();

  }, [fetchEOAssignment]);


  /*
   * RELEASE RESULT
   */
  const handleReleaseResult = async (electionId) => {

    if (!electionId) {
      return;
    }

    const confirmed =
      window.confirm(
        "Are you sure you want to release the result? Once announced, the result cannot be released again."
      );

    if (!confirmed) {
      return;
    }

    try {

      setReleasingResult(electionId);

      const token = getToken();

      if (!token) {
        logout();
        navigate("/login");
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/results/${electionId}/release`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`
          }
        }
      );

      const responseText =
        await response.text();

      if (
        response.status === 401 ||
        response.status === 403
      ) {
        logout();
        navigate("/login");
        return;
      }

      if (!response.ok) {

        throw new Error(
          responseText ||
          "Unable to release result."
        );

      }


      /*
       * Refresh EO information after
       * successfully releasing the result.
       */
      await fetchEOAssignment();

      alert(
        responseText ||
        "Result announced successfully"
      );

    } catch (error) {

      console.error(
        "Release result error:",
        error
      );

      alert(
        error.message ||
        "Unable to release result."
      );

    } finally {

      setReleasingResult(null);

    }

  };


  /*
   * LOGOUT
   */
  const handleLogout = () => {

    logout();

    navigate("/login");

  };


  /*
   * ELECTION FILTERS
   */
  const ongoingElections =
    elections.filter((election) => {

      const status =
        election.status?.toUpperCase();

      return (
        status === "ONGOING" ||
        status === "ACTIVE"
      );

    });


  const upcomingElections =
    elections.filter((election) => {

      const status =
        election.status?.toUpperCase();

      return status === "UPCOMING";

    });


  const completedElections =
    elections.filter((election) => {

      const status =
        election.status?.toUpperCase();

      return (
        status === "COMPLETED" ||
        status === "ENDED" ||
        status === "CLOSED"
      );

    });


  /*
   * ELECTION CARD
   */
  const ElectionCard = ({
    election,
    type
  }) => {

    return (

      <div className="election-card">

        <div className="card-top">

          <span
            className={`election-status ${type}`}
          >
            {election.status}
          </span>

          <span className="card-icon">
            🗳️
          </span>

        </div>


        <h3>
          {election.title}
        </h3>


        <div className="election-info">

          <div className="info-row">

            <span className="info-label">
              Department
            </span>

            <span className="info-value">
              {election.department ||
                "All Departments"}
            </span>

          </div>


          <div className="info-row">

            <span className="info-label">
              Election ID
            </span>

            <span className="info-value">
              #{election.id}
            </span>

          </div>

        </div>


        {type === "ongoing" && (

          <button
            className="view-election-button vote-action"
            onClick={() =>
              navigate(
                `/elections/${election.id}`
              )
            }
          >
            Vote Now
            <span>→</span>
          </button>

        )}


        {type === "upcoming" && (

          <button
            className="view-election-button upcoming-action"
            onClick={() =>
              navigate(
                `/elections/${election.id}`
              )
            }
          >
            View Details
            <span>→</span>
          </button>

        )}


        {type === "completed" && (

          <button
            className="view-election-button completed-action"
            onClick={() =>
              navigate(
                `/elections/${election.id}`
              )
            }
          >
            View Results
            <span>→</span>
          </button>

        )}

      </div>

    );

  };


  return (

    <div className="dashboard-page">

      {/* HEADER */}

      <header className="dashboard-header">

        <div className="dashboard-brand">

          <div className="brand-icon">
            🗳️
          </div>

          <div>

            <h1>
              Online Voting System
            </h1>

            <p>
              Secure • Simple • Transparent
            </p>

          </div>

        </div>


        <button
          className="logout-button"
          onClick={handleLogout}
        >
          Logout
        </button>

      </header>


      <main className="dashboard-container">


        {/* WELCOME */}

        <section className="welcome-card">

          <div className="welcome-text">

            <span className="welcome-label">
              VOTER DASHBOARD
            </span>

            <h2>
              Welcome,{" "}
              {user?.firstName ||
                user?.email ||
                "User"} 👋
            </h2>

            <p>
              View elections and participate
              in the voting process securely.
            </p>

          </div>


          <div className="welcome-icon">
            🗳️
          </div>

        </section>


        {/* NOTIFICATIONS */}

        <Notifications />


        {/* ELECTION OFFICER PANEL */}

        {!eoLoading &&
          !eoError &&
          (eoAssignment ||
            pendingResults.length > 0) && (

            <section className="eo-section">

              <div className="eo-section-header">

                <div>

                  <span className="eo-badge">
                    ELECTION OFFICER
                  </span>

                  <h2>
                    Election Officer Panel
                  </h2>

                  <p>
                    Manage your current election
                    and any pending results.
                  </p>

                </div>


                <div className="eo-icon">
                  👨‍💼
                </div>

              </div>


              {/* ACTIVE EO */}

              {eoAssignment &&
                eoAssignment.status === "ACTIVE" && (

                  !eoAssignment.electionId ? (

                    <div className="eo-content">

                      <h3>
                        Ready to create an election
                      </h3>

                      <p>
                        You currently do not have an
                        election assigned. You can create
                        an election and define its
                        eligibility criteria.
                      </p>


                      <button
                        className="eo-create-button"
                        onClick={() =>
                          navigate(
                            "/election-officer/create-election"
                          )
                        }
                      >
                        Create Election
                        <span>→</span>
                      </button>

                    </div>

                  ) : (

                    <div className="eo-content">

                      <h3>
                        Current Election
                      </h3>

                      <p>
                        {eoAssignment.electionTitle}
                      </p>

                      <span className="eo-election-id">
                        Election ID: #
                        {eoAssignment.electionId}
                      </span>


                      <button
                        className="eo-create-button"
                        onClick={() =>
                          navigate(
                            "/election-officer/nominations"
                          )
                        }
                      >
                        Manage Nominations
                        <span>→</span>
                      </button>

                    </div>

                  )

                )}


              {/* ENDED EO */}

              {eoAssignment &&
                eoAssignment.status === "ENDED" && (

                  <div className="eo-content">

                    <h3>
                      Election Completed
                    </h3>

                    <p>
                      {eoAssignment.electionTitle}
                    </p>

                    <span className="eo-election-id">
                      Election ID: #
                      {eoAssignment.electionId}
                    </span>


                    <button
                      className="eo-create-button"
                      onClick={() =>
                        handleReleaseResult(
                          eoAssignment.electionId
                        )
                      }
                      disabled={
                        releasingResult ===
                        eoAssignment.electionId
                      }
                    >

                      {releasingResult ===
                      eoAssignment.electionId
                        ? "Releasing Result..."
                        : "Release Results"}

                      {releasingResult !==
                        eoAssignment.electionId && (
                        <span>→</span>
                      )}

                    </button>

                  </div>

                )}


              {/* PENDING RESULTS */}

              {pendingResults.length > 0 && (

                <div className="eo-content">

                  <h3>
                    Pending Results
                  </h3>

                  <p>
                    These elections have ended,
                    but their results have not
                    been released yet.
                  </p>


                  {pendingResults.map(
                    (pendingElection) => (

                      <div
                        key={
                          pendingElection.assignmentId
                        }
                        className="eo-pending-result"
                      >

                        <div>

                          <strong>
                            {
                              pendingElection.electionTitle
                            }
                          </strong>

                          <span className="eo-election-id">
                            Election ID: #
                            {
                              pendingElection.electionId
                            }
                          </span>

                        </div>


                        <button
                          className="eo-create-button"
                          onClick={() =>
                            handleReleaseResult(
                              pendingElection.electionId
                            )
                          }
                          disabled={
                            releasingResult ===
                            pendingElection.electionId
                          }
                        >

                          {releasingResult ===
                          pendingElection.electionId
                            ? "Releasing Result..."
                            : "Release Results"}

                          {releasingResult !==
                            pendingElection.electionId && (
                            <span>→</span>
                          )}

                        </button>

                      </div>

                    )
                  )}

                </div>

              )}

            </section>

          )}


        {/* ELECTION LOADING */}

        {loading && (

          <div className="status-card">

            <div className="loading-spinner"></div>

            <p>
              Loading elections...
            </p>

          </div>

        )}


        {/* ELECTION ERROR */}

        {!loading && error && (

          <div className="error-card">

            <div className="status-icon">
              ⚠️
            </div>

            <div>

              <h3>
                Unable to load elections
              </h3>

              <p>
                {error}
              </p>

            </div>

          </div>

        )}


        {/* ELECTIONS */}

        {!loading && !error && (

          <>

            {/* ONGOING */}

            <section className="election-section">

              <div className="section-heading">

                <div>

                  <h2>
                    🟢 Ongoing Elections
                  </h2>

                  <p>
                    Elections currently open
                    for voting
                  </p>

                </div>

                <span className="section-count">
                  {ongoingElections.length}
                </span>

              </div>


              {ongoingElections.length === 0 ? (

                <div className="empty-section">
                  No ongoing elections.
                </div>

              ) : (

                <div className="elections-grid">

                  {ongoingElections.map(
                    (election) => (

                      <ElectionCard
                        key={election.id}
                        election={election}
                        type="ongoing"
                      />

                    )
                  )}

                </div>

              )}

            </section>


            {/* UPCOMING */}

            <section className="election-section">

              <div className="section-heading">

                <div>

                  <h2>
                    🟡 Upcoming Elections
                  </h2>

                  <p>
                    Elections that have not
                    started yet
                  </p>

                </div>

                <span className="section-count">
                  {upcomingElections.length}
                </span>

              </div>


              {upcomingElections.length === 0 ? (

                <div className="empty-section">
                  No upcoming elections.
                </div>

              ) : (

                <div className="elections-grid">

                  {upcomingElections.map(
                    (election) => (

                      <ElectionCard
                        key={election.id}
                        election={election}
                        type="upcoming"
                      />

                    )
                  )}

                </div>

              )}

            </section>


            {/* COMPLETED */}

            <section className="election-section completed-section">

              <div className="section-heading">

                <div>

                  <h2>
                    ⚪ Completed Elections
                  </h2>

                  <p>
                    Elections whose voting period
                    has ended
                  </p>

                </div>

                <span className="section-count">
                  {completedElections.length}
                </span>

              </div>


              {completedElections.length === 0 ? (

                <div className="empty-section">
                  No completed elections.
                </div>

              ) : (

                <div className="elections-grid">

                  {completedElections.map(
                    (election) => (

                      <ElectionCard
                        key={election.id}
                        election={election}
                        type="completed"
                      />

                    )
                  )}

                </div>

              )}

            </section>

          </>

        )}

      </main>

    </div>

  );

}

export default Dashboard;


