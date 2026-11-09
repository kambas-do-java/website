import module java.base;

// Gerador do site Kambas do Java (JUG Luanda).
// Markdown em posts/, events/, members/  ->  templates/*.html com ${chave}  ->  site/
// Sem dependências: só o JDK.

static final Path OUT = Path.of("site");
static final String[] MONTHS = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
    "Setembro", "Outubro", "Novembro", "Dezembro" };
static final String[] MON = { "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez" };

record Post(String title, String slug, String html, LocalDate date, String description, String author,
    List<String> tags, int minutes) {
  String href(String root) {
    return root + "posts/%d/%02d/%s.html".formatted(date.getYear(), date.getMonthValue(), slug);
  }
}

/** date == null significa "data por anunciar". */
record Event(String title, String slug, String html, LocalDate date, String location, String type,
    String description) {
  boolean upcoming() {
    return date == null || !date.isBefore(LocalDate.now());
  }

  String href(String root) {
    return root + "events/" + slug + ".html";
  }
}

record Member(String user, String name, String role, String avatar, String html, Map<String, String> links) {
  String href(String root) {
    return root + "members/" + user + ".html";
  }
}

void main() throws Exception {
  long t0 = System.currentTimeMillis();
  clean(OUT);

  var posts = loadPosts();
  var events = loadEvents();
  var members = loadMembers();

  for (var p : posts) write("posts/%d/%02d/%s.html".formatted(p.date().getYear(), p.date().getMonthValue(), p.slug()),
      page("post", p.title() + " · Kambas do Java", p.description(), "../../../", "posts", Map.of(
          "post_title", esc(p.title()),
          "post_html", p.html(),
          "post_date", longDate(p.date()),
          "post_author", esc(p.author()),
          "post_minutes", String.valueOf(p.minutes()),
          "post_tags", p.tags().stream().map(t -> "<li><a href=\"../../../all-posts.html?tag=" + URLEncoder.encode(t, StandardCharsets.UTF_8) + "\">" + esc(t) + "</a></li>").collect(Collectors.joining()))));

  for (var e : events) write("events/" + e.slug() + ".html",
      page("event", e.title() + " · Kambas do Java", e.description(), "../", "events", Map.of(
          "event_title", esc(e.title()),
          "event_html", e.html(),
          "event_day", e.date() == null ? "—" : "%02d".formatted(e.date().getDayOfMonth()),
          "event_mon", e.date() == null ? "em breve" : MON[e.date().getMonthValue() - 1],
          "event_date", e.date() == null ? "Data por anunciar" : longDate(e.date()),
          "event_location", esc(e.location()),
          "event_type", esc(e.type()))));

  for (var m : members) write("members/" + m.user() + ".html",
      page("member", m.name() + " · Kambas do Java", m.name() + ", membro da comunidade Kambas do Java",
          "../", "members", Map.of(
              "member_name", esc(m.name()),
              "member_role", esc(m.role()),
              "member_avatar", avatar(m),
              "member_html", m.html(),
              "member_links", m.links().entrySet().stream()
                  .map(l -> "<a href=\"%s\" target=\"_blank\" rel=\"noopener\">%s</a>".formatted(safeUrl(l.getValue()),
                      esc(l.getKey())))
                  .collect(Collectors.joining()))));

  // --- listagens ---
  var upcoming = events.stream().filter(Event::upcoming).toList();
  var past = events.stream().filter(e -> !e.upcoming()).toList();

  write("index.html", page("index", "Kambas do Java · JUG Luanda",
      "Kambas do Java, o JUG Luanda: a comunidade de programadores Java de Angola. Eventos, artigos e gente.", "",
      "home", Map.of(
          "board", board(upcoming.isEmpty() ? null : upcoming.get(0), posts.size(), events.size(), members.size()),
          "home_events", upcoming.isEmpty()
              ? "<p class=\"empty\">Ainda não há paragens marcadas. Entra no Discord para saber primeiro.</p>"
              : upcoming.stream().limit(3).map(e -> eventRow(e, "")).collect(Collectors.joining()),
          "home_posts", posts.isEmpty() ? "<p class=\"empty\">Os primeiros artigos estão a caminho.</p>"
              : posts.stream().limit(4).map(p -> postRow(p, "")).collect(Collectors.joining()),
          "home_members", members.stream().limit(12).map(m -> chip(m, "")).collect(Collectors.joining()),
          "member_count", String.valueOf(members.size()))));

  write("all-posts.html", page("list", "Artigos · Kambas do Java",
      "Artigos e tutoriais da comunidade Kambas do Java.", "", "posts", Map.of(
          "list_title", "Artigos",
          "list_lede", "Tutoriais, notas e novidades sobre Java, escritos por quem o usa em Angola.",
          "list_tools", filterBar(posts),
          "list_body", posts.isEmpty() ? "<p class=\"empty\">Ainda não há artigos.</p>" : groupPosts(posts))));

  write("all-events.html", page("list", "Eventos · Kambas do Java",
      "Meetups, workshops e encontros do JUG Luanda.", "", "events", Map.of(
          "list_title", "Eventos",
          "list_lede", "Meetups, workshops e encontros. Cada evento é uma paragem: aparece quem quer.",
          "list_body", (upcoming.isEmpty() ? "<p class=\"empty\">Sem paragens marcadas por agora.</p>"
              : "<h2 class=\"group\">Próximas paragens</h2>"
                  + upcoming.stream().map(e -> eventRow(e, "")).collect(Collectors.joining()))
              + (past.isEmpty() ? ""
                  : "<h2 class=\"group\">Já passaram</h2>"
                      + past.stream().map(e -> eventRow(e, "")).collect(Collectors.joining())))));

  write("all-members.html", page("list", "Membros · Kambas do Java",
      "Quem faz parte da comunidade Kambas do Java.", "", "members", Map.of(
          "list_title", "Membros",
          "list_lede", "Os kambas que já estão a bordo. Queres aparecer aqui? Abre um pull request com o teu perfil.",
          "list_body", members.isEmpty() ? "<p class=\"empty\">Ainda não há perfis.</p>"
              : "<div class=\"people\">" + members.stream().map(m -> chip(m, "")).collect(Collectors.joining())
                  + "</div>")));

  write("404.html", page("404", "Página não encontrada · Kambas do Java", "Esta paragem não existe.", "/", "", Map.of()));

  copyAssets();
  System.out.println("Site gerado em " + OUT.toAbsolutePath());
  System.out.println("Artigos: %d · Eventos: %d · Membros: %d · %d ms".formatted(posts.size(), events.size(), members.size(),
      System.currentTimeMillis() - t0));
}

