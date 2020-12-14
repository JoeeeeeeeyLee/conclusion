DROP TABLE IF EXISTS "user";
CREATE TABLE "user"
(
    "id"            varchar(255),
    "name"          varchar(255),
    "age"           INTEGER ,
    "phone"         varchar(255),
    PRIMARY KEY (id,name)
);