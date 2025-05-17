 create table users (
        id bigint primary key unique not null ,
        user_name varchar (25) unique,
        password_user varchar (30),
        role_user varchar (30),
        data_user date
);

alter TABLE materals_db ADD COLUMN user_id bigint references users (id);

alter table materals_db ADD COLUMN strength_class double precision;

alter table materals_db ADD COLUMN comments varchar (100);



