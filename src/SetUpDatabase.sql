# Script for setting up the database for Skate Spots-server

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`(
	`email` VARCHAR(50) PRIMARY KEY,
	`pass` CHAR(32) NOT NULL,
	`displayname` VARCHAR(12) NOT NULL,
	`latitude` DOUBLE(14,10),
	`longitude` DOUBLE(14,10),
	`locationtime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	`bluid` CHAR(17) NOT NULL
	);
	

DROP TABLE IF EXISTS `skatespots`;
CREATE TABLE `skatespots`(
	`id` INT AUTO_INCREMENT PRIMARY KEY, 
	`name` VARCHAR(40), 
	`description` VARCHAR(500),
	`type` VARCHAR(6) CHECK(`type`="street" OR `type`="park" OR `type`="indoor"),
	`author` VARCHAR(50) REFERENCES `users`(`email`), 
	`latitude` DOUBLE(14,10), 
	`longitude` DOUBLE(14,10), 
	CHECK(NOT NULL));

DROP TABLE IF EXISTS `wifi`;
CREATE TABLE `wifi`(
	`seenat` INT REFERENCES `skatespots`(`id`),
	`ssid` CHAR(17),
	CHECK(NOT NULL),
	PRIMARY KEY(`seenat`,`ssid`));