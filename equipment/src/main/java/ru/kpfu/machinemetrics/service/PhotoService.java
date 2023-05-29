package ru.kpfu.machinemetrics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kpfu.machinemetrics.properties.AppProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final AppProperties appProperties;

    public Resource getPhoto(String path) {
        Resource photoResource = new FileSystemResource(appProperties.getPhotoSaveDirectory() + "/" + path);
        if (photoResource.exists()) {
            return photoResource;
        }
        return new ClassPathResource("equipment-default.jpg");
    }

    public Resource getDefault() {
        return new ClassPathResource("equipment-default.jpg");
    }

    public String savePhoto(String name, MultipartFile photo) throws IOException {
        String filePath = appProperties.getPhotoSaveDirectory() + "/" + name;
        photo.transferTo(new File(filePath));

        return filePath;
    }

    public void deleteUnusedPhotos(List<String> photoNames) {
        File directory = new File(appProperties.getPhotoSaveDirectory());

        // Get all files in the directory
        File[] files = directory.listFiles();

        // Iterate over the files and print their names
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && !photoNames.contains(file.getName())) {
                    file.delete();
                }
            }
        }
    }
}
