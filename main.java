import module java.base;

void main() throws Exception {
  var startTime = System.currentTimeMillis();
  Path siteDir = Paths.get("site");

  Utils.Processor.clearSiteDir(siteDir);

  var posts = Utils.Processor.processPosts(siteDir);
  var events = Utils.Processor.processEvents(siteDir);
  var members = Utils.Processor.processMembers(siteDir);

  Utils.Render.render(siteDir, posts, events, members);

  println("Site gerado em: " + siteDir.toAbsolutePath());
  println("Posts processados: " + posts.size());
  println("Eventos processados: " + events.size());
  println("Membros processados: " + members.size());
  var endTime = System.currentTimeMillis();
  println("Tempo total: " + (endTime - startTime) + " ms");
}

class DataSet {
  record Post(String title, String slug, String content, int year, int month, String description) {
  }

  record Member(String name, String githubUsername, String avatarUrl, String content,
      Map<String, String> socialLinks) {
  }

  record Event(String title, String slug, String content, LocalDate date,
      String location, String description, String eventType, boolean isUpcoming) {
  }
}

class Utils {
  static class MarkdownProcessor {
    public static String markdownToHtml(String md) {
      String[] lines = md.split("\r?\n");
      StringBuilder sb = new StringBuilder();
      boolean inList = false;
      for (String line : lines) {
        if (line.startsWith("@")) {
          // ignora metadados
          continue;
        }
        
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
      if (inList)
        sb.append("</ul>\n");
      return sb.toString();
    }

    public static String extractDescription(String md) {
      // Extrai a primeira linha não vazia como descrição
      String[] lines = md.split("\r?\n");
      for (String line : lines) {
        if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
          return line.trim().replaceAll("[#*\\-_>]", "").trim();
        }
      }
      return "Artigo da comunidade Kambas do Java";
    }

    public static Map<String, String> extractEventMetadata(String md) {
      Map<String, String> metadata = new HashMap<>();
      String[] lines = md.split("\r?\n");

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

    public static DataSet.Event parseEventFromMarkdown(String md, String fileName) {
      Map<String, String> metadata = extractEventMetadata(md);

      String title = fileName.replace(".md", "");
      String slug = title.toLowerCase()
          .replace(" ", "-")
          .replaceAll("[^a-z0-9-]", "");

      // Extrai conteúdo (remove metadata)
      String content = md.replaceAll("(?s)<!--.*?-->", "").trim();
      content = Utils.MarkdownProcessor.markdownToHtml(content);

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
      String description = metadata.getOrDefault("description", extractDescription(md));
      String EventType = metadata.getOrDefault("type", "meetup");

      boolean isUpcoming = date.isAfter(LocalDate.now().minusDays(1));

      return new DataSet.Event(title, slug, content, date, location, description, EventType, isUpcoming);
    }

    public static DataSet.Member parseMemberFromMarkdown(String md, String githubUsername) {
      String[] lines = md.split("\r?\n");
      var metadata = Utils.Processor.readMetadata(md);
      String name = metadata.getOrDefault("name", githubUsername);
      Map<String, String> socialLinks = new HashMap<>();
      StringBuilder content = new StringBuilder();

      for (String line : lines) {
        if (!line.trim().isEmpty() && !line.trim().startsWith("@")) {
          content.append(line).append("\n");
        }
      }

      var socials = metadata.getOrDefault("socials", "").split(",");

      Arrays.stream(socials).forEach(line -> {
        String[] parts = line.split(":", 2);
        if (parts.length == 2) {
          socialLinks.put(parts[0].trim(), parts[1].trim());
        }
      });

      String avatarUrl = metadata.getOrDefault("avatar-url", "https://github.com/" + githubUsername + ".png");

      return new DataSet.Member(name, githubUsername, avatarUrl,
          Utils.MarkdownProcessor.markdownToHtml(content.toString()), socialLinks);
    }

    public static String escapeHtml(String s) {
      return s.replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;")
          .replace("\"", "&quot;")
          .replace("'", "&#39;");
    }
  }

  static class TemplateEngine {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM",
        new Locale.Builder().setLanguage("pt").build());
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    public static String renderIndex(List<DataSet.Post> posts, List<DataSet.Event> events, List<DataSet.Member> members)
        throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/index.html"));

