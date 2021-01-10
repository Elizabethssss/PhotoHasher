package com.elizabeth.photo.hasher.service;

import com.elizabeth.photo.hasher.entity.Photo;
import com.elizabeth.photo.hasher.repository.PhotoRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class PhotoService {
    private final PhotoRepository photoRepository;

    public void savePhoto(Photo photo) {
        photoRepository.save(photo);
    }

    @Async
    public void savePhotoAsFile(BufferedImage bufferedImage, String filename) throws IOException {
        File outputFile = new File("photos/" + filename);
        ImageIO.write(bufferedImage, "jpg", outputFile);
    }

    public List<Photo> findAllPhotos() {
        return photoRepository.findAll();
    }
}
