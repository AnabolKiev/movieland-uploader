DROP SCHEMA IF EXISTS movieland;
CREATE SCHEMA movieland;

select * from genre;
select * from user;

SET NAMES 'utf8';
SELECT default_character_set_name FROM information_schema.SCHEMATA S
WHERE schema_name = "sql7281012";
ALTER DATABASE sql7281012 CHARACTER SET utf8 COLLATE utf8_bin;

DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS movieGenre;
DROP TABLE IF EXISTS movieCountry;
DROP TABLE IF EXISTS movie;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS country;


CREATE TABLE user (
  id INT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(30) UNIQUE NOT NULL,
  nickname VARCHAR(30),
  password VARCHAR(76),
  salt VARCHAR(76),
  role VARCHAR(20)
);

CREATE TABLE genre (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) UNIQUE
);

CREATE TABLE country (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(30) UNIQUE
);

CREATE TABLE movie (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nameRussian VARCHAR(100) ,
  nameNative VARCHAR(100),
  description VARCHAR(1000),
  yearOfRelease VARCHAR(4),
  rating DOUBLE,
  price DOUBLE,
  picturePath VARCHAR(255)
);

CREATE TABLE movieGenre (
  movieId INT,
  genreId INT,
  
  FOREIGN KEY (movieId) REFERENCES movie(id) ON DELETE CASCADE,
  FOREIGN KEY (genreId) REFERENCES genre(id) ON DELETE CASCADE
);

CREATE TABLE movieCountry (
  movieId INT,
  countryId INT,
  
  FOREIGN KEY (movieId) REFERENCES movie(id) ON DELETE CASCADE,
  FOREIGN KEY (countryId) REFERENCES country(id) ON DELETE CASCADE
);

CREATE TABLE review (
  id INT PRIMARY KEY AUTO_INCREMENT,
  movieId INT,
  userId INT,
  text VARCHAR(1000),
  
  FOREIGN KEY (movieId) REFERENCES movie(id) ON DELETE CASCADE,
  FOREIGN KEY (userId) REFERENCES user(id) ON DELETE CASCADE  
);