      // Prepara posts recentes
      List<DataSet.Post> recentPosts = posts.stream().limit(3).collect(Collectors.toList());
      StringBuilder postsHtml = new StringBuilder();
      for (DataSet.Post post : recentPosts) {
        postsHtml.append("<div class='post-preview'>")
            .append("<h3><a href='posts/").append(post.year()).append("/")
            .append(String.format("%02d", post.month())).append("/")
            .append(post.slug()).append(".html'>").append(post.title()).append("</a></h3>")
            .append("<p class='post-meta'>").append(String.format("%02d", post.month()))
            .append("/").append(post.year()).append("</p>")
            .append("<p>").append(post.description()).append("</p>")
            .append("</div>");
      }

      // Prepara Dataset.Eventos próximos
      List<DataSet.Event> upcomingEvents = events.stream()
          .filter(e -> e.isUpcoming())
          .limit(2)
          .collect(Collectors.toList());

      StringBuilder eventsHtml = new StringBuilder();
      for (var event : upcomingEvents) {
        eventsHtml.append("<div class='event'>")
            .append("<div class='event-date'>")
            .append("<span class='event-day'>").append(event.date().format(DAY_FORMATTER)).append("</span>")
            .append("<span class='event-month'>").append(event.date().format(MONTH_FORMATTER)).append("</span>")
            .append("</div>")
            .append("<div class='event-details'>")
            .append("<h3><a href='events/").append(event.slug()).append(".html'>").append(event.title())
            .append("</a></h3>")
            .append("<p class='event-location'>").append(event.location()).append("</p>")
            .append("<p>").append(event.description()).append("</p>")
            .append("<a href='events/").append(event.slug()).append(".html' class='event-link'>Mais informações</a>")
            .append("</div>")
            .append("</div>");
      }

      // Prepara membros
      StringBuilder membersHtml = new StringBuilder();
      for (DataSet.Member member : members) {
        membersHtml.append("<div class='member-card'>")
            .append("<a href='members/").append(member.githubUsername()).append(".html'>")
            .append("<img src='https://github.com/" + member.githubUsername() + ".png' alt='" + member.name() + "'>")
            .append("<h3>").append(member.name()).append("</h3>")
            .append("</a>")
            .append("</div>");
      }

      // Substitui placeholders
      content = content.replace("${recent_posts}", postsHtml.toString())
          .replace("${upcoming_events}", eventsHtml.toString())
          .replace("${members}", membersHtml.toString())
          .replace("${event_totals}", events.size() + "")
          .replace("${post_totals}", posts.size() + "")
          .replace("${member_totals}", members.size() + "");

      return applyBaseTemplate(template, content, "Kambas do Java - Comunidade Java de Luanda",
          "Comunidade de desenvolvedores Java de Luanda, Angola. Dataset.Eventos, tutoriais e notícias sobre Java e tecnologia.",
          true);
    }

    static String renderPost(DataSet.Post post) throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/post.html"));

      // Formata a data do post
      String postDate = String.format("%02d/%d", post.month(), post.year());

      // Substitui placeholders
      content = content.replace("${post_title}", post.title())
        .replace("${post_content}", post.content())
        .replace("${post_date}", postDate);

      // Calcula o caminho relativo para assets
      String basePath = "../../../";

