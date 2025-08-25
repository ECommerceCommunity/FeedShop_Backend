-- EventResultDetail 테이블에 피드 정보 컬럼 추가
ALTER TABLE event_result_details 
ADD COLUMN feed_id BIGINT COMMENT '우승한 피드 ID',
ADD COLUMN feed_title VARCHAR(100) COMMENT '우승한 피드 제목';

-- 기존 데이터에 대한 인덱스 추가 (선택사항)
CREATE INDEX idx_event_result_details_feed_id ON event_result_details(feed_id);
