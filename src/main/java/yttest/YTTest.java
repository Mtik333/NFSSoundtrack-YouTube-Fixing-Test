package yttest;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.AccessPolicy;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetailsRegionRestriction;
import com.google.api.services.youtube.model.VideoListResponse;
import song.Song;
import song.SongComparator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YTTest {
    /*useful regex:
    ([\w]),([\w]) - comma between alphanumerical chars
    ('[^,]*[a-zA-Z0-9]), - delete comma when inside value of column
    */

    private static List<Song> songsFromFile;
    private final static Logger logger = Logger.getLogger(YTTest.class.getName());
    private final static YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
            request -> {
            }).setApplicationName("").build();
    private final static int startIndex = 0;
    private final static int stopIndex = 1000; //max: 12774

    public static void main(String[] args) {
        logger.log(Level.INFO, "Start of tool");
        songsFromFile = new ArrayList<>();
        try {
            loadFile();
            iterateOverList();
            exportSongsToFile();
            logger.log(Level.INFO, "Finished tool");
        } catch (IOException | URISyntaxException exp) {
            logger.log(Level.SEVERE, "Error: " + exp.getMessage());
        }
    }

    private static void loadFile() throws URISyntaxException, IOException {
        logger.log(Level.INFO, "Started method loadFile");
        Path path = Paths.get(Objects.requireNonNull(YTTest.class.getClassLoader()
                .getResource("songs_14_12_2019.sql")).toURI());
        logger.log(Level.INFO, "Path to input file: " + path.toString());
        List<String> lines = Files.readAllLines(path);
        logger.log(Level.INFO, "Number of lines: " + lines.size());
        for (String line : lines) {
            logger.log(Level.INFO, "Line text: " + line);
            String skipStartEndChars = line.substring(1, line.length() - 2);
            String[] splitByComma = skipStartEndChars.split(",");
            for (int i = 0; i < splitByComma.length; i++) {
                String value = splitByComma[i].trim().replaceAll("'$", "").replaceFirst("^'", "");
                logger.log(Level.INFO, "Value from line is: " + value);
                splitByComma[i] = value;
            }
            Song song = createSong(splitByComma);
            logger.log(Level.INFO, "Song loaded is: " + song);
            songsFromFile.add(song);
        }
    }

    private static Song createSong(String[] values) {
        logger.log(Level.INFO, "Started method createSong");
        Integer id = Integer.valueOf(values[0]);
        String band = values[1];
        String title = values[2];
        Integer game_id = Integer.valueOf(values[4]);
        String src_id = values[3];
        return new Song(id, band, title, game_id, src_id);
    }

    private static List<Video> testYoutubeDetails(String srcId) throws IOException {
        logger.log(Level.INFO, "Started method testYoutubeDetails");
        YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics,contentDetails,status");
        videoRequest.setId(srcId);
        videoRequest.setKey("");
        logger.log(Level.INFO, "Request input for YT: " + videoRequest.toString());
        VideoListResponse listResponse = videoRequest.execute();
        logger.log(Level.INFO, "Response from YT: " + listResponse.toPrettyString());
        return listResponse.getItems();
    }

    private static void setSongStatus(Song song, List<Video> videos) throws IOException {
        logger.log(Level.INFO, "Started method setSongStatus");
        if (videos.isEmpty()) {
            //non-existing video gives empty list
            logger.log(Level.INFO, "No videos in list, it is DELETED");
            song.setStatus(Song.Status.DELETED);
        } else {
            Video video = videos.get(0);
            logger.log(Level.INFO, "Video from response: " + video.toPrettyString());
            VideoContentDetailsRegionRestriction regionRestriction = video.getContentDetails().getRegionRestriction();
            AccessPolicy policy = video.getContentDetails().getCountryRestriction();
            if (policy == null && regionRestriction == null) {
                //means there are no restrictions
                logger.log(Level.INFO, "Video is available everywhere, so it is WORKING");
                song.setStatus(Song.Status.WORKING);
            } else {
                //video with restrictions gives regionRestriction field
                song.setStatus(Song.Status.RESTRICTED);
                song.setRegionRestriction(regionRestriction);
                logger.log(Level.INFO, "Video has restrictions, so it is RESTRICTED");
            }
        }
    }

    private static void iterateOverList() throws IOException {
        logger.log(Level.INFO, "Started method iterateOverList; num of songs: " + songsFromFile.size());
        songsFromFile.sort(new SongComparator());
        logger.log(Level.INFO, "Start index: " + startIndex + "; stop index: " + stopIndex);
        for (int i = startIndex; i < stopIndex; i++) {
            Song song = songsFromFile.get(i);
            if (!song.getSrc_id().isEmpty() && !song.getSrc_id().contentEquals("0")) {
                List<Video> ytResponse = testYoutubeDetails(song.getSrc_id());
                setSongStatus(song, ytResponse);
            }
        }
    }

    private static void exportSongsToFile() throws IOException {
        logger.log(Level.INFO, "Started method exportSongsToFile; songs list size: " + songsFromFile.size());
        File resultFile = new File("result" + "_" + startIndex + "_" + stopIndex + ".txt");
        if (!resultFile.exists()) {
            Files.createFile(resultFile.toPath());
        }
        logger.log(Level.INFO, "Path to export file: " + resultFile.getPath());
        List<String> songStrings = new ArrayList<>();
        for (int i = startIndex; i < stopIndex; i++) {
            songStrings.add(songsFromFile.get(i).toString());
        }
        Files.write(resultFile.toPath(), songStrings);
    }
}