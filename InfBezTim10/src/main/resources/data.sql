INSERT INTO authority(authority_name)
VALUES ('ROLE_ADMIN');

INSERT INTO authority(authority_name)
VALUES ('ROLE_USER');

INSERT INTO users(name, surname, email, password, telephone_number, authority_id, is_active)
VALUES ('Admin', 'Admin', 'admin@gmail.com', 'admin123', '+381611667584', '1' , true);
INSERT INTO Admin (id) values ( 1 );