package graphgenerator.pdfimport;

class FacebookDetail {
    private String link;
    private String status;

    String getLink() {
        return this.link;
    }

    String status() {
        return this.status;
    }

    @Override
    String toString() {
        return this.link + " (" + this.status + ")";
    }
}