/*
 * Copyright (c) 2022 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.imageio.jpegxl.jni;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

import org.jpeg.jpegxl.wrapper.Decoder;
import org.jpeg.jpegxl.wrapper.ImageData;
import org.jpeg.jpegxl.wrapper.PixelFormat;
import vavi.imageio.WrappedImageInputStream;

import static java.lang.System.getLogger;


/**
 * JpegXLImageReader.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2022-10-06 umjammer initial version <br>
 */
public class JpegXLImageReader extends ImageReader {

    private static final Logger logger = getLogger(JpegXLImageReader.class.getName());

    /** */
    private BufferedImage image;

    /** */
    public JpegXLImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IIOException {
        return 1;
    }

    /** */
    private void checkIndex(int imageIndex) {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException("bad index");
        }
    }

    @Override
    public int getWidth(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        return image.getWidth();
    }

    @Override
    public int getHeight(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        return image.getHeight();
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param)
        throws IIOException {

logger.log(Level.DEBUG, "decode start");
long t = System.currentTimeMillis();
        InputStream stream = new WrappedImageInputStream((ImageInputStream) input);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            while (true) {
                int r = stream.read(b, 0, b.length);
                if (r < 0) break;
                baos.write(b, 0, r);
            }
            int l = baos.size();
logger.log(Level.DEBUG, "size: " + l);
            ByteBuffer bb = ByteBuffer.allocateDirect(l);
            bb.put(baos.toByteArray(), 0, l);

            ImageData imageData = Decoder.decode(bb, PixelFormat.RGBA_8888);
logger.log(Level.DEBUG, "image: " + imageData.width + "x" + imageData.height);
logger.log(Level.DEBUG, "decoded: " + imageData.pixels.capacity());

            image = new BufferedImage(imageData.width, imageData.height, BufferedImage.TYPE_4BYTE_ABGR);
            ByteBuffer decoded = (ByteBuffer) imageData.pixels;
            byte[] raster = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
logger.log(Level.DEBUG, "raster: " + raster.length);
            int p = 0;
            for (int y = 0; y < imageData.height; y++) {
                for (int x = 0; x < imageData.width; x++) {
                    raster[p + 3] = decoded.get();
                    raster[p + 2] = decoded.get();
                    raster[p + 1] = decoded.get();
                    raster[p + 0] = decoded.get();
                    p += 4;
                }
            }

            return image;
        } catch (IOException e) {
            throw new IIOException(e.getMessage(), e);
} finally {
logger.log(Level.DEBUG, "time: " + (System.currentTimeMillis() - t));
        }
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IIOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        return null;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IIOException {
        checkIndex(imageIndex);
        ImageTypeSpecifier specifier = new ImageTypeSpecifier(image);
        List<ImageTypeSpecifier> l = new ArrayList<>();
        l.add(specifier);
        return l.iterator();
    }
}
