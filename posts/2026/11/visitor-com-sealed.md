@title Visitor: sealed e pattern matching tornam-no quase desnecessário
@date 2026-11-09
@author Dumilde Fernando
@tags Design Patterns, Java 25, Pattern Matching
@description O Visitor clássico exige um método accept em cada classe. Com sealed interfaces, records e switch, o mesmo resultado cabe numa função.

O Visitor serve para acrescentar operações a uma hierarquia de tipos sem a modificar. O custo clássico é pesado: uma interface `Visitor`, um método `accept` em cada classe e dupla despacho.

## Modelar a hierarquia

Uma pequena árvore de expressões, com um conjunto fechado de tipos:

```java
sealed interface Expr permits Num, Soma, Mul {}
record Num(double valor) implements Expr {}
record Soma(Expr esq, Expr dir) implements Expr {}
record Mul(Expr esq, Expr dir) implements Expr {}
```

## Cada operação é um switch

```java
double avaliar(Expr e) {
    return switch (e) {
        case Num(var v) -> v;
        case Soma(var a, var b) -> avaliar(a) + avaliar(b);
        case Mul(var a, var b) -> avaliar(a) * avaliar(b);
    };
}

String mostrar(Expr e) {
    return switch (e) {
        case Num(var v) -> String.valueOf(v);
        case Soma(var a, var b) -> "(" + mostrar(a) + " + " + mostrar(b) + ")";
        case Mul(var a, var b) -> mostrar(a) + " * " + mostrar(b);
    };
}

void main() {
    Expr e = new Mul(new Soma(new Num(2), new Num(3)), new Num(4));
    IO.println(mostrar(e) + " = " + avaliar(e)); // (2.0 + 3.0) * 4.0 = 20.0
}
```

## Porque é melhor

- **Sem `accept`.** As classes não sabem que existem operações sobre elas.
- **O compilador verifica.** O `switch` sobre uma interface `sealed` tem de cobrir todos os casos, por isso, se acrescentares `Sub`, o código deixa de compilar até o tratares em cada operação.
- **Operações novas** são funções novas, sem tocar na hierarquia.

## O limite

O Visitor clássico continua a fazer sentido quando a hierarquia **não é fechada** (bibliotecas com extensões de terceiros). Com um conjunto de tipos conhecido, `sealed` mais `switch` ganha em quase tudo.
