@title Virtual threads em dez minutos
@date 2026-11-05
@author Dumilde Fernando
@tags Java 21, Concorrência, Performance
@description Como as virtual threads permitem servir milhares de pedidos em paralelo com código simples e bloqueante.

Durante muito tempo, atender muitos pedidos em simultâneo em Java obrigava a escolher entre código simples (uma thread por pedido, que não escala) e código assíncrono (que escala, mas é difícil de ler e depurar). As virtual threads, finais desde o Java 21, acabam com esse dilema.

## A ideia

Uma thread tradicional é cara: ocupa memória do sistema operativo e há um limite prático de milhares. Uma virtual thread é gerida pela JVM e custa muito pouco. Quando bloqueia à espera de rede ou disco, a JVM liberta a thread real para fazer outra coisa.

## Em código

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 10_000; i++) {
        int pedido = i;
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return pedido;
        });
    }
}
```

Dez mil tarefas a dormir um segundo cada terminam em pouco mais de um segundo no total. Com uma pool de threads tradicional, demorariam bem mais.

## Regras práticas

- **Uma virtual thread por tarefa.** Não faças pool delas: são baratas.
- **Serve para trabalho que espera** (rede, base de dados). Não acelera cálculo puro.
- **Cuidado com `synchronized` longos** em torno de operações bloqueantes: usa `ReentrantLock`.
- **Mantém o código bloqueante.** É o ponto de tudo isto.

## Para experimentar

Cria um ficheiro `Teste.java` com o exemplo acima, adiciona `import java.util.concurrent.*; import java.time.Duration;` e corre `java Teste.java`.
