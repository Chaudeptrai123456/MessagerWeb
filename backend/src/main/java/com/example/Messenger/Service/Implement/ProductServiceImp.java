package com.example.Messenger.Service.Implement;

import com.example.Messenger.Entity.*;
import com.example.Messenger.Record.ImageRequest;
import com.example.Messenger.Record.ProductRequest;
import com.example.Messenger.Repository.CategoryRepository;
import com.example.Messenger.Repository.ImageRepository;
import com.example.Messenger.Repository.ProductRepository;
import com.example.Messenger.Service.EmbeddingService;
import com.example.Messenger.Service.ProductService;
import com.example.Messenger.Service.RedisService;
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
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImp implements ProductService {
    private static final Duration PRODUCT_TTL = Duration.ofHours(1);
    private static final Duration PRODUCT_PAGE_TTL = Duration.ofMinutes(5);
    private ProductIdUtil productIdUtil;
    private final RedisService redisService;

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    @Autowired
    public ProductServiceImp(RedisService redisService, ProductRepository productRepository,
                             EmbeddingService embeddingService,
                             CategoryRepository categoryRepository, ImageRepository imageRepository) {
        this.redisService = redisService;
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

//        // 6. 🔄 Tạo embedding sau (nếu lỗi cũng không sao)
//        CompletableFuture.runAsync(() -> {
//            try {
//                List<Feature> featureList = features;
//                ProductEmbedding embedding = new ProductEmbedding(product.getId(), product.getName(),product.getDescription(),category,features);
//                float[] vector = embeddingService.generateEmbedding(embedding);
//                saved.setEmbedding(Arrays.toString(vector));
//                productRepository.save(saved);
//                System.out.println("✅ Embedding created for product: " + saved.getName());
//            } catch (Exception e) {
//                System.err.println("⚠️ Embedding failed for " + saved.getName() + ": " + e.getMessage());
//            }
//        });

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

        existing.setName(newProduct.getName() == null ? existing.getName():newProduct.getName());
        existing.setDescription(newProduct.getDescription() == null ? existing.getDescription(): newProduct.getDescription());
        existing.setPrice(newProduct.getPrice() == null ? existing.getPrice() : existing.getPrice()+ newProduct.getPrice());
//        existing.setEmbedding(newProduct.getEmbedding() == null ? existing.ge);
        existing.setCategory(newProduct.getCategory() == null ? existing.getCategory(): newProduct.getCategory());
        existing.setQuantity(newProduct.getQuantity() == null ? existing.getQuantity(): existing.getQuantity()+ newProduct.getQuantity());
        // reset features
//        existing.getFeatures().clear();
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
    public Page<Product> getAllProducts(int page, int size) {
        String cacheKey = "product:page:" + page + ":" + size;
        // Cache hit
        PageWrapper cachedPage = redisService.get(cacheKey, PageWrapper.class);
        if (cachedPage != null) {
            return cachedPage.toPage();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products = productRepository.findAll(pageable);

        // Cache miss -> save to Redis
        redisService.save(cacheKey, new PageWrapper(products), PRODUCT_PAGE_TTL);
        return products;
    }

    @Override
    public Product getProductById(String id) {
        String cacheKey = "product:" + id;
        Product cached = redisService.get(cacheKey, Product.class);
        if (cached != null) {
            return cached;
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        redisService.save(cacheKey, product, PRODUCT_TTL);
        return product;
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
    public Product addImagesToProduct(String productId, List<MultipartFile> files) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        for (MultipartFile file : files) {
            Image image = new Image();
            image.setProduct(product);
            image.setContentType(file.getContentType());
            image.setFilename(file.getOriginalFilename());
            image.setData(file.getBytes());
            product.getImages().add(image);
            imageRepository.save(image);
        }
        return productRepository.save(product);
    }
    @Override
    public Page<Product> searchProducts(
            String categoryId,
            Double minPrice,
            Double maxPrice,
            String featureName,
            String featureValue, int page, int size) {
        String cacheKey = String.format(
                "search:%s:%s:%s:%s:%s:%d:%d",
                categoryId, minPrice, maxPrice, featureName, featureValue, page, size
        );

        PageWrapper<Product> cached = redisService.getList(cacheKey, PageWrapper.class);
        if (cached != null) {
            System.out.println("✅ Cache hit for key: " + cacheKey);
            return cached.toPage();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> result = productRepository.searchProducts(
                categoryId, minPrice, maxPrice, featureName, featureValue, pageable
        );

        redisService.saveList(cacheKey, new PageWrapper<>(result));
        return result;
    }
}
