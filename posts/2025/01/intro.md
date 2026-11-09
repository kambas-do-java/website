@author Dumilde Fernando
@tags Java, Java 17, Começar
@title Introdução ao Java 17
@description As novidades do Java 17 que mudaram a forma de modelar classes: sealed classes e pattern matching.

Este foi o primeiro artigo da comunidade Kambas do Java. O Java 17 é uma versão LTS e trouxe ferramentas que ainda hoje usamos todos os dias.

## Novidades do Java 17

- **Sealed classes:** controlam quais classes podem estender uma classe.
- **Pattern matching:** simplifica a verificação de tipos e o casting.
- **Novos métodos** em classes utilitárias.

## Como usar

```java
public sealed class Shape permits Circle, Square {
}

public final class Circle extends Shape {
    private double radius;
}
```
