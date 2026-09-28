
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./ElectionDetails.css";

import API_BASE_URL from "../services/api";
import { getToken, logout } from "../services/auth";

function ElectionDetails() {
  const { electionId } = useParams();
  const navigate = useNavigate();

  const [election, setElection] = useState(null);
  const [nomination, setNomination] = useState(null);

  const [loading, setLoading] = useState(true);
  const [nominationLoading, setNominationLoading] = useState(true);
  const [submittingNomination, setSubmittingNomination] = useState(false);

  const [error, setError] = useState("");
  const [nominationError, setNominationError] = useState("");

  useEffect(() => {
    const fetchElection = async () => {
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

        if (response.status === 401 || response.status === 403) {
          logout();
          navigate("/login");
          return;
        }

        if (!response.ok) {
          throw new Error("Failed to load election.");
        }

        const data = await response.json();

        const selectedElection = data.find(
          (item) => String(item.id) === String(electionId)
        );

        if (!selectedElection) {
          throw new Error("Election not found.");
        }

        setElection(selectedElection);

      } catch (error) {
        console.error("Election details error:", error);
        setError(error.message || "Something went wrong.");
      } finally {
        setLoading(false);
      }
    };

    fetchElection();
  }, [electionId, navigate]);

  // ------------------------------------------------
  // GET CURRENT USER'S NOMINATION
  // ------------------------------------------------

  useEffect(() => {
    const fetchMyNomination = async () => {
      try {
        const token = getToken();

        if (!token) {
          navigate("/login");
          return;
        }

        const response = await fetch(
          `${API_BASE_URL}/api/nominations/my/${electionId}`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );

        // 404 simply means the user has not submitted
        // a nomination for this election.
        if (response.status === 404) {
          setNomination(null);
          return;
        }

        if (response.status === 401 || response.status === 403) {
          logout();
          navigate("/login");
          return;
        }

        const responseText = await response.text();

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
              : "Failed to load nomination status."
          );
        }

        setNomination(data);

      } catch (error) {
        console.error("Nomination status error:", error);

        setNominationError(
          error.message || "Unable to load nomination status."
        );
      } finally {
        setNominationLoading(false);
      }
    };

    fetchMyNomination();
  }, [electionId, navigate]);

  const getStatus = () => {
    if (!election?.status) {
      return "";
    }

    return election.status.toUpperCase();
  };

  const status = getStatus();

  const isUpcoming = status === "UPCOMING";

  const isOngoing =
    status === "ONGOING" ||
    status === "ACTIVE";

  const isCompleted =
    status === "COMPLETED" ||
    status === "ENDED" ||
    status === "CLOSED";

  const handleViewCandidates = () => {
    navigate(`/elections/${election.id}/candidates`);
  };

  // ------------------------------------------------
  // SUBMIT NOMINATION
  // ------------------------------------------------

  const handleSubmitNomination = async () => {
    try {
      setSubmittingNomination(true);
      setNominationError("");

      const token = getToken();

      if (!token) {
        navigate("/login");
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/nominations`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          },
          body: JSON.stringify({
            electionId: Number(electionId)
          })
        }
      );

      if (response.status === 401 || response.status === 403) {
        logout();
        navigate("/login");
        return;
      }

      const responseText = await response.text();

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
            : "Unable to submit nomination."
        );
      }

      // Backend returns the newly-created nomination.
      setNomination(data);

    } catch (error) {
      console.error("Submit nomination error:", error);

      setNominationError(
        error.message || "Unable to submit nomination."
      );
    } finally {
      setSubmittingNomination(false);
    }
  };

  // ------------------------------------------------
  // NOMINATION STATUS UI
  // ------------------------------------------------

  const renderNominationSection = () => {
    if (nominationLoading) {
      return (
        <div className="nomination-section">
          <div className="nomination-header">
            <h2>Nomination</h2>
          </div>

          <p className="nomination-loading">
            Checking nomination status...
          </p>
        </div>
      );
    }

    const nominationStatus =
      nomination?.status?.toUpperCase();

    // ------------------------------------------------
    // NO NOMINATION
    // ------------------------------------------------

    if (!nomination) {
      return (
        <div className="nomination-section">
          <div className="nomination-header">
            <h2>Nomination</h2>
          </div>

          {isUpcoming ? (
            <>
              <p className="nomination-description">
                You can submit your nomination for this
                election before voting begins.
              </p>

              <button
                className="nomination-button"
                onClick={handleSubmitNomination}
                disabled={submittingNomination}
              >
                {submittingNomination
                  ? "Submitting..."
                  : "Submit Nomination"}
              </button>
            </>
          ) : (
            <p className="nomination-description">
              Nomination is not available for this election.
            </p>
          )}

          {nominationError && (
            <div className="nomination-error">
              {nominationError}
            </div>
          )}
        </div>
      );
    }

    // ------------------------------------------------
    // PENDING
    // ------------------------------------------------

    if (nominationStatus === "PENDING") {
      return (
        <div className="nomination-section">
          <div className="nomination-header">
            <h2>Nomination Status</h2>
          </div>

          <div className="nomination-status-card pending">
            <div className="nomination-status-icon">
              ⏳
            </div>

            <div>
              <h3>Pending</h3>

              <p>
                Your nomination is waiting for
                Election Officer review.
              </p>
            </div>
          </div>

          <div className="nomination-position">
            <span>Position</span>
            <strong>
              {nomination.position || "Not available"}
            </strong>
          </div>
        </div>
      );
    }

    // ------------------------------------------------
    // APPROVED
    // ------------------------------------------------

    if (nominationStatus === "APPROVED") {
      return (
        <div className="nomination-section">
          <div className="nomination-header">
            <h2>Nomination Status</h2>
          </div>

          <div className="nomination-status-card approved">
            <div className="nomination-status-icon">
              ✓
            </div>

            <div>
              <h3>Approved</h3>

              <p>
                Your nomination has been approved.
                You are now a candidate.
              </p>
            </div>
          </div>

          <div className="nomination-position">
            <span>Position</span>
            <strong>
              {nomination.position || "Not available"}
            </strong>
          </div>
        </div>
      );
    }

    // ------------------------------------------------
    // REJECTED
    // ------------------------------------------------

    if (nominationStatus === "REJECTED") {
      return (
        <div className="nomination-section">
          <div className="nomination-header">
            <h2>Nomination Status</h2>
          </div>

          <div className="nomination-status-card rejected">
            <div className="nomination-status-icon">
              !
            </div>

            <div>
              <h3>Rejected</h3>

              <p>
                Your nomination was not approved
                for this election.
              </p>
            </div>
          </div>

          <div className="nomination-position">
            <span>Position</span>
            <strong>
              {nomination.position || "Not available"}
            </strong>
          </div>
        </div>
      );
    }

    return null;
  };

  if (loading) {
    return (
      <div className="election-details-page">
        <div className="election-details-card">
          <p>Loading election...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="election-details-page">
        <div className="election-details-card">
          <div className="election-error">
            {error}
          </div>

          <button
            className="back-button"
            onClick={() => navigate("/dashboard")}
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  if (!election) {
    return null;
  }

  return (
    <div className="election-details-page">

      <div className="election-details-card">

        <button
          className="back-button"
          onClick={() => navigate("/dashboard")}
        >
          ← Back to Dashboard
        </button>

        <div className="election-header">

          <span className={`status-badge ${status.toLowerCase()}`}>
            {status}
          </span>

          <h1>{election.title}</h1>

          <p className="election-description">
            {election.description}
          </p>

        </div>

        <div className="election-info">

          <div className="info-item">
            <span className="info-label">
              Department
            </span>

            <span className="info-value">
              {election.department || "All Departments"}
            </span>
          </div>

          <div className="info-item">
            <span className="info-label">
              Start Date
            </span>

            <span className="info-value">
              {election.startDate
                ? new Date(election.startDate).toLocaleString()
                : "Not available"}
            </span>
          </div>

          <div className="info-item">
            <span className="info-label">
              End Date
            </span>

            <span className="info-value">
              {election.endDate
                ? new Date(election.endDate).toLocaleString()
                : "Not available"}
            </span>
          </div>

        </div>

        {/* NOMINATION */}
        {renderNominationSection()}

        <div className="election-actions">

          {/* UPCOMING */}
          {isUpcoming && (
            <>
              <p
                className="action-message"
                style={{ fontWeight: "700" }}
              >
                Voting has not started yet.
              </p>

              <button
                className="primary-button"
                onClick={handleViewCandidates}
              >
                View Candidates
              </button>
            </>
          )}

          {/* ONGOING */}
          {isOngoing && (
            <>
              <p className="action-message">
                Voting is currently open.
              </p>

              <button
                className="primary-button"
                onClick={handleViewCandidates}
              >
                View Candidates & Vote
              </button>
            </>
          )}

          {/* COMPLETED */}
          {isCompleted && (
            <>
              <p className="action-message">
                Voting has ended.
              </p>

              <button
                className="primary-button"
                onClick={handleViewCandidates}
              >
                View Candidates
              </button>

              <button
                className="secondary-button"
                onClick={() =>
                  navigate(
                    `/elections/${election.id}/results`
                  )
                }
              >
                View Results
              </button>
            </>
          )}

        </div>

      </div>

    </div>
  );
}

export default ElectionDetails;

