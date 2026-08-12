'use client';
import Link from 'next/link';
import { usePathname, useRouter, useSearchParams } from 'next/navigation';
import { Suspense, useEffect, useState } from 'react';
import { ApiError } from '@/lib/api';
import { formatDate } from '@/lib/format';
import { BoardCategory, BoardPostData, getBoardPosts } from '@/lib/board-api';
import { useStore } from '@/lib/store';

const CATEGORY_LABEL: Record<BoardCategory, string> = {
  NOTICE: '공지사항',
  FREE: '자유게시판',
  PLANT_QNA: '식물 Q&A',
};

const CATEGORY_TEXT: Record<BoardCategory, string> = {
  NOTICE: 'text-[#b5872f]',
  FREE: 'text-[#3a76a8]',
  PLANT_QNA: 'text-brand-dark',
};

const TABS: { key: 'ALL' | BoardCategory; label: string }[] = [
  { key: 'ALL', label: '전체' },
  { key: 'NOTICE', label: '공지사항' },
  { key: 'FREE', label: '자유게시판' },
  { key: 'PLANT_QNA', label: '식물 Q&A' },
];

const PAGE_SIZE = 15;
const CATEGORY_KEYS = new Set(['NOTICE', 'FREE', 'PLANT_QNA']);

function parseTab(value: string | null): 'ALL' | BoardCategory {
  return value && CATEGORY_KEYS.has(value) ? (value as BoardCategory) : 'ALL';
}

function parsePage(value: string | null): number {
  const page = Number(value);
  return Number.isFinite(page) && page > 0 ? Math.floor(page) - 1 : 0;
}

export default function BoardPage() {
  return (
    <Suspense fallback={null}>
      <BoardPageContent />
    </Suspense>
  );
}

