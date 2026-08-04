"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { ApiError, getAddresses, UserAddress } from "@/lib/api";

export interface AddressFields {
  receiverName: string;
  receiverPhone: string;
  zipCode: string;
  address: string;
  addressDetail: string;
}

export const EMPTY_ADDRESS_FIELDS: AddressFields = {
  receiverName: "",
  receiverPhone: "",
  zipCode: "",
  address: "",
  addressDetail: "",
};

// 백엔드 UserAddress 검증(하이픈 없이 010/011 + 숫자 7~8자리)과 동일한 규칙.
// Order/ExchangeOrder는 이 형식을 서버에서 강제하지 않으므로, 자유 입력 경로에서
// 잘못된 형식이 그대로 저장되지 않도록 프론트에서 검증한다.
const PHONE_PATTERN = /^(010|011)\d{7,8}$/;

export function isValidPhone(phone: string): boolean {
  return PHONE_PATTERN.test(phone);
}

export function isCompleteAddress(fields: AddressFields): boolean {
  return (
    fields.receiverName.trim() !== "" &&
    isValidPhone(fields.receiverPhone) &&
    fields.zipCode.trim() !== "" &&
    fields.address.trim() !== ""
  );
}

const FIELD =
  "w-full rounded-xl border-[1.5px] border-line px-[13px] py-3 outline-none";
const LABEL = "text-[13px] font-bold text-[#6d7a68]";

/**
 * 교환/주문 양쪽에서 공용으로 쓰는 배송지 입력.
 * 저장된 배송지 목록을 불러와 선택하면 필드에 채워주고, 그 필드를 자유롭게 다시 고칠 수 있다.
 * 여기서 고친 값은 이번 주문/교환의 스냅샷일 뿐 마이페이지 배송지북에는 반영되지 않는다.
 */
