@title Java 25: o que muda no teu dia a dia
@date 2026-11-09
@author Dumilde Fernando
@tags Java 25, LTS, Novidades
@description O Java 25 é a mais recente versão LTS. Estas são as mudanças que sentes logo ao escrever código, sem decorar a lista de JEPs.

O Java 25 saiu em setembro de 2025 e é uma versão LTS: é a que as empresas vão adotar nos próximos anos. Em vez de listar todas as JEPs, ficam as que mais mexem em quem escreve código.

## Programas pequenos sem cerimónia

Já não precisas de `public static void main(String[] args)` dentro de uma classe para escrever um programa. Um ficheiro com um método `main` simples chega:

```java
void main() {
    IO.println("Olá, Luanda!");
}
```

Corre-se com `java Ola.java`. Para quem está a começar, isto tira meia hora de explicações sobre `static` e `String[]` antes do primeiro resultado.

## Importar um módulo inteiro

Em vez de dez linhas de `import java.util.*`, podes importar tudo o que o módulo `java.base` exporta:

```java
import module java.base;
```

É o que este site usa para se gerar.

## Construtores mais flexíveis

Podes escrever código **antes** de chamar `super(...)`, desde que não uses `this`. Serve sobretudo para validar argumentos sem truques estáticos:

```java
class Conta extends Titular {
    Conta(String nome, double saldo) {
        if (saldo < 0) throw new IllegalArgumentException("Saldo negativo");
        super(nome);
    }
}
```

## Menos memória, de graça

Os *compact object headers* encolhem o cabeçalho de cada objeto. Em aplicações com muitos objetos pequenos, usas menos memória e o GC trabalha menos, sem mudar uma linha de código. Em máquinas modestas, isto conta.

## O que fazer agora

1. Instala o JDK 25 e corre o teu projeto atual.
2. Lê os avisos de depreciação que aparecerem.
3. Experimenta o `main` simples num ficheiro de teste.

> Se ainda estás no Java 8 ou 11, não saltes tudo de uma vez: passa primeiro para o 17, depois para o 21 e só então para o 25.
