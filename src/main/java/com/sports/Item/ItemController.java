package com.sports.Item;

import com.sports.Category.Category;
import com.sports.Category.CategoryDTO;
import com.sports.Category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.sports.Item.ItemDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/shop")
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemService itemService;
    private final CategoryService categoryService;
    private final S3Service s3Service;

    @GetMapping("/list")
    public List<ItemDTO> shopList() {
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .map(item -> new ItemDTO(
                        item.getId(),
                        item.getTitle(),
                        item.getPrice(),
                        item.getDesc(),
                        item.getImgurl(),
                        item.getStock(),
                        item.getCategory() != null ? item.getCategory().getId() : null))
                .collect(Collectors.toList());
    }

    @GetMapping("/presigned-url")
    @ResponseBody
    String getURL(@RequestParam String filename){
        return s3Service.createPreSignedURL("img/" + filename);
    }

    @GetMapping("/post")
    public List<CategoryDTO> getCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return categories.stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .collect(Collectors.toList());
    }

    @PostMapping("/add")
    public String addItem(String title,
                          Integer price,
                          String desc,
                          String imgurl,
                          Integer stock,
                          @RequestParam Long categoryId) {
        itemService.addItem(title, price, desc, imgurl, stock, categoryId);
        return "redirect:/shop/list";
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ItemDTO> detailItem(@PathVariable Long id) {
        Optional<Item> optionalItem = itemRepository.findById(id);

        if (optionalItem.isPresent()) {
            Item item = optionalItem.get();
            ItemDTO itemDTO = new ItemDTO(
                    item.getId(),
                    item.getTitle(),
                    item.getPrice(),
                    item.getDesc(),
                    item.getImgurl(),
                    item.getStock(),
                    item.getCategory() != null ? item.getCategory().getId() : null
            );
            return ResponseEntity.ok(itemDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}