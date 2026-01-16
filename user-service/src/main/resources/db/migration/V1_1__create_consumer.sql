CREATE TABLE consumer (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username varchar(63) UNIQUE NOT NULL,
    email varchar(63) NOT NULL,
    password_hash varchar(63) NOT NULL,
    birth_date date,
    joined_on date DEFAULT current_date NOT NULL,
    weight numeric (6, 3) CHECK (weight > 0),
    enabled bool DEFAULT false
);

CREATE TABLE "role" (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name varchar(63) UNIQUE NOT NULL
);

CREATE TABLE consumer_roles (
    consumer_id bigint,
    role_id bigint,
    CONSTRAINT consumer_roles_pkey PRIMARY KEY (consumer_id, role_id),
    CONSTRAINT consumer_id_fk FOREIGN KEY (consumer_id) REFERENCES consumer(id),
    CONSTRAINT role_id_fk FOREIGN KEY (role_id) REFERENCES role(id)
);

-- test data
INSERT INTO "consumer" (email, username, password_hash, birth_date, weight, enabled) VALUES ('strangerforfallen@gmail.com', 'kirill', '???', '2004-02-26', 72.0, true);
INSERT INTO "role" (name) VALUES ('Admin');
INSERT INTO "consumer_roles" (consumer_id, role_id) VALUES (1, 1);