package com.example.Messenger.Controller;

import com.example.Messenger.Entity.Category;
import com.example.Messenger.Entity.Image;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.ImageRequest;
import com.example.Messenger.Record.ProductRequest;
import com.example.Messenger.Repository.CategoryRepository;
import com.example.Messenger.Service.Implement.ProductServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductServiceImp productService;
    private final CategoryRepository categoryRepository;
    @Autowired
    public ProductController(ProductServiceImp productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }
    @GetMapping("/category")
    public ResponseEntity<List<Category>> getAllCategory(){
        var list = categoryRepository.findAll();
        return new ResponseEntity<List<Category>>(list, HttpStatusCode.valueOf(200));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestBody() ProductRequest req
    ) throws IOException {
        return ResponseEntity.ok(productService.createProduct(req));
    }
    @PostMapping("/{id}/images")
    public ResponseEntity<Image> addImage(
            @PathVariable String id,
            @RequestBody ImageRequest req
    ) {
        Image image = productService.addImageToProduct(id, req);
        return ResponseEntity.ok(image);
    }
    @PostMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @RequestPart("product") Product product
    ) throws IOException {
        return ResponseEntity.ok(productService.updateProduct(id, product, null));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Product> productPage = productService.getAllProducts(page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productPage.getContent());
        response.put("currentPage", productPage.getNumber());
        response.put("totalItems", productPage.getTotalElements());
        response.put("totalPages", productPage.getTotalPages());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> uploadImages(
            @PathVariable String id,
            @RequestPart("images") List<MultipartFile> images
    ) throws IOException {
        Product updatedProduct = productService.addImagesToProduct(id, images);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String featureName,
            @RequestParam(required = false) String featureValue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Product> products = productService.searchProducts(
                categoryId, minPrice, maxPrice, featureName, featureValue, page, size
        );
        return ResponseEntity.ok(products);
    }
}