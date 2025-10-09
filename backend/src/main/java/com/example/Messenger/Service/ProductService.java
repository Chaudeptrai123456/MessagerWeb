package com.example.Messenger.Service;

import com.example.Messenger.Entity.Image;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.ImageRequest;
import com.example.Messenger.Record.ProductRequest;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface ProductService {
    public Image addImageToProduct(String productId, ImageRequest req) ;
    Product createProduct(ProductRequest req) throws IOException;
    Product updateProduct(String id, Product product, List<MultipartFile> images) throws IOException;
    Product getProductById(String id);
    void deleteProduct(String id);
    public Page<Product> getAllProducts(int page, int size) ;
    public Product addImagesToProduct(String productId, List<MultipartFile> files) throws IOException;
    public Page<Product> searchProducts(
            String categoryId,
            Double minPrice,
            Double maxPrice,
            String featureName,
            String featureValue, int page, int size);
}