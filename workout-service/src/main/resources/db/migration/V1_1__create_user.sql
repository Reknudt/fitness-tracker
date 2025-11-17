CREATE TABLE "user" (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name varchar(63) NOT NULL,
    middle_name varchar(63) NOT NULL,
    last_name varchar(63) NOT NULL,
    birth_date date,
    started_on date DEFAULT current_date NOT NULL,
    weight numeric (6, 3) CHECK (weight > 0),
    date timestamp with time zone,
    is_hidden bool DEFAULT false
);