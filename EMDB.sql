CREATE TABLE users (
    id varchar(100) PRIMARY KEY,
	username varchar(255) not null unique,
    email varchar(255) not null unique,
	password varchar(255) not null,
	phone varchar(100),
	address varchar(250),
	avatar text,
	is_active bit default 0
);

CREATE TABLE roles (
    id int IDENTITY(1,1) PRIMARY KEY,
	name varchar(255) not null unique
);

CREATE TABLE user_roles (
    user_id varchar(100) FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
	role_id int FOREIGN KEY REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE categories (
    id varchar(100) PRIMARY KEY,
	name varchar(255) not null unique,
	is_active bit default 0
);

CREATE TABLE maintenances (
    id varchar(100) PRIMARY KEY,
	date_maintenance date not null,
	lastdate_maintenance date,
	status int default 0,
	repeatable bit default 0,
	repeated_type int default 0,
	created_at datetime,
	user_id varchar(100) FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE equipments (
    id varchar(100) PRIMARY KEY,
	name varchar(255) not null,
	qrcode varchar(255) not null unique,
	status int default 0,
	width float(15),
	height float(15),
	range float(15),
	resolution float(15),
	weight float(15),
	description text,
	location text,
	created_at datetime,
	lastdate_maintenance date,
	category_id varchar(100) FOREIGN KEY REFERENCES categories(id) ON DELETE CASCADE,
	maintenance_id varchar(100) FOREIGN KEY REFERENCES maintenances(id) ON DELETE CASCADE,
);

CREATE TABLE images (
    id varchar(100) PRIMARY KEY,
	name varchar(255) not null,
	path varchar(255) not null,
	equipment_id varchar(100) FOREIGN KEY REFERENCES equipments(id) ON DELETE CASCADE,
);

CREATE TABLE comments (
    id varchar(100) PRIMARY KEY,
	title varchar(255) not null,
	description text,
	created_at datetime,
	equipment_id varchar(100) FOREIGN KEY REFERENCES equipments(id),
	user_id varchar(100) FOREIGN KEY REFERENCES users(id)
);

CREATE TABLE notifications (
    id varchar(100) PRIMARY KEY,
	title varchar(255) not null,
	description text,
	readed bit default 0,
	created_at datetime,
	maintenance_id varchar(100) FOREIGN KEY REFERENCES maintenances(id) ON DELETE CASCADE
);

create table bulk_equipment_logs
(
    id int identity
        constraint bulk_equipment_logs_pk
        primary key nonclustered,
    quantity int not null,
    created int not null,
    status int default 0,
    image_paths text,
    data text not null,
    created_at datetime not null,
    completed_at datetime,
    qrcode_list text not null
)

create table codes
(
    id int identity
        constraint codes_pk
        primary key nonclustered,
    code varchar(255) not null,
    used bit default 0,
    user_id varchar(100) FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE
)

-----------SEED DATA----------------------
INSERT INTO roles(name) VALUES('ROLE_ADMIN');
INSERT INTO roles(name) VALUES('ROLE_MAINTAINER');
INSERT INTO roles(name) VALUES('ROLE_GUEST');