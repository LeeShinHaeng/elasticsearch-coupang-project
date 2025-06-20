package com.example.coupangapiserver.product;

import com.example.coupangapiserver.product.domain.Product;
import com.example.coupangapiserver.product.domain.ProductDocument;
import com.example.coupangapiserver.product.domain.ProductDocumentRepository;
import com.example.coupangapiserver.product.dto.CreateProductRequestDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductDocumentRepository productDocumentRepository;

	public ProductService(ProductRepository productRepository,
						  ProductDocumentRepository productDocumentRepository) {
		this.productRepository = productRepository;
		this.productDocumentRepository = productDocumentRepository;
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
}
