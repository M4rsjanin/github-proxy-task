# GitHub Proxy

Aplikacja typu proxy, która pobiera dane o repozytoriach użytkownika z API GitHub, filtruje je i zwraca w uproszczonym formacie.

## Wymagania systemowe
* Java 25
* Maven

## Budowanie i uruchamianie

Aby uruchomić aplikację:

./mvnw spring-boot:run


Aby zbudować projekt i uruchomić testy:
```bash
./mvnw clean package
```
Aby uruchomić aplikację:
```bash
./mvnw spring-boot:run
```

Stack technologiczny
Język: Java 25

Framework: Spring Boot 4.0.0

Komunikacja HTTP: Spring RestClient

Testy: JUnit 5, WireMock (Standalone), WebTestClientI tera