function BoardPageContent() {
  const { state, hydrated } = useStore();
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();

  const [tab, setTab] = useState<'ALL' | BoardCategory>(() => parseTab(searchParams.get('category')));
  const [page, setPage] = useState(() => parsePage(searchParams.get('page')));
  const [posts, setPosts] = useState<BoardPostData[]>([]);
  const [notices, setNotices] = useState<BoardPostData[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // 공지사항은 카테고리/페이지와 무관하게 항상 맨 위에 고정해서 보여준다.
  useEffect(() => {
    if (!hydrated) return;
    const controller = new AbortController();

    getBoardPosts('NOTICE', 0, 50, state.accessToken, controller.signal)
      .then((result) => setNotices(result.content))
      .catch((requestError) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return;
        setNotices([]);
      });

    return () => controller.abort();
  }, [hydrated, state.accessToken]);

  // 카테고리 탭/페이지를 URL 쿼리로 반영해 새로고침해도 그대로 유지되게 한다.
  useEffect(() => {
    const params = new URLSearchParams();
    if (tab !== 'ALL') params.set('category', tab);
    if (page > 0) params.set('page', String(page + 1));
    const query = params.toString();
    router.replace(query ? `${pathname}?${query}` : pathname, { scroll: false });
  }, [tab, page, pathname, router]);

  useEffect(() => {
    if (!hydrated) return;
    const controller = new AbortController();
    setLoading(true);
    setError('');

    getBoardPosts(tab === 'ALL' ? undefined : tab, page, PAGE_SIZE, state.accessToken, controller.signal)
      .then((result) => {
        setPosts(result.content);
        setTotalElements(result.totalElements);
        setTotalPages(result.totalPages);
      })
      .catch((requestError) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return;
        setPosts([]);
        setError(
          requestError instanceof ApiError
            ? requestError.message
            : '게시글을 불러오지 못했어요. 잠시 후 다시 시도해 주세요.',
        );
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });

    return () => controller.abort();
  }, [hydrated, tab, page, state.accessToken]);

  // NOTICE 탭 자체를 볼 때는 목록이 이미 전부 공지라 별도 고정 영역이 필요 없다.
  const pinned = tab === 'NOTICE' ? [] : notices;
  const normal = tab === 'NOTICE' ? posts : posts.filter((p) => p.category !== 'NOTICE');
  // ALL 탭만 공지가 실제로 목록에 섞여서 totalElements에 포함되니 그만큼 빼준다.
  // FREE/PLANT_QNA/NOTICE는 이미 카테고리로 필터링된 개수라 그대로 쓴다.
  const nonNoticeTotal = tab === 'ALL' ? Math.max(0, totalElements - notices.length) : totalElements;
  const baseNumber = nonNoticeTotal - page * PAGE_SIZE;

  const changeTab = (next: 'ALL' | BoardCategory) => {
    setTab(next);
    setPage(0);
  };

  return (
    <div className="container">
      <div className="mb-1.5 flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-[27px] font-extrabold">커뮤니티 게시판</h1>
        {state.accessToken && (
          <Link
            href="/board/new"
            className="rounded-xl bg-brand px-5 py-3 text-[15px] font-bold text-white hover:text-white"
          >
            + 글쓰기
          </Link>
        )}
      </div>
      <p className="mb-5 text-sub">다른 사람들과 식물 키우는 이야기를 나눠보세요.</p>

      <div className="mb-4 flex flex-wrap gap-2">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            onClick={() => changeTab(t.key)}
            className={`cursor-pointer rounded-full border-[1.5px] px-[15px] py-2 text-sm font-bold ${
              tab === t.key ? 'border-brand bg-brand text-white' : 'border-line bg-white text-[#6d7a68]'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <div className="overflow-hidden rounded-[18px] bg-white shadow-card">
        <div className="grid grid-cols-[28px_1fr_40px_40px] sm:grid-cols-[60px_1fr_90px_84px_56px_56px] items-center gap-2 border-b-2 border-ink/80 px-4 py-3 text-xs font-extrabold text-faint sm:px-5">
          <div className="text-center">번호</div>
          <div>제목</div>
          <div className="hidden text-center sm:block">글쓴이</div>
          <div className="hidden text-center sm:block">작성일</div>
          <div className="text-center">조회</div>
          <div className="text-center">추천</div>
        </div>

        {loading ? (
          <div className="px-5 py-[60px] text-center text-sub">게시글을 불러오고 있어요 🌱</div>
        ) : error ? (
          <div className="px-5 py-[60px] text-center text-sub">{error}</div>
        ) : posts.length === 0 && pinned.length === 0 ? (
          <div className="px-5 py-[60px] text-center text-sub">
            아직 게시글이 없어요. 첫 글을 남겨보세요 🌱
          </div>
        ) : (
          <div className="divide-y divide-[#f0f1ea]">
            {pinned.map((post) => (
              <Link
                key={post.id}
                href={`/board/${post.id}`}
                className="grid grid-cols-[28px_1fr_40px_40px] sm:grid-cols-[60px_1fr_90px_84px_56px_56px] items-center gap-2 bg-brand-soft/40 px-4 py-3 text-ink hover:bg-brand-soft/70 hover:text-ink sm:px-5"
              >
                <div className="text-center text-[15px]">📌</div>
                <div className="flex min-w-0 items-baseline gap-1.5">
                  <span className={`shrink-0 text-xs font-extrabold ${CATEGORY_TEXT[post.category]}`}>
                    [{CATEGORY_LABEL[post.category]}]
                  </span>
                  <span className="min-w-0 truncate font-extrabold">{post.title}</span>
                  {post.commentCount > 0 && (
                    <span className="shrink-0 text-xs font-bold text-[#b5502f]">[{post.commentCount}]</span>
                  )}
                </div>
                <div className="hidden truncate text-center text-sm text-sub sm:block">{post.nickname}</div>
                <div className="hidden text-center text-xs text-faint sm:block">{formatDate(post.createdAt)}</div>
                <div className="text-center text-sm text-faint">{post.viewCount}</div>
                <div className="text-center text-sm font-bold text-[#b5502f]">{post.likeCount}</div>
              </Link>
            ))}

            {normal.length === 0 && (
              <div className="px-5 py-[40px] text-center text-sub">아직 게시글이 없어요.</div>
            )}

            {normal.map((post, index) => (
              <Link
                key={post.id}
                href={`/board/${post.id}`}
                className="grid grid-cols-[28px_1fr_40px_40px] sm:grid-cols-[60px_1fr_90px_84px_56px_56px] items-center gap-2 px-4 py-3 text-ink hover:bg-[#F8FAF3] hover:text-ink sm:px-5"
              >
                <div className="text-center text-sm text-faint">{baseNumber - index}</div>
                <div className="flex min-w-0 items-baseline gap-1.5">
                  <span className={`shrink-0 text-xs font-extrabold ${CATEGORY_TEXT[post.category]}`}>
                    [{CATEGORY_LABEL[post.category]}]
                  </span>
                  <span className="min-w-0 truncate font-bold">{post.title}</span>
                  {post.commentCount > 0 && (
                    <span className="shrink-0 text-xs font-bold text-[#b5502f]">[{post.commentCount}]</span>
                  )}
                </div>
                <div className="hidden truncate text-center text-sm text-sub sm:block">{post.nickname}</div>
                <div className="hidden text-center text-xs text-faint sm:block">{formatDate(post.createdAt)}</div>
                <div className="text-center text-sm text-faint">{post.viewCount}</div>
                <div className="text-center text-sm font-bold text-[#b5502f]">{post.likeCount}</div>
              </Link>
            ))}
          </div>
        )}
      </div>
      {!loading && !error && posts.length > 0 && (
        <>
          <div className="mt-3 text-right text-xs text-faint">전체 {totalElements}개</div>
          {totalPages > 1 && (
            <div className="mt-4 flex flex-wrap items-center justify-center gap-1.5">
              <button
                type="button"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                className="cursor-pointer rounded-lg border-[1.5px] border-line bg-white px-3 py-2 text-sm font-bold text-sub disabled:cursor-not-allowed disabled:opacity-40"
              >
                이전
              </button>
              {Array.from({ length: totalPages }, (_, i) => i).map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => setPage(p)}
                  className={`h-9 min-w-9 cursor-pointer rounded-lg border-[1.5px] px-2 text-sm font-bold ${
                    p === page ? 'border-brand bg-brand text-white' : 'border-line bg-white text-sub'
                  }`}
                >
                  {p + 1}
                </button>
              ))}
              <button
                type="button"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="cursor-pointer rounded-lg border-[1.5px] border-line bg-white px-3 py-2 text-sm font-bold text-sub disabled:cursor-not-allowed disabled:opacity-40"
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
