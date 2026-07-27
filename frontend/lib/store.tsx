'use client';
// 키워볼래 — shared cross-page store (React Context + localStorage persistence)
// Mirrors the prototype store.js: single wallet, journal/plant/card counters.
//
// accessToken/user/authed are persisted to localStorage along with everything
// else, so a page reload keeps the session without any silent-refresh round
// trip. lib/api.ts still keeps its own in-memory copy of the access token
// (kept in sync via setAccessToken below) purely so it can attach the
// Authorization header without importing the store.
import { createContext, useContext, useEffect, useState, useCallback, ReactNode } from 'react';
import { reissue, logout as apiLogout, setAccessToken, setUnauthorizedHandler } from '@/lib/api';

const KEY = 'kwb_store_v1';

export interface Wallet {
  free: number;
  paid: number;
}

export type NotificationType = 'DELIVERY' | 'POINT' | 'JOURNAL_REMINDER' | 'INQUIRY' | 'NOTICE';

export interface NotificationItem {
  id: number;
  type: NotificationType;
  title: string;
  content: string;
  date: string;
  unread: boolean;
  broken?: boolean;
}

// Trimmed to just what the UI actually needs (identity, nav display name, role
// gating) — deliberately not the full UserResponse, so phone/status/etc. never
// end up sitting in localStorage.
export interface CurrentUser {
  id: number;
  email: string;
  nickname: string;
  role: string;
  level: number;
}

export interface StoreState {
  authed: boolean;
  accessToken: string | null;
  user: CurrentUser | null;
  wallet: Wallet;
  rewardedToday: boolean;
  wroteToday: boolean;
  growingCount: number;
  plantCount: number;
  readyCards: number;
  cartCount: number;
  lastReward: number;
  notifications: NotificationItem[];
}

export type StorePatch = Partial<StoreState> | ((s: StoreState) => Partial<StoreState>);

export interface StoreContextValue {
  state: StoreState;
  hydrated: boolean;
  set: (patch: StorePatch) => void;
  spend: (amount: number) => void;
  creditFree: (amount: number) => void;
  creditPaid: (amount: number) => void;
  reset: () => void;
  balance: number;
  unreadCount: number;
  markNotifRead: (id: number) => void;
  markAllNotifsRead: () => void;
  login: (accessToken: string, user: CurrentUser) => void;
  logout: () => void;
}

const DEFAULTS: StoreState = {
  authed: false,
  accessToken: null,
  user: null,
  wallet: { free: 1240, paid: 3000 },
  rewardedToday: false,
  wroteToday: false,
  growingCount: 3,
  plantCount: 5,
  readyCards: 2,
  cartCount: 4,
  lastReward: 30,
  notifications: [
    { id: 1, type: 'DELIVERY', title: '주문하신 상품이 배송을 시작했어요 📦', content: 'ORD-20260709-0022 · 방울토마토 모종', date: '오늘', unread: true },
    { id: 2, type: 'POINT', title: '일지 보상 30P가 지급됐어요 ☀️', content: '토실이의 오늘 기록', date: '오늘', unread: true },
    { id: 3, type: 'JOURNAL_REMINDER', title: '오늘 쌈싸리의 모습을 남겨볼까요? 🌱', content: '아직 오늘의 일지를 쓰지 않으셨어요', date: '오늘', unread: true },
    { id: 4, type: 'INQUIRY', title: '문의하신 내용에 답변이 도착했어요 💬', content: '배송 관련 문의', date: '어제', unread: false, broken: false },
    { id: 5, type: 'NOTICE', title: '새로운 카드가 상점에 입고됐어요 📢', content: '감자 카드를 만나보세요', date: '어제', unread: false, broken: true },
  ],
};

const StoreCtx = createContext<StoreContextValue | null>(null);

