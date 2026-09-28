
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import API_BASE_URL from "../services/api";
import {
  getToken,
  logout
} from "../services/auth";

import "./Nominations.css";

function Nominations() {

  const navigate = useNavigate();

  const [assignment, setAssignment] = useState(null);
  const [nominations, setNominations] = useState([]);

  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(null);

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");


  /*
   * FETCH EO ASSIGNMENT
   * Then fetch pending nominations
   */

  useEffect(() => {

    const fetchData = async () => {

      try {

        const token = getToken();

        if (!token) {
          navigate("/login");
          return;
        }


        /*
         * STEP 1:
         * Find the election assigned
         * to the logged-in EO.
         */

        const assignmentResponse =
          await fetch(
            `${API_BASE_URL}/api/election-officer/my-assignment`,
            {
              method: "GET",
              headers: {
                Authorization: `Bearer ${token}`
              }
            }
          );


        if (
          assignmentResponse.status === 401 ||
          assignmentResponse.status === 403
        ) {
          logout();
          navigate("/login");
          return;
        }


        if (!assignmentResponse.ok) {
          throw new Error(
            "Failed to load Election Officer assignment."
          );
        }


        const assignmentData =
          await assignmentResponse.json();


        setAssignment(assignmentData);


        /*
         * No election assigned yet.
         */

        if (!assignmentData?.electionId) {

          setNominations([]);

          return;
        }


        /*
         * STEP 2:
         * Fetch pending nominations
         * for the assigned election.
         */

        const nominationResponse =
          await fetch(
            `${API_BASE_URL}/api/nominations/election/${assignmentData.electionId}/pending`,
            {
              method: "GET",
              headers: {
                Authorization: `Bearer ${token}`
              }
            }
          );


        if (
          nominationResponse.status === 401 ||
          nominationResponse.status === 403
        ) {
          logout();
          navigate("/login");
          return;
        }


        const responseText =
          await nominationResponse.text();

        let nominationData = [];


        if (responseText.trim() !== "") {

          try {

            nominationData =
              JSON.parse(responseText);

          } catch {

            nominationData = [];

          }

        }


        if (!nominationResponse.ok) {

          throw new Error(
            typeof nominationData === "string"
              ? nominationData
              : "Failed to load pending nominations."
          );

        }


        setNominations(
          Array.isArray(nominationData)
            ? nominationData
            : []
        );

      } catch (error) {

        console.error(
          "Nominations fetch error:",
          error
        );

        setError(
          error.message ||
          "Unable to load nominations."
        );

      } finally {

        setLoading(false);

      }

    };


    fetchData();

  }, [navigate]);


  /*
   * APPROVE / REJECT NOMINATION
   */

  const handleNominationAction = async (
    nominationId,
    action
  ) => {

    setActionLoading(
      `${action}-${nominationId}`
    );

    setError("");
    setSuccess("");


    try {

      const token = getToken();

      if (!token) {
        navigate("/login");
        return;
      }


      const response = await fetch(
        `${API_BASE_URL}/api/nominations/${nominationId}/${action}`,
        {
          method: "PUT",
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
            : `Failed to ${action} nomination.`
        );

      }


      /*
       * Remove the processed nomination
       * from the pending list.
       */

      setNominations((previous) =>
        previous.filter(
          (nomination) =>
            nomination.id !== nominationId
        )
      );


      if (action === "approve") {

        setSuccess(
          "Nomination approved successfully. The candidate has been created."
        );

      } else {

        setSuccess(
          "Nomination rejected successfully."
        );

      }

    } catch (error) {

      console.error(
        `Nomination ${action} error:`,
        error
      );

      setError(
        error.message ||
        `Unable to ${action} nomination.`
      );

    } finally {

      setActionLoading(null);

    }

  };


  /*
   * FORMAT DATE
   */

  const formatDate = (date) => {

    if (!date) {
      return "Not available";
    }

    return new Date(date).toLocaleString();

  };


  /*
   * LOADING
   */

  if (loading) {

    return (

      <div className="nominations-page">

        <div className="nominations-card">

          <div className="nominations-loading">

            <div className="nominations-spinner"></div>

            <p>
              Loading pending nominations...
            </p>

          </div>

        </div>

      </div>

    );

  }


  /*
   * PAGE
   */

  return (

    <div className="nominations-page">

      <div className="nominations-card">


        {/* HEADER */}

        <div className="nominations-header">

          <button
            className="nominations-back-button"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            ← Back to Dashboard
          </button>


          <span className="nominations-badge">
            ELECTION OFFICER
          </span>


          <h1>
            Pending Nominations
          </h1>


          <p>
            Review and manage nominations submitted
            for your assigned election.
          </p>

        </div>


        {/* ASSIGNED ELECTION */}

        {assignment?.electionId && (

          <div className="assigned-election">

            <div>

              <span className="assigned-election-label">
                ASSIGNED ELECTION
              </span>

              <h2>
                {assignment.electionTitle ||
                  `Election #${assignment.electionId}`}
              </h2>

            </div>


            <span className="assigned-election-id">
              Election #{assignment.electionId}
            </span>

          </div>

        )}


        {/* NO ELECTION */}

        {!assignment?.electionId && (

          <div className="nominations-empty">

            <div className="empty-icon">
              🗳️
            </div>

            <h2>
              No Election Assigned
            </h2>

            <p>
              You currently do not have an election
              assigned to you.
            </p>

          </div>

        )}


        {/* ERROR */}

        {error && (

          <div className="nominations-error">
            ⚠️ {error}
          </div>

        )}


        {/* SUCCESS */}

        {success && (

          <div className="nominations-success">
            ✓ {success}
          </div>

        )}


        {/* NOMINATION COUNT */}

        {assignment?.electionId && (

          <div className="nomination-summary">

            <div>

              <h2>
                Pending Nominations
              </h2>

              <p>
                Applications waiting for your review.
              </p>

            </div>


            <span className="nomination-count">
              {nominations.length}
            </span>

          </div>

        )}


        {/* EMPTY NOMINATIONS */}

        {assignment?.electionId &&
          nominations.length === 0 && (

            <div className="nominations-empty">

              <div className="empty-icon">
                ✓
              </div>

              <h2>
                No Pending Nominations
              </h2>

              <p>
                There are currently no nominations
                waiting for your review.
              </p>

            </div>

          )}


        {/* NOMINATION LIST */}

        {nominations.length > 0 && (

          <div className="nominations-list">

            {nominations.map(
              (nomination) => (

                <div
                  className="nomination-card"
                  key={nomination.id}
                >


                  <div className="nomination-card-header">

                    <div>

                      <span className="nomination-id">
                        Nomination #{nomination.id}
                      </span>

                      <h3>
                        {nomination.user?.firstName}{" "}
                        {nomination.user?.lastName}
                      </h3>

                    </div>


                    <span className="pending-badge">
                      {nomination.status}
                    </span>

                  </div>


                  <div className="nomination-details">


                    <div className="nomination-detail">

                      <span>
                        Student ID
                      </span>

                      <strong>
                        {nomination.user?.studentId ||
                          "Not available"}
                      </strong>

                    </div>


                    <div className="nomination-detail">

                      <span>
                        Email
                      </span>

                      <strong>
                        {nomination.user?.email ||
                          "Not available"}
                      </strong>

                    </div>


                    <div className="nomination-detail">

                      <span>
                        Department
                      </span>

                      <strong>
                        {nomination.user?.department ||
                          "Not available"}
                      </strong>

                    </div>


                    <div className="nomination-detail">

                      <span>
                        Course
                      </span>

                      <strong>
                        {nomination.user?.course ||
                          "Not available"}
                      </strong>

                    </div>


                    <div className="nomination-detail">

                      <span>
                        Year
                      </span>

                      <strong>
                        {nomination.user?.year ||
                          "Not available"}
                      </strong>

                    </div>


                    <div className="nomination-detail">

                      <span>
                        Position
                      </span>

                      <strong>
                        {nomination.position ||
                          "Not available"}
                      </strong>

                    </div>


                    <div className="nomination-detail">

                      <span>
                        Submitted
                      </span>

                      <strong>
                        {formatDate(
                          nomination.submittedAt
                        )}
                      </strong>

                    </div>

                  </div>


                  {/* ACTIONS */}

                  <div className="nomination-actions">

                    <button
                      className="reject-nomination-button"
                      disabled={
                        actionLoading !== null
                      }
                      onClick={() =>
                        handleNominationAction(
                          nomination.id,
                          "reject"
                        )
                      }
                    >

                      {actionLoading ===
                        `reject-${nomination.id}`
                        ? "Rejecting..."
                        : "Reject"}

                    </button>


                    <button
                      className="approve-nomination-button"
                      disabled={
                        actionLoading !== null
                      }
                      onClick={() =>
                        handleNominationAction(
                          nomination.id,
                          "approve"
                        )
                      }
                    >

                      {actionLoading ===
                        `approve-${nomination.id}`
                        ? "Approving..."
                        : "Approve"}

                    </button>

                  </div>

                </div>

              )
            )}

          </div>

        )}


      </div>

    </div>

  );

}

export default Nominations;