// ---------------------------------------------------------------- conteúdo

record Doc(Map<String, String> meta, String body, String name) {
}

/** Linhas "@chave valor" no topo do ficheiro são metadados; o resto é o corpo. */
Doc readDoc(Path f) throws IOException {
  var meta = new LinkedHashMap<String, String>();
  var body = new StringBuilder();
  boolean head = true;
  for (String line : Files.readString(f).split("\r?\n", -1)) {
    if (head && line.startsWith("@")) {
      String[] kv = line.substring(1).split("\\s+", 2);
      meta.put(kv[0].toLowerCase(), kv.length > 1 ? kv[1].trim() : "");
    } else if (head && line.isBlank()) {
      continue;
    } else {
      head = false;
      body.append(line).append('\n');
    }
  }
  String n = f.getFileName().toString();
  return new Doc(meta, body.toString().strip(), n.substring(0, n.length() - 3));
}

List<Path> mdFiles(String dir) throws IOException {
  if (!Files.isDirectory(Path.of(dir)))
    return List.of();
  try (var s = Files.walk(Path.of(dir))) {
    return s.filter(p -> p.toString().endsWith(".md") && !p.getFileName().toString().startsWith("_")).sorted()
        .toList();
  }
}

String humanize(String n) {
  String s = n.replaceAll("[-_]+", " ").strip();
  return s.isEmpty() ? n : Character.toUpperCase(s.charAt(0)) + s.substring(1);
}

