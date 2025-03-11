package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    // In-memory storage for documents
    private final Map<String, Document> documents = new ConcurrentHashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        // Generate id if necessary
        String id = document.getId();
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        Document existing = documents.get(id);
        if (existing != null) {
            // Upsert: update fields except [created] which should remain unchanged.
            Document updated = Document.builder()
                .id(id)
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(existing.getCreated())
                .build();
            documents.put(id, updated);
            return updated;
        } else {
            // For new documents, if created is null, use current time.
            Document toSave = Document.builder()
                .id(id)
                .title(document.getTitle())
                .content(document.getContent())
                .author(document.getAuthor())
                .created(document.getCreated() != null ? document.getCreated() : Instant.now())
                .build();
            documents.put(id, toSave);
            return toSave;
        }
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documents.values().stream()
            .filter(doc -> {
                // If titlePrefixes is provided, the title should start with at least one prefix.
                if (request.getTitlePrefixes() != null && !request.getTitlePrefixes().isEmpty()) {
                    boolean match = false;
                    for (String prefix : request.getTitlePrefixes()) {
                        if (doc.getTitle() != null && doc.getTitle().startsWith(prefix)) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        return false;
                    }
                }
                // Check that the document content contains all specified substrings.
                if (request.getContainsContents() != null && !request.getContainsContents().isEmpty()) {
                    for (String keyword : request.getContainsContents()) {
                        if (doc.getContent() == null || !doc.getContent().contains(keyword)) {
                            return false;
                        }
                    }
                }
                // If authorIds is provided, document's author id should be one of them.
                if (request.getAuthorIds() != null && !request.getAuthorIds().isEmpty()) {
                    if (doc.getAuthor() == null ||
                        doc.getAuthor().getId() == null ||
                        !request.getAuthorIds().contains(doc.getAuthor().getId())) {
                        return false;
                    }
                }
                // Filter based on created time range.
                if (request.getCreatedFrom() != null) {
                    if (doc.getCreated() == null || doc.getCreated().isBefore(request.getCreatedFrom())) {
                        return false;
                    }
                }
                if (request.getCreatedTo() != null) {
                    if (doc.getCreated() == null || doc.getCreated().isAfter(request.getCreatedTo())) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
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
