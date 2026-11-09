@title Observer: SubmissionPublisher e o pacote Flow
@date 2026-11-09
@author Dumilde Fernando
@tags Design Patterns, Java 25, Concorrência
@description O JDK já traz um publicador de eventos assíncrono, sem precisares de escrever listeners à mão.

O Observer notifica vários interessados quando algo acontece: um evento novo, um pagamento confirmado, um ficheiro alterado. Durante anos escrevíamos a nossa lista de `Listener`. Desde o Java 9 existe uma alternativa no JDK.

## O mais simples possível

`SubmissionPublisher` publica itens e entrega-os de forma assíncrona a quem se subscreveu:

```java
import java.util.concurrent.SubmissionPublisher;

void main() {
    try (var publicador = new SubmissionPublisher<String>()) {
        var fim = publicador.consume(msg -> IO.println("Recebi: " + msg));

        publicador.submit("Novo evento marcado");
        publicador.submit("Novo artigo publicado");

        publicador.close();
        fim.join(); // espera que tudo seja entregue
    }
}
```

Saída:

```
Recebi: Novo evento marcado
Recebi: Novo artigo publicado
```

## Pontos a ter em conta

- **Subscreve antes de publicar.** Itens enviados antes de haver subscritor perdem-se.
- **É assíncrono.** Se o programa terminar antes da entrega, perdes mensagens. Por isso o `fim.join()`.
- **Vários subscritores** recebem cada um a sua cópia dos itens.

## Backpressure

A interface `Flow.Subscriber` permite ao consumidor dizer quantos itens consegue processar (`request(n)`). Se for mais lento do que o produtor, o publicador aplica contrapressão em vez de se encher de memória. Para casos simples, `consume` trata disto por ti.

## Quando não usar

Para notificações simples dentro do mesmo thread, uma lista de `Consumer<Evento>` chega. Para fluxos complexos com transformações, bibliotecas como o Reactor ou o RxJava têm mais operadores.
