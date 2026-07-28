const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

// Backend error codes/messages: see docs/error-codes.md. The server already returns
// user-facing Korean messages, so no client-side translation table is needed here.
export class ApiError extends Error {
  code: string;
  status: number;

  constructor(code: string, message: string, status: number) {
    super(message);
    this.code = code;
    this.status = status;
  }
}

// The access token lives only in memory (never localStorage) — the store sets this
// whenever it changes so plain fetch calls elsewhere can still attach it.
let currentAccessToken: string | null = null;

export function setAccessToken(token: string | null) {
  currentAccessToken = token;
}

// The store registers a handler that calls /auth/reissue (using the httpOnly refresh
// cookie) and updates in-memory state. Returns the new access token, or null if the
// refresh itself failed (session is over — caller should treat this like a 401).
type UnauthorizedHandler = () => Promise<string | null>;
let onUnauthorized: UnauthorizedHandler | null = null;

export function setUnauthorizedHandler(handler: UnauthorizedHandler | null) {
  onUnauthorized = handler;
}

async function request<T>(path: string, options: RequestInit = {}, isRetry = false): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };
  if (currentAccessToken && !headers.Authorization) {
    headers.Authorization = `Bearer ${currentAccessToken}`;
  }

  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: 'include', // send/receive the httpOnly refresh_token cookie
  });

  // signup/login/reissue/logout never go through the refresh-and-retry flow — a 401
  // there means "bad credentials" or "no session", not "expired token". /auth/me is a
  // regular authenticated endpoint (like any other protected resource), so it's excluded
  // from this list and does get the retry-after-refresh treatment.
  const isCredentialEndpoint = ['/api/v1/auth/signup', '/api/v1/auth/login', '/api/v1/auth/reissue', '/api/v1/auth/logout'].includes(path);
  if (res.status === 401 && !isRetry && onUnauthorized && !isCredentialEndpoint) {
    const newToken = await onUnauthorized();
    if (newToken) {
      return request<T>(path, options, true);
    }
  }

  const body = await res.json().catch(() => null);

  if (!res.ok) {
    const code = body?.code || 'UNKNOWN_ERROR';
    const message = body?.message || '요청 처리 중 문제가 발생했어요.';
    throw new ApiError(code, message, res.status);
  }

  return body.data as T;
}

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  name: string;
  phoneNumber: string | null;
  provider: string;
  role: string;
  level: number;
  experience: number;
  status: string;
  suspendedReason: string | null;
  withdrawnAt: string | null;
  createdAt: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
}

export interface AccessTokenResponse {
  accessToken: string;
  tokenType: string;
  user: UserResponse;
}

export interface SignupPayload {
  email: string;
  password: string;
  nickname: string;
  name: string;
  phoneNumber?: string;
}

export function login(email: string, password: string): Promise<LoginResponse> {
  return request<LoginResponse>('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

export function signup(payload: SignupPayload): Promise<UserResponse> {
  return request<UserResponse>('/api/v1/auth/signup', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export function requestEmailVerification(email: string): Promise<void> {
  return request<void>('/api/v1/auth/signup/email-verification', {
    method: 'POST',
    body: JSON.stringify({ email }),
  });
}

export function confirmEmailVerification(email: string, code: string): Promise<void> {
  return request<void>('/api/v1/auth/signup/email-verification/confirm', {
    method: 'POST',
    body: JSON.stringify({ email, code }),
  });
}

export function requestPasswordResetVerification(email: string): Promise<void> {
  return request<void>('/api/v1/auth/password/reset/email-verification', {
    method: 'POST',
    body: JSON.stringify({ email }),
  });
}

export function confirmPasswordResetVerification(email: string, code: string): Promise<void> {
  return request<void>('/api/v1/auth/password/reset/email-verification/confirm', {
    method: 'POST',
    body: JSON.stringify({ email, code }),
  });
}

export function resetPassword(email: string, newPassword: string): Promise<void> {
  return request<void>('/api/v1/auth/password/reset', {
    method: 'POST',
    body: JSON.stringify({ email, newPassword }),
  });
}

export interface NicknameAvailabilityResponse {
  available: boolean;
}

export function checkNicknameAvailability(nickname: string): Promise<NicknameAvailabilityResponse> {
  return request<NicknameAvailabilityResponse>(
    `/api/v1/auth/signup/nickname-check?nickname=${encodeURIComponent(nickname)}`,
  );
}

// Silent refresh: relies solely on the httpOnly refresh_token cookie, no body needed.
export function reissue(): Promise<AccessTokenResponse> {
  return request<AccessTokenResponse>('/api/v1/auth/reissue', { method: 'POST' });
}

export function logout(): Promise<void> {
  return request<void>('/api/v1/auth/logout', { method: 'POST' });
}

export interface ProfileUpdatePayload {
  nickname?: string;
  name?: string;
  phoneNumber?: string;
}

export function getMe(): Promise<UserResponse> {
  return request<UserResponse>('/api/v1/auth/me');
}

export function updateProfile(payload: ProfileUpdatePayload): Promise<UserResponse> {
  return request<UserResponse>('/api/v1/auth/me', {
    method: 'PATCH',
    body: JSON.stringify(payload),
  });
}

export function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  return request<void>('/api/v1/auth/me/password', {
    method: 'PATCH',
    body: JSON.stringify({ currentPassword, newPassword }),
  });
}

// Re-checks the current password without changing anything — used to gate access
// to sensitive actions like editing the profile.
export function verifyPassword(password: string): Promise<void> {
  return request<void>('/api/v1/auth/me/password/verify', {
    method: 'POST',
    body: JSON.stringify({ password }),
  });
}

// Soft delete: server flips status to WITHDRAWN and revokes every refresh token —
// nothing is physically removed. password is required for LOCAL accounts only.
export function withdraw(password?: string): Promise<void> {
  return request<void>('/api/v1/auth/me/withdraw', {
    method: 'POST',
    body: JSON.stringify({ password: password || null }),
  });
}
