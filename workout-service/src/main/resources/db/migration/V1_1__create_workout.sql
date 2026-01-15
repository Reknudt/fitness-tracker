CREATE TABLE workout (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint NOT NULL, -- REFERENCES "user"(id),
    name varchar(63) NOT NULL,
    calories numeric (5, 2) NOT NULL CHECK (calories >= 0),
    activity_type int NOT NULL CHECK (activity_type IN (1, 2, 3)) , -- 1 aerobic, 2 anaerobic, 3 hybrid
    plan_type int NOT NULL CHECK (plan_type IN (1, 2)) , -- 1 linear, 2 circle
    planed_at timestamp,
    duration interval,
    note varchar(255),
    is_completed bool DEFAULT false NOT NULL
);

CREATE TABLE workout_detail (
    id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    workout_id bigint NOT NULL REFERENCES workout(id),
   -- exercise_id bigint REFERENCES exercise(id), -- add later, exercise can be custom
    name varchar(63), -- remove later
    queue bigint NOT NULL CHECK (queue > 0),
    reps int,
    sets int,
    exercise_duration interval,
    rest_duration interval,
    note varchar(255),
    UNIQUE (workout_id, queue)
);

CREATE INDEX idx_workout_user_id ON workout(user_id);
CREATE INDEX idx_detail_workout_id ON workout_detail(workout_id);
