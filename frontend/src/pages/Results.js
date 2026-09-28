
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import "./Results.css";

import API_BASE_URL from "../services/api";
import {
  getToken,
  logout
} from "../services/auth";

function Results() {

  const { electionId } = useParams();
  const navigate = useNavigate();

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {

    const fetchResults = async () => {

      try {

        const token = getToken();

        if (!token) {
          navigate("/login");
          return;
        }

        /*
         * Backend API:
         * GET /api/results/{electionId}
         */

        const response = await fetch(
          `${API_BASE_URL}/api/results/${electionId}`,
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

        /*
         * Only 401 means the login/token is invalid.
         * Do not logout on 403.
         */

        if (response.status === 401) {
          logout();
          navigate("/login");
          return;
        }

        if (!response.ok) {

          throw new Error(
            typeof data === "string"
              ? data
              : "Failed to load election results."
          );

        }

        setResults(
          Array.isArray(data)
            ? data
            : []
        );

      } catch (error) {

        console.error(
          "Results fetch error:",
          error
        );

        setError(
          error.message ||
          "Something went wrong."
        );

      } finally {

        setLoading(false);

      }

    };

    fetchResults();

  }, [electionId, navigate]);


  /*
   * LOADING
   */

  if (loading) {

    return (

      <div className="results-page">

        <div className="results-card loading-results">

          <div className="results-spinner"></div>

          <p>
            Loading election results...
          </p>

        </div>

      </div>

    );

  }


  /*
   * ERROR
   */

  if (error) {

    return (

      <div className="results-page">

        <div className="results-card">

          <button
            className="results-back-button"
            onClick={() =>
              navigate(
                `/elections/${electionId}`
              )
            }
          >
            [ Election Details ]
          </button>

          <div className="results-error">

            <span>⚠️</span>

            <div>

              <h3>
                Unable to load results
              </h3>

              <p>
                {error}
              </p>

            </div>

          </div>

        </div>

      </div>

    );

  }


  /*
   * NO RESULTS
   */

  if (results.length === 0) {

    return (

      <div className="results-page">

        <div className="results-card">

          <button
            className="results-back-button"
            onClick={() =>
              navigate(
                `/elections/${electionId}`
              )
            }
          >
            [ Election Details ]
          </button>

          <div className="no-results">

            <div className="no-results-icon">
              🗳️
            </div>

            <h2>
              No Results Available
            </h2>

            <p>
              Results have not been released
              for this election yet.
            </p>

          </div>

        </div>

      </div>

    );

  }


  /*
   * API RESPONSE
   *
   * results[0]
   * ├── result
   * ├── candidate
   * └── voteCount
   */

  const firstResult = results[0];

  const election =
    firstResult.result?.election;

  const winner =
    firstResult.result?.winner;

  const winnerUser =
    winner?.user;

  const candidate =
    firstResult.candidate;

  const candidateUser =
    candidate?.user;


  return (

    <div className="results-page">

      <div className="results-card">


        {/* BACK */}

        <button
          className="results-back-button"
          onClick={() =>
            navigate(
              `/elections/${electionId}`
            )
          }
        >
          [ Election Details ]
        </button>


        {/* HEADER */}

        <div className="results-header">

          <span className="results-badge">
            RESULT DECLARED
          </span>

          <h1>
            {election?.title ||
              "Election Results"}
          </h1>

          <p>
            The voting period for this election
            has ended and the result has been
            released.
          </p>

        </div>


        {/* ELECTION INFORMATION */}

        <div className="results-info-grid">


          <div className="result-info-item">

            <span className="result-info-label">
              Department
            </span>

            <span className="result-info-value">
              {election?.department ||
                "All Departments"}
            </span>

          </div>


          <div className="result-info-item">

            <span className="result-info-label">
              Post
            </span>

            <span className="result-info-value">
              {election?.post?.name ||
                candidate?.nomination?.position ||
                "Not available"}
            </span>

          </div>


          <div className="result-info-item">

            <span className="result-info-label">
              Status
            </span>

            <span className="result-info-value completed-result">
              {election?.status ||
                "COMPLETED"}
            </span>

          </div>

        </div>


        {/* WINNER */}

        <section className="winner-section">

          <div className="winner-heading">

            <span className="winner-trophy">
              🏆
            </span>

            <div>

              <h2>
                Winner
              </h2>

              <p>
                Elected candidate
              </p>

            </div>

          </div>


          <div className="winner-card">


            <div className="winner-avatar">

              {winnerUser?.firstName
                ?.charAt(0)
                ?.toUpperCase()}

            </div>


            <div className="winner-details">

              <h3>

                {winnerUser?.firstName}{" "}
                {winnerUser?.lastName}

              </h3>


              <p>

                Student ID:{" "}
                {winnerUser?.studentId}

              </p>


              <p>

                {winnerUser?.department}
                {" • "}
                {winnerUser?.course}
                {" • Year "}
                {winnerUser?.year}

              </p>

            </div>


            <div className="winner-votes">
  <strong>
    {firstResult.voteCount ?? 0}
  </strong>

  <span>
    Votes
  </span>
</div>

          </div>

        </section>


        {/* CANDIDATE RESULTS */}

        <section className="candidate-results-section">

          <div className="section-title">

            <div>

              <h2>
                Candidate Results
              </h2>

              <p>
                Votes received by the candidate
              </p>

            </div>

          </div>


          <div className="candidate-result-card">


            <div className="candidate-result-info">


              <div className="candidate-result-avatar">

                {candidateUser?.firstName
                  ?.charAt(0)
                  ?.toUpperCase()}

              </div>


              <div>

                <h3>

                  {candidateUser?.firstName}{" "}
                  {candidateUser?.lastName}

                </h3>


                <p>

                  {candidate?.nomination?.position ||
                    election?.post?.name}

                </p>

              </div>

            </div>


          <div className="candidate-result-votes">
  <strong>
    {firstResult.voteCount ?? 0}
  </strong>

  <span>
    votes
  </span>
</div>

          </div>

        </section>


        {/* RELEASE INFORMATION */}

        <section className="release-section">


          <div className="release-icon">
            ✓
          </div>


          <div>

            <h3>
              Result Released
            </h3>


            <p>

              {firstResult.result?.releasedAt
                ? new Date(
                    firstResult.result.releasedAt
                  ).toLocaleString()
                : "Release time unavailable"}

            </p>

          </div>

        </section>


        {/* FOOTER */}

        <div className="results-footer">

          <button
            className="dashboard-button"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            [ Dashboard ]
          </button>

        </div>


      </div>

    </div>

  );

}

export default Results;

