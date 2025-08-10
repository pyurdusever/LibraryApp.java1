import java.util.*;
import java.io.*;
import java.time.LocalDate;

/**
 * Kütüphane Yönetim Sistemi ,
 * Özellikler:
 *  - Karşılama menüsü
 *  - Kitap ekleme, listeleme, başlığa göre arama
 *  - Kitap ödünç alma / iade
 *  - CSV dosyasına kayıt ve dosyadan yükleme
 */
public class LibraryApp {

    public static void main(String[] args) {
        Library library = new Library();
        library.loadFromFile("library.csv"); // Başlangıçta dosyadan yükle

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Seçiminiz: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> addBookFlow(sc, library);
                    case "2" -> listBooksFlow(library);
                    case "3" -> searchByTitleFlow(sc, library);
                    case "4" -> borrowFlow(sc, library);
                    case "5" -> returnFlow(sc, library);
                    case "6" -> {
                        running = false;
                        library.saveToFile("library.csv"); // Çıkarken kaydet
                    }
                    default ->
                            System.out.println("⚠ Geçersiz seçim. Lütfen 1-6 arasında girin.");
                }

                if (running) {
                    System.out.println("\nDevam etmek için Enter'a basın...");
                    sc.nextLine();
                }
            }
        }

        System.out.println("Program sonlandırıldı. Görüşmek üzere!");
    }

    private static void printMenu() {
        System.out.println("\n****************************");
        System.out.println("  KÜTÜPHANE YÖNETİM SİSTEMİ  ");
        System.out.println("******************************");
        System.out.println("1) Yeni Kitap Ekle");
        System.out.println("2) Tüm Kitapları Listele");
        System.out.println("3) Başlığa Göre Kitap Ara");
        System.out.println("4) Kitap Ödünç Al");
        System.out.println("5) Kitap İade Et");
        System.out.println("6) Çıkış");
        System.out.println("******************************");
    }

    // ---- Akış Metotları ----
    private static void addBookFlow(Scanner sc, Library library) {
        System.out.println("\n Yeni Kitap Ekle");
        System.out.print("Başlık: ");
        String title = sc.nextLine().trim();
        System.out.print("Yazar: ");
        String author = sc.nextLine().trim();
        int year = askInt(sc, "Yayın Yılı: ");
        System.out.print("ISBN: ");
        String isbn = sc.nextLine().trim();
        System.out.print("Kategori: ");
        String category = sc.nextLine().trim().toUpperCase(Locale.ROOT);

        Book b = library.addBook(title, author, year, isbn, category);
        System.out.println("Kitap eklendi: " + b);
    }

    private static void listBooksFlow(Library library) {
        System.out.println("\n Kayıtlı Kitaplar");
        List<Book> all = library.getAllBooks();
        if (all.isEmpty()) {
            System.out.println("Henüz kitap bulunmuyor.");
            return;
        }
        all.forEach(b -> System.out.println(" - " + b));
    }

    private static void searchByTitleFlow(Scanner sc, Library library) {
        System.out.println("\n Başlığa Göre Ara");
        System.out.print("Aranacak ifade: ");
        String q = sc.nextLine().trim();
        List<Book> found = library.searchByTitle(q);
        if (found.isEmpty()) {
            System.out.println("Sonuç bulunamadı.");
        } else {
            System.out.println("Bulunanlar:");
            found.forEach(b -> System.out.println(" - " + b));
        }
    }

    private static void borrowFlow(Scanner sc, Library library) {
        System.out.println("\n📥 Kitap Ödünç Al");
        int id = askInt(sc, "Kitap ID: ");
        Optional<Book> opt = library.getById(id);
        if (opt.isEmpty()) {
            System.out.println("ID " + id + " ile kitap bulunamadı.");
            return;
        }
        Book b = opt.get();
        if (b.isBorrowed()) {
            System.out.println("Bu kitap şu anda ödünçte! Tekrar ödünç verilemez.");
        } else {
            library.borrowBook(id, "Kullanıcı", LocalDate.now());
            System.out.println("Ödünç alındı: " + b.getTitle());
        }
    }

    private static void returnFlow(Scanner sc, Library library) {
        System.out.println("\n Kitap İade Et");
        int id = askInt(sc, "Kitap ID: ");
        Optional<Book> opt = library.getById(id);
        if (opt.isEmpty()) {
            System.out.println("ID " + id + " ile kitap bulunamadı.");
            return;
        }
        Book b = opt.get();
        if (!b.isBorrowed()) {
            System.out.println("ℹ Bu kitap zaten kütüphanede (müsait).");
        } else {
            library.returnBook(id);
            System.out.println("İade edildi: " + b.getTitle());
        }
    }

    private static int askInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            try {
                int val = Integer.parseInt(s);
                if (val <= 0) {
                    System.out.println("Pozitif sayı girin.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Geçerli bir sayı girin.");
            }
        }
    }
}

