# Kambas do Java - Static Site Generator

This is the official website for the Kambas do Java community, a community of Java developers in Luanda, Angola.

## Features

*   **Static Site Generation:** Generates a static website from Markdown files.
*   **Content Types:** Supports both blog posts and events.
*   **Templating:** Uses a simple and lightweight template engine.
*   **Markdown Support:** Content for posts and events is written in Markdown.
*   **Responsive Design:** The website is designed to be responsive and work on different devices.
*   **Zero Dependencies:** The project has no external dependencies and uses only standard Java libraries.

## Technologies

*   **Java:** The core logic is written in Java.
*   **Markdown:** Used for creating content for posts and events.
*   **HTML/CSS:** The website is built using standard web technologies.
*   **GitHub Actions:** Used for continuous integration and deployment.
*   **GitHub Pages:** The website is hosted on GitHub Pages.

## How to Run Locally

To run the website locally, you need to have Java installed.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/kambas-do-java/website.git
    cd website
    ```

2.  **Run the generator:**
    ```bash
    java --enable-preview --source 24 main.java
    ```
    This will generate the website in the `site` directory.

3.  **View the website:**
    Open the `site/index.html` file in your browser to view the website.

## How to Add Content

### Posts

To add a new post, create a new Markdown file in the `posts/YYYY/MM` directory, where `YYYY` is the year and `MM` is the month.

### Events

To add a new event, create a new Markdown file in the `events/YYYY/MM` directory, where `YYYY` is the year and `MM` is the month.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
