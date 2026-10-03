# Research Report

## Spring WebClient for OpenAI API

### Summary of Work

<!--One paragraph summary of the research being performed-->

How to use Springboot's WebClient, and specifically how to use it to for OpenAI API.
The intent of this is to get smart suggestions for order and strategy of completing assignments. Includes REST controller and DTO for communicaitng between backend fronted and OpenAI.

### Motivation

<!--Explain why you felt the need to perform this research-->

I have not used Springboot before this class and have therefore never used WebClient.
Making API calls to OpenAI is necessary for the implemenation of our project to have smart suggestions

### Time Spent

<!--Explain how your time was spent-->

I spent about 1.5 hour reading the articles and querying ChatGPT to give me articles, and about 1.5 hour deriving the templates below.

### Results

<!--Explain what you learned/produced/etc. This section should explain the important things you learned so that it can serve as an easy reference for yourself and others who could benefit from reviewing this topic. Include your sources as footnotes. Make sure you include the footnotes where appropriate e.g [^1]-->

This will consist of templates for which we can follow to incorporate WebClient for OpenAI API

Build Dependency (Gradle-Groovy) : implementation 'org.springframework.boot:spring-boot-starter-webflux' : in build.gradle

API Key : openai.api.key=${OPENAI_API_KEY}
export OPENAI_API_KEY=your_real_key_here

Config (Bean) :
@Configuration
public class WebClientConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public WebClient openAiWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

}

Controller (REST) :
@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @PostMapping
    public ResponseEntity<SuggestionResponse> getSuggestion(
            @RequestBody SuggestionRequest request) {

        String recommendation =
                suggestionService.generateSuggestion(request);

        return ResponseEntity.ok(
                new SuggestionResponse(recommendation));
    }

}

Service :
@Service
public class OpenAIClient {

    private final WebClient webClient;

    public OpenAIClient(WebClient openAiWebClient) {
        this.webClient = openAiWebClient;
    }

    public String getSuggestion(String prompt) {

        OpenAIRequest requestBody =
                new OpenAIRequest("gpt-4.1-mini", prompt);

        OpenAIResponse response = webClient.post()
                .uri("/responses")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .block();

        if (response == null ||
            response.getOutput() == null ||
            response.getOutput().isEmpty()) {
            return "No suggestion generated.";
        }

        return response.getOutput()
                .get(0)
                .getContent()
                .get(0)
                .getText();
    }

}
@Service
public class SuggestionService {

    private final OpenAIClient openAIClient;

    public SuggestionService(OpenAIClient openAIClient) {
        this.openAIClient = openAIClient;
    }

    public String generateSuggestion(SuggestionRequest request) {

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("<insert prompt>");

        for (Assignment a : request.getAssignments()) {
            promptBuilder.append("<insert info>")
                    .append("<insert info>\n");
        }

        return openAIClient.getSuggestion(promptBuilder.toString());
    }

}

DTO :
//assignment info
public class Assignment {

    <The assignment info we receive>

}
// what is sent from frontend
public class SuggestionRequest {

    private List<Assignment> assignments;

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

}
// request
public class OpenAIRequest {

    private String model;
    private String input;

    public OpenAIRequest(String model, String input) {
        this.model = model;
        this.input = input;
    }

    public String getModel() { return model; }
    public String getInput() { return input; }

}
//open ai response
import java.util.List;

public class OpenAIResponse {

    private List<Output> output;

    public List<Output> getOutput() {
        return output;
    }

    public void setOutput(List<Output> output) {
        this.output = output;
    }

    public static class Output {
        private List<Content> content;

        public List<Content> getContent() {
            return content;
        }

        public void setContent(List<Content> content) {
            this.content = content;
        }
    }

    public static class Content {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

}
// sent to frontend
public class SuggestionResponse {

    private String recommendation;

    public SuggestionResponse(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getRecommendation() {
        return recommendation;
    }

}

### Sources

<!--list your sources and link them to a footnote with the source url-->

- Spring Framework - WebClient[^1]
- SpringBoot[^2]
- OpenAI API Reference[^3]

[^1]: https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html

[^2]: https://docs.spring.io/spring-boot/docs/current/reference/html/

[^3]: https://platform.openai.com/docs/api-reference
