create table if not exists thread_db
(
    id            bigint generated always as identity
        primary key,
    thread        varchar,
    d_bolt        double precision,
    d_hole        double precision,
    d_head        double precision
);


create table if not exists materals_db
(
    id bigint generated always as identity
        constraint strengths_pkey
            primary key,
    limit_strength    bigint default 0,
    materials         varchar
);


create table if not exists moments_db
(
    id bigint generated always as identity
        constraint "Moments_pkey"
            primary key,
    moments_nm  double precision,
    thread_id   bigint not null
     constraint thread_id_fkey
     references thread_db,
    materals_id bigint not null
        constraint materials_id_fkey
            references materals_db
);





