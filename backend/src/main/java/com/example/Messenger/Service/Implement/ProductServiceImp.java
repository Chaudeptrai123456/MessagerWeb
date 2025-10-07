package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.Category;
import com.example.Messenger.Entity.Feature;
import com.example.Messenger.Entity.Image;
import com.example.Messenger.Entity.Product;
import com.example.Messenger.Record.ImageRequest;
import com.example.Messenger.Record.ProductRequest;
import com.example.Messenger.Repository.CategoryRepository;
import com.example.Messenger.Repository.ImageRepository;
import com.example.Messenger.Repository.ProductRepository;
import com.example.Messenger.Service.EmbeddingService;
import com.example.Messenger.Service.ProductService;
import com.example.Messenger.Utils.ProductIdUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProductServiceImp implements ProductService {

    private ProductIdUtil productIdUtil;

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    @Autowired
    public ProductServiceImp(ProductRepository productRepository,
                             EmbeddingService embeddingService,
                             CategoryRepository categoryRepository, ImageRepository imageRepository) {
        this.productRepository = productRepository;
        this.embeddingService = embeddingService;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public Product createProduct(ProductRequest req) {
        // 1. Lấy category (nếu ko tìm thấy -> lỗi)
        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 2. Tạo product cơ bản
        Product product = new Product();
        product.setId(generateId(req.name()));
        product.setName(req.name());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCreatedAt(LocalDate.now());
        product.setCategory(category);
        product.setQuantity(req.quantity());
        // 3. Map images
        List<Image> images = Optional.ofNullable(req.images())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(bytes -> {
                    Image img = new Image();
                    img.setData(bytes.getBytes());
                    img.setProduct(product);
                    return img;
                })
                .collect(Collectors.toList());

        // 4. Map features
        List<Feature> features = Optional.ofNullable(req.features())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(fr -> {
                    Feature f = new Feature();
                    f.setName(fr.getClass().getName());   // 👈 sửa lại cho đúng
                    f.setValue(fr.getClass().getName()); // 👈 không dùng getClass().getName()
                    f.setProduct(product);
                    return f;
                })
                .collect(Collectors.toList());

        product.setImages(new HashSet<>(images));
        product.setFeatures(new HashSet<>(features));

        // 5. ✅ Lưu xuống DB trước
        Product saved = productRepository.save(product);

        // 6. 🔄 Tạo embedding sau (nếu lỗi cũng không sao)
        CompletableFuture.runAsync(() -> {
            try {
                float[] vector = embeddingService.generateEmbedding(saved);
                saved.setEmbedding(Arrays.toString(vector));
                productRepository.save(saved);
                System.out.println("✅ Embedding created for product: " + saved.getName());
            } catch (Exception e) {
                System.err.println("⚠️ Embedding failed for " + saved.getName() + ": " + e.getMessage());
            }
        });

        return saved;
    }


    private String generateId(String name) {
        // Làm sạch tên: bỏ khoảng trắng, viết thường
        String slug = (name == null ? "item" : name.replaceAll("\\s+", "_").toLowerCase());

        // Thêm ngày tháng
        String datePart = LocalDate.now().toString();

        // Sinh phần hash ngắn từ timestamp + tên (đảm bảo không trùng)
        String randomPart = Integer.toHexString((name + System.nanoTime()).hashCode());

        // Gộp lại thành ID hoàn chỉnh
        return slug + "_" + datePart + "_" + randomPart;
    }


    @Override
    public Product updateProduct(String id, Product newProduct, List<MultipartFile> images) throws IOException {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        existing.setName(newProduct.getName());
        existing.setDescription(newProduct.getDescription());
        existing.setPrice(newProduct.getPrice());
        existing.setEmbedding(newProduct.getEmbedding());
        existing.setCategory(newProduct.getCategory());
        existing.setQuantity(newProduct.getQuantity());
        // reset features
        existing.getFeatures().clear();
        if (newProduct.getFeatures() != null) {
            for (Feature f : newProduct.getFeatures()) {
                f.setProduct(existing);
                existing.getFeatures().add(f);
            }
        }

        // reset images
        existing.getImages().clear();
        if (images != null && !images.isEmpty()) {
            existing.setImages(
                    images.stream().map(file -> {
                        try {
                            return new Image(file.getName(),file.getContentType(),existing, file.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toSet())
            );
        }
        System.out.println(existing.getQuantity());
        return productRepository.save(existing);
    }
    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ Gỡ liên kết khỏi Category
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            category.getProducts().remove(product);
            product.setCategory(null);
        }

        // ✅ Xóa Features và Images (vì có orphanRemoval = true, JPA sẽ tự lo)
        product.getFeatures().clear();
        product.getImages().clear();

        // ✅ Xóa chính product
        productRepository.delete(product);
    }

    @Override
    public Image addImageToProduct(String productId, ImageRequest req) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Giải mã base64 thành byte[]
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(req.base64Data());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid base64 image");
        }
//
//        Image image = Image.builder()
//                .filename(req.filename())
//                .contentType(req.contentType())
//                .data(bytes)
//                .product(product)
//                .build();
        Image image = new Image();
        image.setFilename(req.filename());
        image.setContentType(req.contentType());
        image.setData(bytes);
        image.setProduct(product);
        imageRepository.save(image);
        product.getImages().add(image);
        productRepository.save(product);

        return image;
    }
    @Override
    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findAll(pageable);
    }
}
