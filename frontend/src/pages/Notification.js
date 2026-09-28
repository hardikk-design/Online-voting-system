
import { useEffect, useState } from "react";

import API_BASE_URL from "../services/api";
import {
  getToken,
  getUser,
  logout
} from "../services/auth";

import "./Notification.css";

function Notifications() {

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchNotifications = async () => {

    try {

      const token = getToken();
      const user = getUser();

      if (!token || !user) {
        logout();
        window.location.href = "/login";
        return;
      }

      const response = await fetch(
        `${API_BASE_URL}/api/notifications/user/${user.id}`,
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
        window.location.href = "/login";
        return;
      }

      if (!response.ok) {
        throw new Error(
          "Failed to load notifications."
        );
      }

      const data = await response.json();

      setNotifications(data);

    } catch (error) {

      console.error(
        "Notification error:",
        error
      );

      setError(
        error.message ||
        "Unable to load notifications."
      );

    } finally {

      setLoading(false);

    }
  };


  useEffect(() => {
    fetchNotifications();
  }, []);


  const markAsRead = async (notificationId) => {

    try {

      const token = getToken();

      const response = await fetch(
        `${API_BASE_URL}/api/notifications/${notificationId}/read`,
        {
          method: "PUT",
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
        window.location.href = "/login";
        return;
      }

      if (!response.ok) {
        throw new Error(
          "Unable to mark notification as read."
        );
      }

      setNotifications(
        (currentNotifications) =>
          currentNotifications.map(
            (notification) =>
              notification.id === notificationId
                ? {
                    ...notification,
                    read: true
                  }
                : notification
          )
      );

    } catch (error) {

      console.error(
        "Mark notification error:",
        error
      );

    }
  };


  const formatDate = (date) => {

    if (!date) {
      return "";
    }

    return new Date(date).toLocaleString();

  };


  const getNotificationTitle = (type) => {

    if (type === "NOMINATION_APPROVED") {
      return "Nomination Approved";
    }

    if (type === "NOMINATION_REJECTED") {
      return "Nomination Rejected";
    }

    if (type === "EO_ASSIGNED") {
      return "Election Officer Assigned";
    }

    return "Notification";
  };


  if (loading) {
    return (
      <div className="notifications-section">

        <div className="notifications-header">
          <h2>Notifications</h2>
        </div>

        <p className="notifications-loading">
          Loading notifications...
        </p>

      </div>
    );
  }


  return (
    <div className="notifications-section">

      <div className="notifications-header">

        <div>

          <h2>
            Notifications
          </h2>

          <p>
            Updates about your elections and
            nominations
          </p>

        </div>

        <div className="notification-count">

          {
            notifications.filter(
              (notification) =>
                !notification.read
            ).length
          }

          {" "}unread

        </div>

      </div>


      {error && (
        <div className="notifications-error">
          {error}
        </div>
      )}


      {!error &&
        notifications.length === 0 && (

          <div className="no-notifications">

            <div className="no-notifications-icon">
              🔔
            </div>

            <h3>
              No notifications
            </h3>

            <p>
              You don't have any notifications yet.
            </p>

          </div>

        )}


      {notifications.length > 0 && (

        <div className="notification-list">

          {notifications.map(
            (notification) => (

              <div
                key={notification.id}
                className={`notification-card ${
                  notification.read
                    ? "read"
                    : "unread"
                }`}
                onClick={() => {

                  if (!notification.read) {
                    markAsRead(
                      notification.id
                    );
                  }

                }}
              >

                <div className="notification-icon">

                  {
                    notification.type ===
                    "NOMINATION_APPROVED"
                      ? "✓"
                      : notification.type ===
                        "NOMINATION_REJECTED"
                      ? "!"
                      : notification.type ===
                        "EO_ASSIGNED"
                      ? "👨‍💼"
                      : "🔔"
                  }

                </div>


                <div className="notification-content">

                  <div className="notification-title-row">

                    <h3>
                      {getNotificationTitle(
                        notification.type
                      )}
                    </h3>

                    {!notification.read && (
                      <span className="unread-dot"></span>
                    )}

                  </div>


                  <p className="notification-message">
                    {notification.message}
                  </p>


                  <span className="notification-date">
                    {formatDate(
                      notification.createdAt
                    )}
                  </span>

                </div>

              </div>

            )
          )}

        </div>

      )}

    </div>
  );
}

export default Notifications;

