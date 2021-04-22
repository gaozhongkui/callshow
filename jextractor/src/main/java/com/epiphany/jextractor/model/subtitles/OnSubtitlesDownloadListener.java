package com.epiphany.jextractor.model.subtitles;


public interface OnSubtitlesDownloadListener {
    void onFinished(String subtitles);

    void onError(Throwable throwable);
}
