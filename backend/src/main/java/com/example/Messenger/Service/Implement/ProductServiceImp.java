package com.example.Messenger.Service.Implement;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.Messenger.Entity.*;
import com.example.Messenger.Record.DiscountRequest;
import com.example.Messenger.Record.ImageRequest;
import com.example.Messenger.Record.ProductRequest;
import com.example.Messenger.Repository.CategoryRepository;
import com.example.Messenger.Repository.DiscountRepository;
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
    private final DiscountRepository discountRepository;

    private final ProductRepository productRepository;
    private final EmbeddingService embeddingService;
    private final CategoryRepository categoryRepository;
    private final ImageRepository imageRepository;

    private final Cloudinary cloudinary;

    @Autowired
    public ProductServiceImp(RedisService redisService, DiscountRepository discountRepository, ProductRepository productRepository,
                             EmbeddingService embeddingService,
                             CategoryRepository categoryRepository, ImageRepository imageRepository, Cloudinary cloudinary) {
        this.redisService = redisService;
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
        this.embeddingService = embeddingService;
        this.categoryRepository = categoryRepository;
        this.imageRepository = imageRepository;
        this.cloudinary = cloudinary;
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
//        List<Image> images = Optional.ofNullable(req.images())
//                .orElse(Collections.emptyList())
//                .stream()
//                .filter(Objects::nonNull)
//                .map(bytes -> {
//                    Image img = new Image();
//                    img.setData(bytes.getBytes());
//                    img.setProduct(product);
//                    return img;
//                })
//                .collect(Collectors.toList());

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

//        product.setImages(new HashSet<>(images));
        product.setFeatures(new HashSet<>(features));

        // 5. ✅ Lưu xuống DB trước
        Product saved = productRepository.save(product);
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
    public Product addDiscountToProduct(String productId, DiscountRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có id: " + productId));
        if (request.getPercentage() == null || request.getPercentage() <= 0 || request.getPercentage() > 1) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải nằm trong khoảng (0, 1]");
        }
        Discount discount = new Discount(
                request.getPercentage(),
                request.getStartDate(),
                request.getEndDate()
        );
        // Gắn discount vô product
        discount.setProduct(product);
        product.getDiscounts().add(discount);
        discountRepository.save(discount); // Lưu discount riêng
        return productRepository.save(product);
    }

    @Override
    public Image addImageToProduct(String productId, ImageRequest req) {
        return null;
    }

    @Override
    public Product addImagesToProduct(String productId, List<MultipartFile> files) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<Image> images = new ArrayList<>();

        for (MultipartFile file : files) {
            // 🌥 Upload từng file lên Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "products/" + productId  // mỗi product có folder riêng
            ));

            // 🧾 Lấy URL ảnh từ Cloudinary
            String imageUrl = (String) uploadResult.get("secure_url");

            // 📸 Tạo đối tượng Image và gán thông tin
            Image image = new Image();
            image.setProduct(product);
            image.setUrl(imageUrl);
            image.setFilename(file.getOriginalFilename());
            image.setContentType(file.getContentType());

            images.add(image);
        }

        // 💾 Lưu tất cả ảnh
        imageRepository.saveAll(images);

        // Gắn danh sách ảnh vào product (nếu chưa có)
        if (product.getImages() == null) {
            product.setImages(null);
        }
        product.getImages().addAll(images);

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

    @Override
    public List<Product> getTopDiscountProducts(int limits) {
        Pageable pageable = PageRequest.of(0, limits);
        LocalDate today = LocalDate.now();
        return discountRepository.findTopDiscountProducts(today, pageable);
    }
}