export default function AddressForm({
  accessToken,
  value,
  onChange,
}: {
  accessToken: string | null;
  value: AddressFields;
  onChange: (fields: AddressFields) => void;
}) {
  const [addresses, setAddresses] = useState<UserAddress[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState("");

  useEffect(() => {
    if (!accessToken) return;
    setLoading(true);
    setLoadError("");
    getAddresses()
      .then((list) => {
        setAddresses(list);
        const defaultAddress = list.find((a) => a.isDefault) || list[0];
        if (defaultAddress) {
          setSelectedId(defaultAddress.id);
          onChange({
            receiverName: defaultAddress.receiverName,
            receiverPhone: defaultAddress.receiverPhone,
            zipCode: defaultAddress.zipCode,
            address: defaultAddress.address,
            addressDetail: defaultAddress.addressDetail || "",
          });
        }
      })
      .catch((requestError) => {
        setLoadError(
          requestError instanceof ApiError
            ? requestError.message
            : "배송지를 불러오지 못했어요.",
        );
      })
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken]);

  const selectSaved = (a: UserAddress) => {
    setSelectedId(a.id);
    onChange({
      receiverName: a.receiverName,
      receiverPhone: a.receiverPhone,
      zipCode: a.zipCode,
      address: a.address,
      addressDetail: a.addressDetail || "",
    });
  };

  const startNew = () => {
    setSelectedId(null);
    onChange(EMPTY_ADDRESS_FIELDS);
  };

  return (
    <div>
      {loading && (
        <div className="mb-3 text-sm text-sub">배송지를 불러오고 있어요…</div>
      )}
      {loadError && <div className="mb-3 text-sm text-danger">{loadError}</div>}

      {!loading && addresses.length === 0 && (
        <div className="mb-3.5 rounded-[14px] border-[1.5px] border-dashed border-[#cfe0b6] bg-white p-3.5 text-center text-sm text-sub">
          등록된 배송지가 없어요. 아래에 새 배송지를 입력해 주세요.{" "}
          <Link href="/my" className="font-bold text-brand-dark underline">
            마이페이지에서 등록하기
          </Link>
        </div>
      )}

      {addresses.length > 0 && (
        <div className="mb-3.5 flex flex-col gap-2">
          {addresses.map((a) => (
            <button
              key={a.id}
              type="button"
              onClick={() => selectSaved(a)}
              className={`cursor-pointer rounded-[14px] border-2 bg-white px-4 py-[13px] text-left ${
                selectedId === a.id ? "border-brand" : "border-[#eceee5]"
              }`}
            >
              <div className="flex items-center gap-2 font-bold">
                {a.receiverName}
                {a.isDefault && (
                  <span className="rounded-full bg-brand-soft px-2 py-0.5 text-[11px] text-brand-dark">
                    기본
                  </span>
                )}
              </div>
              <div className="mt-1 text-[13.5px] text-sub">
                {a.receiverPhone} · [{a.zipCode}] {a.address} {a.addressDetail}
              </div>
            </button>
          ))}
          <button
            type="button"
            onClick={startNew}
            className={`cursor-pointer rounded-[14px] border-2 border-dashed bg-white px-4 py-[13px] text-left text-sm font-bold text-sub ${
              selectedId === null
                ? "border-brand text-brand-dark"
                : "border-[#eceee5]"
            }`}
          >
            + 새 배송지로 입력
          </button>
        </div>
      )}

      <p className="mb-2 text-[12.5px] text-sub">
        {selectedId === null
          ? "이번에만 사용할 배송지를 입력해 주세요."
          : "필요하면 아래 내용을 자유롭게 고쳐서 이번 배송에만 적용할 수 있어요."}
      </p>

      <label className={LABEL}>
        받는 분 <span className="text-[#e5533b]">*</span>
      </label>
      <input
        value={value.receiverName}
        onChange={(e) => onChange({ ...value, receiverName: e.target.value })}
        maxLength={50}
        placeholder="이름"
        className={`${FIELD} mb-3.5 mt-1.5`}
      />
      <label className={LABEL}>
        연락처 <span className="text-[#e5533b]">*</span>
      </label>
      <input
        value={value.receiverPhone}
        onChange={(e) =>
          onChange({
            ...value,
            receiverPhone: e.target.value.replace(/[^0-9]/g, ""),
          })
        }
        maxLength={11}
        inputMode="numeric"
        placeholder="01000000000 (하이픈 없이 숫자만)"
        className={`${FIELD} mt-1.5 ${
          value.receiverPhone !== "" && !isValidPhone(value.receiverPhone)
            ? "mb-1.5 border-danger"
            : "mb-3.5"
        }`}
      />
      {value.receiverPhone !== "" && !isValidPhone(value.receiverPhone) && (
        <p className="mb-3.5 text-[12.5px] text-danger">
          010 또는 011로 시작하는 숫자 9~11자리로 입력해 주세요.
        </p>
      )}
      <label className={LABEL}>
        우편번호 <span className="text-[#e5533b]">*</span>
      </label>
      <input
        value={value.zipCode}
        onChange={(e) =>
          onChange({ ...value, zipCode: e.target.value.replace(/[^0-9]/g, "") })
        }
        maxLength={10}
        inputMode="numeric"
        placeholder="12345"
        className={`${FIELD} mb-3.5 mt-1.5`}
      />
      <label className={LABEL}>
        주소 <span className="text-[#e5533b]">*</span>
      </label>
      <input
        value={value.address}
        onChange={(e) => onChange({ ...value, address: e.target.value })}
        maxLength={200}
        placeholder="도로명 주소"
        className={`${FIELD} mb-3.5 mt-1.5`}
      />
      <label className={LABEL}>상세 주소</label>
      <input
        value={value.addressDetail}
        onChange={(e) => onChange({ ...value, addressDetail: e.target.value })}
        maxLength={100}
        placeholder="동/호수 등"
        className={`${FIELD} mb-3.5 mt-1.5`}
      />
    </div>
  );
}
