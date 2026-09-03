// Helper to decode JWT tokens and read payload claims (sub, permissions, groups, exp).

export function decodeToken(token) {
  try {
    const payloadBase64 = token.split(".")[1];
    const normalized = payloadBase64.replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(normalized));
  } catch (e) {
    return null;
  }
}

export function isTokenExpired(decoded) {
  if (!decoded || !decoded.exp) return false;
  return Date.now() >= decoded.exp * 1000;
}
