# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack

Static HTML/CSS site generated from Markdown by a zero-dependency, single-file Java 24 program (`main.java`, JDK preview source-file launch). No Maven/Gradle, no external Java libraries, no JS framework. Output ships as plain HTML/CSS/vanilla JS to `site/`, deployed via GitHub Pages/Actions.

## Users

Java developers in Luanda, Angola — from beginners to experienced engineers — who want to learn, find community events, read articles, and see who else is part of the local Java scene.

## Product Purpose

The official site for Kambas do Java, a Java developer community in Luanda, Angola. It promotes the Java ecosystem locally through shared knowledge, and gives the community a home for posts (tutorials/news), events (meetups/workshops), and member profiles.

## Positioning

The only Java-specific developer community site for Luanda/Angola — content and events are locally relevant (Portuguese-language, Angola-based meetups) rather than a generic international Java resource.

## Operating Context

- Content is authored as Markdown files in `posts/YYYY/MM/*.md`, `events/YYYY/MM/*.md`, and `members/<github-username>.md`.
- `main.java` parses these, applies a custom minimal Markdown-to-HTML converter and `${placeholder}` template engine (`templates/*.html`), and writes static output to `site/`.
- Built and previewed locally with `run.sh` / `run.bat` (`java --enable-preview --source 24 main.java`).
- Content language is Portuguese.

## Capabilities and Constraints

- **Zero dependencies is a hard constraint**: the Java generator must not gain external libraries (no Markdown parser lib, no templating engine lib). Any improvement to Markdown handling, templating, or content generation must stay hand-rolled in the JDK standard library.
- The generator pipeline (`main.java`'s overall processing flow: read Markdown → parse metadata → render via `${placeholder}` templates → write static HTML) may be refactored/extended within that constraint, since design/content-generation quality is explicitly in scope for this work.
- Output must remain plain static HTML/CSS (+ optional vanilla JS) — no client-side framework, no build step beyond the Java generator.
- Known generator gaps observed in the code (candidates for improvement, not yet confirmed in scope): the hand-rolled Markdown converter doesn't support nested lists, code blocks/fences, inline formatting (bold/italic/links), or ordered lists; `member.html` template replacement for social links replaces an empty string match (`content.replace("", ...)`) rather than a named placeholder.

## Brand Commitments

- Name "Kambas do Java" is fixed.
- Existing logo (`assets/images/logo.png`) is kept.
- Existing social accounts are kept: YouTube, LinkedIn, Discord, GitHub — all under the `kambasdojava` handle/org.
- Visual design itself (colors, typography, layout, components) is open for a full redesign — nothing pinned there beyond logo and socials.

## Evidence on Hand

- Existing content: 1 sample member (`dumijdev`), sample posts under `posts/1999/06` and `posts/2025/01`, sample event under `events/2025/09`.
- No analytics, testimonials, or membership numbers on hand — stat counters on the homepage (`${member_totals}`, `${event_totals}`, `${post_totals}`) are computed live from actual content counts, not fabricated.

## Product Principles

1. Zero-dependency, hand-rolled Java stays non-negotiable — sophistication in output, not in the toolchain.
2. Content in Portuguese, scoped to what's factually true of a small, real local community — no invented scale or activity.
3. Markdown authoring must stay simple for non-designer contributors adding posts/events/members.
4. Locally relevant (Luanda/Angola) over generically international.
