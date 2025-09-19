# Kambas do Java - Site Estático 🇦🇴☕

Site oficial da comunidade **Kambas do Java (Luanda JUG)** - a primeira comunidade de desenvolvedores Java em Luanda, Angola.

## 🚀 Tecnologias

- **Java 25** com main instance method
- **JTE** (Java Template Engine)
- **Single File** - Tudo em um arquivo Java
- **GitHub Actions** para CI/CD
- **GitHub Pages** para hospedagem
- **Markdown** para posts
- **CSS moderno** com animações e responsividade

## 📁 Estrutura do Projeto

```
kambas-do-java/
├── StaticSiteGenerator.java    # Gerador principal (arquivo único)
├── posts/                      # Posts em Markdown
│   ├── 2024/
│   │   ├── 12/
│   │   │   └── bem-vindos-kambas-java.md
│   │   └── 11/
│   │       └── java-21-novidades.md
├── site/                      # Site gerado (criado automaticamente)
│   ├── index.html
│   ├── posts/
│   └── assets/
├── .github/workflows/
│   └── deploy.yml             # GitHub Actions
└── README.md
```

## 🎨 Design

O site utiliza as cores da bandeira de Angola:
- **Vermelho**: `#CE1126`
- **Preto**: `#000000`
- **Amarelo**: `#FFCD00`
- **Azul**: `#0F47AF`
- **Fundo**: Branco predominante

### Características do Design:
- ✅ Totalmente responsivo
- ✅ Animações suaves e modernas
- ✅ SEO otimizado
- ✅ Estrutura semântica HTML5
- ✅ Schema.org para melhor indexação
- ✅ Open Graph para redes sociais
- ✅ Performance otimizada

## 📝 Como Adicionar Posts

1. Crie um arquivo `.md` na pasta `posts/YYYY/MM/`
2. Use o formato frontmatter:

```markdown
---
title: Título do Post
date: 2024-12-01
excerpt: Breve descrição do post
---

# Conteúdo do Post

Seu conteúdo em Markdown aqui...
```

### Markdown Suportado:
- Headers (`#`, `##`, `###`)
- **Negrito** e *Itálico*
- `Código inline`
- ```Blocos de código```
- Listas com `-`
- Parágrafos automáticos

## 🔧 Como Executar Localmente

```bash
# Compilar
javac StaticSiteGenerator.java

# Executar
java StaticSiteGenerator

# O site será gerado na pasta 'site/'
# Abra site/index.html no navegador
```

## 🚀 Deploy Automático

O site é automaticamente deployado no GitHub Pages quando você:

1. Faz push para a branch `main`
2. O GitHub Actions executa:
   - Compila o `StaticSiteGenerator.java`
   - Executa e gera o site estático
   - Faz deploy para GitHub Pages

### Configurar GitHub Pages:
1. Vá em **Settings > Pages**
2. Source: **GitHub Actions**
3. O site ficará disponível em: `https://[username].github.io/[repository]/`

## 🏗️ Arquitetura

### StaticSiteGenerator.java
- **Single File**: Todo o código em um arquivo Java
- **Zero Dependencies**: Usa apenas Java padrão
- **Markdown Parser**: Parser simples integrado
- **Template Engine**: JTE-like templates em strings
- **CSS Generator**: CSS moderno gerado programaticamente

### Funcionalidades:
- ✅ Parse de Markdown para HTML
- ✅ Frontmatter metadata
- ✅ Agrupamento por ano/mês
- ✅ Geração de páginas individuais
- ✅ SEO completo
- ✅ Sitemap automático
- ✅ CSS responsivo e animado

## 📊 SEO Features

- Meta tags completas
- Open Graph para redes sociais
- Schema.org structured data
- URLs amigáveis
- Títulos e descrições otimizados
- Performance otimizada

## 🤝 Como Contribuir

1. Fork o repositório
2. Crie uma branch: `git checkout -b minha-feature`
3. Adicione seus posts ou melhorias
4. Commit: `git commit -m 'Adicionar nova feature'`
5. Push: `git push origin minha-feature`
6. Abra um Pull Request

## 📧 Contato

- **Email**: contato@kambasdojava.ao
- **WhatsApp**: +244 900 000 000
- **GitHub**: [github.com/kambas-do-java](https://github.com/kambas-do-java)
- **LinkedIn**: Kambas do Java Luanda

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para detalhes.

---

**Feito com ❤️ e ☕ em Angola** 🇦🇴

*Kambas do Java - Construindo o futuro da programação em Luanda*