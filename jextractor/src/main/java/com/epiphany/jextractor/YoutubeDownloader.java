package com.epiphany.jextractor;


import com.alibaba.fastjson.JSONObject;
import com.epiphany.jextractor.cipher.CipherFunction;
import com.epiphany.jextractor.model.VideoDetails;
import com.epiphany.jextractor.model.YoutubeVideo;
import com.epiphany.jextractor.model.formats.Format;
import com.epiphany.jextractor.model.playlist.PlaylistDetails;
import com.epiphany.jextractor.model.playlist.PlaylistVideoDetails;
import com.epiphany.jextractor.model.playlist.YoutubePlaylist;
import com.epiphany.jextractor.model.subtitles.SubtitlesInfo;
import com.epiphany.jextractor.parser.DefaultParser;
import com.epiphany.jextractor.parser.Parser;

import java.util.List;

public class YoutubeDownloader {

    private Parser parser;

    public YoutubeDownloader() {
        this.parser = new DefaultParser();
    }

    public YoutubeDownloader(Parser parser) {
        this.parser = parser;
    }

    public void setParserRequestProperty(String key, String value) {
        parser.getExtractor().setRequestProperty(key, value);
    }

    public void setParserRetryOnFailure(int retryOnFailure) {
        parser.getExtractor().setRetryOnFailure(retryOnFailure);
    }

    public void addCipherFunctionPattern(int priority, String regex) {
        parser.getCipherFactory().addInitialFunctionPattern(priority, regex);
    }

    public void addCipherFunctionEquivalent(String regex, CipherFunction function) {
        parser.getCipherFactory().addFunctionEquivalent(regex, function);
    }

    public YoutubeVideo getVideo(String videoId) throws YoutubeException {
        String htmlUrl = "https://www.youtube.com/watch?v=" + videoId;

        JSONObject ytPlayerConfig = parser.getPlayerConfig(htmlUrl);
        ytPlayerConfig.put("yt-downloader-videoId", videoId);

        VideoDetails videoDetails = parser.getVideoDetails(ytPlayerConfig);

        List<Format> formats = parser.parseFormats(ytPlayerConfig);

        List<SubtitlesInfo> subtitlesInfo = parser.getSubtitlesInfoFromCaptions(ytPlayerConfig);

        String clientVersion = parser.getClientVersion(ytPlayerConfig);

        return new YoutubeVideo(videoDetails, formats, subtitlesInfo, clientVersion);
    }

    public YoutubePlaylist getPlaylist(String playlistId) throws YoutubeException {
        String htmlUrl = "https://www.youtube.com/playlist?list=" + playlistId;

        JSONObject ytInitialData = parser.getInitialData(htmlUrl);
        if (!ytInitialData.containsKey("metadata")) {
            throw new YoutubeException.BadPageException("Invalid initial data json");
        }

        PlaylistDetails playlistDetails = parser.getPlaylistDetails(playlistId, ytInitialData);

        List<PlaylistVideoDetails> videos = parser.getPlaylistVideos(ytInitialData, playlistDetails.videoCount());

        return new YoutubePlaylist(playlistDetails, videos);
    }

    public YoutubePlaylist getChannelUploads(String channelId) throws YoutubeException {
        String playlistId = parser.getChannelUploadsPlaylistId(channelId);
        return getPlaylist(playlistId);
    }

    public List<SubtitlesInfo> getVideoSubtitles(String videoId) throws YoutubeException {
        return parser.getSubtitlesInfo(videoId);
    }
}
