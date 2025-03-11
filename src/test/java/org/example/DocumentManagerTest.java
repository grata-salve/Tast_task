package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DocumentManagerTest {

  private DocumentManager manager;

  @BeforeEach
  void setUp() {
    manager = new DocumentManager();
  }

  private DocumentManager.Document createDocument(String title, String content, String authorId,
      String authorName, Instant created, String id) {
    DocumentManager.Document.DocumentBuilder builder = DocumentManager.Document.builder()
        .title(title)
        .content(content)
        .author(DocumentManager.Author.builder()
            .id(authorId)
            .name(authorName)
            .build());
    if (created != null) {
      builder.created(created);
    }
    if (id != null) {
      builder.id(id);
    }
    return builder.build();
  }

  // Overload for documents without created timestamp and id.
  private DocumentManager.Document createDocument(String title, String content, String authorId, String authorName) {
    return createDocument(title, content, authorId, authorName, null, null);
  }

  // Overload for documents with created timestamp but without id.
  private DocumentManager.Document createDocument(String title, String content, String authorId, String authorName, Instant created) {
    return createDocument(title, content, authorId, authorName, created, null);
  }

  @Test
  void testSaveNewDocument() {
    // Create a document without id and created timestamp
    DocumentManager.Document doc = createDocument("Test Title", "Test Content", "author1", "John Doe");

    DocumentManager.Document saved = manager.save(doc);
    assertNotNull(saved.getId(), "ID should be generated");
    assertNotNull(saved.getCreated(), "Created timestamp should be set");
    assertEquals("Test Title", saved.getTitle());
  }

  @Test
  void testUpdateDocument() {
    Instant created = Instant.now();
    String id = "doc-123";
    DocumentManager.Document doc = createDocument("Original Title", "Original Content", "author1", "John Doe", created, id);
    DocumentManager.Document saved = manager.save(doc);

    // Update the document, attempting to change the created timestamp (it should remain unchanged)
    DocumentManager.Document updatedDoc = createDocument("Updated Title", "Updated Content", "author2", "Jane Doe", Instant.now(), id);
    DocumentManager.Document updated = manager.save(updatedDoc);
    assertEquals(id, updated.getId());
    assertEquals("Updated Title", updated.getTitle());
    assertEquals("Updated Content", updated.getContent());
    assertEquals("author2", updated.getAuthor().getId());
    assertEquals(created, updated.getCreated(), "Created timestamp should remain unchanged");
  }

  @Test
  void testFindById() {
    DocumentManager.Document doc = createDocument("Test Title", "Test Content", "author1", "John Doe");
    DocumentManager.Document saved = manager.save(doc);
    Optional<DocumentManager.Document> found = manager.findById(saved.getId());
    assertTrue(found.isPresent());
    assertEquals(saved.getId(), found.get().getId());
  }

  @Test
  void testSearchByTitlePrefix() {
    DocumentManager.Document doc1 = createDocument("Hello World", "Content 1", "author1", "John Doe", Instant.now());
    DocumentManager.Document doc2 = createDocument("Hi there", "Content 2", "author2", "Jane Doe", Instant.now());

    manager.save(doc1);
    manager.save(doc2);

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
        .titlePrefixes(List.of("Hello"))
        .build();

    List<DocumentManager.Document> results = manager.search(request);
    assertEquals(1, results.size());
    assertEquals("Hello World", results.get(0).getTitle());
  }

  @Test
  void testSearchByContentContains() {
    DocumentManager.Document doc1 = createDocument("Doc1", "This is a sample document", "author1", "John Doe", Instant.now());
    DocumentManager.Document doc2 = createDocument("Doc2", "Another document example", "author2", "Jane Doe", Instant.now());

    manager.save(doc1);
    manager.save(doc2);

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
        .containsContents(List.of("sample"))
        .build();

    List<DocumentManager.Document> results = manager.search(request);
    assertEquals(1, results.size());
    assertEquals("Doc1", results.get(0).getTitle());
  }

  @Test
  void testSearchByAuthorId() {
    DocumentManager.Document doc1 = createDocument("Doc1", "Content", "author1", "John Doe", Instant.now());
    DocumentManager.Document doc2 = createDocument("Doc2", "Content", "author2", "Jane Doe", Instant.now());

    manager.save(doc1);
    manager.save(doc2);

    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
        .authorIds(List.of("author2"))
        .build();

    List<DocumentManager.Document> results = manager.search(request);
    assertEquals(1, results.size());
    assertEquals("Doc2", results.get(0).getTitle());
  }

  @Test
  void testSearchByCreatedRange() {
    Instant now = Instant.now();
    Instant past = now.minusSeconds(3600); // one hour ago
    Instant future = now.plusSeconds(3600); // one hour ahead

    DocumentManager.Document doc1 = createDocument("Doc1", "Content", "author1", "John Doe", past);
    DocumentManager.Document doc2 = createDocument("Doc2", "Content", "author2", "Jane Doe", now);
    DocumentManager.Document doc3 = createDocument("Doc3", "Content", "author3", "Jim Doe", future);

    manager.save(doc1);
    manager.save(doc2);
    manager.save(doc3);

    // Search for documents created between past and now (inclusive)
    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
        .createdFrom(past)
        .createdTo(now)
        .build();

    List<DocumentManager.Document> results = manager.search(request);
    assertEquals(2, results.size());
  }

  @Test
  void testCombinedSearchCriteria() {
    Instant now = Instant.now();

    DocumentManager.Document doc1 = createDocument("Hello World", "This is a sample document with keyword", "author1", "John Doe", now);
    DocumentManager.Document doc2 = createDocument("Hello Universe", "Another document without the keyword", "author2", "Jane Doe", now);

    manager.save(doc1);
    manager.save(doc2);

    // Search by multiple criteria at the same time
    DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
        .titlePrefixes(List.of("Hello"))
        .containsContents(List.of("keyword"))
        .authorIds(List.of("author1"))
        .createdFrom(now.minusSeconds(10))
        .createdTo(now.plusSeconds(10))
        .build();

    List<DocumentManager.Document> results = manager.search(request);
    assertEquals(1, results.size());
    assertEquals("Hello World", results.get(0).getTitle());
  }
}
