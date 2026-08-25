import axiosClient from "./axiosClient";

// Backend returns plain text ("User registered successfully!"), not JSON.
export const registerUser = (data) => axiosClient.post("/api/auth/register", data);

// Backend returns the raw JWT string as the response body.
export const loginUser = (data) => axiosClient.post("/api/auth/login", data);
