@title Builder e Factory: records com validação
@date 2026-11-09
@author Dumilde Fernando
@tags Design Patterns, Java 25, Records
@description Quando um record chega, quando precisas de Builder e como validar dados na construção.

O Builder resolve dois problemas: construtores com muitos parâmetros e objetos que podem ficar num estado inválido. Os records tiram metade do trabalho.

## Validar no construtor compacto

Um record valida logo na criação, sem Builder:

```java
record Utilizador(String nome, String email, int idade) {
    Utilizador {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("nome obrigatório");
        if (idade < 0) throw new IllegalArgumentException("idade inválida");
    }
}
```

Um `Utilizador` inválido nunca existe, porque o construtor recusa.

## "Withers" manuais

Os records são imutáveis, por isso uma alteração cria uma nova instância:

```java
record Utilizador(String nome, String email, int idade) {
    Utilizador {
        if (nome == null || nome.isBlank()) throw new IllegalArgumentException("nome obrigatório");
        if (idade < 0) throw new IllegalArgumentException("idade inválida");
    }

    Utilizador comEmail(String novo) { return new Utilizador(nome, novo, idade); }
}

void main() {
    var u = new Utilizador("Ana", null, 30).comEmail("ana@exemplo.ao");
    IO.println(u); // Utilizador[nome=Ana, email=ana@exemplo.ao, idade=30]
}
```

## Quando ainda precisas de Builder

- **Muitos parâmetros opcionais** (mais de quatro ou cinco), em que a ordem baralha.
- **Construção em passos**, com regras que só se verificam no fim.
- **Objetos que não são records**, como classes com herança.

Um Builder mínimo para esse caso:

```java
class Pedido {
    private final String cliente;
    private final int quantidade;

    private Pedido(Builder b) { cliente = b.cliente; quantidade = b.quantidade; }

    static class Builder {
        private String cliente;
        private int quantidade = 1;

        Builder cliente(String c) { this.cliente = c; return this; }
        Builder quantidade(int q) { this.quantidade = q; return this; }

        Pedido build() {
            if (cliente == null) throw new IllegalStateException("cliente obrigatório");
            return new Pedido(this);
        }
    }
}
```

## Factory: nome em vez de construtor

Quando a criação tem nome e regras, um método estático diz mais do que `new`:

```java
record Dinheiro(long kwanzas) {
    static Dinheiro deKwanzas(long k) { return new Dinheiro(k); }
    static Dinheiro zero() { return new Dinheiro(0); }
}
```

## Para guardar

Começa pelo record. Só chegas ao Builder quando os parâmetros opcionais começam a incomodar.
