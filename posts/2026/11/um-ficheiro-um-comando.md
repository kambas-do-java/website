@title Um ficheiro, um comando: Java sem build tool
@date 2026-11-03
@author Dumilde Fernando
@tags Java, Começar, Ferramentas
@description Para scripts, exercícios e ferramentas pequenas, o Java corre ficheiros diretamente. Sem Maven, sem Gradle, sem projeto.

A ideia de que Java exige Maven, Gradle e uma estrutura de pastas para tudo é antiga. Desde o Java 11, podes correr um ficheiro de código diretamente:

```bash
java Ola.java
```

O JDK compila em memória e executa. Não fica nada no disco.

## Quando faz sentido

- Exercícios e testes de ideias.
- Scripts de automação em Java, a linguagem que já conheces.
- Ferramentas pequenas que ficam num ficheiro só.

## Exemplo: ler um ficheiro e contar linhas

```java
import java.nio.file.*;

void main(String[] args) throws Exception {
    var linhas = Files.readAllLines(Path.of(args[0]));
    IO.println(linhas.size() + " linhas");
}
```

```bash
java Contar.java notas.txt
```

## Um exemplo real

O site que estás a ler é gerado por **um único ficheiro Java**, `main.java`, sem bibliotecas externas. Lê os artigos escritos em Markdown, aplica templates e escreve HTML. Se quiseres perceber como, o código está no GitHub da comunidade, e é um bom primeiro projeto para ler.

## Limites

Passa a usar Maven ou Gradle quando precisares de dependências externas, testes organizados ou vários módulos. Até lá, um ficheiro chega.
