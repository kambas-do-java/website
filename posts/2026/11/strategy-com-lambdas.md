@title Strategy: trocar o algoritmo sem if em cadeia
@date 2026-11-09
@author Dumilde Fernando
@tags Design Patterns, Java 25, Lambdas
@description O padrão Strategy em Java moderno: uma interface funcional e lambdas substituem hierarquias inteiras de classes.

Série de design patterns em Java moderno: o que sobrou de cada padrão clássico quando a linguagem já tem records, lambdas e pattern matching.

## O problema

Tens uma regra que muda conforme o contexto, por exemplo o desconto de um pedido. A tentação é uma cadeia de `if/else` que cresce a cada campanha nova:

```java
if (tipo.equals("blackfriday")) { ... }
else if (tipo.equals("fiel")) { ... }
else if (tipo.equals("nenhum")) { ... }
```

Cada desconto novo obriga a mexer no mesmo método.

## O padrão

O Strategy isola cada algoritmo atrás de uma interface comum. Em Java moderno, a interface é **funcional** e cada estratégia é uma lambda:

```java
import java.util.Map;

@FunctionalInterface
interface Desconto { double aplicar(double preco); }

record Pedido(double total) {
    double comDesconto(Desconto d) { return d.aplicar(total); }
}

void main() {
    Map<String, Desconto> descontos = Map.of(
        "nenhum", p -> p,
        "blackfriday", p -> p * 0.8,
        "fiel", p -> p - Math.min(p, 5_000));

    var pedido = new Pedido(40_000);
    IO.println(pedido.comDesconto(descontos.get("blackfriday"))); // 32000.0
    IO.println(pedido.comDesconto(descontos.get("fiel")));        // 35000.0
}
```

Grava num ficheiro `Strategy.java` e corre `java Strategy.java`.

## O que ganhaste

- **Acrescentar um desconto** é acrescentar uma linha ao mapa. O `Pedido` não muda.
- **Cada estratégia testa-se sozinha**, porque é só uma função.
- **Sem classes** `DescontoBlackFriday`, `DescontoFiel`, e por aí fora.

## Já o usas sem saber

O `Comparator` é um Strategy: `lista.sort(Comparator.comparing(Pedido::total))` passa o algoritmo de ordenação como argumento.

## Quando ainda vale a pena uma classe

Se a estratégia tem estado, várias operações ou precisa de configuração própria, uma classe que implementa a interface continua a ser a melhor opção. Para uma única função, a lambda chega.
