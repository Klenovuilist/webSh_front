create table sqrew_entity (
                              id SERIAL PRIMARY KEY not null unique ,
                              sqrew_name varchar (20),
                              sqrew_limit bigint,
                              sqrew_depth double precision,
                              id_user bigint references users(id),
                              comment varchar,
                              data_create date
);

create table bolt_entity (
                             id SERIAL PRIMARY KEY not null unique ,
                             bolt_name varchar (20),
                             bolt_limit bigint,
                             id_user bigint references users(id),
                             comment varchar,
                             data_create date
);