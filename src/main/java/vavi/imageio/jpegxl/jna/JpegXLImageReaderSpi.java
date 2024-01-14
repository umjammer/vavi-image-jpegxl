/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.jpegxl.jna;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import vavi.image.jpegxl.JpegXL;
import vavi.util.Debug;


/**
 * JpegXLImageReaderSpi.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022-10-06 umjammer initial version <br>
 */
public class JpegXLImageReaderSpi extends ImageReaderSpi {

    private static final String VendorName = "https://github.com/umjammer/vavi-image-jpegxl";
    private static final String Version = "0.0.5";
    private static final String ReaderClassName =
        "vavi.imageio.jpegxl.jna.JpegXLImageReader";
    private static final String[] Names = {
        "jpegxl", "JpegXL"
    };
    private static final String[] Suffixes = {
        "jxl"
    };
    private static final String[] mimeTypes = {
        "image/jpeg-xl"
    };
    static final String[] WriterSpiNames = {};
    private static final boolean SupportsStandardStreamMetadataFormat = false;
    private static final String NativeStreamMetadataFormatName = null;
    private static final String NativeStreamMetadataFormatClassName = null;
    private static final String[] ExtraStreamMetadataFormatNames = null;
    private static final String[] ExtraStreamMetadataFormatClassNames = null;
    private static final boolean SupportsStandardImageMetadataFormat = false;
    private static final String NativeImageMetadataFormatName = "jpeg-xl";
    private static final String NativeImageMetadataFormatClassName = null;
    private static final String[] ExtraImageMetadataFormatNames = null;
    private static final String[] ExtraImageMetadataFormatClassNames = null;

    /** */
    public JpegXLImageReaderSpi() {
        super(VendorName,
              Version,
              Names,
              Suffixes,
              mimeTypes,
              ReaderClassName,
              new Class[] { ImageInputStream.class },
              WriterSpiNames,
              SupportsStandardStreamMetadataFormat,
              NativeStreamMetadataFormatName,
              NativeStreamMetadataFormatClassName,
              ExtraStreamMetadataFormatNames,
              ExtraStreamMetadataFormatClassNames,
              SupportsStandardImageMetadataFormat,
              NativeImageMetadataFormatName,
              NativeImageMetadataFormatClassName,
              ExtraImageMetadataFormatNames,
              ExtraImageMetadataFormatClassNames);
    }

    @Override
    public String getDescription(Locale locale) {
        return "Jpeg XL Image";
    }

    @Override
    public boolean canDecodeInput(Object obj) throws IOException {
Debug.println(Level.FINE, "input: " + obj);
        if (obj instanceof ImageInputStream) {
            ImageInputStream stream = (ImageInputStream) obj;
            stream.mark();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            while (true) {
                int r = stream.read(b, 0, b.length);
                if (r < 0) break;
                baos.write(b, 0, r);
            }
            int l = baos.size();
Debug.println(Level.FINE, "size: " + l);
            stream.reset();
            return JpegXL.canDecode(baos.toByteArray());
        } else {
            return false;
        }
    }

    @Override
    public ImageReader createReaderInstance(Object obj) {
        return new JpegXLImageReader(this);
    }
}
