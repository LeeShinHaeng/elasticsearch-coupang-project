package com.example.coupangapiserver.product;

import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.example.coupangapiserver.product.domain.Product;
import com.example.coupangapiserver.product.domain.ProductDocument;
import com.example.coupangapiserver.product.domain.ProductDocumentRepository;
import com.example.coupangapiserver.product.dto.CreateProductRequestDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductDocumentRepository productDocumentRepository;
	private final ElasticsearchOperations elasticsearchOperations;

	public ProductService(ProductRepository productRepository,
						  ProductDocumentRepository productDocumentRepository,
						  ElasticsearchOperations elasticsearchOperations) {
		this.productRepository = productRepository;
		this.productDocumentRepository = productDocumentRepository;
		this.elasticsearchOperations = elasticsearchOperations;
	}

	public List<Product> getProducts(int page, int size) {
		Pageable pageable = PageRequest.of(page - 1, size);
		return productRepository.findAll(pageable).getContent();
	}

	public Product createProduct(CreateProductRequestDto createProductRequestDto) {
		Product product = new Product(
			createProductRequestDto.getName(),
			createProductRequestDto.getDescription(),
			createProductRequestDto.getPrice(),
			createProductRequestDto.getRating(),
			createProductRequestDto.getCategory()
		);
		Product saved = productRepository.save(product);

		ProductDocument productDocument = new ProductDocument(
			saved.getId().toString(),
			saved.getName(),
			saved.getDescription(),
			saved.getPrice(),
			saved.getRating(),
			saved.getCategory()
		);
		productDocumentRepository.save(productDocument);

		return saved;
	}

	public void deleteProduct(Long id) {
		productRepository.deleteById(id);
		productDocumentRepository.deleteById(id.toString());
	}

	public List<String> getSuggestions(String query) {
		Query multimatchQuery = MultiMatchQuery.of(
			m -> m.query(query)
				.type(TextQueryType.BoolPrefix)
				.fields(
					"name.auto_complete",
					"name.auto_complete._2gram",
					"name.auto_complete._3gram"
				)
		)._toQuery();

		NativeQuery nativeQuery = NativeQuery.builder()
			.withQuery(multimatchQuery)
			.withPageable(PageRequest.of(0, 5))
			.build();

		SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(nativeQuery, ProductDocument.class);

		return searchHits.getSearchHits().stream()
			.map(hit -> hit.getContent().getName())
			.toList();
	}
}
