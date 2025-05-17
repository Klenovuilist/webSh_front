alter table bolt_entity alter column data_create TYPE timestamp;
-- alter TABLE users add constraint SERIAL PRIMARY KEY(id);

-- 1. Добавьте новую колонку с типом SERIAL
-- ALTER TABLE users ADD COLUMN new_id SERIAL;
--
-- -- 2. Скопируйте данные из старой колонки в новую
-- UPDATE users SET new_id = id;
--
-- -- 3. Удалите старую колонку
-- ALTER TABLE users DROP COLUMN id;
-- --
-- -- -- 4. Переименуйте новую колонку в старое имя
-- ALTER TABLE users RENAME COLUMN new_id TO id;
--
-- -- 5. Убедитесь, что новая колонка уникальна и не содержит NULL значений (это должно быть уже обеспечено)
-- ALTER TABLE users ADD CONSTRAINT unique_id UNIQUE (id);
