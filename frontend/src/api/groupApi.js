import axiosClient from "./axiosClient";

export const getAllGroups = (params) =>
  axiosClient.get("/api/groups/groups", { params });

export const getAllPermissions = (params) =>
  axiosClient.get("/api/groups/permissions", { params });

export const createGroup = (data) =>
  axiosClient.post("/api/groups", data);

export const createPermission = (data) =>
  axiosClient.post("/api/groups/permissions", data);

export const assignUserToGroup = (groupId, userId) =>
  axiosClient.post(`/api/groups/${groupId}/users/${userId}`);

export const removeUserFromGroup = (groupId, userId) =>
  axiosClient.delete(`/api/groups/${groupId}/users/${userId}`);

export const addPermissionToGroup = (groupId, perId) =>
  axiosClient.post(`/api/groups/${groupId}/permission/${perId}`);