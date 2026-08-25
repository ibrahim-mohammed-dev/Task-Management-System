import axiosClient from "./axiosClient";

// Requires the UserController with GET /api/users/me added on the backend.
// Returns the logged-in user's UserResponseDto: { id, username, email, role }.
export const getCurrentUser = () => axiosClient.get("/api/users/me");
