package song;

import com.google.api.services.youtube.model.VideoContentDetailsRegionRestriction;

import java.io.Serializable;
import java.util.Objects;

public class Song implements Serializable {

    private final Integer id;
    private final String band;
    private final String title;
    private final Integer game_id;
    private final String src_id;
    private Status status;
    private VideoContentDetailsRegionRestriction regionRestriction;

    public Song(Integer id, String band, String title, Integer game_id, String src_id) {
        this.id = id;
        this.band = band;
        this.title = title;
        this.game_id = game_id;
        this.src_id = src_id;
    }

    public Integer getId() {
        return id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setRegionRestriction(VideoContentDetailsRegionRestriction regionRestriction) {
        this.regionRestriction = regionRestriction;
    }

    public String getSrc_id() {
        return src_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Song{" + "id=").append(id).append(", band='").append(band).append('\'').append(", title='").append(title).append('\'').append(", game_id=").append(game_id).append(", src_id='").append(src_id).append('\'');
        if (status != null) {
            stringBuilder.append(", status=").append(status);
        }
        if (regionRestriction != null) {
            stringBuilder.append(", regionRestriction=").append(regionRestriction).append('}');
        }
        return stringBuilder.toString();
    }

    public enum Status {
        WORKING,
        RESTRICTED,
        DELETED
    }


}
