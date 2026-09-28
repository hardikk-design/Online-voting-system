
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import "./Candidates.css";

import API_BASE_URL from "../services/api";
import {
  getToken,
  logout
} from "../services/auth";

function Candidates() {
  const { electionId } = useParams();
  const navigate = useNavigate();

  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Voting states
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [showOtpModal, setShowOtpModal] = useState(false);
  const [otp, setOtp] = useState("");
  const [generatedOtp, setGeneratedOtp] = useState("");
  const [otpMessage, setOtpMessage] = useState("");
  const [votingLoading, setVotingLoading] = useState(false);

  // Toast/message
  const [toast, setToast] = useState(null);

  useEffect(() => {
    const fetchCandidates = async () => {
      try {
        const token = getToken();

        if (!token) {
          navigate("/login");
          return;
        }

        const response = await fetch(
          `${API_BASE_URL}/api/candidates/election/${electionId}`,
          {
            method: "GET",
            headers: {
              Authorization: `Bearer ${token}`
            }
          }
        );

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
              : "Failed to load candidates."
          );
        }

        if (!Array.isArray(data)) {
          throw new Error(
            "Invalid candidates response."
          );
        }

        setCandidates(data);

      } catch (error) {
        console.error(
          "Candidates error:",
          error
        );

        setError(
          error.message ||
          "Unable to load candidates."
        );

      } finally {
        setLoading(false);
      }
    };

    fetchCandidates();

  }, [electionId, navigate]);


  // Show toast for a few seconds
  const showToast = (type, message) => {
    setToast({
      type,
      message
    });

    setTimeout(() => {
      setToast(null);
    }, 3500);
  };


  // Select candidate and start voting
  const handleSelectCandidate = async (candidate) => {

    // Election status comes from the backend
    const electionStatus =
      candidate.election?.status?.toUpperCase();

    // Upcoming election
    if (
      electionStatus === "UPCOMING" ||
      electionStatus === "SCHEDULED"
    ) {
      showToast(
        "warning",
        "Voting has not started yet."
      );
      return;
    }

    // Completed election
    if (
      electionStatus === "COMPLETED" ||
      electionStatus === "ENDED" ||
      electionStatus === "CLOSED"
    ) {
      showToast(
        "warning",
        "Voting has ended."
      );
      return;
    }

    // Only active election can continue
    if (
      electionStatus !== "ACTIVE" &&
      electionStatus !== "ONGOING"
    ) {
      showToast(
        "warning",
        "Election is not currently active."
      );
      return;
    }

    setSelectedCandidate(candidate);
    setOtp("");
    setGeneratedOtp("");
    setOtpMessage("");
    setVotingLoading(true);

    try {
      const token = getToken();

      if (!token) {
        navigate("/login");
        return;
      }

      // Request OTP
      const response = await fetch(
        `${API_BASE_URL}/api/votes/request-otp`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },
          body: JSON.stringify({
            electionId: Number(electionId)
          })
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
        throw new Error(
          typeof data === "string"
            ? data
            : data?.message ||
              "Unable to request OTP."
        );
      }

      // Backend returns:
      // {
      //   "electionId": 19,
      //   "otp": "158396"
      // }

      setGeneratedOtp(data?.otp || "");
      setOtpMessage(
        "OTP generated successfully."
      );

      setShowOtpModal(true);

    } catch (error) {
      console.error(
        "Request OTP error:",
        error
      );

      showToast(
        "warning",
        error.message ||
          "Unable to request OTP."
      );

      setSelectedCandidate(null);

    } finally {
      setVotingLoading(false);
    }
  };


  // Verify OTP
  const handleVerifyOtp = async () => {

    if (!otp.trim()) {
      setOtpMessage(
        "Please enter the OTP."
      );
      return;
    }

    setVotingLoading(true);
    setOtpMessage("");

    try {
      const token = getToken();

      if (!token) {
        navigate("/login");
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/votes/verify-otp`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },
          body: JSON.stringify({
            electionId: Number(electionId),
            otp: otp.trim()
          })
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
        throw new Error(
          typeof data === "string"
            ? data
            : data?.message ||
              "OTP verification failed."
        );
      }

      // OTP verified successfully
      setOtpMessage(
        typeof data === "string"
          ? data
          : data?.message ||
            "OTP verified successfully."
      );

      // Give the user a short moment to see success
      setTimeout(() => {
        castVote();
      }, 700);

    } catch (error) {
      console.error(
        "Verify OTP error:",
        error
      );

      setOtpMessage(
        error.message ||
          "Invalid OTP. Please try again."
      );

      setVotingLoading(false);
    }
  };


  // Cast vote
  const castVote = async () => {

    try {
      const token = getToken();

      if (!token) {
        navigate("/login");
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/votes`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },
          body: JSON.stringify({
            electionId: Number(electionId),
            candidateId: Number(
              selectedCandidate.id
            )
          })
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
        throw new Error(
          typeof data === "string"
            ? data
            : data?.message ||
              "Unable to cast vote."
        );
      }

      // Close OTP modal
      setShowOtpModal(false);
      setSelectedCandidate(null);
      setOtp("");
      setGeneratedOtp("");
      setOtpMessage("");

      showToast(
        "success",
        typeof data === "string"
          ? data
          : data?.message ||
            "Vote stored successfully"
      );

    } catch (error) {
      console.error(
        "Cast vote error:",
        error
      );

      /*
       * Backend already tells us when
       * the user has voted.
       */
      if (
        error.message?.toLowerCase()
          .includes("already voted")
      ) {
        setShowOtpModal(false);
        setSelectedCandidate(null);
        setOtp("");
        setGeneratedOtp("");
        setOtpMessage("");

        showToast(
          "warning",
          "You have already voted in this election."
        );

      } else {
        setOtpMessage(
          error.message ||
            "Unable to cast vote."
        );
      }

    } finally {
      setVotingLoading(false);
    }
  };


  // Close OTP modal
  const closeOtpModal = () => {

    if (votingLoading) {
      return;
    }

    setShowOtpModal(false);
    setSelectedCandidate(null);
    setOtp("");
    setGeneratedOtp("");
    setOtpMessage("");
  };


  if (loading) {
    return (
      <div className="candidates-page">
        <div className="candidates-status-card">
          <div className="candidates-spinner"></div>

          <p>
            Loading candidates...
          </p>
        </div>
      </div>
    );
  }


  if (error) {
    return (
      <div className="candidates-page">
        <div className="candidates-error-card">

          <div className="candidates-error-icon">
            ⚠️
          </div>

          <h2>
            Unable to load candidates
          </h2>

          <p>
            {error}
          </p>

          <button
            onClick={() =>
              navigate(
                `/elections/${electionId}`
              )
            }
          >
            Back to Election
          </button>

        </div>
      </div>
    );
  }


  return (
    <div className="candidates-page">

      {/* Toast */}
      {toast && (
        <div
          className={`voting-toast ${toast.type}`}
        >
          <div className="toast-icon">
            {toast.type === "success"
              ? "✓"
              : "⚠"}
          </div>

          <div className="toast-content">
            <strong>
              {toast.type === "success"
                ? "Success"
                : "Warning"}
            </strong>

            <span>
              {toast.message}
            </span>
          </div>

          <button
            onClick={() => setToast(null)}
          >
            ×
          </button>
        </div>
      )}


      <header className="candidates-header">

        <button
          className="candidate-back-button"
          onClick={() =>
            navigate(
              `/elections/${electionId}`
            )
          }
        >
          ← Election Details
        </button>

        <h1>
          Candidates
        </h1>

      </header>


      <main className="candidates-container">

        <div className="candidates-heading">

          <div>

            <span className="candidates-label">
              ELECTION #{electionId}
            </span>

            <h2>
              Approved Candidates
            </h2>

            <p>
              Select a candidate to continue
              with the voting process.
            </p>

          </div>

          <div className="candidate-count">
            {candidates.length}
          </div>

        </div>


        {candidates.length === 0 ? (

          <div className="no-candidates">

            <div className="no-candidates-icon">
              👤
            </div>

            <h3>
              No candidates available
            </h3>

            <p>
              There are currently no approved
              candidates for this election.
            </p>

          </div>

        ) : (

          <div className="candidates-grid">

            {candidates.map((candidate) => (

              <div
                className="candidate-card"
                key={candidate.id}
              >

                <div className="candidate-avatar">

                  {candidate.user?.firstName
                    ?.charAt(0)
                    ?.toUpperCase() || "?"}

                </div>


                <div className="candidate-content">

                  <span className="candidate-position">

                    {candidate.nomination?.position ||
                      "Candidate"}

                  </span>


                  <h3>

                    {candidate.user?.firstName || ""}{" "}

                    {candidate.user?.lastName || ""}

                  </h3>


                  <div className="candidate-info">

                    <div>
                      <span>
                        Student ID
                      </span>

                      <strong>
                        {candidate.user?.studentId ||
                          "Not available"}
                      </strong>
                    </div>


                    <div>
                      <span>
                        Department
                      </span>

                      <strong>
                        {candidate.user?.department ||
                          "Not available"}
                      </strong>
                    </div>


                    <div>
                      <span>
                        Course
                      </span>

                      <strong>
                        {candidate.user?.course ||
                          "Not available"}
                      </strong>
                    </div>


                    <div>
                      <span>
                        Year
                      </span>

                      <strong>
                        {candidate.user?.year ||
                          "Not available"}
                      </strong>
                    </div>

                  </div>


                  <div className="candidate-approved">

                    <span>
                      ✓
                    </span>

                    Approved Candidate

                  </div>


                  <button
                    className="select-candidate-button"
                    onClick={() =>
                      handleSelectCandidate(candidate)
                    }
                    disabled={votingLoading}
                  >
                    {votingLoading &&
                    selectedCandidate?.id ===
                      candidate.id
                      ? "Processing..."
                      : "Select Candidate"}

                    <span>→</span>
                  </button>

                </div>

              </div>

            ))}

          </div>

        )}

      </main>


      {/* OTP MODAL */}
      {showOtpModal && (
        <div className="otp-overlay">

          <div className="otp-modal">

            <button
              className="otp-close-button"
              onClick={closeOtpModal}
              disabled={votingLoading}
            >
              ×
            </button>


            <div className="otp-icon">
              🔐
            </div>


            <h2>
              Verify Your Vote
            </h2>

            <p className="otp-description">
              Enter the OTP generated by the
              voting system to confirm your vote.
            </p>


            <div className="selected-candidate-box">

              <span>
                Voting for
              </span>

              <strong>
                {selectedCandidate?.user?.firstName}{" "}
                {selectedCandidate?.user?.lastName}
              </strong>

              <small>
                {selectedCandidate?.nomination?.position}
              </small>

            </div>


            {/* Demo OTP */}
            {generatedOtp && (
              <div className="demo-otp-box">

                <span>
                  Demo OTP
                </span>

                <strong>
                  {generatedOtp}
                </strong>

              </div>
            )}


            <div className="otp-input-group">

              <label htmlFor="vote-otp">
                Enter OTP
              </label>

              <input
                id="vote-otp"
                type="text"
                inputMode="numeric"
                maxLength="6"
                value={otp}
                onChange={(event) =>
                  setOtp(
                    event.target.value
                      .replace(/\D/g, "")
                  )
                }
                placeholder="Enter 6-digit OTP"
                disabled={votingLoading}
                autoFocus
              />

            </div>


            {otpMessage && (
              <div
                className={`otp-message ${
                  otpMessage
                    .toLowerCase()
                    .includes("success")
                    ? "success"
                    : "warning"
                }`}
              >
                {otpMessage}
              </div>
            )}


            <button
              className="verify-otp-button"
              onClick={handleVerifyOtp}
              disabled={
                votingLoading ||
                otp.length === 0
              }
            >
              {votingLoading
                ? "Verifying..."
                : "Verify OTP & Cast Vote"}
            </button>


            <button
              className="cancel-vote-button"
              onClick={closeOtpModal}
              disabled={votingLoading}
            >
              Cancel
            </button>

          </div>

        </div>
      )}

    </div>
  );
}

export default Candidates;


