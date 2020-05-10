package se.ek.musicplayer;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.formats.AudioFormat;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.github.kiulian.downloader.model.YoutubeVideo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.kiulian.downloader.model.formats.Format;
import org.jsoup.select.Elements;
import se.ek.musicplayer.model.YoutubeSong;

public final class YoutubeRepository {

    private Log log = LogFactory.getLog(getClass());

    public List<YoutubeSong> getSearchResults(String url) {
        List<YoutubeSong> youtubeVideoData = new ArrayList<>();

        org.jsoup.nodes.Document doc = null;
        try {
            doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
        } catch (IOException e) {
            log.error("Error when trying to get html from Jsoup: " + e.getMessage());
        }

        for (Element element : doc.select(".yt-lockup-title")) {
            Elements a = element.select("a[title]");
            Elements span = element.select("span");

            String title, artist = "", uri, thumbnailUrl, duration = "";
            title = a.attr("title");

            Pattern p = Pattern.compile("\\d");
            Matcher m = p.matcher(span.text());
            if (m.find()) {
                int indexOfFirstDigit = m.start();
                duration = span.text().substring(indexOfFirstDigit).replaceAll("[^0-9:]", "");
            }
            uri = a.attr("href");
            thumbnailUrl = "http://i4.ytimg.com/vi/" + uri.substring(uri.indexOf("=") + 1) + "/hqdefault.jpg";

            if (title.contains("-")) {
                artist = title.split("-")[0];
                title = title.split("-")[1];
            }
            youtubeVideoData.add(new YoutubeSong(title, artist, uri, thumbnailUrl, duration));
        }
        return youtubeVideoData;
    }

    public File downloadMp3FromVideo(YoutubeSong song, String downloadPath) {
        try {
            YoutubeDownloader downloader = new YoutubeDownloader();
            downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
            downloader.setParserRetryOnFailure(1);

            YoutubeVideo video = downloader.getVideo(song.getVideoId());

            List<AudioFormat> audioFormats = new LinkedList<>();
            for (int i = 0; i < video.formats().size(); i++) {
                Format format = video.formats().get(i);
                if (format instanceof AudioFormat && !(format instanceof AudioVideoFormat)) {
                    audioFormats.add((AudioFormat) format);
                }
            }
            File outputDir = new File(downloadPath);
            return video.download(audioFormats.get(0), outputDir);
        } catch (IOException e) {
            log.error("Error creating output file: " + e.getMessage());
        } catch (YoutubeException e) {
            log.error("Error fetching sound format from youtube " + e.getMessage());
        }
        return null;
    }

}
