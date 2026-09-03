import axiosClient from "./axiosClient";

export const getAllUsers = (params) =>
  axiosClient.get("/api/admin/users", { params });

export const getAllTasksAdmin = (params) =>
  axiosClient.get("/api/admin/tasks", { params });

export const deleteTaskAdmin = (id) =>
  axiosClient.delete(`/api/admin/tasks/${id}`);
