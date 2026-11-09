@title Records, sealed e pattern matching: dados bem modelados
@date 2026-11-07
@author Dumilde Fernando
@tags Java 21, Records, Pattern Matching
@description Três funcionalidades do Java moderno que, juntas, substituem páginas de código repetitivo por poucas linhas legíveis.

Durante anos, modelar dados em Java significava escrever getters, `equals`, `hashCode` e `toString` à mão. O Java moderno resolve isto com três peças que se complementam.

## Records: dados sem cerimónia

```java
record Pagamento(String referencia, long kwanzas) {}
```

Uma linha dá-te construtor, acessores, `equals`, `hashCode` e `toString`. Os records são imutáveis, o que evita uma classe inteira de bugs.

## Sealed: fechar as opções

Uma interface `sealed` diz exatamente quem a pode implementar:

```java
sealed interface Metodo permits Multicaixa, Referencia, Dinheiro {}
record Multicaixa(String cartao) implements Metodo {}
record Referencia(String codigo) implements Metodo {}
record Dinheiro() implements Metodo {}
```

## Pattern matching: usar tudo junto

O `switch` conhece as opções e o compilador avisa se esqueceste alguma:

```java
String descrever(Metodo m) {
    return switch (m) {
        case Multicaixa(var cartao) -> "Cartão " + cartao;
        case Referencia(var codigo) -> "Referência " + codigo;
        case Dinheiro() -> "Dinheiro";
    };
}
```

Se amanhã adicionares `Transferencia` à interface, este código deixa de compilar até tratares o novo caso. O compilador passa a ser o teu revisor.

## Quando usar

- **Records** para dados que passam de um lado para o outro.
- **Sealed** quando o conjunto de variantes é conhecido e fechado.
- **Switch com patterns** para decidir por tipo sem cadeias de `instanceof`.

Funciona desde o Java 21. Se ainda não migraste, é uma boa razão.