// ---- Kitap Sınıfı ----
class Book {
    private final int id;
    private final String title;
    private final String author;
    private final int year;
    private final String isbn;
    private final String category;
    private boolean borrowed;
    private String borrowerName;
    private LocalDate borrowDate;

    public Book(int id, String title, String author, int year, String isbn, String category,
                boolean borrowed, String borrowerName, LocalDate borrowDate) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.isbn = isbn;
        this.category = category;
        this.borrowed = borrowed;
        this.borrowerName = borrowerName;
        this.borrowDate = borrowDate;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }
    public void setBorrowerName(String name) { this.borrowerName = name; }
    public void setBorrowDate(LocalDate date) { this.borrowDate = date; }

    @Override
    public String toString() {
        return String.format("#%d | %s — %s (%d) | ISBN: %s | Kategori: %s | Durum: %s",
                id, title, author, year, isbn, category, borrowed ? "ÖDÜNÇTE" : "MÜSAİT");
    }

    public String toCsv() {
        return String.join(",",
                String.valueOf(id), title, author, String.valueOf(year),
                isbn, category, String.valueOf(borrowed),
                borrowerName == null ? "" : borrowerName,
                borrowDate == null ? "" : borrowDate.toString());
    }

    public static Book fromCsv(String csv) {
        String[] p = csv.split(",", -1);
        return new Book(
                Integer.parseInt(p[0]), p[1], p[2], Integer.parseInt(p[3]),
                p[4], p[5], Boolean.parseBoolean(p[6]),
                p[7].isEmpty() ? null : p[7],
                p[8].isEmpty() ? null : LocalDate.parse(p[8])
        );
    }
}

// ---- Kütüphane Sınıfı ----
class Library {
    private final List<Book> books = new ArrayList<>();
    private int nextId = 1;

    public Book addBook(String title, String author, int year, String isbn, String category) {
        Book b = new Book(nextId++, title, author, year, isbn, category, false, null, null);
        books.add(b);
        return b;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public Optional<Book> getById(int id) {
        return books.stream().filter(b -> b.getId() == id).findFirst();
    }

    public List<Book> searchByTitle(String query) {
        String q = query.toLowerCase(Locale.ROOT);
        List<Book> res = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase(Locale.ROOT).contains(q)) {
                res.add(b);
            }
        }
        return res;
    }

    public boolean borrowBook(int id, String borrower, LocalDate date) {
        Optional<Book> opt = getById(id);
        if (opt.isPresent() && !opt.get().isBorrowed()) {
            Book b = opt.get();
            b.setBorrowed(true);
            b.setBorrowerName(borrower);
            b.setBorrowDate(date);
            return true;
        }
        return false;
    }

    public boolean returnBook(int id) {
        Optional<Book> opt = getById(id);
        if (opt.isPresent() && opt.get().isBorrowed()) {
            Book b = opt.get();
            b.setBorrowed(false);
            b.setBorrowerName(null);
            b.setBorrowDate(null);
            return true;
        }
        return false;
    }

    public void saveToFile(String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (Book b : books) {
                out.println(b.toCsv());
            }
        } catch (IOException e) {
            System.out.println("⚠ Dosya kaydedilirken hata: " + e.getMessage());
        }
    }

    public void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                books.add(Book.fromCsv(line));
            }
            if (!books.isEmpty()) {
                nextId = books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
            }
        } catch (IOException e) {
            System.out.println("⚠ Dosya yüklenirken hata: " + e.getMessage());
        }
    }
}


