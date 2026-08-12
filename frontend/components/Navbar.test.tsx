import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import Navbar from "./Navbar";

vi.mock("next/navigation", () => ({
  usePathname: () => "/gacha",
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock("@/lib/store", async (importOriginal) => {
  const original = await importOriginal<typeof import("@/lib/store")>();
  return {
    ...original,
    useStore: () => ({
      balance: 0,
      state: {
        authed: false,
        accessToken: null,
        cartCount: 0,
        notifications: [],
        user: null,
      },
      hydrated: false,
      logout: vi.fn(),
      unreadCount: 0,
      markNotifRead: vi.fn(),
      markAllNotifsRead: vi.fn(),
    }),
  };
});

vi.mock("@/lib/ui", () => ({
  useUI: () => ({ showToast: vi.fn(), askConfirm: vi.fn() }),
}));

vi.mock("@/features/gacha/use-gacha-cosmetics", () => ({
  useGachaCosmetics: () => ({ title: null, border: null }),
}));

describe("Navbar", () => {
  afterEach(cleanup);

  it("모바일 하단 '더보기' 시트를 열면 가챠 바로가기를 표시한다", () => {
    const { container } = render(<Navbar />);

    // 데스크톱 상단 내비게이션에는 항상 가챠 링크가 있다.
    expect(container.querySelectorAll('a[href="/gacha"]')).toHaveLength(1);

    // 모바일 하단 탭은 쿠폰/가챠를 "더보기" 시트 안으로 몰아뒀으므로, 열기 전에는 없다가
    // 열면 나타나야 한다.
    fireEvent.click(screen.getByText("더보기"));

    const gachaLinks = container.querySelectorAll('a[href="/gacha"]');
    expect(gachaLinks).toHaveLength(2);
    expect(screen.getByText("casino").closest("a")).toHaveAttribute(
      "href",
      "/gacha",
    );
  });

  it("모바일 하단 탭에 커뮤니티 바로가기를 표시한다", () => {
    render(<Navbar />);

    expect(screen.getByText("forum").closest("a")).toHaveAttribute(
      "href",
      "/board",
    );
  });
});
