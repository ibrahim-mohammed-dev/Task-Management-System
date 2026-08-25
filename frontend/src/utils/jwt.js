// Small helper to read data out of a JWT on the client side.
// Your token only carries { sub: username, iat, exp } — no role — so this
// is just used to pre-check expiry before bothering to call the API.
// The actual username/role come from GET /api/users/me instead.

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
