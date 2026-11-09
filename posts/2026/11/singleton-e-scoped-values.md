@title Singleton e Scoped Values: menos estado global
@date 2026-11-09
@author Dumilde Fernando
@tags Design Patterns, Java 25, Scoped Values
@description Como fazer um Singleton correto em Java e quando o Scoped Value do Java 25 é uma alternativa melhor ao estado global.

O Singleton garante uma única instância. É o padrão mais usado e o mais abusado, porque é estado global com bom marketing.

## Singleton correto: enum

A forma mais simples e segura em Java é um `enum` de um só valor. É seguro com threads e resiste a serialização e reflexão:

```java
enum Configuracao {
    INSTANCIA;

    private final java.util.Properties props = new java.util.Properties();

    String get(String chave) { return props.getProperty(chave, "?"); }
}
```

Usa-se com `Configuracao.INSTANCIA.get("url")`.

## O problema real

Muitas vezes o "singleton" serve para passar contexto, como o utilizador do pedido atual. Aí, o estado global é perigoso: threads diferentes pisam-se, e testar fica difícil.

## Scoped Values (final no Java 25)

Um `ScopedValue` liga um valor a **um bloco de código e às threads que ele cria**, e desaparece no fim. É imutável dentro do âmbito:

```java
static final ScopedValue<String> UTILIZADOR = ScopedValue.newInstance();

void tratarPedido() {
    IO.println("Utilizador: " + UTILIZADOR.get());
}

void main() {
    ScopedValue.where(UTILIZADOR, "ana").run(this::tratarPedido);
}
```

Saída: `Utilizador: ana`.

## Comparação

- **Singleton:** uma instância para toda a aplicação. Bom para recursos partilhados e sem estado por pedido.
- **Scoped Value:** um valor por contexto de execução. Bom para utilizador, id de pedido, transação.
- **`ThreadLocal`:** faz algo parecido, mas é mutável e vive até alguém limpar. Com virtual threads, o Scoped Value é mais leve.

## Regra prática

Antes de criar um Singleton, pergunta se aquilo é mesmo único na aplicação inteira ou apenas único **por pedido**. Se for por pedido, usa um Scoped Value, ou passa o valor como argumento.
