package dev.vsp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageMetadataCleaner {
  /*  public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ImageMetadataCleaner <source_directory> <copy_directory>");
            return;
        }
        Path sourceDir = Paths.get(args[0]);
        Path copyDir = Paths.get(args[0] + "_CP");
        processDirectory(sourceDir, copyDir);
    }

    private static void processDirectory(Path sourceDir, Path copyDir) {
        try {
            Files.walk(sourceDir)
                    .filter(Files::isRegularFile)
                    .filter(ImageMetadataCleaner::isImageFile)
                    .forEach(path -> processImage(path, sourceDir, copyDir));
        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
        }
    }

    private static boolean isImageFile(Path path) {
        String lowerCaseName = path.toString().toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".tiff") ||
                lowerCaseName.endsWith(".gif") || lowerCaseName.endsWith(".bmp");
    }

    private static void processImage(Path imagePath, Path sourceDir, Path copyDir) {
        File imageFile = imagePath.toFile();
        long originalSize = imageFile.length();
        File newFile = removeMetadata(imageFile, sourceDir, copyDir);
        long newFileSize = newFile != null ? newFile.length() : 0;
        System.out.println("F: " + imageFile.getName() + ", OS: " + originalSize + ", CS: " + newFileSize);
    }

    private static File removeMetadata(File imageFile, Path sourceDir, Path copyDir) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            if (img != null) {
                Path relativePath = sourceDir.relativize(imageFile.toPath());
                Path newFilePath = copyDir.resolve(relativePath);
                Files.createDirectories(newFilePath.getParent());
                String newFileName = newFilePath.toString().replaceFirst("(\\.[^.]+)$", "_cp$1");
                File newFile = new File(newFileName);
                ImageIO.write(img, getFormatName(imageFile), newFile);
                return newFile;
            }
        } catch (IOException e) {
            System.err.println("Error processing image: " + imageFile.getName());
        }
        return null;
    }

    private static String getFormatName(File file) {
        String name = file.getName().toLowerCase();
        return switch (name.substring(name.lastIndexOf('.') + 1)) {
            case "jpg", "jpeg" -> "jpeg";
            case "png" -> "png";
            case "tiff" -> "tiff";
            case "gif" -> "gif";
            case "bmp" -> "bmp";
            default -> "jpeg";
        };
    }

   */
}

