create TABLE USERS (
    id serial primary key,
    email varchar(100) unique,
    password varchar(100),
    active boolean
)

