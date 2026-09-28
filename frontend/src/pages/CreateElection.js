
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import API_BASE_URL from "../services/api";
import {
  getToken,
  logout
} from "../services/auth";

import "./CreateElection.css";

function CreateElection() {

  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    postId: "",
    department: "",
    startDate: "",
    endDate: "",
    eligibilityDepartment: "",
    eligibilityCourse: "",
    eligibilityYear: ""
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (event) => {

    const {
      name,
      value
    } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value
    }));

  };


  const handleSubmit = async (event) => {

    event.preventDefault();

    setError("");
    setSuccess("");

    const token = getToken();

    if (!token) {
      navigate("/login");
      return;
    }


    /*
     * BASIC VALIDATION
     */

    if (
      !formData.title.trim() ||
      !formData.description.trim() ||
      !formData.postId ||
      !formData.department.trim() ||
      !formData.startDate ||
      !formData.endDate ||
      !formData.eligibilityDepartment.trim() ||
      !formData.eligibilityCourse.trim() ||
      !formData.eligibilityYear
    ) {

      setError(
        "Please fill in all the required fields."
      );

      return;
    }


    if (
      new Date(formData.endDate) <=
      new Date(formData.startDate)
    ) {

      setError(
        "End date and time must be after the start date and time."
      );

      return;
    }


    setLoading(true);


    try {

      const requestBody = {

        title: formData.title.trim(),

        description:
          formData.description.trim(),

        postId:
          Number(formData.postId),

        department:
          formData.department.trim(),

        startDate:
          formData.startDate,

        endDate:
          formData.endDate,

        eligibility: {

          department:
            formData.eligibilityDepartment.trim(),

          course:
            formData.eligibilityCourse.trim(),

          year:
            Number(formData.eligibilityYear)

        }

      };


      const response = await fetch(
        `${API_BASE_URL}/api/election-officer/elections`,
        {
          method: "POST",

          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`
          },

          body: JSON.stringify(requestBody)
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

        if (response.status === 401) {

          logout();
          navigate("/login");
          return;

        }

        throw new Error(
          typeof data === "string"
            ? data
            : "Failed to create election."
        );

      }


      /*
       * SUCCESS
       */

      setSuccess(
        `Election "${data?.title || formData.title}" created successfully.`
      );


      /*
       * Wait briefly so the user can see
       * the success message.
       */

      setTimeout(() => {

        navigate("/dashboard");

      }, 1200);


    } catch (error) {

      console.error(
        "Create election error:",
        error
      );

      setError(
        error.message ||
        "Something went wrong while creating the election."
      );

    } finally {

      setLoading(false);

    }

  };


  return (

    <div className="create-election-page">

      <div className="create-election-card">


        {/* HEADER */}

        <div className="create-election-header">

          <button
            type="button"
            className="create-election-back"
            onClick={() =>
              navigate("/dashboard")
            }
          >
            ← Back to Dashboard
          </button>


          <span className="create-election-badge">
            ELECTION OFFICER
          </span>


          <h1>
            Create Election
          </h1>


          <p>
            Create an election and define who is
            eligible to participate.
          </p>

        </div>


        {/* ERROR */}

        {error && (

          <div className="create-election-error">
            ⚠️ {error}
          </div>

        )}


        {/* SUCCESS */}

        {success && (

          <div className="create-election-success">
            ✓ {success}
          </div>

        )}


        {/* FORM */}

        <form
          className="create-election-form"
          onSubmit={handleSubmit}
        >


          {/* BASIC DETAILS */}

          <div className="form-section">

            <div className="form-section-title">

              <h2>
                Election Details
              </h2>

              <p>
                Enter the basic information about
                the election.
              </p>

            </div>


            <div className="form-group">

              <label htmlFor="title">
                Election Title
              </label>

              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleChange}
                placeholder="e.g. MCA 2nd Year Election"
                disabled={loading}
              />

            </div>


            <div className="form-group">

              <label htmlFor="description">
                Description
              </label>

              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                placeholder="Enter the purpose of this election"
                rows="4"
                disabled={loading}
              />

            </div>


            <div className="form-grid">


              <div className="form-group">

                <label htmlFor="postId">
                  Post ID
                </label>

                <input
                  id="postId"
                  name="postId"
                  type="number"
                  min="1"
                  value={formData.postId}
                  onChange={handleChange}
                  placeholder="e.g. 2"
                  disabled={loading}
                />

                <small>
                  Enter the ID of the post for this election.
                </small>

              </div>


              <div className="form-group">

                <label htmlFor="department">
                  Election Department
                </label>

                <input
                  id="department"
                  name="department"
                  type="text"
                  value={formData.department}
                  onChange={handleChange}
                  placeholder="e.g. Computer Applications"
                  disabled={loading}
                />

              </div>

            </div>

          </div>


          {/* ELECTION TIMING */}

          <div className="form-section">

            <div className="form-section-title">

              <h2>
                Election Schedule
              </h2>

              <p>
                Set when voting will start and end.
              </p>

            </div>


            <div className="form-grid">


              <div className="form-group">

                <label htmlFor="startDate">
                  Start Date & Time
                </label>

                <input
                  id="startDate"
                  name="startDate"
                  type="datetime-local"
                  value={formData.startDate}
                  onChange={handleChange}
                  disabled={loading}
                />

              </div>


              <div className="form-group">

                <label htmlFor="endDate">
                  End Date & Time
                </label>

                <input
                  id="endDate"
                  name="endDate"
                  type="datetime-local"
                  value={formData.endDate}
                  onChange={handleChange}
                  disabled={loading}
                />

              </div>

            </div>

          </div>


          {/* ELIGIBILITY */}

          <div className="form-section">

            <div className="form-section-title">

              <h2>
                Eligibility Criteria
              </h2>

              <p>
                Define which students can participate
                in this election.
              </p>

            </div>


            <div className="form-grid">


              <div className="form-group">

                <label htmlFor="eligibilityDepartment">
                  Eligible Department
                </label>

                <input
                  id="eligibilityDepartment"
                  name="eligibilityDepartment"
                  type="text"
                  value={
                    formData.eligibilityDepartment
                  }
                  onChange={handleChange}
                  placeholder="e.g. Computer Applications"
                  disabled={loading}
                />

              </div>


              <div className="form-group">

                <label htmlFor="eligibilityCourse">
                  Eligible Course
                </label>

                <input
                  id="eligibilityCourse"
                  name="eligibilityCourse"
                  type="text"
                  value={
                    formData.eligibilityCourse
                  }
                  onChange={handleChange}
                  placeholder="e.g. MCA"
                  disabled={loading}
                />

              </div>


              <div className="form-group">

                <label htmlFor="eligibilityYear">
                  Eligible Year
                </label>

                <input
                  id="eligibilityYear"
                  name="eligibilityYear"
                  type="number"
                  min="1"
                  max="6"
                  value={
                    formData.eligibilityYear
                  }
                  onChange={handleChange}
                  placeholder="e.g. 2"
                  disabled={loading}
                />

              </div>

            </div>

          </div>


          {/* SUBMIT */}

          <div className="form-actions">

            <button
              type="button"
              className="cancel-election-button"
              onClick={() =>
                navigate("/dashboard")
              }
              disabled={loading}
            >
              Cancel
            </button>


            <button
              type="submit"
              className="submit-election-button"
              disabled={loading}
            >

              {loading
                ? "Creating Election..."
                : "Create Election"}

            </button>

          </div>

        </form>

      </div>

    </div>

  );

}

export default CreateElection;
