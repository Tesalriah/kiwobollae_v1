-- =====================================================================================
-- Dev/local(+prod 임시) 시드 데이터.
--
-- InitData/ProductInitData/CardInitData/GachaPackProductInitData/GachaCardInitData/
-- PlantProfileInitData/PlantJournalInitData/NotificationInitData/BoardInitData
-- ApplicationRunner들을 대체한다(PointScenarioInitData는 실제 서비스 로직을 태우는
-- 시나리오라 Java로 남겨둠).
--
-- 주의:
--   * 이 파일은 "완전히 빈 스키마"를 전제로 한다(local 기본값인 ddl-auto: create와
--     짝을 이룸). AUTO_INCREMENT PK에 명시적 id를 직접 넣는다 — MySQL은 이후 실제
--     INSERT에서 그 값을 넘는 번호부터 자동 채번하므로 auto_increment 카운터를 별도로
--     맞춰줄 필요는 없다.
--   * 데이터가 이미 있는 DB(운영 등)에 그대로 재실행하면 안 된다. 재실행 시 실패 대신
--     조용히 건너뛰도록 모든 INSERT를 INSERT IGNORE로 작성했지만, 이는 안전망일 뿐
--     운영 DB에 대한 반복 실행을 권장하는 것은 아니다.
--   * charge_products 시드는 별도 파일(dev-seed-charge-products.sql)로 분리했다 —
--     원본 ChargeProductInitData만 prod에서 app.seed.charge-product.enabled=false로
--     꺼져 있었기 때문에(나머지는 전부 local/prod 공통 true), 이 파일은 local/prod
--     공통으로 로드하고 그 파일은 local에서만 로드하도록 application-*.yaml에서
--     spring.sql.init.data-locations를 다르게 구성했다.
-- =====================================================================================

-- ------------------------------------------------------------------
-- 1) InitData: 시드 유저 3명 + 지갑 + 기본 배송지
--    비밀번호는 전부 "1234"의 BCrypt(strength 10) 해시(spring-security-crypto
--    BCryptPasswordEncoder()로 직접 생성 후 matches("1234", hash)==true 검증 완료).
-- ------------------------------------------------------------------
INSERT IGNORE INTO users
	(id, email, password, nickname, name, phone_number, provider, provider_id, role, status, suspended_reason, withdrawn_at, create_at, update_at)
