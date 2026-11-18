@echo off
title TEC Backend Server (Port 8081)
cd /d D:\Tec-Backend

echo ========================================================
echo  Starting TEC Spring Boot Backend on Port 8081 (Supabase)
echo ========================================================

set SPRING_PROFILES_ACTIVE=prod
set SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-ap-southeast-2.pooler.supabase.com:5432/postgres?sslmode=require
set SPRING_DATASOURCE_USERNAME=postgres.lfdrpsabmghnkxvtssyu
set SPRING_DATASOURCE_PASSWORD=TecSuperadmin123
set JWT_SECRET=4c65634e52542d53757065725365637265744b65792d323032362d5445432d4261636b656e64
set PORT=8081

call mvnw.cmd spring-boot:run
pause

