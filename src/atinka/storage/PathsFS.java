package atinka.storage;

import java.io.IOException;
import java.nio.file.*;

/** Centralizes filesystem paths and ensures directories exist. */
public final class PathsFS {
    public static final Path ROOT = Path.of("data");
    public static final Path DRUGS = ROOT.resolve("drugs.csv");
    public static final Path SUPPLIERS = ROOT.resolve("suppliers.csv");
    public static final Path CUSTOMERS = ROOT.resolve("customers.csv");
    public static final Path PURCHASE_LOG = ROOT.resolve("purchase_log.csv");
    public static final Path SALES_LOG = ROOT.resolve("sales_log.csv");
    public static final Path REPORTS_DIR = ROOT.resolve("reports");

    private PathsFS() {}

    public static void ensure() {
        try {
            Files.createDirectories(ROOT);
            if (Files.notExists(DRUGS)) Files.createFile(DRUGS);
            if (Files.notExists(SUPPLIERS)) Files.createFile(SUPPLIERS);
            if (Files.notExists(CUSTOMERS)) Files.createFile(CUSTOMERS);
            if (Files.notExists(PURCHASE_LOG)) Files.createFile(PURCHASE_LOG);
            if (Files.notExists(SALES_LOG)) Files.createFile(SALES_LOG);
            Files.createDirectories(REPORTS_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to init data folder", e);
        }
    }
}