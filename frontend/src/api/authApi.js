import axiosClient from "./axiosClient";

// Backend returns plain text ("User registered successfully!"), not JSON.
export const registerUser = (data) => axiosClient.post("/api/auth/register", data);

// Backend returns AuthResponseDto: { token, refreshToken, type }
export const loginUser = (data) => axiosClient.post("/api/auth/login", data);

// Backend accepts RefreshTokenRequestDto: { refreshToken } and returns AuthResponseDto
export const refreshTokenUser = (refreshToken) =>
  axiosClient.post("/api/auth/refresh", { refreshToken });

// Backend invalidates refresh token for current user
export const logoutUser = () => axiosClient.post("/api/auth/logout");

