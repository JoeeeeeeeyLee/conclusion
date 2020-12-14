DROP TABLE IF EXISTS "user";
CREATE TABLE "user"
(
    "id"            varchar(255),
    "name"          varchar(255),
    "age"           smallint,
    "phone"         varchar(255),
    PRIMARY KEY (id,name)
);