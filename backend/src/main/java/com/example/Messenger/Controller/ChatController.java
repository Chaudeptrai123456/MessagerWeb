//package com.example.Messenger.Controller;
//
//import com.example.Messenger.Service.EmbeddingService;
//import com.example.Messenger.Service.LocalAiService;
//import com.example.Messenger.Service.VectorDbService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//// controller/ChatController.java
//@RestController
//@RequestMapping("/chat")
//public class ChatController {
//
//    @Autowired
//    private EmbeddingService embeddingService;
//
//    @Autowired
//    private VectorDbService vectorDbService;
//
////    @Autowired
////    private LocalAiService aiModelService;
//
//    @PostMapping("/{userId}")
//    public ResponseEntity<String> chat(@PathVariable String userId, @RequestBody String prompt) throws Exception {
//        float[] queryVector = embeddingService.embed(prompt);
//        List<String> context = vectorDbService.searchSimilar(queryVector, userId);
//
//        String augmentedPrompt = buildPrompt(context, prompt);
//        CompletableFuture<String> modelFuture = aiModelService.queryModel(augmentedPrompt);
//
//        modelFuture.thenAccept(answer -> {
//            vectorDbService.saveEmbedding(prompt + "\n" + answer, userId);
//        });
//
//        String answer = modelFuture.get();
//        return ResponseEntity.ok(answer);
//    }
//
//    private String buildPrompt(List<String> context, String question) {
//        StringBuilder sb = new StringBuilder("Context:\n");
//        context.forEach(c -> sb.append("- ").append(c).append("\n"));
//        sb.append("\nQuestion:\n").append(question);
//        return sb.toString();
//    }
//}
