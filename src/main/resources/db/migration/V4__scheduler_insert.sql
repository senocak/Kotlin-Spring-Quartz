-- INSERT INTO scheduler (created_at, updated_at, id, user_id, class_name, cron, description, group_name, name, status, status_reason)
--    VALUES ('2024-01-19 15:23:45.976000', '2024-01-19 16:23:45.976000', 'a8c13284-b8bb-4bfc-ae58-b1281e8d4784', '2cb9374e-4e52-4142-a1af-16144ef4a27d', 'com.github.senocak.quartz.TarihteBugunJob', '0 1/2 * * * ?', null, 'cron', 'At second :00, every 2 minutes starting at minute :01, of every hour', 'ENABLED', null);

--INSERT INTO scheduler (created_at, updated_at, id, user_id, class_name, cron, description, group_name, name, status, status_reason)
--    VALUES ('2024-01-19 16:23:45.976000', '2024-01-19 16:23:45.976000', 'b8c13284-b8bb-4bfc-ae58-b1281e8d4784', '2cb9374e-4e52-4142-a1af-16144ef4a27d', 'com.github.senocak.quartz.OperatingSystemJob', '0 0/2 * * * ?', null, 'cron', 'At second :00, every 2 minutes starting at minute :00, of every hour', 'ENABLED', null);

INSERT INTO scheduler (created_at, updated_at, id, user_id, class_name, cron, description, group_name, name, status, status_reason)
    VALUES ('2024-01-19 16:23:45.976000', '2024-01-19 16:23:45.976000', 'b8c13284-b8bb-4bfc-ae58-b1281e8d4784', '2cb9374e-4e52-4142-a1af-16144ef4a27d', 'com.github.senocak.quartz.OperatingSystemJob', '* 0/3 * * * ?', null, 'cron', 'At second :00, every 2 minutes starting at minute :00, of every hour', 'ENABLED', null);
