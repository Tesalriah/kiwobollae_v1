'use client';
import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useUI } from '@/lib/ui';
import { useStore } from '@/lib/store';
import { ADDRESSES } from '@/lib/data';
import { getMe, updateProfile, ApiError, type UserResponse } from '@/lib/api';
import { levelTitle } from '@/lib/levels';

const LINKS = [
  { icon: 'receipt_long', label: '주문 내역', href: '/my/orders' },
  { icon: 'redeem', label: '교환 내역', href: '/my/exchanges' },
  { icon: 'paid', label: '포인트 내역', href: '/my/points' },
  { icon: 'style', label: '내 카드', href: '/my/cards' },
  { icon: 'menu_book', label: '내 일지', href: '/journals' },
  { icon: 'mail', label: '1:1 문의', href: '/my/inquiries' },
];

const FIELD = 'w-full rounded-xl border-[1.5px] border-line px-3.5 py-[11px] text-[15px] outline-none';
const LABEL = 'text-[13px] font-bold text-[#6d7a68]';

export default function MyPage() {
  const { showToast } = useUI();
  const { state, set, logout } = useStore();
  const router = useRouter();
  const [addresses, setAddresses] = useState(ADDRESSES);

  const [profile, setProfile] = useState<UserResponse | null>(null);
  const [editing, setEditing] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [nickname, setNickname] = useState('');
  const [name, setName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');

  useEffect(() => {
    getMe()
      .then((res) => {
        setProfile(res);
        setNickname(res.nickname);
        setName(res.name);
        setPhoneNumber(res.phoneNumber ?? '');
      })
      .catch(() => {
        // 헤더의 캐시된 정보로도 화면은 그릴 수 있으니 토스트 정도만 남김
        showToast('프로필을 불러오지 못했어요.', 'err');
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openEdit = () => {
    if (!profile) return;
    setNickname(profile.nickname);
    setName(profile.name);
    setPhoneNumber(profile.phoneNumber ?? '');
    setFormError('');
    setEditing(true);
  };

  const submitEdit = async () => {
    setFormError('');
    setSubmitting(true);
    try {
      const updated = await updateProfile({ nickname, name, phoneNumber: phoneNumber || undefined });
      setProfile(updated);
      // 헤더/네브바가 참조하는 store의 nickname·level도 같이 갱신
      set((s) => (s.user ? { user: { ...s.user, nickname: updated.nickname, level: updated.level } } : {}));
      setEditing(false);
      showToast('프로필을 수정했어요.');
    } catch (e) {
      setFormError(e instanceof ApiError ? e.message : '프로필 수정에 실패했어요. 다시 시도해 주세요.');
    } finally {
      setSubmitting(false);
    }
  };

  const setDefault = (id: number) => {
    setAddresses(addresses.map((a) => ({ ...a, isDefault: a.id === id })));
    showToast('기본 배송지를 변경했어요.');
  };

  const doLogout = () => {
    logout();
    showToast('로그아웃했어요.');
    router.push('/');
  };

  const displayNickname = profile?.nickname ?? state.user?.nickname ?? '게스트';
  const displayLevel = profile?.level ?? state.user?.level ?? 1;
  const displayEmail = profile?.email ?? state.user?.email ?? '';

  return (
    <div className="container max-w-[960px]">
      <div className="mb-6 flex flex-wrap items-center gap-[18px] rounded-[20px] bg-white p-6 shadow-card">
        <div className="flex h-[72px] w-[72px] items-center justify-center rounded-full bg-gradient-to-br from-[#AED581] to-[#7CB342] text-3xl font-extrabold text-white">
          {displayNickname.charAt(0)}
        </div>
        <div className="min-w-[180px] flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <span className="text-xl font-extrabold">{displayNickname}</span>
            <span className="whitespace-nowrap rounded-full bg-brand-soft px-[11px] py-1 text-xs font-extrabold text-brand-dark">Lv.{displayLevel} {levelTitle(displayLevel)}</span>
          </div>
          <div className="mt-1 text-sm text-sub">{displayEmail}</div>
        </div>
        <button
          type="button"
          onClick={openEdit}
          disabled={!profile}
          className="cursor-pointer rounded-[11px] bg-brand-soft px-[18px] py-[11px] font-bold text-brand-dark disabled:opacity-60"
        >
          프로필 수정
        </button>
      </div>

      {editing && (
        <div className="mb-6 rounded-[20px] bg-white p-6 shadow-card">
          <h2 className="mb-4 text-lg font-extrabold">프로필 수정</h2>
          <div className="flex flex-col gap-3.5">
            <div>
              <label className={LABEL}>닉네임</label>
              <input value={nickname} onChange={(e) => setNickname(e.target.value)} className={`${FIELD} mt-1.5`} />
            </div>
            <div>
              <label className={LABEL}>이름</label>
              <input value={name} onChange={(e) => setName(e.target.value)} className={`${FIELD} mt-1.5`} />
            </div>
            <div>
              <label className={LABEL}>전화번호</label>
              <input value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)} placeholder="010-0000-0000" className={`${FIELD} mt-1.5`} />
            </div>
          </div>

          {formError && (
            <div className="mt-3.5 rounded-[11px] bg-danger-soft px-[13px] py-[11px] text-[13px] font-semibold text-danger">
              {formError}
            </div>
          )}

          <div className="mt-5 flex gap-2.5">
            <button
              type="button"
              disabled={submitting}
              onClick={submitEdit}
              className="flex-1 cursor-pointer rounded-xl bg-brand p-3 font-bold text-white transition-colors duration-150 hover:bg-brand-dark disabled:opacity-60"
            >
              저장
            </button>
            <button
              type="button"
              onClick={() => setEditing(false)}
              className="flex-1 cursor-pointer rounded-xl border-[1.5px] border-line bg-white p-3 font-bold text-sub"
            >
              취소
            </button>
          </div>
        </div>
      )}

      <h2 className="mb-3.5 text-lg font-extrabold">바로가기</h2>
      <div className="mb-7 grid gap-3.5 [grid-template-columns:repeat(auto-fill,minmax(150px,1fr))]">
        {LINKS.map((l) => (
          <Link key={l.label} href={l.href} className="rounded-2xl bg-white px-[18px] py-5 text-ink shadow-card hover:text-ink">
            <div><span className="material-symbols-outlined text-[28px] text-brand">{l.icon}</span></div>
            <div className="mt-2 font-extrabold">{l.label}</div>
          </Link>
        ))}
      </div>

      <h2 className="mb-3.5 text-lg font-extrabold">배송지 관리</h2>
      <div className="flex flex-col gap-3">
        {addresses.map((a) => (
          <div key={a.id} className="flex flex-wrap items-center gap-3 rounded-[14px] bg-white px-[18px] py-4 shadow-card">
            <div className="min-w-[180px] flex-1">
              <div className="flex items-center gap-2 font-bold">
                {a.name}
                {a.isDefault && <span className="rounded-full bg-brand-soft px-2 py-0.5 text-[11px] text-brand-dark">기본</span>}
              </div>
              <div className="mt-1 text-[13.5px] text-sub">{a.phone} · {a.addr}</div>
            </div>
            {!a.isDefault && (
              <button type="button" onClick={() => setDefault(a.id)} className="cursor-pointer rounded-[10px] border-[1.5px] border-[#cfe0b6] bg-white px-3.5 py-2 text-[13px] font-bold text-brand-dark">
                기본으로
              </button>
            )}
            <button type="button" className="cursor-pointer rounded-[10px] border-[1.5px] border-line bg-white px-3 py-2 text-[13px] font-bold text-sub">수정</button>
          </div>
        ))}
        <button type="button" className="cursor-pointer rounded-[14px] border-[1.5px] border-dashed border-[#cfe0b6] bg-white p-3.5 text-center font-bold text-brand-dark">
          + 새 배송지 추가
        </button>
      </div>

      <button
        type="button"
        onClick={doLogout}
        className="mt-7 w-full cursor-pointer rounded-[14px] border-[1.5px] border-line bg-white p-3.5 text-center font-bold text-danger"
      >
        로그아웃
      </button>
    </div>
  );
}
