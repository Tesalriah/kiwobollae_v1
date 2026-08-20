-- ChargeProductInitData 대체분. 원본은 app.seed.charge-product.enabled가 local에서는
-- (matchIfMissing=true라) 기본 true, prod에서는 명시적으로 false였다 — 운영 충전 상품은
-- 관리자 콘솔에서 구성하고 자동 생성은 막는다는 prod 쪽 주석 그대로다. 그래서 이 파일은
-- dev-seed-data.sql과 분리해 local의 spring.sql.init.data-locations에만 포함시켰다.
-- charge_products는 BaseEntity가 아니라 created_at/updated_at 없는 마스터 데이터이며
-- @Version 컬럼(version)이 있어 명시적으로 0을 채운다.
INSERT IGNORE INTO charge_products (id, name, price, point_amount, is_active, version) VALUES
	(1, '1,000P 충전', 1000, 1000, true, 0),
	(2, '5,000P 충전', 5000, 5000, true, 0),
	(3, '10,000P 충전', 10000, 10000, true, 0);
