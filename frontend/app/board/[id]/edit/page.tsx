'use client';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useStore } from '@/lib/store';
import { useUI } from '@/lib/ui';
import { ApiError } from '@/lib/api';
import { BoardCategory, BoardPostData, getBoardPost, updateBoardPost } from '@/lib/board-api';

const TITLE_MAX = 100;
const CONTENT_MAX = 2000;

const CATEGORY_LABEL: Record<BoardCategory, string> = {
  NOTICE: '공지사항',
  FREE: '자유게시판',
  PLANT_QNA: '식물 Q&A',
};

export default function EditBoardPostPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const { state, hydrated } = useStore();
  const { showToast } = useUI();
  const postId = Number(params.id);

  const [post, setPost] = useState<BoardPostData | null>(null);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!hydrated || Number.isNaN(postId)) return;
    const controller = new AbortController();
    setLoading(true);
    setError('');

    getBoardPost(postId, state.accessToken, controller.signal)
      .then((data) => {
        if (state.user?.id !== data.userId) {
          setError('본인이 작성한 글만 수정할 수 있어요.');
          setPost(null);
          return;
        }
        setPost(data);
        setTitle(data.title);
        setContent(data.content);
      })
      .catch((requestError) => {
        if (requestError instanceof DOMException && requestError.name === 'AbortError') return;
        setPost(null);
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
  }, [hydrated, postId, state.accessToken, state.user?.id]);

  const submit = async () => {
    if (!state.accessToken || !post) return;
    if (!title.trim()) return showToast('제목을 입력해 주세요.', 'err');
    if (!content.trim()) return showToast('내용을 입력해 주세요.', 'err');

    setSubmitting(true);
    try {
      await updateBoardPost(post.id, { title: title.trim(), content: content.trim() }, state.accessToken);
      showToast('게시글을 수정했어요.');
      router.push(`/board/${post.id}`);
    } catch (requestError) {
      showToast(
        requestError instanceof ApiError ? requestError.message : '게시글 수정에 실패했어요. 잠시 후 다시 시도해 주세요.',
        'err',
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (!hydrated || loading) {
    return (
      <div className="container">
        <div className="px-5 py-[60px] text-center text-sub">게시글을 불러오고 있어요 🌱</div>
      </div>
    );
  }

  if (error || !post) {
    return (
      <div className="container">
        <button
          type="button"
          onClick={() => router.back()}
          className="cursor-pointer rounded-[10px] border-[1.5px] border-line bg-white px-3 py-2 text-sm font-semibold text-sub hover:bg-brand-soft hover:text-brand-dark"
        >
          ← 뒤로
        </button>
        <div className="mt-4 px-5 py-[60px] text-center text-sub">{error || '게시글을 찾을 수 없어요.'}</div>
      </div>
    );
  }

  return (
    <div className="container">
      <button
        type="button"
        onClick={() => router.back()}
        className="cursor-pointer rounded-[10px] border-[1.5px] border-line bg-white px-3 py-2 text-sm font-semibold text-sub hover:bg-brand-soft hover:text-brand-dark"
      >
        ← 뒤로
      </button>
      <h1 className="mb-1 mt-3.5 text-[26px] font-extrabold">게시글 수정</h1>
      <p className="mb-[22px] text-[14.5px] text-sub">제목과 내용만 수정할 수 있어요. 카테고리는 바꿀 수 없어요.</p>

      <div className="max-w-[640px] rounded-[20px] bg-white p-6 shadow-card">
        <div className="mb-2.5 font-extrabold">카테고리</div>
        <div className="mb-[22px] inline-block rounded-full border-[1.5px] border-line bg-[#f9faf6] px-[15px] py-2 text-sm font-bold text-[#a9b3a0]">
          {CATEGORY_LABEL[post.category]}
        </div>

        <div className="mb-2.5 font-extrabold">제목</div>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value.slice(0, TITLE_MAX))}
          placeholder="제목을 입력해 주세요"
          maxLength={TITLE_MAX}
          className="mb-[5px] w-full rounded-[14px] border-[1.5px] border-line p-3.5 text-[15px] outline-none"
        />
        <div className="mb-[22px] text-right text-xs text-faint">{title.length} / {TITLE_MAX}</div>

        <div className="mb-2.5 font-extrabold">내용</div>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value.slice(0, CONTENT_MAX))}
          placeholder="내용을 입력해 주세요"
          maxLength={CONTENT_MAX}
          className="min-h-[220px] w-full resize-y rounded-[14px] border-[1.5px] border-line p-3.5 text-[15px] leading-[1.6] outline-none"
        />
        <div className="mt-[5px] text-right text-xs text-faint">{content.length} / {CONTENT_MAX}</div>

        <button
          type="button"
          onClick={submit}
          disabled={submitting}
          className="mt-3 w-full cursor-pointer rounded-[14px] bg-brand p-[15px] text-base font-extrabold text-white disabled:opacity-60"
        >
          {submitting ? '저장 중...' : '수정 완료'}
        </button>
      </div>
    </div>
  );
}
