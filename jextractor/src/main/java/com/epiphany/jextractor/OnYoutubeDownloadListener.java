package com.epiphany.jextractor;


import java.io.File;

public interface OnYoutubeDownloadListener {

    void onDownloading(int progress);

    void onFinished(File file);

    void onError(Throwable throwable);
}
