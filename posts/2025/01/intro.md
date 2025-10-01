@author Jonas Savimbi
@tags Java, Kotlin, Spring Boot
@title Introdução ao Java 17
@description Esta é a descrição do post

Introdução ao Java 17

Este é o primeiro post da comunidade Kambas do Java! Vamos explorar as novidades do Java 17.

## Novidades do Java 17

- Sealed Classes: permitem controlar quais classes podem estender uma classe
- Pattern Matching: simplifica a verificação de tipos e casting
- Novos métodos em classes utilitárias

## Como usar

```java
public sealed class Shape permits Circle, Square {
    // ...
}

public final class Circle extends Shape {
    private double radius;
    // ...
}

