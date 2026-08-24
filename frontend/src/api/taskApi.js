import axiosClient from "./axiosClient";

export const getTasks = (params) => axiosClient.get("/api/tasks", { params });

export const getTaskById = (id) => axiosClient.get(`/api/tasks/${id}`);

export const createTask = (data) => axiosClient.post("/api/tasks", data);

export const updateTask = (id, data) => axiosClient.put(`/api/tasks/${id}`, data);

export const toggleTask = (id) => axiosClient.patch(`/api/tasks/${id}/toggle`);

export const deleteTask = (id) => axiosClient.delete(`/api/tasks/${id}`);