VALUES
	(1, 'admin@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '관리자', '관리자', '01011112222', 'LOCAL', NULL, 'ADMIN', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(2, 'test@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '초록', '김초록', '01022223333', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(3, 'user@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '바질이', '박바질', '01033334444', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(4, 'seed04@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '촉촉한히아신스', '정하준', '01011482212', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(5, 'seed05@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '푸릇한바질', '강채원', '01011852265', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(6, 'seed06@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '봄날의딸기', '조도윤', '01012222318', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(7, 'seed07@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싹트는선인장', '윤은우', '01012592371', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(8, 'seed08@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '달콤한민트', '장건우', '01012962424', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(9, 'seed09@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱싱한히아신스', '임하윤', '01013332477', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(10, 'seed10@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '작은바질', '한지호', '01013702530', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(11, 'seed11@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '여린딸기', '오현우', '01014072583', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(12, 'seed12@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '튼튼한선인장', '서시우', '01014442636', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(13, 'seed13@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '조용한민트', '신다은', '01014812689', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(14, 'seed14@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '반짝이는히아신스', '권윤서', '01015182742', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(15, 'seed15@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '초록바질', '황지우', '01015552795', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(16, 'seed16@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱그런딸기', '안수아', '01015922848', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(17, 'seed17@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '포근한선인장', '송아린', '01016292901', 'LOCAL', NULL, 'USER', 'SUSPENDED', '커뮤니티 이용 규칙 위반', NULL, NOW(), NOW()),
	(18, 'seed18@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '햇살민트', '전예준', '01016662954', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(19, 'seed19@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '푸릇한튤립', '홍지안', '01017033007', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(20, 'seed20@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '봄날의토마토', '김민준', '01017403060', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(21, 'seed21@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싹트는로즈마리', '이서준', '01017773113', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(22, 'seed22@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '달콤한제라늄', '박유진', '01018143166', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(23, 'seed23@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱싱한고사리', '최서연', '01018513219', 'LOCAL', NULL, 'USER', 'WITHDRAWN', NULL, NOW(), NOW(), NOW()),
	(24, 'seed24@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '작은튤립', '정하준', '01018883272', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(25, 'seed25@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '여린토마토', '강채원', '01019253325', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(26, 'seed26@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '튼튼한로즈마리', '조도윤', '01019623378', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(27, 'seed27@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '조용한제라늄', '윤은우', '01019993431', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(28, 'seed28@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '반짝이는고사리', '장건우', '01020363484', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(29, 'seed29@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '초록튤립', '임하윤', '01020733537', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(30, 'seed30@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱그런토마토', '한지호', '01021103590', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(31, 'seed31@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '포근한로즈마리', '오현우', '01021473643', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(32, 'seed32@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '햇살제라늄', '서시우', '01021843696', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(33, 'seed33@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '촉촉한고사리', '신다은', '01022213749', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(34, 'seed34@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '봄날의해바라기', '권윤서', '01022583802', 'LOCAL', NULL, 'USER', 'SUSPENDED', '커뮤니티 이용 규칙 위반', NULL, NOW(), NOW()),
	(35, 'seed35@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싹트는상추', '황지우', '01022953855', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(36, 'seed36@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '달콤한다육이', '안수아', '01023323908', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(37, 'seed37@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱싱한라벤더', '송아린', '01023693961', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(38, 'seed38@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '작은allium', '전예준', '01024064014', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(39, 'seed39@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '여린해바라기', '홍지안', '01024434067', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(40, 'seed40@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '튼튼한상추', '김민준', '01024804120', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(41, 'seed41@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '조용한다육이', '이서준', '01025174173', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(42, 'seed42@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '반짝이는라벤더', '박유진', '01025544226', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(43, 'seed43@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '초록allium', '최서연', '01025914279', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(44, 'seed44@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱그런해바라기', '정하준', '01026284332', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(45, 'seed45@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '포근한상추', '강채원', '01026654385', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(46, 'seed46@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '햇살다육이', '조도윤', '01027024438', 'LOCAL', NULL, 'USER', 'WITHDRAWN', NULL, NOW(), NOW(), NOW()),
	(47, 'seed47@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '촉촉한라벤더', '윤은우', '01027394491', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(48, 'seed48@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '푸릇한allium', '장건우', '01027764544', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(49, 'seed49@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싹트는바질', '임하윤', '01028134597', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(50, 'seed50@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '달콤한딸기', '한지호', '01028504650', 'LOCAL', NULL, 'USER', 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(51, 'seed51@test.com', '$2a$10$gA93zEFDYrcDoGPPVuQCsuX9wzEZU3wl6tiwHmm0S7JEp.zii4ltS', '싱싱한선인장', '오현우', '01028874703', 'LOCAL', NULL, 'USER', 'SUSPENDED', '커뮤니티 이용 규칙 위반', NULL, NOW(), NOW());

INSERT IGNORE INTO wallets (id, user_id, paid_point, free_point, created_at, updated_at) VALUES
	(1, 1, 3000, 1240, NOW(), NOW()),
	(2, 2, 3000, 1240, NOW(), NOW()),
	(3, 3, 0, 500, NOW(), NOW()),
	(4, 4, 116, 212, NOW(), NOW()),
	(5, 5, 145, 265, NOW(), NOW()),
	(6, 6, 174, 318, NOW(), NOW()),
	(7, 7, 203, 371, NOW(), NOW()),
	(8, 8, 232, 424, NOW(), NOW()),
	(9, 9, 261, 477, NOW(), NOW()),
	(10, 10, 290, 530, NOW(), NOW()),
	(11, 11, 319, 583, NOW(), NOW()),
	(12, 12, 348, 636, NOW(), NOW()),
	(13, 13, 377, 689, NOW(), NOW()),
	(14, 14, 406, 742, NOW(), NOW()),
	(15, 15, 435, 795, NOW(), NOW()),
	(16, 16, 464, 848, NOW(), NOW()),
	(17, 17, 493, 901, NOW(), NOW()),
	(18, 18, 522, 954, NOW(), NOW()),
	(19, 19, 551, 1007, NOW(), NOW()),
	(20, 20, 580, 1060, NOW(), NOW()),
	(21, 21, 609, 1113, NOW(), NOW()),
	(22, 22, 638, 1166, NOW(), NOW()),
	(23, 23, 667, 1219, NOW(), NOW()),
	(24, 24, 696, 1272, NOW(), NOW()),
	(25, 25, 725, 1325, NOW(), NOW()),
	(26, 26, 754, 1378, NOW(), NOW()),
	(27, 27, 783, 1431, NOW(), NOW()),
	(28, 28, 812, 1484, NOW(), NOW()),
	(29, 29, 841, 1537, NOW(), NOW()),
	(30, 30, 870, 1590, NOW(), NOW()),
	(31, 31, 899, 1643, NOW(), NOW()),
	(32, 32, 928, 1696, NOW(), NOW()),
	(33, 33, 957, 1749, NOW(), NOW()),
	(34, 34, 986, 1802, NOW(), NOW()),
	(35, 35, 1015, 1855, NOW(), NOW()),
	(36, 36, 1044, 1908, NOW(), NOW()),
	(37, 37, 1073, 1961, NOW(), NOW()),
	(38, 38, 1102, 14, NOW(), NOW()),
	(39, 39, 1131, 67, NOW(), NOW()),
	(40, 40, 1160, 120, NOW(), NOW()),
	(41, 41, 1189, 173, NOW(), NOW()),
	(42, 42, 1218, 226, NOW(), NOW()),
	(43, 43, 1247, 279, NOW(), NOW()),
	(44, 44, 1276, 332, NOW(), NOW()),
	(45, 45, 1305, 385, NOW(), NOW()),
	(46, 46, 1334, 438, NOW(), NOW()),
	(47, 47, 1363, 491, NOW(), NOW()),
	(48, 48, 1392, 544, NOW(), NOW()),
	(49, 49, 1421, 597, NOW(), NOW()),
	(50, 50, 1450, 650, NOW(), NOW()),
	(51, 51, 1479, 703, NOW(), NOW());

INSERT IGNORE INTO user_address
	(id, user_id, receiver_name, receiver_phone, zip_code, address, address_detail, is_default, create_at, update_at)
VALUES
	(1, 1, '관리자', '01011112222', '06236', '서울특별시 강남구 테헤란로 123', '101동 202호', true, NOW(), NOW()),
	(2, 2, '김초록', '01022223333', '04524', '서울특별시 중구 세종대로 110', '1층', true, NOW(), NOW()),
	(3, 3, '박바질', '01033334444', '48058', '부산광역시 해운대구 센텀중앙로 90', '302호', true, NOW(), NOW()),
	(4, 4, '정하준', '01011482212', '34141', '대전광역시 유성구 대학로 14', '105동 505호', true, NOW(), NOW()),
	(5, 5, '강채원', '01011852265', '42176', '대구광역시 수성구 동대구로 15', '106동 606호', true, NOW(), NOW()),
	(6, 6, '조도윤', '01012222318', '61945', '광주광역시 서구 상무중앙로 16', '7층', true, NOW(), NOW()),
	(7, 7, '윤은우', '01012592371', '48058', '부산광역시 해운대구 센텀중앙로 17', '108동 808호', true, NOW(), NOW()),
	(8, 8, '장건우', '01012962424', '44705', '울산광역시 남구 삼산로 18', '109동 909호', true, NOW(), NOW()),
	(9, 9, '임하윤', '01013332477', '24341', '강원특별자치도 춘천시 중앙로 19', '10층', true, NOW(), NOW()),
	(10, 10, '한지호', '01013702530', '04030', '서울특별시 마포구 월드컵로 20', '111동 202호', true, NOW(), NOW()),
	(11, 11, '오현우', '01014072583', '05551', '서울특별시 송파구 올림픽로 21', '112동 303호', true, NOW(), NOW()),
	(12, 12, '서시우', '01014442636', '13529', '경기도 성남시 분당구 판교역로 22', '13층', true, NOW(), NOW()),
	(13, 13, '신다은', '01014812689', '21998', '인천광역시 연수구 컨벤시아대로 23', '114동 505호', true, NOW(), NOW()),
	(14, 14, '권윤서', '01015182742', '34141', '대전광역시 유성구 대학로 24', '115동 606호', true, NOW(), NOW()),
	(15, 15, '황지우', '01015552795', '42176', '대구광역시 수성구 동대구로 25', '16층', true, NOW(), NOW()),
	(16, 16, '안수아', '01015922848', '61945', '광주광역시 서구 상무중앙로 26', '102동 808호', true, NOW(), NOW()),
	(17, 17, '송아린', '01016292901', '48058', '부산광역시 해운대구 센텀중앙로 27', '103동 909호', true, NOW(), NOW()),
	(18, 18, '전예준', '01016662954', '44705', '울산광역시 남구 삼산로 28', '19층', true, NOW(), NOW()),
	(19, 19, '홍지안', '01017033007', '24341', '강원특별자치도 춘천시 중앙로 29', '105동 202호', true, NOW(), NOW()),
	(20, 20, '김민준', '01017403060', '04030', '서울특별시 마포구 월드컵로 30', '106동 303호', true, NOW(), NOW()),
	(21, 21, '이서준', '01017773113', '05551', '서울특별시 송파구 올림픽로 31', '2층', true, NOW(), NOW()),
	(22, 22, '박유진', '01018143166', '13529', '경기도 성남시 분당구 판교역로 32', '108동 505호', true, NOW(), NOW()),
	(23, 23, '최서연', '01018513219', '21998', '인천광역시 연수구 컨벤시아대로 33', '109동 606호', true, NOW(), NOW()),
	(24, 24, '정하준', '01018883272', '34141', '대전광역시 유성구 대학로 34', '5층', true, NOW(), NOW()),
	(25, 25, '강채원', '01019253325', '42176', '대구광역시 수성구 동대구로 35', '111동 808호', true, NOW(), NOW()),
	(26, 26, '조도윤', '01019623378', '61945', '광주광역시 서구 상무중앙로 36', '112동 909호', true, NOW(), NOW()),
	(27, 27, '윤은우', '01019993431', '48058', '부산광역시 해운대구 센텀중앙로 37', '8층', true, NOW(), NOW()),
	(28, 28, '장건우', '01020363484', '44705', '울산광역시 남구 삼산로 38', '114동 202호', true, NOW(), NOW()),
	(29, 29, '임하윤', '01020733537', '24341', '강원특별자치도 춘천시 중앙로 39', '115동 303호', true, NOW(), NOW()),
	(30, 30, '한지호', '01021103590', '04030', '서울특별시 마포구 월드컵로 40', '11층', true, NOW(), NOW()),
	(31, 31, '오현우', '01021473643', '05551', '서울특별시 송파구 올림픽로 41', '102동 505호', true, NOW(), NOW()),
	(32, 32, '서시우', '01021843696', '13529', '경기도 성남시 분당구 판교역로 42', '103동 606호', true, NOW(), NOW()),
	(33, 33, '신다은', '01022213749', '21998', '인천광역시 연수구 컨벤시아대로 43', '14층', true, NOW(), NOW()),
	(34, 34, '권윤서', '01022583802', '34141', '대전광역시 유성구 대학로 44', '105동 808호', true, NOW(), NOW()),
	(35, 35, '황지우', '01022953855', '42176', '대구광역시 수성구 동대구로 45', '106동 909호', true, NOW(), NOW()),
	(36, 36, '안수아', '01023323908', '61945', '광주광역시 서구 상무중앙로 46', '17층', true, NOW(), NOW()),
	(37, 37, '송아린', '01023693961', '48058', '부산광역시 해운대구 센텀중앙로 47', '108동 202호', true, NOW(), NOW()),
	(38, 38, '전예준', '01024064014', '44705', '울산광역시 남구 삼산로 48', '109동 303호', true, NOW(), NOW()),
	(39, 39, '홍지안', '01024434067', '24341', '강원특별자치도 춘천시 중앙로 49', '20층', true, NOW(), NOW()),
	(40, 40, '김민준', '01024804120', '04030', '서울특별시 마포구 월드컵로 50', '111동 505호', true, NOW(), NOW()),
	(41, 41, '이서준', '01025174173', '05551', '서울특별시 송파구 올림픽로 51', '112동 606호', true, NOW(), NOW()),
	(42, 42, '박유진', '01025544226', '13529', '경기도 성남시 분당구 판교역로 52', '3층', true, NOW(), NOW()),
	(43, 43, '최서연', '01025914279', '21998', '인천광역시 연수구 컨벤시아대로 53', '114동 808호', true, NOW(), NOW()),
	(44, 44, '정하준', '01026284332', '34141', '대전광역시 유성구 대학로 54', '115동 909호', true, NOW(), NOW()),
	(45, 45, '강채원', '01026654385', '42176', '대구광역시 수성구 동대구로 55', '6층', true, NOW(), NOW()),
	(46, 46, '조도윤', '01027024438', '61945', '광주광역시 서구 상무중앙로 56', '102동 202호', true, NOW(), NOW()),
	(47, 47, '윤은우', '01027394491', '48058', '부산광역시 해운대구 센텀중앙로 57', '103동 303호', true, NOW(), NOW()),
	(48, 48, '장건우', '01027764544', '44705', '울산광역시 남구 삼산로 58', '9층', true, NOW(), NOW()),
	(49, 49, '임하윤', '01028134597', '24341', '강원특별자치도 춘천시 중앙로 59', '105동 505호', true, NOW(), NOW()),
	(50, 50, '한지호', '01028504650', '04030', '서울특별시 마포구 월드컵로 60', '106동 606호', true, NOW(), NOW()),
	(51, 51, '오현우', '01028874703', '05551', '서울특별시 송파구 올림픽로 61', '12층', true, NOW(), NOW());

-- ------------------------------------------------------------------
-- 2) ProductInitData: Product 10종.
--    dev의 plant-species-removal 마이그레이션으로 plant_species 카탈로그 테이블이
--    사라지고, products.plant_species_id(FK)가 products.species_name(자유 텍스트)로
--    바뀌었다 — 그에 맞춰 FK 대신 종 이름 문자열을 직접 넣는다.
-- ------------------------------------------------------------------
INSERT IGNORE INTO products
	(id, name, category, point_price, stock, species_name, description, image_url, status, created_at, updated_at)
VALUES
	(1, '방울토마토 홈가드닝 키트', 'KIT', 2500, 18, NULL, '베란다에서도 방울토마토를 시작할 수 있는 화분, 배양토, 씨앗 구성의 입문 키트입니다.', 'products/1/e9f8dd6d-7692-5c67-9172-7c9a2eeae96b.png', 'ACTIVE', NOW(), NOW()),
	(2, '향긋한 바질 씨앗 키트', 'KIT', 1800, 24, NULL, '요리에 바로 활용하기 좋은 바질을 씨앗부터 키워보는 미니 재배 키트입니다.', 'products/2/07e36a18-4d17-5b91-b3bd-9a9a13e75516.png', 'ACTIVE', NOW(), NOW()),
	(3, '청상추 미니 텃밭 키트', 'KIT', 2200, 15, NULL, '실내 창가에서 잎채소를 손쉽게 키울 수 있도록 필요한 재료를 담았습니다.', 'products/3/55ae1ce1-1ff1-5371-8b86-70e2bdd7a50e.png', 'ACTIVE', NOW(), NOW()),
	(4, '루꼴라 스타터 키트', 'KIT', 2000, 0, NULL, '쌉싸름한 루꼴라를 집에서 길러 샐러드로 즐길 수 있는 초보자용 키트입니다.', 'products/4/b9bdb189-9f98-5636-86ce-d5c9852cf082.png', 'ACTIVE', NOW(), NOW()),
	(5, '해바라기 성장 관찰 키트', 'KIT', 1500, 30, NULL, '아이와 함께 발아부터 개화까지 관찰하기 좋은 교육용 해바라기 키트입니다.', 'products/5/4a0eeb55-c63e-5f9c-8657-de18de80d689.png', 'ACTIVE', NOW(), NOW()),
	(6, '스위트 바질 모종', 'SEEDLING', 900, 20, '스위트 바질', '향이 풍부하고 생육이 빠른 스위트 바질 모종입니다.', 'products/6/26484928-4c05-592b-a3fc-cb3e76ad3578.png', 'ACTIVE', NOW(), NOW()),
	(7, '방울토마토 모종', 'SEEDLING', 1200, 12, '방울토마토', '햇빛이 드는 베란다에서 키우기 좋은 방울토마토 모종입니다.', 'products/7/682bcb8b-1814-52a4-ba81-d513ea2fcbeb.png', 'ACTIVE', NOW(), NOW()),
	(8, '아삭한 청상추 모종', 'SEEDLING', 700, 35, '청상추', '수확까지 비교적 짧아 처음 텃밭을 시작할 때 좋은 청상추 모종입니다.', 'products/8/64645cda-841a-55f3-893c-cab4715edc87.png', 'ACTIVE', NOW(), NOW()),
	(9, '향긋한 로즈마리 모종', 'SEEDLING', 1100, 8, '로즈마리', '요리와 방향용으로 활용할 수 있는 향긋한 로즈마리 모종입니다.', 'products/9/86294710-c193-5d62-8f4c-2dc50356355f.png', 'ACTIVE', NOW(), NOW()),
	(10, '설향 딸기 모종', 'SEEDLING', 1500, 10, '설향 딸기', '가정에서 달콤한 열매를 수확해 볼 수 있는 설향 딸기 모종입니다.', 'products/10/15e4d877-7091-5900-842e-6365fc9892c2.png', 'ACTIVE', NOW(), NOW());

-- ------------------------------------------------------------------
-- 3) CardInitData: ExchangeProduct 10종 + Card 10종(1:1, 순서대로 매칭)
-- ------------------------------------------------------------------
INSERT IGNORE INTO exchange_products (id, name, stock, description, image_url, status, created_at, updated_at) VALUES
	(1, '제철 수박 한 통', 8, '시원하고 달콤한 여름 수박입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Watermelon', 'ON_SALE', NOW(), NOW()),
	(2, '방울토마토 1kg', 12, '농장에서 갓 수확한 방울토마토입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Cherry+Tomato', 'ON_SALE', NOW(), NOW()),
	(3, '설향 딸기 한 팩', 5, '향긋하고 달콤한 설향 딸기입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Strawberry', 'ON_SALE', NOW(), NOW()),
	(4, '유기농 당근 1kg', 10, '아삭한 식감의 유기농 당근입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Carrot', 'ON_SALE', NOW(), NOW()),
	(5, '수미감자 2kg', 20, '포슬포슬한 식감의 수미감자입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Potato', 'ON_SALE', NOW(), NOW()),
	(6, '샤인머스캣 한 송이', 4, '달콤하고 향긋한 샤인머스캣입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Shine+Muscat', 'ON_SALE', NOW(), NOW()),
	(7, '초당옥수수 4개', 9, '생으로도 달콤한 초당옥수수입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Sweet+Corn', 'ON_SALE', NOW(), NOW()),
	(8, '꿀고구마 2kg', 14, '구우면 더욱 달콤해지는 꿀고구마입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Sweet+Potato', 'ON_SALE', NOW(), NOW()),
	(9, '부사 사과 2kg', 11, '아삭하고 새콤달콤한 부사 사과입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Apple', 'ON_SALE', NOW(), NOW()),
	(10, '제주 감귤 3kg', 0, '제주에서 자란 새콤달콤한 감귤입니다.', 'https://placehold.co/800x600/FFF3CC/8A6D00?text=Tangerine', 'ON_SALE', NOW(), NOW());

INSERT IGNORE INTO cards
	(id, name, point_price, exchange_product_id, required_count_for_exchange, description, image_url, status, created_at, updated_at)
VALUES
	(1, '수박 쿠폰', 300, 1, 5, '제철 수박 한 통 교환을 위해 모으는 쿠폰입니다.', 'coupons/1/5d085536-b249-56bf-b42f-82e56bd785dd.png', 'ON_SALE', NOW(), NOW()),
	(2, '방울토마토 쿠폰', 200, 2, 4, '방울토마토 1kg 교환을 위해 모으는 쿠폰입니다.', 'coupons/2/a4d206ed-e57c-57aa-b347-6a633f1f08b4.png', 'ON_SALE', NOW(), NOW()),
	(3, '설향 딸기 쿠폰', 350, 3, 6, '설향 딸기 한 팩 교환을 위해 모으는 쿠폰입니다.', 'coupons/3/817208ec-104b-5e78-8d90-84ebeab76ffe.png', 'ON_SALE', NOW(), NOW()),
	(4, '유기농 당근 쿠폰', 150, 4, 3, '유기농 당근 1kg 교환을 위해 모으는 쿠폰입니다.', 'coupons/4/81c3e0a4-e115-5cc9-a21e-5accc4504cbd.png', 'ON_SALE', NOW(), NOW()),
	(5, '수미감자 쿠폰', 180, 5, 5, '수미감자 2kg 교환을 위해 모으는 쿠폰입니다.', 'coupons/5/64303f95-e437-5ee9-8fb0-d9bccefe3a1b.png', 'ON_SALE', NOW(), NOW()),
	(6, '샤인머스캣 쿠폰', 450, 6, 8, '샤인머스캣 한 송이 교환을 위해 모으는 쿠폰입니다.', 'coupons/6/0a0fac0b-b987-5240-8802-e13c719b6475.png', 'ON_SALE', NOW(), NOW()),
	(7, '초당옥수수 쿠폰', 250, 7, 5, '초당옥수수 4개 교환을 위해 모으는 쿠폰입니다.', 'coupons/7/c14efdb8-491e-546d-b234-15ee6ef863b1.png', 'ON_SALE', NOW(), NOW()),
	(8, '꿀고구마 쿠폰', 220, 8, 4, '꿀고구마 2kg 교환을 위해 모으는 쿠폰입니다.', 'coupons/8/6ef1c07a-be33-5e1d-a6c2-75427e43e13e.png', 'ON_SALE', NOW(), NOW()),
	(9, '부사 사과 쿠폰', 280, 9, 5, '부사 사과 2kg 교환을 위해 모으는 쿠폰입니다.', 'coupons/9/cda323a0-8b66-5071-9730-34c3cad1ea16.png', 'ON_SALE', NOW(), NOW()),
	(10, '제주 감귤 쿠폰', 260, 10, 5, '제주 감귤 3kg 교환을 위해 모으는 쿠폰입니다.', 'coupons/10/a5a1f56f-5b64-5df6-b955-9df23797ca9f.png', 'ON_SALE', NOW(), NOW());

-- ------------------------------------------------------------------
-- 4) GachaPackProductInitData: GACHA_PACK 상품 1개 (id=11, species_name 없음)
-- ------------------------------------------------------------------
INSERT IGNORE INTO products
	(id, name, category, point_price, stock, species_name, description, image_url, status, created_at, updated_at)
VALUES
	(11, '시즌 1 가챠 카드팩', 'GACHA_PACK', 100, 0, NULL, '식물 캐릭터 카드 5장이 즉시 개봉되는 시즌 1 카드팩입니다.', 'products/11/f7573887-a33e-5690-b058-f32f7aa2a326.png', 'ACTIVE', NOW(), NOW());

-- ------------------------------------------------------------------
-- 5) GachaCardInitData: TradingCard 43종 (SEASON_01). id는 1..43으로 명시해서
--    imageKey의 "cards/{id}/..." 접두어와 실제 PK가 일치하도록 맞춘다.
-- ------------------------------------------------------------------
INSERT IGNORE INTO trading_cards
	(id, series_code, code, name, rarity, description, image_key, draw_weight, display_order, status, created_at, updated_at)
VALUES
	(1, 'SEASON_01', 'common_cabbage', '양배추', 'COMMON', '양배추을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/1/8d09cd4e-6956-539c-a858-2f146d206fb3.png', 98000, 1, 'ACTIVE', NOW(), NOW()),
	(2, 'SEASON_01', 'common_carrot', '당근', 'COMMON', '당근을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/2/73a2d109-2d8f-58fd-8172-0a93f2e0cf1b.png', 98000, 2, 'ACTIVE', NOW(), NOW()),
	(3, 'SEASON_01', 'common_cherry_tomato', '방울토마토', 'COMMON', '방울토마토을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/3/f909b85a-5be6-537d-af72-8dfd3d019ad5.png', 98000, 3, 'ACTIVE', NOW(), NOW()),
	(4, 'SEASON_01', 'common_chili_pepper', '고추', 'COMMON', '고추을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/4/63108783-1e70-56eb-b7ed-519b2b29afbb.png', 98000, 4, 'ACTIVE', NOW(), NOW()),
	(5, 'SEASON_01', 'common_corn', '옥수수', 'COMMON', '옥수수을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/5/6a79ae47-df92-5105-926b-3323c0804d2f.png', 98000, 5, 'ACTIVE', NOW(), NOW()),
	(6, 'SEASON_01', 'common_cucumber', '오이', 'COMMON', '오이을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/6/f686745a-8d4c-5464-8f5d-234d49bbdf3b.png', 98000, 6, 'ACTIVE', NOW(), NOW()),
	(7, 'SEASON_01', 'common_green_onion', '대파', 'COMMON', '대파을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/7/2f372b19-276f-5966-a655-6b1191792506.png', 98000, 7, 'ACTIVE', NOW(), NOW()),
	(8, 'SEASON_01', 'common_lettuce', '상추', 'COMMON', '상추을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/8/399f7998-3e24-5d1d-b69c-68da63b839ef.png', 98000, 8, 'ACTIVE', NOW(), NOW()),
	(9, 'SEASON_01', 'common_pea', '완두콩', 'COMMON', '완두콩을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/9/725d9440-6a96-5329-be45-e5d62be2c3c1.png', 98000, 9, 'ACTIVE', NOW(), NOW()),
	(10, 'SEASON_01', 'common_perilla', '깻잎', 'COMMON', '깻잎을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/10/75f95dea-065e-5e74-ad88-a0c94d3f9854.png', 98000, 10, 'ACTIVE', NOW(), NOW()),
	(11, 'SEASON_01', 'common_potato', '감자', 'COMMON', '감자을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/11/2680e6db-335d-5e37-a128-d984497537b3.png', 98000, 11, 'ACTIVE', NOW(), NOW()),
	(12, 'SEASON_01', 'common_radish', '무', 'COMMON', '무을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/12/62466e97-406c-513a-9861-1ce1c715cd04.png', 98000, 12, 'ACTIVE', NOW(), NOW()),
	(13, 'SEASON_01', 'common_spinach', '시금치', 'COMMON', '시금치을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/13/b7232c3d-5791-5b6e-8d59-99711d8f474e.png', 98000, 13, 'ACTIVE', NOW(), NOW()),
	(14, 'SEASON_01', 'common_sweet_potato', '고구마', 'COMMON', '고구마을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/14/f09e0d5f-9ff2-539b-8f1f-60ee096dc2bc.png', 98000, 14, 'ACTIVE', NOW(), NOW()),
	(15, 'SEASON_01', 'common_zucchini', '애호박', 'COMMON', '애호박을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/15/3cfafc95-bf72-5ff0-b0bd-6b155d8ac977.png', 98000, 15, 'ACTIVE', NOW(), NOW()),
	(16, 'SEASON_01', 'rare_asparagus', '아스파라거스', 'RARE', '아스파라거스을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/16/d39de513-9715-57af-b5de-d6d4c93a745a.png', 30000, 16, 'ACTIVE', NOW(), NOW()),
	(17, 'SEASON_01', 'rare_basil', '바질', 'RARE', '바질을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/17/acdf6f51-4216-5a52-b949-25a989279fde.png', 30000, 17, 'ACTIVE', NOW(), NOW()),
	(18, 'SEASON_01', 'rare_blueberry', '블루베리', 'RARE', '블루베리을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/18/7686007b-53b5-5155-b828-b040f2e68cd0.png', 30000, 18, 'ACTIVE', NOW(), NOW()),
	(19, 'SEASON_01', 'rare_broccoli', '브로콜리', 'RARE', '브로콜리을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/19/78f6e3d1-9c4c-50be-9912-0f1404ea155c.png', 30000, 19, 'ACTIVE', NOW(), NOW()),
	(20, 'SEASON_01', 'rare_cauliflower', '콜리플라워', 'RARE', '콜리플라워을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/20/d5293f7e-c7c7-55bd-a1c6-f24db75c8602.png', 30000, 20, 'ACTIVE', NOW(), NOW()),
	(21, 'SEASON_01', 'rare_eggplant', '가지', 'RARE', '가지을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/21/e9669b23-fba3-5431-8644-74f3f015d7c2.png', 30000, 21, 'ACTIVE', NOW(), NOW()),
	(22, 'SEASON_01', 'rare_fig', '무화과', 'RARE', '무화과을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/22/83e7f166-97e8-5ecb-9eda-00383bc54a12.png', 30000, 22, 'ACTIVE', NOW(), NOW()),
	(23, 'SEASON_01', 'rare_ginger', '생강', 'RARE', '생강을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/23/bc0cb20f-beed-549d-8ad8-a055ba3564f9.png', 30000, 23, 'ACTIVE', NOW(), NOW()),
	(24, 'SEASON_01', 'rare_paprika', '파프리카', 'RARE', '파프리카을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/24/c6cf9e95-a165-52c8-bf83-046ba0f8f742.png', 30000, 24, 'ACTIVE', NOW(), NOW()),
	(25, 'SEASON_01', 'rare_peanut', '땅콩', 'RARE', '땅콩을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/25/f53a18d7-c1f6-55d4-8c43-956f82ed74fa.png', 30000, 25, 'ACTIVE', NOW(), NOW()),
	(26, 'SEASON_01', 'rare_pumpkin', '호박', 'RARE', '호박을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/26/26be55ab-c750-5286-9f5f-e81a3152b3a5.png', 30000, 26, 'ACTIVE', NOW(), NOW()),
	(27, 'SEASON_01', 'rare_rosemary', '로즈메리', 'RARE', '로즈메리을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/27/60b2d60d-8a59-53f9-ab01-238cec084867.png', 30000, 27, 'ACTIVE', NOW(), NOW()),
	(28, 'SEASON_01', 'rare_strawberry', '딸기', 'RARE', '딸기을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/28/67887ea5-a7ca-5b0e-a843-234556e9c162.png', 30000, 28, 'ACTIVE', NOW(), NOW()),
	(29, 'SEASON_01', 'rare_watermelon', '수박', 'RARE', '수박을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/29/03e0a40e-1cc1-56d5-ab5f-d29af7bea2db.png', 30000, 29, 'ACTIVE', NOW(), NOW()),
	(30, 'SEASON_01', 'super_rare_artichoke', '아티초크', 'SUPER_RARE', '아티초크을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/30/fcf5dfde-2da6-5b42-b67e-66af139a5da3.png', 23625, 30, 'ACTIVE', NOW(), NOW()),
	(31, 'SEASON_01', 'super_rare_cacao', '카카오', 'SUPER_RARE', '카카오을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/31/8c26cc7a-16d6-5388-b557-45a225393a00.png', 23625, 31, 'ACTIVE', NOW(), NOW()),
	(32, 'SEASON_01', 'super_rare_coffee_cherry', '커피 체리', 'SUPER_RARE', '커피 체리을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/32/65761d37-e88c-54e7-ba8a-f1741be9a6ea.png', 23625, 32, 'ACTIVE', NOW(), NOW()),
	(33, 'SEASON_01', 'super_rare_dragon_fruit', '용과', 'SUPER_RARE', '용과을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/33/12f05e35-b2d0-5493-89ec-bca670e3f569.png', 23625, 33, 'ACTIVE', NOW(), NOW()),
	(34, 'SEASON_01', 'super_rare_passion_fruit', '패션프루트', 'SUPER_RARE', '패션프루트을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/34/5ea74c98-fe6e-5569-8cab-35c689b7da37.png', 23625, 34, 'ACTIVE', NOW(), NOW()),
	(35, 'SEASON_01', 'super_rare_saffron_crocus', '사프란 크로커스', 'SUPER_RARE', '사프란 크로커스을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/35/dcfb9ba0-b951-548b-8dce-ab00131b07b2.png', 23625, 35, 'ACTIVE', NOW(), NOW()),
	(36, 'SEASON_01', 'super_rare_vanilla_orchid', '바닐라 난초', 'SUPER_RARE', '바닐라 난초을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/36/f2a736d1-e444-58c1-bf40-b26a4332761c.png', 23625, 36, 'ACTIVE', NOW(), NOW()),
	(37, 'SEASON_01', 'super_rare_wasabi', '와사비', 'SUPER_RARE', '와사비을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/37/c7b0d87c-541b-5cf3-ade6-84eb8cb1819f.png', 23625, 37, 'ACTIVE', NOW(), NOW()),
	(38, 'SEASON_01', 'hyper_rare_apple_mango', '애플망고', 'HYPER_RARE', '애플망고을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/38/cf045f5f-da60-5b20-b6b1-498f7655f336.png', 6993, 38, 'ACTIVE', NOW(), NOW()),
	(39, 'SEASON_01', 'hyper_rare_shine_muscat', '샤인머스켓', 'HYPER_RARE', '샤인머스켓을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/39/4a9576a7-9e0a-54f4-b004-13fa13f45387.png', 6993, 39, 'ACTIVE', NOW(), NOW()),
	(40, 'SEASON_01', 'hyper_rare_white_strawberry', '화이트 스트로베리', 'HYPER_RARE', '화이트 스트로베리을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/40/e2e0430e-1731-55cc-9194-c5dcce51c1e3.png', 6993, 40, 'ACTIVE', NOW(), NOW()),
	(41, 'SEASON_01', 'golden_rare_golden_sun_corn', '황금 태양 옥수수', 'GOLDEN_RARE', '황금 태양 옥수수을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/41/f6442e98-d414-576d-bd12-6ced74a9c475.png', 7, 41, 'ACTIVE', NOW(), NOW()),
	(42, 'SEASON_01', 'golden_rare_moonlight_tomato', '월광 토마토', 'GOLDEN_RARE', '월광 토마토을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/42/4eb1d007-ec5e-5c14-8f81-2f251b923e82.png', 7, 42, 'ACTIVE', NOW(), NOW()),
	(43, 'SEASON_01', 'golden_rare_stardust_strawberry', '별가루 딸기', 'GOLDEN_RARE', '별가루 딸기을 모티브로 한 시즌 1 트레이딩 카드입니다.', 'cards/43/be7b328b-f538-5d38-8a62-5952d2bc5745.png', 7, 43, 'ACTIVE', NOW(), NOW());

-- ------------------------------------------------------------------
-- 6) PlantProfileInitData: test@test.com의 식물 프로필 5개
--    LocalDate.now().minusDays(N)을 CURRENT_DATE - INTERVAL N DAY로 대체.
--    plant_profile은 BaseEntity(id만)를 상속하고 updated_at 컬럼이 없다.
--    dev의 plant-species-removal 마이그레이션으로 specie_id(FK)가 species_name
--    (NOT NULL 자유 텍스트)으로 바뀌었다.
-- ------------------------------------------------------------------
INSERT IGNORE INTO plant_profile (id, user_id, species_name, plant_name, start_date, plant_image, status, created_at) VALUES
	(1, 2, '방울토마토', '토실이', CURRENT_DATE - INTERVAL 42 DAY, NULL, 'GROWING', NOW()),
	(2, 2, '스위트 바질', '바질이', CURRENT_DATE - INTERVAL 15 DAY, NULL, 'GROWING', NOW()),
	(3, 2, '청상추', '쌈싸리', CURRENT_DATE - INTERVAL 8 DAY, NULL, 'GROWING', NOW()),
	(4, 2, '설향 딸기', '딸기공주', CURRENT_DATE - INTERVAL 120 DAY, NULL, 'HARVESTED', NOW()),
	(5, 2, '로즈마리', '로즈랑이', CURRENT_DATE - INTERVAL 30 DAY, NULL, 'FAILED', NOW());

-- ------------------------------------------------------------------
-- 7) PlantJournalInitData: 프로필 1개당 일지 2개(+대표 이미지 1개씩), 총 10개.
--    seedIndex는 (프로필 순서 * 2 + entry)로 0..9. writtenDate는 프로필마다
--    "어제"부터 entry만큼 뒤로(0,1일 전) 리셋된다(프로필을 넘나들며 누적되지 않음).
--    createdAt 시각은 8 + seedIndex % 10 시. content는 SAMPLE_CONTENTS[seedIndex % 3].
-- ------------------------------------------------------------------
INSERT IGNORE INTO plant_journals (id, plant_profile_id, user_id, content, written_date, created_at, updated_at, deleted_at) VALUES
	(1, 1, 2, '오늘도 잎이 한 뼘 더 자랐어요. 아침마다 조금씩 커지는 게 신기해요.',
		CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '08:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '08:00:00'), NULL),
	(2, 1, 2, '물을 듬뿍 줬더니 훨씬 생기가 도네요. 내일은 지지대를 세워줘야겠어요.',
		CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '09:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '09:00:00'), NULL),
	(3, 2, 2, '새잎이 세 장이나 났어요. 곧 첫 수확 할 수 있을 것 같아요.',
		CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '10:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '10:00:00'), NULL),
	(4, 2, 2, '오늘도 잎이 한 뼘 더 자랐어요. 아침마다 조금씩 커지는 게 신기해요.',
		CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '11:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '11:00:00'), NULL),
	(5, 3, 2, '물을 듬뿍 줬더니 훨씬 생기가 도네요. 내일은 지지대를 세워줘야겠어요.',
		CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '12:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '12:00:00'), NULL),
	(6, 3, 2, '새잎이 세 장이나 났어요. 곧 첫 수확 할 수 있을 것 같아요.',
		CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '13:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '13:00:00'), NULL),
	(7, 4, 2, '오늘도 잎이 한 뼘 더 자랐어요. 아침마다 조금씩 커지는 게 신기해요.',
		CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '14:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '14:00:00'), NULL),
	(8, 4, 2, '물을 듬뿍 줬더니 훨씬 생기가 도네요. 내일은 지지대를 세워줘야겠어요.',
		CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '15:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '15:00:00'), NULL),
	(9, 5, 2, '새잎이 세 장이나 났어요. 곧 첫 수확 할 수 있을 것 같아요.',
		CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '16:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '16:00:00'), NULL),
	(10, 5, 2, '오늘도 잎이 한 뼘 더 자랐어요. 아침마다 조금씩 커지는 게 신기해요.',
		CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '17:00:00'), TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '17:00:00'), NULL);

INSERT IGNORE INTO journals_images (id, journal_id, user_id, image_url, image_hash, is_representative, written_date, created_at, updated_at) VALUES
	(1, 1, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=토실이', 'seed-0', true, CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '08:00:00'), NULL),
	(2, 2, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=토실이', 'seed-1', true, CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '09:00:00'), NULL),
	(3, 3, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=바질이', 'seed-2', true, CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '10:00:00'), NULL),
	(4, 4, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=바질이', 'seed-3', true, CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '11:00:00'), NULL),
	(5, 5, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=쌈싸리', 'seed-4', true, CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '12:00:00'), NULL),
	(6, 6, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=쌈싸리', 'seed-5', true, CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '13:00:00'), NULL),
	(7, 7, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=딸기공주', 'seed-6', true, CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '14:00:00'), NULL),
	(8, 8, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=딸기공주', 'seed-7', true, CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '15:00:00'), NULL),
	(9, 9, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=로즈랑이', 'seed-8', true, CURRENT_DATE - INTERVAL 1 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 1 DAY, '16:00:00'), NULL),
	(10, 10, 2, 'https://placehold.co/800x800/E8F3D8/4B7A1E?text=로즈랑이', 'seed-9', true, CURRENT_DATE - INTERVAL 2 DAY, TIMESTAMP(CURRENT_DATE - INTERVAL 2 DAY, '17:00:00'), NULL);

-- ------------------------------------------------------------------
-- 8) NotificationInitData: 시드 유저 3명 x 샘플 5개 = 15건.
--    read=true인 2건(JOURNAL_REMINDER, INQUIRY)은 read_at = created_at.
-- ------------------------------------------------------------------
INSERT IGNORE INTO notification (id, user_id, type, title, content, link_url, ref_type, ref_id, is_read, read_at, created_at) VALUES
	(1, 1, 'DELIVERY', '주문하신 상품이 배송을 시작했어요 📦', 'ORD-20260709-0022 · 방울토마토 모종', '/my/orders', NULL, NULL, false, NULL, NOW()),
	(2, 1, 'JOURNAL_REMINDER', '오늘 쌈싸리의 모습을 남겨볼까요? 🌱', '아직 오늘의 일지를 쓰지 않으셨어요', '/journals', NULL, NULL, true, NOW(), NOW()),
	(3, 1, 'COMMUNITY', '내 게시글에 댓글이 달렸어요 💬', '"저도 이 방법으로 키우고 있어요!"', NULL, NULL, NULL, false, NULL, NOW() - INTERVAL 1 DAY),
	(4, 1, 'INQUIRY', '문의하신 내용에 답변이 도착했어요 💬', '배송 관련 문의', '/my/inquiries', NULL, NULL, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
	(5, 1, 'NOTICE', '새로운 쿠폰이 상점에 입고됐어요 📢', '감자 쿠폰을 만나보세요', '/cards', NULL, NULL, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
	(6, 2, 'DELIVERY', '주문하신 상품이 배송을 시작했어요 📦', 'ORD-20260709-0022 · 방울토마토 모종', '/my/orders', NULL, NULL, false, NULL, NOW()),
	(7, 2, 'JOURNAL_REMINDER', '오늘 쌈싸리의 모습을 남겨볼까요? 🌱', '아직 오늘의 일지를 쓰지 않으셨어요', '/journals', NULL, NULL, true, NOW(), NOW()),
	(8, 2, 'COMMUNITY', '내 게시글에 댓글이 달렸어요 💬', '"저도 이 방법으로 키우고 있어요!"', NULL, NULL, NULL, false, NULL, NOW() - INTERVAL 1 DAY),
	(9, 2, 'INQUIRY', '문의하신 내용에 답변이 도착했어요 💬', '배송 관련 문의', '/my/inquiries', NULL, NULL, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
	(10, 2, 'NOTICE', '새로운 쿠폰이 상점에 입고됐어요 📢', '감자 쿠폰을 만나보세요', '/cards', NULL, NULL, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
	(11, 3, 'DELIVERY', '주문하신 상품이 배송을 시작했어요 📦', 'ORD-20260709-0022 · 방울토마토 모종', '/my/orders', NULL, NULL, false, NULL, NOW()),
	(12, 3, 'JOURNAL_REMINDER', '오늘 쌈싸리의 모습을 남겨볼까요? 🌱', '아직 오늘의 일지를 쓰지 않으셨어요', '/journals', NULL, NULL, true, NOW(), NOW()),
	(13, 3, 'COMMUNITY', '내 게시글에 댓글이 달렸어요 💬', '"저도 이 방법으로 키우고 있어요!"', NULL, NULL, NULL, false, NULL, NOW() - INTERVAL 1 DAY),
	(14, 3, 'INQUIRY', '문의하신 내용에 답변이 도착했어요 💬', '배송 관련 문의', '/my/inquiries', NULL, NULL, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
	(15, 3, 'NOTICE', '새로운 쿠폰이 상점에 입고됐어요 📢', '감자 쿠폰을 만나보세요', '/cards', NULL, NULL, true, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY);

-- ------------------------------------------------------------------
-- 9) BoardInitData: 게시글 20개(NOTICE 3 / FREE 12 / PLANT_QNA 5), 댓글 18개,
--    좋아요 27건. like_count/comment_count는 원본 로직(incrementXxxCount)이 최종적으로
--    만드는 값을 미리 계산해 넣었다. PLANT_QNA 5개는 test@test.com의 처음 5개 일지
--    (plant_journals id 1~5, PlantJournalRepository.search가 정렬 없이 PK 오름차순으로
--    돌려주는 것과 동일)에 순서대로 연결된다.
-- ------------------------------------------------------------------
INSERT IGNORE INTO board_posts
	(id, user_id, category, title, content, journal_id, view_count, like_count, comment_count, status, hidden_by, hidden_at, created_at, updated_at)
VALUES
	(1, 1, 'NOTICE', '커뮤니티 게시판 오픈 안내 🌱', '키워볼래 커뮤니티 게시판이 열렸어요! 식물 이야기, 궁금한 점 자유롭게 나눠주세요.', NULL, 0, 0, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(2, 1, 'NOTICE', '게시판 이용 규칙 안내', '서로 존중하는 말투 부탁드려요. 광고성 게시글이나 도배는 예고 없이 숨김 처리될 수 있어요.', NULL, 0, 1, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(3, 1, 'NOTICE', '8월 정기 점검 안내 (8/15 02:00~04:00)', '안정적인 서비스 운영을 위해 정기 점검을 진행해요. 점검 시간 동안은 접속이 잠시 제한될 수 있습니다.', NULL, 0, 2, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(4, 2, 'FREE', '다들 베란다 온도 어떻게 관리하세요?', '요즘 낮밤 기온 차가 심해서 베란다 화분들이 걱정이에요. 다들 어떻게 관리하시나요?', NULL, 0, 1, 3, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(5, 3, 'FREE', '오늘 첫 수확했어요! 🍅', '작은 방울토마토였는데 직접 키운 거라 그런지 엄청 달아요. 다들 첫 수확 기억 있으세요?', NULL, 0, 2, 2, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(6, 1, 'FREE', '다육이 키우기 입문자 추천 종류 있을까요', '물주기를 자주 깜빡하는 편인데 그래도 잘 버텨주는 다육이 종류 추천 부탁드려요.', NULL, 0, 2, 2, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(7, 2, 'FREE', '화분 흙 냄새가 나는데 정상인가요?', '물을 준 다음날부터 흙에서 살짝 쿰쿰한 냄새가 나요. 과습인 걸까요?', NULL, 0, 1, 1, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(8, 3, 'FREE', '베란다 텃밭 사진 공유해요 📸', '주말마다 조금씩 채소 모종을 늘리고 있어요. 상추랑 깻잎이 제일 잘 자라네요.', NULL, 0, 2, 1, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(9, 2, 'FREE', '식물 영양제 vs 비료, 뭐가 더 나을까요', '둘 다 써보긴 했는데 차이를 잘 모르겠어요. 경험담 들려주세요.', NULL, 0, 2, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(10, 3, 'FREE', '장마철 화분 관리 팁 공유합니다', '비 많이 오는 날은 화분을 처마 밑으로 옮기는 게 제일 확실한 것 같아요.', NULL, 0, 1, 1, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(11, 1, 'FREE', '식물 이름 짓는 나만의 규칙 있으신가요?', '저는 종류 앞글자 따서 짓는데 다들 어떻게 이름 지으시는지 궁금해요 ㅎㅎ', NULL, 0, 1, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(12, 2, 'FREE', '분갈이 시기 놓친 것 같은데 지금 해도 될까요', '봄에 했어야 했는데 미루다가 지금까지 왔어요. 여름 분갈이 괜찮을까요?', NULL, 0, 2, 1, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(13, 3, 'FREE', '반려식물 이름 뭘로 지으셨어요?', '저는 첫 식물한테 ''토실이''라고 지어줬어요. 다들 이름 자랑해주세요!', NULL, 0, 1, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(14, 1, 'FREE', '식물 키우면서 제일 뿌듯했던 순간', '죽어가던 화분을 살려냈을 때가 제일 뿌듯했어요. 여러분은 언제 그러셨나요?', NULL, 0, 1, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(15, 2, 'FREE', '실내 습도 어느 정도로 맞추고 계세요?', '가습기를 틀어야 하나 고민 중이에요. 적정 습도 기준이 있을까요?', NULL, 0, 2, 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(16, 2, 'PLANT_QNA', '잎끝이 갈색으로 마르는데 이유가 뭘까요?', '최근 일지에 남긴 것처럼 잎끝부터 갈색으로 마르기 시작했어요. 물은 평소대로 주고 있는데 원인을 모르겠어요.', 1, 0, 1, 2, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(17, 2, 'PLANT_QNA', '새잎이 나오다가 멈췄어요', '2주 전까지는 새잎이 계속 나왔는데 요즘은 그대로예요. 계절 때문일까요?', 2, 0, 1, 1, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(18, 2, 'PLANT_QNA', '화분 밑으로 벌레가 보이는데 어떻게 하나요', '흙 표면 근처에서 작은 날벌레가 보여요. 방제 방법 아시는 분 계실까요?', 3, 0, 2, 2, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(19, 2, 'PLANT_QNA', '물주기 주기를 얼마나 둬야 할까요', '지금은 3일에 한 번 주고 있는데 흙이 계속 축축한 것 같아서요.', 4, 0, 1, 1, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(20, 2, 'PLANT_QNA', '줄기가 갑자기 휘었어요, 지지대 필요할까요', '한쪽으로 계속 자라서 그런지 줄기가 휘기 시작했어요. 지지대를 세워줘야 할지 궁금해요.', 5, 0, 1, 1, 'ACTIVE', NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO board_comments (id, post_id, user_id, parent_comment_id, content, like_count, status, hidden_by, hidden_at, created_at, updated_at) VALUES
	(1, 4, 3, NULL, '저는 밤에는 뽁뽁이로 감싸줘요! 확실히 효과 있더라고요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(2, 4, 2, 1, '오 뽁뽁이 좋은 방법이네요, 저도 해봐야겠어요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(3, 4, 3, 2, '네! 대신 낮에는 꼭 벗겨주세요, 안 그러면 습기 차더라고요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(4, 5, 1, NULL, '우와 색이 정말 예쁘네요! 축하드려요 🎉', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(5, 5, 2, NULL, '저도 이번 주말에 첫 수확 도전해봐야겠어요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(6, 6, 2, NULL, '스투키나 산세베리아 추천드려요, 거의 안 죽어요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(7, 6, 3, 6, '저도 스투키로 시작했는데 정말 튼튼하더라고요!', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(8, 7, 3, NULL, '과습 냄새일 수 있어요, 물 주는 간격을 좀 늘려보세요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(9, 8, 1, NULL, '사진 잘 봤어요! 상추 정말 싱싱해 보이네요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(10, 10, 1, NULL, '저는 장마철엔 아예 실내로 들여요, 그게 제일 편하더라고요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(11, 12, 2, NULL, '지금이라도 늦지 않았어요! 뿌리 상태만 확인하고 진행해보세요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(12, 16, 3, NULL, '습도가 낮아서 그럴 수 있어요! 가습기나 분무 한번 해보세요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(13, 16, 2, 12, '감사해요, 오늘부터 분무 해볼게요!', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(14, 17, 3, NULL, '온도 변화가 큰 계절이라 잠시 성장을 멈춘 걸 수도 있어요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(15, 18, 1, NULL, '혹시 코바늘 벌레라면 흙 표면에 계핏가루를 뿌려보세요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(16, 18, 2, 15, '오 계핏가루는 처음 들어봐요, 시도해볼게요!', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(17, 19, 3, NULL, '겉흙이 마른 뒤에 주는 걸로 바꿔보시는 게 어떨까요?', 0, 'ACTIVE', NULL, NULL, NOW(), NOW()),
	(18, 20, 1, NULL, '가벼운 지지대 하나 세워주면 훨씬 안정적일 거예요.', 0, 'ACTIVE', NULL, NULL, NOW(), NOW());

INSERT IGNORE INTO board_post_likes (id, post_id, user_id, created_at) VALUES
	(1, 2, 2, NOW()),
	(2, 3, 2, NOW()),
	(3, 3, 3, NOW()),
	(4, 4, 1, NOW()),
	(5, 5, 1, NOW()),
	(6, 5, 2, NOW()),
	(7, 6, 2, NOW()),
	(8, 6, 3, NOW()),
	(9, 7, 1, NOW()),
	(10, 8, 1, NOW()),
	(11, 8, 2, NOW()),
	(12, 9, 1, NOW()),
	(13, 9, 3, NOW()),
	(14, 10, 1, NOW()),
	(15, 11, 2, NOW()),
	(16, 12, 1, NOW()),
	(17, 12, 3, NOW()),
	(18, 13, 1, NOW()),
	(19, 14, 2, NOW()),
	(20, 15, 1, NOW()),
	(21, 15, 3, NOW()),
	(22, 16, 1, NOW()),
	(23, 17, 1, NOW()),
	(24, 18, 1, NOW()),
	(25, 18, 3, NOW()),
	(26, 19, 1, NOW()),
	(27, 20, 1, NOW());
