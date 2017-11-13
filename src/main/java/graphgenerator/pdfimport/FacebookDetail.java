package graphgenerator.pdfimport;

class FacebookDetail {
    private String link;
    private String status;

    public FacebookDetail(String link) {
        this.link = link;
    }

    public FacebookDetail(String link, String status) {
        this.link = link;
        this.status = status;
    }

    String getLink() {
        return this.link;
    }

    String status() {
        return this.status;
    }

}