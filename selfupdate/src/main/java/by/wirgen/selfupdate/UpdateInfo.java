package by.wirgen.selfupdate;

class UpdateInfo {

    private final String title;
    private final String name;
    private final int version;
    private final String link;
    private final int timestamp;

    UpdateInfo(String title, String name, int version, String link, int timestamp) {
        this.title = title;
        this.name = name;
        this.version = version;
        this.link = link;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public String getLink() {
        return link;
    }

    public int getTimestamp() {
        return timestamp;
    }
}
