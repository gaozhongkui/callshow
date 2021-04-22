package com.epiphany.jextractor.parser;


import com.alibaba.fastjson.JSONObject;
import com.epiphany.jextractor.YoutubeException;
import com.epiphany.jextractor.cipher.CipherFactory;
import com.epiphany.jextractor.extractor.Extractor;
import com.epiphany.jextractor.model.VideoDetails;
import com.epiphany.jextractor.model.formats.Format;
import com.epiphany.jextractor.model.playlist.PlaylistDetails;
import com.epiphany.jextractor.model.playlist.PlaylistVideoDetails;
import com.epiphany.jextractor.model.subtitles.SubtitlesInfo;

import java.util.List;

public interface Parser {

    Extractor getExtractor();

    CipherFactory getCipherFactory();

    /* Video */

    JSONObject getPlayerConfig(String htmlUrl) throws YoutubeException;

    String getClientVersion(JSONObject config);

    VideoDetails getVideoDetails(JSONObject config);

    String getJsUrl(JSONObject config) throws YoutubeException;

    List<SubtitlesInfo> getSubtitlesInfoFromCaptions(JSONObject config);

    List<SubtitlesInfo> getSubtitlesInfo(String videoId) throws YoutubeException;

    List<Format> parseFormats(JSONObject json) throws YoutubeException;

    /* Playlist */

    JSONObject getInitialData(String htmlUrl) throws YoutubeException;

    PlaylistDetails getPlaylistDetails(String playlistId, JSONObject initialData);

    List<PlaylistVideoDetails> getPlaylistVideos(JSONObject initialData, int videoCount) throws YoutubeException;

    String getChannelUploadsPlaylistId(String channelId) throws YoutubeException;
}