String slug(String s) {
  return Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "")
      .replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
}

List<Post> loadPosts() throws IOException {
  var list = new ArrayList<Post>();
  for (Path f : mdFiles("posts")) {
    var d = readDoc(f);
    int year = Integer.parseInt(f.getParent().getParent().getFileName().toString());
    int month = Integer.parseInt(f.getParent().getFileName().toString());
    LocalDate date = LocalDate.of(year, month, 1);
    if (d.meta().containsKey("date"))
      date = LocalDate.parse(d.meta().get("date"));
    String html = md(d.body());
    String desc = d.meta().getOrDefault("description", excerpt(d.body()));
    var tags = Arrays.stream(d.meta().getOrDefault("tags", "").split(",")).map(String::trim)
        .filter(t -> !t.isEmpty()).toList();
    int words = d.body().split("\\s+").length;
    list.add(new Post(d.meta().getOrDefault("title", humanize(d.name())), slug(d.name()), html, date, desc,
        d.meta().getOrDefault("author", "Kambas do Java"), tags, Math.max(1, (int) Math.ceil(words / 200.0))));
  }
  list.sort(Comparator.comparing(Post::date).reversed());
  return list;
}

List<Event> loadEvents() throws IOException {
  var list = new ArrayList<Event>();
  for (Path f : mdFiles("events")) {
    var d = readDoc(f);
    LocalDate date = null;
    try {
      if (d.meta().containsKey("date"))
        date = LocalDate.parse(d.meta().get("date"));
    } catch (Exception ex) {
      System.err.println("Aviso: data inválida em " + f + " (usa AAAA-MM-DD); fica 'por anunciar'.");
    }
    list.add(new Event(d.meta().getOrDefault("title", humanize(d.name())), slug(d.name()), md(d.body()), date,
        d.meta().getOrDefault("location", "Local por anunciar"), d.meta().getOrDefault("type", "meetup"),
        d.meta().getOrDefault("description", excerpt(d.body()))));
  }
  var byDate = Comparator.comparing(Event::date, Comparator.nullsLast(Comparator.naturalOrder()));
  list.sort(Comparator.comparing((Event e) -> !e.upcoming())
      .thenComparing((a, b) -> a.upcoming() ? byDate.compare(a, b) : byDate.reversed().compare(a, b)));
  return list;
}

List<Member> loadMembers() throws IOException {
  var list = new ArrayList<Member>();
  for (Path f : mdFiles("members")) {
    var d = readDoc(f);
    String user = d.name();
    var links = new LinkedHashMap<String, String>();
    links.put("GitHub", "https://github.com/" + user);
    for (String part : d.meta().getOrDefault("socials", "").split(",")) {
      String[] kv = part.split(":", 2);
      if (kv.length == 2 && !kv[0].isBlank())
        links.put(kv[0].trim(), kv[1].trim());
    }
    list.add(new Member(user, d.meta().getOrDefault("name", user), d.meta().getOrDefault("role", "Membro"),
        d.meta().getOrDefault("avatar-url", "https://github.com/" + user + ".png"), md(d.body()), links));
  }
  list.sort(Comparator.comparing(m -> m.name().toLowerCase()));
  return list;
}

String excerpt(String body) {
  for (String p : body.split("\n\\s*\n")) {
    String t = p.strip();
    if (!t.isEmpty() && !t.startsWith("#") && !t.startsWith("```")) {
      t = t.replaceAll("[`*_>\\[\\]]|\\(.*?\\)", "").replaceAll("\\s+", " ").strip();
      return t.length() > 160 ? t.substring(0, 157) + "…" : t;
    }
  }
  return "Conteúdo da comunidade Kambas do Java.";
}

// ---------------------------------------------------------------- fragmentos

