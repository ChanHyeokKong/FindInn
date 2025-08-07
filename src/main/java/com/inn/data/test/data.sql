INSERT INTO character_type (id, code, name, description, trait, activity_level, healing_level, challenge_level, emotion_level, image_url)
VALUES 
(1, 'H01', '조용한 힐러', '고요한 자연을 사랑하는 당신은 숲과 바람 속에서 진정한 쉼을 찾습니다.', 'healing', 1, 5, 2, 2, '/images/healing.png'),
(2, 'E02', '감성 탐험가', '도심 속 인스타 감성에 진심인 여행자! 카페와 갤러리는 당신의 놀이터.', 'emotion', 2, 2, 1, 5, '/images/emotion.png'),
(3, 'A03', '열정의 모험가', '레저와 활동이 빠지면 여행이 아니지! 새로운 도전을 사랑하는 당신.', 'activity', 5, 1, 3, 1, '/images/activity.png'),
(4, 'C04', '즉흥 실속파', '실용적인 선택과 즉흥적인 계획으로 여행을 이끄는 현실주의자!', 'challenge', 2, 1, 5, 2, '/images/challenge.png');

-- 추천 숙소 삽입 (character_type_recommended_accommodations는 @ElementCollection의 자동 생성 테이블)
INSERT INTO character_type_recommended_accommodations (character_type_id, recommended_accommodations) VALUES
(1, '숲속 펜션'),
(1, '한옥스테이'),

(2, '도심 부티크 호텔'),
(2, '감성 호캉스'),

(3, '풀빌라'),
(3, '복층 리조트'),

(4, '비즈니스 호텔'),
(4, '게스트하우스');