export function StoreProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<StoreState>(DEFAULTS);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    try {
      const raw = localStorage.getItem(KEY);
      if (raw) setState({ ...DEFAULTS, ...JSON.parse(raw) });
    } catch (e) {}
    setHydrated(true);
  }, []);

  // Keep lib/api.ts's in-memory copy of the token in sync with whatever was
  // just restored from (or later written to) localStorage.
  useEffect(() => {
    setAccessToken(state.accessToken);
  }, [state.accessToken]);

  // Registered once so any plain `request()` call in lib/api.ts that gets a 401 can
  // trigger the same silent-refresh flow and retry with the new token.
  useEffect(() => {
    setUnauthorizedHandler(async () => {
      try {
        const res = await reissue();
        const user: CurrentUser = {
          id: res.user.id,
          email: res.user.email,
          nickname: res.user.nickname,
          role: res.user.role,
          level: res.user.level,
        };
        setState((s) => ({ ...s, authed: true, accessToken: res.accessToken, user }));
        return res.accessToken;
      } catch {
        setState((s) => ({ ...s, authed: false, accessToken: null, user: null }));
        return null;
      }
    });
    return () => setUnauthorizedHandler(null);
  }, []);

  useEffect(() => {
    if (!hydrated) return;
    try { localStorage.setItem(KEY, JSON.stringify(state)); } catch (e) {}
  }, [state, hydrated]);

  const set = useCallback((patch: StorePatch) => {
    setState((s) => ({ ...s, ...(typeof patch === 'function' ? patch(s) : patch) }));
  }, []);

  // 무상 포인트 먼저 차감
  const spend = useCallback((amount: number) => {
    setState((s) => {
      let { free, paid } = s.wallet;
      const uf = Math.min(free, amount);
      free -= uf; paid -= (amount - uf);
      return { ...s, wallet: { free: Math.max(0, free), paid: Math.max(0, paid) } };
    });
  }, []);

  const creditFree = useCallback((amount: number) => {
    setState((s) => ({ ...s, wallet: { free: s.wallet.free + amount, paid: s.wallet.paid } }));
  }, []);

  const creditPaid = useCallback((amount: number) => {
    setState((s) => ({ ...s, wallet: { free: s.wallet.free, paid: s.wallet.paid + amount } }));
  }, []);

  const reset = useCallback(() => setState(DEFAULTS), []);

  const login = useCallback((accessToken: string, user: CurrentUser) => {
    // Re-pick fields explicitly: callers may pass a full UserResponse (structurally
    // compatible), but only id/email/nickname/role/level should ever reach localStorage.
    const trimmed: CurrentUser = {
      id: user.id,
      email: user.email,
      nickname: user.nickname,
      role: user.role,
      level: user.level,
    };
    setState((s) => ({ ...s, authed: true, accessToken, user: trimmed }));
  }, []);
  const logout = useCallback(() => {
    apiLogout().catch(() => {}); // best-effort: revoke the refresh token server-side too
    setState((s) => ({ ...s, authed: false, accessToken: null, user: null }));
  }, []);

  const markNotifRead = useCallback((id: number) => {
    setState((s) => ({ ...s, notifications: s.notifications.map((n) => (n.id === id ? { ...n, unread: false } : n)) }));
  }, []);

  const markAllNotifsRead = useCallback(() => {
    setState((s) => ({ ...s, notifications: s.notifications.map((n) => ({ ...n, unread: false })) }));
  }, []);

  const balance = state.wallet.free + state.wallet.paid;
  const unreadCount = state.notifications.filter((n) => n.unread).length;

  return (
    <StoreCtx.Provider
      value={{ state, hydrated, set, spend, creditFree, creditPaid, reset, balance, unreadCount, markNotifRead, markAllNotifsRead, login, logout }}
    >
      {children}
    </StoreCtx.Provider>
  );
}

export function useStore(): StoreContextValue {
  const ctx = useContext(StoreCtx);
  if (!ctx) throw new Error('useStore must be used within <StoreProvider>');
  return ctx;
}

export const fmt = (n: number | string) => Number(n).toLocaleString('en-US');
