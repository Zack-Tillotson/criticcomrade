create table movies
(
      movie_id int auto_increment primary key,
      title varchar(100) not null,
      release_date timestamp not null default current_timestamp,
	rating char(10),
	rating_reason varchar(300),
	studio char(100)
);

create table movie_genres
(
	movie_id int,
	genre varchar(20) not null
);

create table movie_people
(
	mp_id int auto_increment primary key,
	name varchar(100)
);

create table movie_people_assoc
(
	movie_id int,
	mp_id int not null,
	role char(1) not null
);

create table movie_summaries
(
	movie_id int not null,
	summary longtext not null,
	source varchar(100),
	link mediumtext,
	create_date timestamp not null default current_timestamp
);

create table c_reviews
(
	review_id int auto_increment primary key,
	movie_id int not null,
	critic_id int not null,
	score int not null,
	summary longtext,
	link mediumtext,
	review_date timestamp not null default current_timestamp,
	source varchar(100)
);

create table critics
(
      critic_id int auto_increment primary key,
      name varchar(100),
      publisher varchar(100)
);

create table u_reviews
(
      review_id int auto_increment primary key,
      movie_id int not null,
      user_id int not null,
	score int not null,
	summary longtext,
	review_date timestamp not null default current_timestamp
);

create table u_review_reports
(
	report_id int primary key,
	review_id int not null,
	user_id int not null,
	reason longtext,
	report_date timestamp not null default current_timestamp,
	valid int(1) not null default 1,
	reviewed int(1) not null default 0
);

create table users
(
      user_id int auto_increment primary key,
	user_name varchar(100),
	password varchar(100),
      first_name varchar(100),
      last_name varchar(100),
      email varchar(100),
	sex varchar(1),
	age int,
	create_date timestamp not null default current_timestamp
);

create table web_sessions
(
	session_id varchar(27) primary key,
	user_id int,
	activity timestamp not null default current_timestamp,
	expires int(1) not null default 1
);

create table review_pos_cutoffs
(
	source varchar(100) primary key,
	cutoff int not null
);

create table movie_rankings
(
    movie_id int not null,
    week int not null,
    rank int not null
);

grant all on critic_review.* to 'critic_review'@'localhost' identified by 'critic_review_pwd';