String longDate(LocalDate d) {
  return "%d de %s de %d".formatted(d.getDayOfMonth(), MONTHS[d.getMonthValue() - 1].toLowerCase(), d.getYear());
}

String eventRow(Event e, String root) {
  String day = e.date() == null ? "—" : "%02d".formatted(e.date().getDayOfMonth());
  String mon = e.date() == null ? "breve" : MON[e.date().getMonthValue() - 1];
  return """
      <a class="route" href="%s">
        <span class="route-no"><b>%s</b><i>%s</i></span>
        <span class="route-body"><strong>%s</strong><span>%s · %s</span></span>
        <span class="route-go" aria-hidden="true">→</span>
      </a>
      """.formatted(e.href(root), day, mon, esc(e.title()), esc(e.type()), esc(e.location()));
}

String postRow(Post p, String root) {
  return """
      <a class="route" href="%s" data-tags="%s" data-q="%s">
        <span class="route-no"><b>%s</b><i>%d</i></span>
        <span class="route-body"><strong>%s</strong><span>%s</span></span>
        <span class="route-go" aria-hidden="true">→</span>
      </a>
      """.formatted(p.href(root), esc(String.join("|", p.tags()).toLowerCase()),
      esc((p.title() + " " + p.description()).toLowerCase()), MON[p.date().getMonthValue() - 1],
      p.date().getYear(), esc(p.title()), esc(p.description()));
}

/** Controlos de filtro; ficam escondidos (hidden) e o main.js só os mostra se houver JavaScript. */
String filterBar(List<Post> posts) {
  if (posts.size() < 2)
    return "";
  var tags = new TreeMap<String, String>();
  var count = new HashMap<String, Integer>();
  for (var p : posts)
    for (var tg : p.tags()) {
      tags.putIfAbsent(tg.toLowerCase(), tg);
      count.merge(tg.toLowerCase(), 1, Integer::sum);
    }
  var chips = tags.entrySet().stream()
      .sorted(Comparator.comparing((Map.Entry<String, String> e) -> -count.get(e.getKey()))
          .thenComparing(Map.Entry::getKey))
      .map(e -> "<button type=\"button\" class=\"chip\" aria-pressed=\"false\" data-tag=\"%s\">%s</button>"
          .formatted(esc(e.getKey()), esc(e.getValue())))
      .collect(Collectors.joining());
  return """
      <div class="filter" hidden>
        <label class="sr" for="q">Pesquisar artigos</label>
        <input id="q" type="search" placeholder="Pesquisar artigos…" autocomplete="off">
        <div class="chips" role="group" aria-label="Filtrar por tag">%s</div>
        <p class="count" aria-live="polite"></p>
      </div>
      """.formatted(chips);
}

String groupPosts(List<Post> posts) {
  var sb = new StringBuilder();
  Integer year = null;
  for (var p : posts) {
    if (year == null || year != p.date().getYear()) {
      year = p.date().getYear();
      sb.append("<h2 class=\"group\">").append(year).append("</h2>");
    }
    sb.append(postRow(p, ""));
  }
  return sb.toString();
}

String avatar(Member m) {
  return "<span class=\"avatar\" data-i=\"%s\"><img src=\"%s\" alt=\"\" loading=\"lazy\" onerror=\"this.remove()\"></span>"
      .formatted(esc(m.name().substring(0, 1).toUpperCase()), safeUrl(m.avatar()));
}

String chip(Member m, String root) {
  return "<a class=\"person\" href=\"%s\">%s<span>%s</span></a>".formatted(m.href(root), avatar(m), esc(m.name()));
}

