package stud;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {
    private final Map<String, Document> documentStore = new HashMap<>();
    private final Map<String, Author> authorStore = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */

    // Save author to  DB
    public Author save(Author author) {

        if (author.getId() == null) {
            String uniqueID = UUID.randomUUID().toString();
            author.setId(uniqueID);
        }

        author.setName(author.getName());

        authorStore.put(author.getId(), author);
        return author;
    }

    // Save Document to DB
    public Document save(Document document) {
        Author author = findAuthorById(document.getAuthor().getId())
                .orElseThrow(() ->
                        new RuntimeException("Can't find author by id "
                                + document.getAuthor().getId()));

        if (document.getId() == null) {
            String uniqueID = UUID.randomUUID().toString();
            document.setId(uniqueID);
        }

        document.setTitle(document.getTitle());
        document.setContent(document.getContent());
        document.setCreated(Instant.now());
        document.setAuthor(author);

        documentStore.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documentStore.values().stream()
                .filter(doc -> matches(doc, request))
                .collect(Collectors.toList());
    }

    private boolean matches(Document doc, SearchRequest request) {
        boolean matches = true;

        if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
            matches &= request.getTitlePrefixes().stream().anyMatch(doc.getTitle()::startsWith);
        }
        if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
            matches &= request.getContainsContents().stream().anyMatch(doc.getContent()::contains);
        }
        if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
            matches &= request.getAuthorIds().contains(doc.getAuthor().getId());
        }
        if (request.getCreatedFrom() != null) {
            matches &= !doc.getCreated().isBefore(request.getCreatedFrom());
        }
        if (request.getCreatedTo() != null) {
            matches &= !doc.getCreated().isAfter(request.getCreatedTo());
        }

        return matches;
    }


    /*public Specification<Document> build(SearchRequest searchRequest) {
        Specification<Document> spec = Specification.where(null);

        if (searchRequest.getTitlePrefixes() != null && !searchRequest.getTitlePrefixes().isEmpty()) {
            spec = spec.and(documentSpecificationProviderManager.getSpecificationProvider("titlePrefixes")
                    .getSpecification(searchRequest.getTitlePrefixes()));
        }

        if (searchRequest.getContainsContents() != null && !searchRequest.getContainsContents().isEmpty()) {
            spec = spec.and(documentSpecificationProviderManager.getSpecificationProvider("containsContents")
                    .getSpecification(searchRequest.getContainsContents()));
        }

        if (searchRequest.getAuthorIds() != null && !searchRequest.getAuthorIds().isEmpty()) {
            spec = spec.and(documentSpecificationProviderManager.getSpecificationProvider("authorIds")
                    .getSpecification(searchRequest.getAuthorIds()));
        }

        if (searchRequest.getCreatedFrom() != null) {
            spec = spec.and(documentSpecificationProviderManager.getSpecificationProvider("createdFrom")
                    .getSpecification(searchRequest.getCreatedFrom()));
        }

        if (searchRequest.getCreatedTo() != null) {
            spec = spec.and(documentSpecificationProviderManager.getSpecificationProvider("createdTo")
                    .getSpecification(searchRequest.getCreatedTo()));
        }

        return spec;
    }*/

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */

    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documentStore.get(id));
    }

    public Optional<Author> findAuthorById(String id) {
        return Optional.ofNullable(authorStore.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
