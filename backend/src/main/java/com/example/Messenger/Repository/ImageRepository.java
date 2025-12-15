package com.example.Messenger.Repository;

import com.example.Messenger.Entity.Category;
import com.example.Messenger.Entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {}
