-- 01-init-databases.sql
-- StayBook Multi-Database Architecture Initialisation Script
-- Creates three isolated PostgreSQL databases.
-- Tables, constraints, and seed data are automatically managed by Liquibase migrations on application startup.

CREATE DATABASE staybook_user_db;
CREATE DATABASE staybook_room_db;
CREATE DATABASE staybook_booking_db;
