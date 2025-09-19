void main() throws Exception {
  Path postsDir = Paths.get("posts");
  Path eventsDir = Paths.get("events");
  Path siteDir = Paths.get("site");
  Path assetsDir = Paths.get("assets");
  Path templatesDir = Paths.get("templates");

  // Limpa siteDir se existir
  if (Files.exists(siteDir)) {
    Files.walk(siteDir)
        .sorted(Comparator.reverseOrder())
        .map(Path::toFile)
        .forEach(File::delete);
  }
  Files.createDirectories(siteDir);

  List<Post> posts = new ArrayList<>();
  List<Event> events = new ArrayList<>();

  // Processa posts
  if (Files.exists(postsDir)) {
    Files.walk(postsDir)
        .filter(p -> p.toString().endsWith(".md"))
        .forEach(mdFile -> {
          try {
            String md = Files.readString(mdFile);
            String description = MarkdownProcessor.extractDescription(md);
            String html = MarkdownProcessor.markdownToHtml(md);
            String fileName = mdFile.getFileName().toString().replace(".md", "");
            int year = Integer.parseInt(mdFile.getParent().getParent().getFileName().toString());
            int month = Integer.parseInt(mdFile.getParent().getFileName().toString());
            String slug = fileName.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9\\-]", "");
            Post post = new Post(fileName, slug, html, year, month, description);
            posts.add(post);

            // Gera HTML do post
            String postHtml = TemplateEngine.renderPost(post);
            Path outDir = siteDir.resolve("posts/" + year + "/" + String.format("%02d", month));
            Files.createDirectories(outDir);
            Files.writeString(outDir.resolve(post.slug + ".html"), postHtml);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  // Processa eventos
  if (Files.exists(eventsDir)) {
    Files.walk(eventsDir)
        .filter(p -> p.toString().endsWith(".md"))
        .forEach(mdFile -> {
          try {
            String md = Files.readString(mdFile);
            String fileName = mdFile.getFileName().toString();
            Event event = MarkdownProcessor.parseEventFromMarkdown(md, fileName);
            events.add(event);

            // Gera HTML do evento
            String eventHtml = TemplateEngine.renderEvent(event);
            Path outDir = siteDir.resolve("events");
            Files.createDirectories(outDir);
            Files.writeString(outDir.resolve(event.slug + ".html"), eventHtml);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  // Ordena posts do mais recente para o mais antigo
  posts.sort((p1, p2) -> {
    if (p1.year != p2.year) return Integer.compare(p2.year, p1.year);
    return Integer.compare(p2.month, p1.month);
  });

  // Ordena eventos: próximos primeiro, depois passados
  events.sort((e1, e2) -> {
    if (e1.isUpcoming != e2.isUpcoming) {
      return Boolean.compare(e2.isUpcoming, e1.isUpcoming); // Upcoming first
    }
    return e2.date.compareTo(e1.date); // Most recent first
  });

  // Gera index
  String indexHtml = TemplateEngine.renderIndex(posts, events);
  Files.writeString(siteDir.resolve("index.html"), indexHtml);

  // Gera página com todos os posts
  String allPostsHtml = TemplateEngine.renderAllPosts(posts);
  Files.writeString(siteDir.resolve("all-posts.html"), allPostsHtml);

  // Gera página com todos os eventos
  String allEventsHtml = TemplateEngine.renderAllEvents(events);
  Files.writeString(siteDir.resolve("all-events.html"), allEventsHtml);

  // Copia assets
  if (Files.exists(assetsDir)) {
    Files.walk(assetsDir).forEach(p -> {
      try {
        Path rel = assetsDir.relativize(p);
        Path target = siteDir.resolve("assets").resolve(rel);
        if (Files.isDirectory(p)) {
          Files.createDirectories(target);
        } else {
          Files.createDirectories(target.getParent());
          Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  println("Site gerado em: " + siteDir.toAbsolutePath());
  println("Posts processados: " + posts.size());
  println("Eventos processados: " + events.size());
}

class Post {
  public String title;
  public String slug;
  public String content;
  public int year;
  public int month;
  public String description;

  public Post(String title, String slug, String content, int year, int month, String description) {
    this.title = title;
    this.slug = slug;
    this.content = content;
    this.year = year;
    this.month = month;
    this.description = description;
  }
}

class MarkdownProcessor {

  public static String markdownToHtml(String md) {
    String[] lines = md.split("\\r?\\n");
    StringBuilder sb = new StringBuilder();
    boolean inList = false;
    for (String line : lines) {
      if (line.startsWith("### ")) {
        if (inList) {
          sb.append("</ul>\n");
          inList = false;
        }
        sb.append("<h3>").append(escapeHtml(line.substring(4))).append("</h3>\n");
      } else if (line.startsWith("## ")) {
        if (inList) {
          sb.append("</ul>\n");
          inList = false;
        }
        sb.append("<h2>").append(escapeHtml(line.substring(3))).append("</h2>\n");
      } else if (line.startsWith("# ")) {
        if (inList) {
          sb.append("</ul>\n");
          inList = false;
        }
        sb.append("<h1>").append(escapeHtml(line.substring(2))).append("</h1>\n");
      } else if (line.startsWith("- ")) {
        if (!inList) {
          sb.append("<ul>\n");
          inList = true;
        }
        sb.append("<li>").append(escapeHtml(line.substring(2))).append("</li>\n");
      } else if (line.startsWith("> ")) {
        if (inList) {
          sb.append("</ul>\n");
          inList = false;
        }
        sb.append("<blockquote>").append(escapeHtml(line.substring(2))).append("</blockquote>\n");
      } else {
        if (inList) {
          sb.append("</ul>\n");
          inList = false;
        }
        if (line.isBlank()) {
          // ignora
        } else {
          sb.append("<p>").append(escapeHtml(line)).append("</p>\n");
        }
      }
    }
    if (inList) sb.append("</ul>\n");
    return sb.toString();
  }

  public static String extractDescription(String md) {
    // Extrai a primeira linha não vazia como descrição
    String[] lines = md.split("\\r?\\n");
    for (String line : lines) {
      if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
        return line.trim().replaceAll("[#*\\-_>]", "").trim();
      }
    }
    return "Artigo da comunidade Kambas do Java";
  }

  public static Map<String, String> extractEventMetadata(String md) {
    Map<String, String> metadata = new HashMap<>();
    String[] lines = md.split("\\r?\\n");

    for (String line : lines) {
      if (line.startsWith("<!--") && line.contains(":")) {
        String cleanLine = line.replace("<!--", "").replace("-->", "").trim();
        String[] parts = cleanLine.split(":", 2);
        if (parts.length == 2) {
          String key = parts[0].trim().toLowerCase();
          String value = parts[1].trim();
          metadata.put(key, value);
        }
      } else if (line.startsWith("#")) {
        break; // Para no primeiro heading
      }
    }

    return metadata;
  }

  public static Event parseEventFromMarkdown(String md, String fileName) {
    Map<String, String> metadata = extractEventMetadata(md);

    String title = fileName.replace(".md", "");
    String slug = title.toLowerCase()
        .replace(" ", "-")
        .replaceAll("[^a-z0-9\\-]", "");

    // Extrai conteúdo (remove metadata)
    String content = md.replaceAll("(?s)<!--.*?-->", "").trim();
    content = markdownToHtml(content);

    // Parse metadata
    LocalDate date = LocalDate.now();
    if (metadata.containsKey("date")) {
      try {
        date = LocalDate.parse(metadata.get("date"));
      } catch (Exception e) {
        // Usa data atual se parsing falhar
      }
    }

    String location = metadata.getOrDefault("location", "Online");
    String description = metadata.getOrDefault("description",
        extractDescription(md));
    String eventType = metadata.getOrDefault("type", "meetup");

    boolean isUpcoming = date.isAfter(LocalDate.now().minusDays(1));

    return new Event(title, slug, content, date, location, description, eventType, isUpcoming);
  }

  public static String escapeHtml(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }
}

class TemplateEngine {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM", new Locale("pt"));
  private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

  public static String renderIndex(List<Post> posts, List<Event> events) throws Exception {
    String template = Files.readString(Paths.get("templates/base.html"));
    String content = Files.readString(Paths.get("templates/index.html"));

    // Prepara posts recentes
    List<Post> recentPosts = posts.stream().limit(3).collect(Collectors.toList());
    StringBuilder postsHtml = new StringBuilder();
    for (Post post : recentPosts) {
      postsHtml.append("<div class=\"post-preview\">")
          .append("<h3><a href=\"posts/").append(post.year).append("/")
          .append(String.format("%02d", post.month)).append("/")
          .append(post.slug).append(".html\">").append(post.title).append("</a></h3>")
          .append("<p class=\"post-meta\">").append(String.format("%02d", post.month))
          .append("/").append(post.year).append("</p>")
          .append("<p>").append(post.description).append("</p>")
          .append("</div>");
    }

    // Prepara eventos próximos
    List<Event> upcomingEvents = events.stream()
        .filter(e -> e.isUpcoming)
        .limit(2)
        .collect(Collectors.toList());

    StringBuilder eventsHtml = new StringBuilder();
    for (Event event : upcomingEvents) {
      eventsHtml.append("<div class=\"event\">")
          .append("<div class=\"event-date\">")
          .append("<span class=\"event-day\">").append(event.date.format(DAY_FORMATTER)).append("</span>")
          .append("<span class=\"event-month\">").append(event.date.format(MONTH_FORMATTER)).append("</span>")
          .append("</div>")
          .append("<div class=\"event-details\">")
          .append("<h3><a href=\"events/").append(event.slug).append(".html\">").append(event.title).append("</a></h3>")
          .append("<p class=\"event-location\">").append(event.location).append("</p>")
          .append("<p>").append(event.description).append("</p>")
          .append("<a href=\"events/").append(event.slug).append(".html\" class=\"event-link\">Mais informações</a>")
          .append("</div>")
          .append("</div>");
    }

    // Substitui placeholders
    content = content.replace("${recent_posts}", postsHtml.toString())
        .replace("${upcoming_events}", eventsHtml.toString());

    return applyBaseTemplate(template, content, "Kambas do Java - Comunidade Java de Luanda",
        "Comunidade de desenvolvedores Java de Luanda, Angola. Eventos, tutoriais e notícias sobre Java e tecnologia.", true);
  }

  public static String renderPost(Post post) throws Exception {
    String template = Files.readString(Paths.get("templates/base.html"));
    String content = Files.readString(Paths.get("templates/post.html"));

    // Formata a data do post
    String postDate = String.format("%02d/%d", post.month, post.year);

    // Substitui placeholders
    content = content.replace("${post_title}", post.title)
        .replace("${post_content}", post.content)
        .replace("${post_date}", postDate);

    // Calcula o caminho relativo para assets
    String basePath = "../../../";

    // Aplica o template base
    return applyBaseTemplate(template, content, post.title + " - Kambas do Java",
        post.description, false, basePath);
  }

  public static String renderAllPosts(List<Post> posts) throws Exception {
    String template = Files.readString(Paths.get("templates/base.html"));
    String content = Files.readString(Paths.get("templates/all-posts.html"));

    // Agrupa posts por ano e mês
    Map<Integer, Map<Integer, List<Post>>> postsByYearMonth = posts.stream()
        .collect(Collectors.groupingBy(p -> p.year,
            TreeMap::new,
            Collectors.groupingBy(p -> p.month, TreeMap::new, Collectors.toList())));

    StringBuilder postsHtml = new StringBuilder();

    for (Integer year : postsByYearMonth.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
      postsHtml.append("<div class=\"year-section\">")
          .append("<h2>").append(year).append("</h2>");

      Map<Integer, List<Post>> postsByMonth = postsByYearMonth.get(year);
      for (Integer month : postsByMonth.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList())) {
        postsHtml.append("<div class=\"month-section\">")
            .append("<h3>").append(getMonthName(month)).append("</h3>")
            .append("<div class=\"posts-list\">");

        for (Post post : postsByMonth.get(month)) {
          postsHtml.append("<div class=\"post-item\" data-categories=\"java tutorial\">")
              .append("<div class=\"post-date\">")
              .append("<span class=\"post-day\">").append(String.format("%02d", post.month)).append("</span>")
              .append("<span class=\"post-month\">").append(getMonthAbbr(month)).append("</span>")
              .append("</div>")
              .append("<div class=\"post-content\">")
              .append("<h4><a href=\"posts/").append(year).append("/")
              .append(String.format("%02d", month)).append("/")
              .append(post.slug).append(".html\">").append(post.title).append("</a></h4>")
              .append("<p class=\"post-excerpt\">").append(post.description).append("</p>")
              .append("<div class=\"post-meta\">")
              .append("<span class=\"post-category\">Java</span>")
              .append("<span>").append(post.description.length() > 100 ? "5 min read" : "2 min read").append("</span>")
              .append("</div>")
              .append("</div>")
              .append("</div>");
        }

        postsHtml.append("</div></div>");
      }

      postsHtml.append("</div>");
    }

    // Substitui placeholders
    content = content.replace("${all_posts}", postsHtml.toString())
        .replace("${total_posts}", String.valueOf(posts.size()))
        .replace("${total_years}", String.valueOf(postsByYearMonth.keySet().size()))
        .replace("${total_categories}", "8");

    // Aplica o template base
    return applyBaseTemplate(template, content, "Todos os Posts - Kambas do Java",
        "Lista completa de artigos e tutoriais da comunidade Kambas do Java", true);
  }

  private static String getMonthName(int month) {
    String[] months = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
    if (month >= 1 && month <= 12) {
      return months[month - 1];
    }
    return "Mês inválido";
  }

  private static String getMonthAbbr(int month) {
    String[] months = {"JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
        "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"};
    return months[month - 1];
  }

  public static String renderEvent(Event event) throws Exception {
    String template = Files.readString(Paths.get("templates/base.html"));
    String content = Files.readString(Paths.get("templates/event.html"));

    // Substitui placeholders
    content = content.replace("${event_title}", event.title)
        .replace("${event_content}", event.content)
        .replace("${event_date}", event.date.format(DATE_FORMATTER))
        .replace("${event_location}", event.location)
        .replace("${event_type}", event.eventType);

    String basePath = "../";
    return applyBaseTemplate(template, content, event.title + " - Kambas do Java",
        event.description, false, basePath);
  }

  public static String renderAllEvents(List<Event> events) throws Exception {
    String template = Files.readString(Paths.get("templates/base.html"));
    String content = Files.readString(Paths.get("templates/all-events.html"));

    // Separa eventos por status
    List<Event> upcomingEvents = events.stream()
        .filter(e -> e.isUpcoming)
        .collect(Collectors.toList());

    List<Event> pastEvents = events.stream()
        .filter(e -> !e.isUpcoming)
        .collect(Collectors.toList());

    StringBuilder eventsHtml = new StringBuilder();

    // Eventos próximos
    if (!upcomingEvents.isEmpty()) {
      eventsHtml.append("<div class=\"events-section\">")
          .append("<h2>Próximos Eventos</h2>")
          .append("<div class=\"events-list\">");

      for (Event event : upcomingEvents) {
        eventsHtml.append(renderEventItem(event));
      }

      eventsHtml.append("</div></div>");
    }

    // Eventos passados
    if (!pastEvents.isEmpty()) {
      eventsHtml.append("<div class=\"events-section\">")
          .append("<h2>Eventos Passados</h2>")
          .append("<div class=\"events-list\">");

      for (Event event : pastEvents) {
        eventsHtml.append(renderEventItem(event));
      }

      eventsHtml.append("</div></div>");
    }

    content = content.replace("${all_events}", eventsHtml.toString())
        .replace("${total_events}", String.valueOf(events.size()))
        .replace("${upcoming_count}", String.valueOf(upcomingEvents.size()))
        .replace("${past_count}", String.valueOf(pastEvents.size()));

    return applyBaseTemplate(template, content, "Todos os Eventos - Kambas do Java",
        "Calendário completo de eventos, meetups e workshops da comunidade Kambas do Java", true);
  }

  private static String renderEventItem(Event event) {
    return "<div class=\"event-item\">" +
        "<div class=\"event-date\">" +
        "<span class=\"event-day\">" + event.date.format(DAY_FORMATTER) + "</span>" +
        "<span class=\"event-month\">" + event.date.format(MONTH_FORMATTER) + "</span>" +
        "<span class=\"event-year\">" + event.date.getYear() + "</span>" +
        "</div>" +
        "<div class=\"event-content\">" +
        "<h3><a href=\"events/" + event.slug + ".html\">" + event.title + "</a></h3>" +
        "<p class=\"event-meta\">" +
        "<span class=\"event-location\">📍 " + event.location + "</span>" +
        "<span class=\"event-type\">" + getEventTypeIcon(event.eventType) + " " + event.eventType + "</span>" +
        "</p>" +
        "<p class=\"event-description\">" + event.description + "</p>" +
        "<a href=\"events/" + event.slug + ".html\" class=\"btn btn-outline\">Ver Detalhes</a>" +
        "</div>" +
        "</div>";
  }

  private static String getEventTypeIcon(String eventType) {
    return switch (eventType.toLowerCase()) {
      case "workshop" -> "🔧";
      case "meetup" -> "👥";
      case "conference" -> "🎤";
      case "hackathon" -> "💻";
      default -> "📅";
    };
  }

  private static String applyBaseTemplate(String template, String content, String title,
                                          String description, boolean isIndex) {
    return applyBaseTemplate(template, content, title, description, isIndex, "");
  }

  private static String applyBaseTemplate(String template, String content, String title,
                                          String description, boolean isIndex, String basePath) {
    String currentYear = String.valueOf(LocalDate.now().getYear());

    return template.replace("${page_title}", title)
        .replace("${page_description}", description)
        .replace("${page_content}", content)
        .replace("${current_year}", currentYear)
        .replace("${base_path}", basePath);
  }
}


static class Event {
  public String title;
  public String slug;
  public String content;
  public LocalDate date;
  public String location;
  public String description;
  public String eventType; // workshop, meetup, conference, etc.
  public boolean isUpcoming;

  public Event(String title, String slug, String content, LocalDate date,
               String location, String description, String eventType, boolean isUpcoming) {
    this.title = title;
    this.slug = slug;
    this.content = content;
    this.date = date;
    this.location = location;
    this.description = description;
    this.eventType = eventType;
    this.isUpcoming = isUpcoming;
  }
}