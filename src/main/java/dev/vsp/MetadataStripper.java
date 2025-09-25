package dev.vsp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class MetadataStripper {

    // List of image file extensions to process (case-insensitive)
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "bmp", "gif");
    // List of recognized video file extensions (case-insensitive)
    private static final Set<String> VIDEO_EXTENSIONS =  Set.of("mp4", "avi", "mkv", "mov", "wmv");

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java ImageMetadataRemover <input-directory> <output-directory>");
            System.exit(1);
        }

        Path inputDir = Paths.get(args[0]);
        Path outputDir = Paths.get(args[1]);

//        try {
//            Set<Path> input = Files.walk(inputDir).filter(Files::isRegularFile).map(f->f.getFileName()).collect(Collectors.toSet());
//            Set<Path> output = Files.walk(outputDir).filter(Files::isRegularFile).map(f->f.getFileName()).collect(Collectors.toSet());
//
//            List<Path> diff = input.stream().filter(el -> !output.contains(el)).collect(Collectors.toList());
//
//            System.out.println("diff: " + diff.size() + " - \n" + diff);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//        if(true)
//            return;

        if (!Files.isDirectory(inputDir)) {
            System.err.println("Input path is not a directory");
            System.exit(1);
        }

        // First, collect all image files into a List
        List<Path> imageFiles;
        try {
            imageFiles = Files.walk(inputDir)
                    .filter(Files::isRegularFile)
//                    .filter(MetadataStripper::isImageFile)
                    .filter(MetadataStripper::isVideoFile)
                    .toList();
        } catch (IOException e) {
            System.err.println("Error traversing directory: " + e.getMessage());
            return;
        }

        long startTime = System.currentTimeMillis();
        ForkJoinPool customThreadPool = new ForkJoinPool(4);
        try {
            customThreadPool.submit(() -> {
                imageFiles.parallelStream().forEach(sourceFile -> {

                    // Compute the relative path from the input directory
                    Path relativePath = inputDir.relativize(sourceFile);
                    Path targetFile = outputDir.resolve(relativePath);
                    try {
                        // Ensure the target directory exists
                        Files.createDirectories(targetFile.getParent());

                        if (isImageFile(sourceFile)) {
                            // Process image: remove metadata then write the image
//                            processImage(sourceFile.toFile(), targetFile.toFile());
                            return;
                        } else if (isVideoFile(sourceFile)) {
                            // Copy video file directly
                            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        }

                        // Preserve the original last modified date
                        FileTime lastModifiedTime = Files.getLastModifiedTime(sourceFile);
                        Files.setLastModifiedTime(targetFile, lastModifiedTime);

                        System.out.println("Processed: " + sourceFile.toString());
                    } catch (IOException e) {
                        System.err.println("Error processing file " + sourceFile + ": " + e.getMessage());
                    }
                });
            }).get(); // Wait for the submitted task to complete.
            System.out.println("time spent: " + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            System.err.println("Error during parallel processing: " + e.getMessage());
        } finally {
            customThreadPool.shutdown();
        }
    }

    // Check if a file has one of the image extensions
    private static boolean isImageFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(ext -> fileName.endsWith("." + ext));
    }

    // Returns true if the file extension indicates a video file.
    private static boolean isVideoFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return VIDEO_EXTENSIONS.stream().anyMatch(ext -> fileName.endsWith("." + ext));
    }

    // Read the image and write it to the target file.
    // Reading and writing via buffered streams improves performance.
    // Note: Writing via ImageIO does not preserve metadata.
    private static void processImage(File source, File target) throws IOException {
        // Read image with a buffered input stream.
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(source.toPath()))) {
            BufferedImage image = ImageIO.read(bis);
            if (image == null) {
                throw new IOException("File is not a valid image: " + source);
            }

            // Determine the format from the source file extension.
            String formatName = getFormatName(source.getName());
            if (formatName == null) {
                throw new IOException("Unsupported image format: " + source.getName());
            }

            // Write image with a buffered output stream.
            try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(target.toPath()))) {
                if (!ImageIO.write(image, formatName, bos)) {
                    throw new IOException("Error writing image: " + target);
                }
            }
        }
    }

    // Utility to extract the format name from the file name.
    private static String getFormatName(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return null;
        }
        String ext = fileName.substring(dotIndex + 1).toLowerCase();
        // Use ext as the format name (ImageIO accepts common extensions)
        return ext;
    }
}

