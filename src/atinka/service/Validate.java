package atinka.service;

import atinka.model.Drug;

/** Business rules & guards (throw IllegalArgumentException with clear messages). */
final class Validate {
    private Validate(){}

    static void drugNew(Drug d){
        if (d == null) throw new IllegalArgumentException("Drug is required");
        if (isBlank(d.getCode())) throw new IllegalArgumentException("Drug code is required");
        if (isBlank(d.getName())) throw new IllegalArgumentException("Drug name is required");
        if (d.getPrice() < 0) throw new IllegalArgumentException("Price must be >= 0");
        if (d.getStock() < 0) throw new IllegalArgumentException("Stock must be >= 0");
        if (d.getReorderThreshold() < 0) throw new IllegalArgumentException("Reorder threshold must be >= 0");
    }
    static void drugUpdate(Drug d){ drugNew(d); }

    static void purchase(String code, int qty, double unitPrice){
        if (isBlank(code)) throw new IllegalArgumentException("Drug code is required");
        if (qty < 0) throw new IllegalArgumentException("Quantity must be >= 0");
        if (unitPrice <= 0) throw new IllegalArgumentException("Unit price must be > 0");
    }
    static void sale(String code, int qty){
        if (isBlank(code)) throw new IllegalArgumentException("Drug code is required");
        if (qty <= 0) throw new IllegalArgumentException("Quantity must be > 0");
    }

    static boolean isBlank(String s){ return s == null || s.trim().length() == 0; }
}
