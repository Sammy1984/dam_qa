package ru.spice.at.common.dto.dam;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RandomStringUtils;
import ru.spice.at.common.emuns.dam.ImageFormat;

@Setter
@Accessors(chain  = true)
public class ImageData {
    private String filename;

    @Getter
    private Integer size = 1000;

    @Getter
    private Integer width = 600;

    @Getter
    private Integer height = 600;

    @Getter
    private ImageFormat format;

    private String url;

    private String key;

    private String sku;

    private String hash;

    @Getter
    private Boolean handle = false;

    @Getter
    private Boolean frame = false;

    public ImageData(ImageFormat format) {
        this.format = format;
    }

    public ImageData() {}

    public String getFilename() {
        return filename == null ? filename = String.format("%s_%s.%s", getSku(), RandomStringUtils.randomNumeric(3), getFormat().getFormatName()) : filename;
    }

    public String getUrl() {
        return url == null ?
                url = String.format("https://%s/%s/%s",
                        RandomStringUtils.randomAlphanumeric(15),
                        RandomStringUtils.randomAlphanumeric(15),
                        RandomStringUtils.randomAlphanumeric(15)) : url;
    }

    public String getKey() {
        return key == null ? key = RandomStringUtils.randomAlphanumeric(15) : key;
    }

    public String getSku() {
        return sku == null ? sku = RandomStringUtils.randomAlphabetic(8) : sku;
    }

    public String getHash() {
        return hash == null ? hash = RandomStringUtils.randomAlphabetic(10) : hash;
    }

    @Override
    public ImageData clone() {
        return new ImageData().
                setFilename(this.filename).
                setSize(this.size).
                setWidth(this.width).
                setHeight(this.height).
                setFormat(this.format).
                setUrl(this.url).
                setKey(this.key).
                setSku(this.sku).
                setHash(this.hash);
    }
}