/** O painel de destino da página inicial: só mostra o que é verdade no conteúdo. */
String board(Event next, int posts, int events, int members) {
  String line1 = next == null ? "SEM PARAGEM MARCADA" : esc(next.title()).toUpperCase();
  String line2 = next == null ? "ENTRA NO DISCORD PARA SABER PRIMEIRO"
      : (next.date() == null ? "DATA POR ANUNCIAR"
          : "%02d %s %d".formatted(next.date().getDayOfMonth(), MON[next.date().getMonthValue() - 1].toUpperCase(),
              next.date().getYear()))
          + " · " + esc(next.location()).toUpperCase();
  return """
      <div class="board" role="group" aria-label="Próxima paragem">
        <p class="board-label">Próxima paragem</p>
        <p class="board-line board-main">%s</p>
        <p class="board-line">%s</p>
        <p class="board-foot">%s · %s · %s</p>
      </div>
      """.formatted(line1, line2, plural(members, "membro", "membros"), plural(events, "evento", "eventos"),
      plural(posts, "artigo", "artigos"));
}

String plural(int n, String um, String varios) {
  return n + " " + (n == 1 ? um : varios);
}

// ---------------------------------------------------------------- templates

String read(String name) throws IOException {
  return Files.readString(Path.of("templates", name + ".html"));
}

/** Uma só passagem: valores inseridos nunca são re-interpretados como ${chave}. */
String fill(String tpl, Map<String, String> vars) {
  return Pattern.compile("\\$\\{(\\w+)}").matcher(tpl)
      .replaceAll(m -> Matcher.quoteReplacement(vars.getOrDefault(m.group(1), "")));
}

String page(String view, String title, String desc, String root, String nav, Map<String, String> vars)
    throws IOException {
  var v = new HashMap<>(vars);
  v.put("root", root);
  String content = fill(read(switch (view) {
    case "list" -> "all-posts";
    default -> view;
  }), v);
  var b = new HashMap<String, String>();
  b.put("title", esc(title));
  b.put("description", esc(desc));
  b.put("root", root);
  b.put("content", content);
  b.put("year", String.valueOf(LocalDate.now().getYear()));
  for (String n : List.of("home", "posts", "events", "members"))
    b.put("nav_" + n, n.equals(nav) ? " aria-current=\"page\"" : "");
  return fill(read("base"), b);
}

// ---------------------------------------------------------------- markdown

static final Pattern LI = Pattern.compile("^(\\s*)([-*]|\\d+\\.)\\s+(.*)$");
static final Pattern CODE = Pattern.compile("`([^`]+)`");
static final Pattern IMG = Pattern.compile("!\\[([^\\]]*)]\\(([^)\\s]+)\\)");
static final Pattern LINK = Pattern.compile("\\[([^\\]]+)]\\(([^)\\s]+)\\)");
static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*");
static final Pattern ITAL = Pattern.compile("(?<![\\w*])\\*([^*\\s][^*]*?)\\*(?![\\w*])");

String md(String src) {
  var out = new StringBuilder();
  var para = new StringBuilder();
  var lists = new ArrayDeque<String>();
  var code = new StringBuilder();
  boolean fence = false;
  String lang = "";

  for (String line : src.split("\r?\n", -1)) {
    if (fence) {
      if (line.stripTrailing().equals("```")) {
        out.append("<pre><code").append(lang.isBlank() ? "" : " class=\"language-" + esc(lang) + "\"").append(">")
            .append(esc(code.toString().stripTrailing())).append("</code></pre>\n");
        fence = false;
      } else
        code.append(line).append('\n');
      continue;
    }
    if (line.startsWith("```")) {
      flush(out, para);
      closeLists(out, lists);
      fence = true;
      lang = line.substring(3).trim();
      code.setLength(0);
      continue;
    }
    if (line.isBlank()) {
      flush(out, para);
      continue;
    }
    Matcher li = LI.matcher(line);
    if (li.matches()) {
      flush(out, para);
      int depth = Math.min(li.group(1).length() / 2 + 1, lists.size() + 1);
      String tag = Character.isDigit(li.group(2).charAt(0)) ? "ol" : "ul";
      while (lists.size() > depth) {
        out.append("</li></").append(lists.pop()).append(">");
      }
      if (lists.size() == depth)
        out.append("</li>");
      else {
        lists.push(tag);
        out.append("<").append(tag).append(">");
      }
      out.append("<li>").append(inline(li.group(3)));
      continue;
    }
    flush(out, para);
    closeLists(out, lists);
    if (line.matches("^(-{3,}|\\*{3,})\\s*$"))
      out.append("<hr>\n");
    else if (line.matches("^#{1,4}\\s+.*")) {
      int n = line.indexOf(' ');
      // # do corpo passa a h2: o h1 da página é o título.
      out.append("<h%d>%s</h%d>\n".formatted(n + 1, inline(line.substring(n + 1).strip()), n + 1));
    } else if (line.startsWith(">"))
      out.append("<blockquote><p>").append(inline(line.replaceFirst("^>\\s?", ""))).append("</p></blockquote>\n");
    else
      para.append(para.isEmpty() ? "" : " ").append(line.strip());
  }
  if (fence)
    out.append("<pre><code>").append(esc(code.toString().stripTrailing())).append("</code></pre>\n");
  flush(out, para);
  closeLists(out, lists);
  return out.toString();
}

