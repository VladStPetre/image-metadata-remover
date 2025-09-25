package dev.vsp;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Hello world!
 */
public class ImageMetadataProcessor {
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "bmp", "gif");

//    public static void main(String[] args) {
//        if (args.length != 1) {
//            System.out.println("Usage: java PhotoMetadataProcessor <directory-path>");
//            return;
//        }
//
//        File directory = new File(args[0]);
//        if (!directory.exists() || !directory.isDirectory()) {
//            System.out.println("Invalid directory: " + directory.getAbsolutePath());
//            return;
//        }
//
//        System.out.println("Processing directory: " + directory.getAbsolutePath());
//        processDirectory(directory);
//    }
//
//    private static void processDirectory(File directory) {
//        Map<File, Long> directoryMetadataSizes = new TreeMap<>();
//        calculateMetadataSizeRecursively(directory, directoryMetadataSizes);
//
//        System.out.println("\nMetadata Size Summary (in bytes):");
//        directoryMetadataSizes.forEach((dir, size) ->
//                System.out.println(dir.getAbsolutePath() + ": " + size + " bytes"));
//    }
//
//    private static void calculateMetadataSizeRecursively(File dir, Map<File, Long> directoryMetadataSizes) {
//        long totalSize = 0;
//
//        File[] files = dir.listFiles();
//        if (files == null) return;
//
//        for (File file : files) {
//            if (file.isDirectory()) {
//                calculateMetadataSizeRecursively(file, directoryMetadataSizes);
//            } else if (isImageFile(file)) {
//                long metadataSize = calculateMetadataSize(file);
//                totalSize += metadataSize;
//            }
//        }
//
//        directoryMetadataSizes.put(dir, totalSize);
//    }
//
//    private static boolean isImageFile(File file) {
//        String extension = getFileExtension(file.getName());
//        return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
//    }
//
//    private static String getFileExtension(String filename) {
//        int index = filename.lastIndexOf('.');
//        return (index > 0) ? filename.substring(index + 1) : "";
//    }
//
//    private static long calculateMetadataSize(File file) {
//        try {
//            MetadataWrapper wrapper = new MetadataWrapper(ImageMetadataReader.readMetadata(file));
//            if (wrapper.getMetadata() == null) return 0;
//
//            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
//                oos.writeObject(wrapper);
//                oos.flush();
//                return baos.size();
//            }
//
//        } catch (Exception e) {
//            System.err.println("Failed to process metadata for: " + file.getAbsolutePath() + " - " + e.getMessage());
//            return 0;
//        }
//    }
/*
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PhotoMetadataProcessor <directory-path>");
            return;
        }

        File directory = new File(args[0]);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory: " + directory.getAbsolutePath());
            return;
        }

        System.out.println("Processing directory: " + directory.getAbsolutePath());
        processDirectory(directory);
    }

    private static void processDirectory(File directory) {
        Map<File, Long> directoryMetadataSizes = new TreeMap<>();
        calculateMetadataSizeRecursively(directory, directoryMetadataSizes, 0);

        System.out.println("\nDirectory Metadata Size Summary (in bytes):");
        directoryMetadataSizes.forEach((dir, size) ->
                System.out.println(dir.getAbsolutePath() + ": " + size + " bytes"));
    }

    private static void calculateMetadataSizeRecursively(File dir, Map<File, Long> directoryMetadataSizes, int depth) {
        long totalSize = 0;

        File[] files = dir.listFiles();
        if (files == null) return;

        String indent = "  ".repeat(depth);
        System.out.println(indent + "Directory: " + dir.getName());

        for (File file : files) {
            if (file.isDirectory()) {
                calculateMetadataSizeRecursively(file, directoryMetadataSizes, depth + 1);
            } else if (isImageFile(file)) {
                long metadataSize = calculateMetadataSize(file);
                long withoutMetaSize = calculateMetaSizeTempFile(file);
                long originalSize = file.length();
                totalSize += metadataSize;

                System.out.printf("%s  F: %s, OS: %d bytes, WM: %d bytes, MS: %d bytes%n",
                        indent, file.getName(), originalSize, withoutMetaSize, metadataSize);
            }
        }

        directoryMetadataSizes.put(dir, totalSize);
    }

    private static long calculateMetaSizeTempFile(File file) {
        long finalSize = 0;
        try {
            File tempFile = new File(file.getPath() + "wm_" + file.getName());
            copyImagePreservingQuality(file, tempFile);
//            boolean written = ImageIO.write(ImageIO.read(file), "jpg", tempFile);
//            System.out.println("written: " + tempFile.getAbsolutePath());
            finalSize = tempFile.length();
//            tempFile.delete();

        } catch (Exception e) {
            System.err.println("cannot serialize file: " + file.getAbsolutePath() + " - " + e.getMessage());
        }
        return finalSize;
    }

    private static void copyImagePreservingQuality(File source, File destination) throws IOException {
        BufferedImage image = ImageIO.read(source);
        if (image == null) throw new IOException("Failed to read image: " + source.getName());

        String extension = getFileExtension(source.getName());

        try (FileOutputStream fos = new FileOutputStream(destination);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

            ImageWriter writer = ImageIO.getImageWritersByFormatName(extension).next();
            writer.setOutput(ios);

            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            if (writeParam.canWriteCompressed()) {
                writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                writeParam.setCompressionQuality(1.0f); // Maximum quality
            }

            writer.write(null, new IIOImage(image, null, null), writeParam);
            writer.dispose();
        }
    }

    private static boolean isImageFile(File file) {
        String extension = getFileExtension(file.getName());
        return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    private static String getFileExtension(String filename) {
        int index = filename.lastIndexOf('.');
        return (index > 0) ? filename.substring(index + 1) : "";
    }

    private static long calculateMetadataSize(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            if (metadata == null) return 0;

            long size = 0;
            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    size += estimateSize(tag.getTagName());
                    size += estimateSize(tag.getDescription());
                }
            }
            return size;
        } catch (Exception e) {
            System.err.println("Failed to process metadata for: " + file.getAbsolutePath() + " - " + e.getMessage());
            return 0;
        }
    }

    private static long estimateSize(String text) {
        return (text != null) ? text.getBytes().length : 0;
    }

    private static void print(Metadata metadata, String method) {
        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.print(' ');
        System.out.print(method);
        System.out.println("-------------------------------------------------");
        System.out.println();

        //
        // A Metadata object contains multiple Directory objects
        //
        for (Directory directory : metadata.getDirectories()) {

            //
            // Each Directory stores values in Tag objects
            //
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }

            //
            // Each Directory may also contain error messages
            //
            for (String error : directory.getErrors()) {
                System.err.println("ERROR: " + error);
            }
        }
    }

 */
}
