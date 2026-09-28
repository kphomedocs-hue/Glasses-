package com.parkarsite.g6a;

public enum MediaKind {
    JPG(".jpg"), MP4(".mp4"), OPUS(".opus");
    private final String extension;
    MediaKind(String extension){ this.extension=extension; }
    public String extension(){ return extension; }
}
