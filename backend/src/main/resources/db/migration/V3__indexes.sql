-- Лише індекси для швидкодії читання (NFR-01, NFR-02); таблиці, колонки й зв'язки з V1 не змінюються.
CREATE INDEX idx_rooms_dormitory_floor        ON rooms (dormitory_id, floor);
CREATE INDEX idx_beds_room                    ON beds (room_id);
CREATE INDEX idx_settlements_bed_status       ON settlements (bed_id, status);
CREATE INDEX idx_settlements_resident_status  ON settlements (resident_account_id, status);
CREATE INDEX idx_applications_dorm_status     ON settlement_applications (dormitory_id, status);
CREATE INDEX idx_payments_settlement          ON payments (settlement_id, status);
CREATE INDEX idx_repairs_room                 ON repair_requests (room_id);
CREATE INDEX idx_repairs_resident             ON repair_requests (resident_account_id);
CREATE INDEX idx_announcements_dorm_created   ON announcements (dormitory_id, created_at DESC);
CREATE INDEX idx_dorm_managers_account        ON dormitory_managers (account_id, dormitory_id);
