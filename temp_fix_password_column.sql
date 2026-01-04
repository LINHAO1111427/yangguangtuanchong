-- 重命名密码字段以匹配后端代码
ALTER TABLE member_user RENAME COLUMN password_hash TO password;

-- 删除不需要的password_algo字段（因为后端使用固定的BCrypt）
ALTER TABLE member_user DROP COLUMN IF EXISTS password_algo;
