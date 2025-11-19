CREATE TABLE "user" (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name varchar(63) NOT NULL,
    middle_name varchar(63) NOT NULL,
    last_name varchar(63) NOT NULL,
    birth_date date,
    joined_on date DEFAULT current_date NOT NULL,
    weight numeric (6, 3) CHECK (weight > 0),
    is_hidden bool DEFAULT false
);

-- test data
INSERT INTO "user" (first_name, middle_name, last_name, birth_date, weight, is_hidden) VALUES ('Кирилл', 'Admin', 'Admin', '2004-02-26', 70.0, true);
