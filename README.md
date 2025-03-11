# Document Manager

This project implements a simple in-memory document management system. It supports basic operations such as saving (with upsert functionality), searching documents by various criteria, and retrieving documents by their ID.

## Features

- **Save and Update Documents:**  
  Documents can be created or updated using the `save` method. When updating, the original creation timestamp is preserved.

- **Search Functionality:**  
  You can search documents by:
  - Title prefixes.
  - Content substrings.
  - Author IDs.
  - Creation date ranges.

- **Retrieve by ID:**  
  The `findById` method allows you to fetch a document by its unique identifier.

- **In-Memory Storage:**  
  The implementation uses an in-memory collection to store documents for simplicity.

- **Unit Tested:**  
  The functionality is covered by JUnit 5 tests.

## Project Structure

- **DocumentManager.java:**  
  Contains the main implementation of the document management functionality.

- **DocumentManagerTest.java:**  
  Contains unit tests for the `DocumentManager` class.

## Getting Started

### Prerequisites

- Java 8 or higher.
- Maven (for building the project and managing dependencies).

### Build

```bash
mvn clean install
```

### Run Tests

```bash
mvn test
```

## Usage

Here is a simple example of how to use the `DocumentManager`:

```java
DocumentManager manager = new DocumentManager();

// Creating and saving a new document
DocumentManager.Document doc = DocumentManager.Document.builder()
    .title("Example Document")
    .content("This is the content of the document.")
    .author(DocumentManager.Author.builder()
        .id("author1")
        .name("John Doe")
        .build())
    .build();

DocumentManager.Document savedDoc = manager.save(doc);

// Searching for documents with titles starting with "Example"
DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
    .titlePrefixes(List.of("Example"))
    .build();
List<DocumentManager.Document> results = manager.search(request);
```

