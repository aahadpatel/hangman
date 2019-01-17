DROP DATABASE IF EXISTS hangmanDatabase;
CREATE DATABASE hangmanDatabase;
USE hangmanDatabase;
CREATE TABLE Players (
					username VARCHAR(50), 
					password VARCHAR(50), 
					wins VARCHAR(300),
					losses VARCHAR(25)
				);