void flush(StringBuilder out, StringBuilder para) {
  if (!para.isEmpty())
    out.append("<p>").append(inline(para.toString())).append("</p>\n");
  para.setLength(0);
}

void closeLists(StringBuilder out, Deque<String> lists) {
  while (!lists.isEmpty())
    out.append("</li></").append(lists.pop()).append(">\n");
}

String inline(String s) {
  var codes = new ArrayList<String>();
  var sb = new StringBuilder();
  Matcher m = CODE.matcher(s);
  while (m.find()) {
    codes.add(esc(m.group(1)));
    m.appendReplacement(sb, "\u0000" + (codes.size() - 1) + "\u0000");
  }
  m.appendTail(sb);
  String t = esc(sb.toString());
  t = IMG.matcher(t).replaceAll(r -> Matcher
      .quoteReplacement("<img src=\"" + safeUrl(r.group(2)) + "\" alt=\"" + r.group(1) + "\" loading=\"lazy\">"));
  t = LINK.matcher(t).replaceAll(r -> Matcher
      .quoteReplacement("<a href=\"" + safeUrl(r.group(2)) + "\">" + r.group(1) + "</a>"));
  t = BOLD.matcher(t).replaceAll("<strong>$1</strong>");
  t = ITAL.matcher(t).replaceAll("<em>$1</em>");
  return Pattern.compile("\u0000(\\d+)\u0000").matcher(t)
      .replaceAll(r -> Matcher.quoteReplacement("<code>" + codes.get(Integer.parseInt(r.group(1))) + "</code>"));
}

/** Só http(s), mailto, caminhos relativos e âncoras. Recebe texto já escapado ou cru. */
String safeUrl(String u) {
  String raw = u.replace("&amp;", "&").strip();
  boolean ok = raw.matches("(?i)^(https?://|mailto:|/|\\.|#)[^\\s\"'<>]*$") || raw.matches("^[\\w-][^\\s:\"'<>]*$");
  return ok ? esc(raw) : "#";
}

String esc(String s) {
  return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
      .replace("'", "&#39;");
}

// ---------------------------------------------------------------- ficheiros

void write(String rel, String html) throws IOException {
  Path p = OUT.resolve(rel);
  Files.createDirectories(p.getParent());
  Files.writeString(p, html, StandardCharsets.UTF_8);
}

void clean(Path dir) throws IOException {
  if (Files.exists(dir))
    try (var s = Files.walk(dir)) {
      s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
    }
  Files.createDirectories(dir);
}

void copyAssets() throws IOException {
  Path src = Path.of("assets");
  try (var s = Files.walk(src)) {
    for (Path p : s.toList()) {
      Path t = OUT.resolve("assets").resolve(src.relativize(p).toString());
      if (Files.isDirectory(p))
        Files.createDirectories(t);
      else
        Files.copy(p, t, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
