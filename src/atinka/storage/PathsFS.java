package atinka.storage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Centralized paths; ensures data/ exists. */
public final class PathsFS {
    private PathsFS(){}

    public static Path dataDir(){
        Path p = Paths.get("data");
        try { if (!Files.exists(p)) Files.createDirectories(p); } catch(Exception ignored){}
        return p;
    }

    public static Path drugsPath(){ return dataDir().resolve("drugs.csv"); }
    public static Path suppliersPath(){ return dataDir().resolve("suppliers.csv"); }
    public static Path customersPath(){ return dataDir().resolve("customers.csv"); }
    public static Path purchaseLogPath(){ return dataDir().resolve("purchases.csv"); }
    public static Path salesLogPath(){ return dataDir().resolve("sales.csv"); }

    public static Path reportsDir(){
        Path p = dataDir().resolve("reports");
        try { if (!Files.exists(p)) Files.createDirectories(p); } catch(Exception ignored){}
        return p;
    }

    public static Path reportPath(String name){
        return reportsDir().resolve(name);
    }
}
