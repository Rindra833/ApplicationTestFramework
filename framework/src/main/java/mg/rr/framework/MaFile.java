package mg.rr.framework;

/**
 * Sprint 8 : represente un fichier uploadé via un formulaire HTML.
 * Instancié automatiquement par le framework (RouteMapping).
 */
public class MaFile {
    private String fileName;  // nom du fichier envoye (ex: "photo.jpg")
    private String type;      // type MIME (ex: "image/jpeg") ou extension
    private byte[] content;   // contenu binaire du fichier

    public MaFile() {}

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }

    /**
     * Petit helper pour savoir si un fichier a bien ete envoye.
     */
    public boolean exists() {
        return content != null && content.length > 0 && fileName != null && !fileName.isEmpty();
    }

    @Override
    public String toString() {
        return "MaFile{fileName='" + fileName + "', type='" + type + 
               "', size=" + (content != null ? content.length : 0) + " bytes}";
    }
}