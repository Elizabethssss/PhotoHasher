package com.elizabeth.photo.hasher.controller;

import com.elizabeth.photo.hasher.entity.Photo;
import com.elizabeth.photo.hasher.service.DHash;
import com.elizabeth.photo.hasher.service.PhotoService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PhotoController {

    private final DHash dHash;
    private final PhotoService photoService;

    @GetMapping("/image")
    public String showBrowsePage() {
        return "browse";
    }

    @PostMapping("/image/browse")
    public String browseImages(@RequestParam("file") MultipartFile[] multipartFiles) {
        Arrays.stream(multipartFiles).parallel().forEach(file -> {
            try {
                final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
                final String filename = file.getOriginalFilename();

                photoService.savePhotoAsFile(bufferedImage, filename);

                final long hash = dHash.getHash(bufferedImage);

                createAndSavePhoto(filename, hash);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return "redirect:/image";
    }

    @GetMapping("/search")
    public String showSearchPage(Model model) {
//        model.addAttribute()
        return "search";
    }

    @PostMapping("/search")
    public String searchSimilarImages(@RequestParam("file") MultipartFile multipartFile,
                                      @RequestParam("threshold") double threshold, RedirectAttributes attributes) {
        try {
            if(multipartFile.isEmpty()) {
                return "redirect:/search";
            }
            final BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(multipartFile.getBytes()));
            final long hash = dHash.getHash(bufferedImage);

            final List<Photo> allPhotos = photoService.findAllPhotos();
            final List<Photo> similarPhotos = allPhotos.stream().parallel().
                    filter(photo -> {
                        photo.setThreshold(dHash.getSimilarity(photo.getHash(), hash) * 100);
                        return dHash.areSimilarHashes(photo.getHash(), hash, threshold);
                    }).collect(Collectors.toList());
            if(similarPhotos.isEmpty()) {
                createAndSavePhoto(multipartFile.getOriginalFilename(), hash);
            }
            else {
                attributes.addFlashAttribute("similarPhotos", similarPhotos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:/search";
    }

    private void createAndSavePhoto(String filename, long hash) {
        final Photo photo = new Photo();
        photo.setPlace("D:\\Java\\hasher\\photos" + filename);
        photo.setHash(hash);
        photoService.savePhoto(photo);
    }

}