      // Aplica o template base
      return applyBaseTemplate(template, content, post.title() + " - Kambas do Java",
          post.description(), false, basePath);
    }

    public static String renderAllPosts(List<DataSet.Post> posts) throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/all-posts.html"));

      // Agrupa posts por ano e mês
      Map<Integer, Map<Integer, List<DataSet.Post>>> postsByYearMonth = posts.stream()
          .collect(Collectors.groupingBy(p -> p.year(),
              TreeMap::new,
              Collectors.groupingBy(p -> p.month(), TreeMap::new, Collectors.toList())));

      StringBuilder postsHtml = new StringBuilder();

      for (Integer year : postsByYearMonth.keySet().stream().sorted(Comparator.reverseOrder())
          .collect(Collectors.toList())) {
        postsHtml.append("<div class='year-section'>")
            .append("<h2>").append(year).append("</h2>");

        Map<Integer, List<DataSet.Post>> postsByMonth = postsByYearMonth.get(year);
        for (Integer month : postsByMonth.keySet().stream().sorted(Comparator.reverseOrder())
            .collect(Collectors.toList())) {
          postsHtml.append("<div class='month-section'>")
              .append("<h3>").append(getMonthName(month)).append("</h3>")
              .append("<div class='posts-list'>");

          for (DataSet.Post post : postsByMonth.get(month)) {
            postsHtml.append("<div class='post-item' data-categories='java tutorial'>")
                .append("<div class='post-date'>")
                .append("<span class='post-day'>").append(String.format("%02d", post.month())).append("</span>")
                .append("<span class='post-month'>").append(getMonthAbbr(month)).append("</span>")
                .append("</div>")
                .append("<div class='post-content'>")
                .append("<h4><a href='posts/").append(year).append("/")
                .append(String.format("%02d", month)).append("/")
                .append(post.slug()).append(".html'>").append(post.title()).append("</a></h4>")
                .append("<p class='post-excerpt'>").append(post.description()).append("</p>")
                .append("<div class='post-meta'>")
                .append("<span class='post-category'>Java</span>")
                .append("<span>").append(post.description().length() > 100 ? "5 min read" : "2 min read")
                .append("</span>")
                .append("</div>")
                .append("</div>")
                .append("</div>");
          }

          postsHtml.append("</div></div>");
        }

        postsHtml.append("</div>");
      }

      // Substitui placeholders
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
      String[] months = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
          "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro" };
      if (month >= 1 && month <= 12) {
        return months[month - 1];
      }
      return "Mês inválido";
    }

    private static String getMonthAbbr(int month) {
      String[] months = { "JAN", "FEV", "MAR", "ABR", "MAI", "JUN",
          "JUL", "AGO", "SET", "OUT", "NOV", "DEZ" };
      return months[month - 1];
    }

    static String renderEvent(DataSet.Event event) throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/event.html"));

      // Substitui placeholders
      content = content.replace("${event_title}", event.title())
          .replace("${event_content}", event.content())
          .replace("${event_date}", event.date.format(DATE_FORMATTER))
          .replace("${event_location}", event.location())
          .replace("${event_type}", event.eventType());

      String basePath = "../";
      return applyBaseTemplate(template, content, event.title() + " - Kambas do Java",
          event.description(), false, basePath);
    }

    public static String renderAllEvents(List<DataSet.Event> events) throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/all-events.html"));

      // Separa Dataset.Eventos por status
      List<DataSet.Event> upcomingEvents = events.stream()
          .filter(e -> e.isUpcoming())
          .collect(Collectors.toList());

      List<DataSet.Event> pastEvents = events.stream()
          .filter(e -> !e.isUpcoming())
          .collect(Collectors.toList());

      StringBuilder eventsHtml = new StringBuilder();

      // Dataset.Eventos próximos
      if (!upcomingEvents.isEmpty()) {
        eventsHtml.append("<div class='events-section'>")
            .append("<h2>Próximos Dataset.Eventos</h2>")
            .append("<div class='events-list'>");

        for (DataSet.Event event : upcomingEvents) {
          eventsHtml.append(renderEventItem(event));
        }

        eventsHtml.append("</div></div>");
      }

      // Dataset.Eventos passados
      if (!pastEvents.isEmpty()) {
        eventsHtml.append("<div class='events-section'>")
            .append("<h2>Eventos Passados</h2>")
            .append("<div class='events-list'>");

        for (DataSet.Event event : pastEvents) {
          eventsHtml.append(renderEventItem(event));
        }

        eventsHtml.append("</div></div>");
      }

      content = content.replace("${all_events}", eventsHtml.toString())
          .replace("${total_events}", String.valueOf(events.size()))
          .replace("${upcoming_count}", String.valueOf(upcomingEvents.size()))
          .replace("${past_count}", String.valueOf(pastEvents.size()));

      return applyBaseTemplate(template, content, "Todos os Dataset.Eventos - Kambas do Java",
          "Calendário completo de Dataset.Eventos, meetups e workshops da comunidade Kambas do Java", true);
    }

    static String renderMember(DataSet.Member member) throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/member.html"));

      content = content.replace("${member_name}", member.name())
          .replace("${member_avatar_url}", member.avatarUrl())
          .replace("${member_content}", member.content());

      StringBuilder socialLinksHtml = new StringBuilder();
      for (Map.Entry<String, String> entry : member.socialLinks().entrySet()) {
        socialLinksHtml.append("<a href='" + entry.getValue() + "' target='_blank'>" + entry.getKey() + "</a>");
      }
      content = content.replace("", socialLinksHtml.toString());

      String basePath = "../";
      return applyBaseTemplate(template, content, member.name() + " - Kambas do Java",
          member.githubUsername(), false, basePath);
    }

    public static String renderAllMembers(List<DataSet.Member> members) throws Exception {
      String template = Files.readString(Paths.get("templates/base.html"));
      String content = Files.readString(Paths.get("templates/all-members.html"));

      StringBuilder membersHtml = new StringBuilder();
      for (DataSet.Member member : members) {
        membersHtml.append("<div class='member-card'>")
            .append("<a href='members/").append(member.githubUsername).append(".html'>")
            .append("<img src='").append(member.avatarUrl).append("' alt='").append(member.name).append("'>")
            .append("<h3>").append(member.name).append("</h3>")
            .append("</a>")
            .append("</div>");
      }

      content = content.replace("${all_members}", membersHtml.toString());

      return applyBaseTemplate(template, content, "Membros - Kambas do Java",
          "Conheça os membros da comunidade Kambas do Java", true);
    }

    private static String renderEventItem(DataSet.Event event) {
      return "<div class='event-item'>" +
          "<div class='event-date'>" +
          "<span class='event-day'>" + event.date().format(DAY_FORMATTER) + "</span>" +
          "<span class='event-month'>" + event.date().format(MONTH_FORMATTER) + "</span>" +
          "<span class='event-year'>" + event.date().getYear() + "</span>" +
          "</div>" +
          "<div class='event-content'>" +
          "<h3><a href='events/" + event.slug() + ".html'>" + event.title() + "</a></h3>" +
          "<p class='event-meta'>" +
          "<span class='event-location'>📍 " + event.location() + "</span>" +
          "<span class='event-type'>" + getEventTypeIcon(event.eventType()) + " " + event.eventType() + "</span>" +
          "</p>" +
          "<p class='event-description'>" + event.description() + "</p>" +
          "<a href='events/" + event.slug() + ".html' class='btn btn-outline'>Ver Detalhes</a>" +
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

  static class Processor {

    static List<DataSet.Post> processPosts(Path siteDir) throws IOException {
      Path postsDir = Paths.get("posts");

      List<DataSet.Post> posts = new ArrayList<>();

      // Processa posts
      if (Files.exists(postsDir)) {
        Files.walk(postsDir)
            .filter(p -> p.toString().endsWith(".md")) // Process only .md files
            .filter(p -> !p.toFile().getName().startsWith("_")) // Ignore files starting with _
            .forEach(mdFile -> {
              try {
                String md = Files.readString(mdFile);
                var metadata = Utils.Processor.readMetadata(md);  
                String description = metadata.getOrDefault("description", "");
                String html = Utils.MarkdownProcessor.markdownToHtml(md);
                String fileName = mdFile.getFileName().toString().replace(".md", "");
                int year = Integer.parseInt(mdFile.getParent().getParent().getFileName().toString());
                int month = Integer.parseInt(mdFile.getParent().getFileName().toString());
                String slug = fileName.toLowerCase()
                    .replace(" ", "-")
                    .replaceAll("[^a-z0-9-]", "");
                var title = metadata.getOrDefault("title", fileName);


                DataSet.Post post = new DataSet.Post(title, slug, html, year, month, description);
                posts.add(post);

                // Gera HTML do post
                String postHtml = Utils.TemplateEngine.renderPost(post);
                Path outDir = siteDir.resolve("posts/" + year + "/" + String.format("%02d", month));
                Files.createDirectories(outDir);
                Files.writeString(outDir.resolve(post.slug() + ".html"), postHtml);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
      }

      // Ordena posts do mais recente para o mais antigo
      posts.sort((p1, p2) -> {
        if (p1.year() != p2.year())
          return Integer.compare(p2.year(), p1.year());
        return Integer.compare(p2.month(), p1.month());
      });

      return posts;

    }

    static List<DataSet.Event> processEvents(Path siteDir) throws IOException {
      Path eventsDir = Paths.get("events");

      List<DataSet.Event> events = new ArrayList<>();

      // Processa Dataset.Eventos
      if (Files.exists(eventsDir)) {
        Files.walk(eventsDir)
            .filter(p -> p.toString().endsWith(".md")) // Process only .md files
            .filter(p -> !p.toFile().getName().startsWith("_")) // Ignore files starting with _
            .forEach(mdFile -> {
              try {
                String md = Files.readString(mdFile);
                String fileName = mdFile.getFileName().toString();
                DataSet.Event event = Utils.MarkdownProcessor.parseEventFromMarkdown(md, fileName);
                events.add(event);

                // Gera HTML do Dataset.Evento
                String eventHtml = Utils.TemplateEngine.renderEvent(event);
                Path outDir = siteDir.resolve("events");
                Files.createDirectories(outDir);
                Files.writeString(outDir.resolve(event.slug + ".html"), eventHtml);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
      }

      // Ordena Dataset.Eventos: próximos primeiro, depois passados
      events.sort((e1, e2) -> {
        if (e1.isUpcoming != e2.isUpcoming) {
          return Boolean.compare(e2.isUpcoming, e1.isUpcoming); // Upcoming first
        }
        return e2.date.compareTo(e1.date); // Most recent first
      });

      return events;
    }

    static List<DataSet.Member> processMembers(Path siteDir) throws IOException {
      Path membersDir = Paths.get("members");

      List<DataSet.Member> members = new ArrayList<>();

      // Processa membros
      if (Files.exists(membersDir)) {
        Files.walk(membersDir)
            .filter(p -> p.toString().endsWith(".md")) // Process only .md files
            .filter(p -> !p.toFile().getName().startsWith("_")) // Ignore files starting with _
            .forEach(mdFile -> {
              try {
                String md = Files.readString(mdFile);
                String githubUsername = mdFile.getFileName().toString().replace(".md", "");
                DataSet.Member member = Utils.MarkdownProcessor.parseMemberFromMarkdown(md, githubUsername);
                members.add(member);

                // Gera HTML do membro
                String memberHtml = Utils.TemplateEngine.renderMember(member);
                Path outDir = siteDir.resolve("members");
                Files.createDirectories(outDir);
                Files.writeString(outDir.resolve(member.githubUsername() + ".html"), memberHtml);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
      }

      return members;
    }

    static void clearSiteDir(Path siteDir) throws Exception {
      if (Files.exists(siteDir)) {
        Files.walk(siteDir)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
      }
      Files.createDirectories(siteDir);
    }

    static Map<String, String> readMetadata(String file) {
      Map<String, String> variables = new HashMap<>();
      try (BufferedReader reader = new BufferedReader(new StringReader(file))) {
        // Use parallel stream to process lines in parallel
        variables = reader.lines()
            .parallel()
            .filter(line -> line.startsWith("@"))
            .map(line -> line.substring(1).split(" ", 2))
            .collect(Collectors.toMap(
                parts -> parts[0].replace("@", "").trim(),
                parts -> parts.length > 1 ? parts[1].trim() : "",
                (v1, v2) -> v1 // merge function in case of duplicate keys
            ));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return variables;
    }

  }

  static class Render {
     static void render(Path siteDir, List<DataSet.Post> posts, List<DataSet.Event> events,
        List<DataSet.Member> members) throws Exception {
      Path assetsDir = Paths.get("assets");

      // Gera index
      String indexHtml = Utils.TemplateEngine.renderIndex(posts, events, members);
      Files.writeString(siteDir.resolve("index.html"), indexHtml, StandardCharsets.UTF_8);
      println("Index gerado.");

      // Gera página com todos os posts
      String allPostsHtml = Utils.TemplateEngine.renderAllPosts(posts);
      Files.writeString(siteDir.resolve("all-posts.html"), allPostsHtml, StandardCharsets.UTF_8);
      println("Página de todos os posts gerada.");

      // Gera página com todos os Dataset.Eventos
      String allEventsHtml = Utils.TemplateEngine.renderAllEvents(events);
      Files.writeString(siteDir.resolve("all-events.html"), allEventsHtml, StandardCharsets.UTF_8);
      println("Página de todos os eventos gerada.");

      // Gera página com todos os membros
      String allMembersHtml = Utils.TemplateEngine.renderAllMembers(members);
      Files.writeString(siteDir.resolve("all-members.html"), allMembersHtml, StandardCharsets.UTF_8);
      println("Página de todos os membros gerada.");

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
        println("Assets copiados.");
      }
    }
  }
}
