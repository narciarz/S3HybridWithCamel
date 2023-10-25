CREATE TABLE CAMEL_MESSAGEPROCESSED
(
    id  number(38) primary key not null,
    processor_name VARCHAR(255),
    message_id VARCHAR(100),
    created_at TIMESTAMP
);
