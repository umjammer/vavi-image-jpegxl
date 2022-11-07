// Copyright (c) the JPEG XL Project Authors. All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package org.jpeg.jpegxl.wrapper;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JNI test.
 */
@PropsEntity(url = "file:local.properties")
public class DecoderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "file")
    String file = "src/test/resources/test.jxl";

    @BeforeEach
    void setup() throws IOException {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    private static final int SIMPLE_IMAGE_DIM = 1024;
    // Base64: "/wr6H0GRCAYBAGAASzgkunkeVbaSBu95EXDn0e7ABz2ShAMA"
    private static final byte[] SIMPLE_IMAGE_BYTES = {-1, 10, -6, 31, 65, -111, 8, 6, 1, 0, 96, 0, 75,
            56, 36, -70, 121, 30, 85, -74, -110, 6, -17, 121, 17, 112, -25, -47, -18, -64, 7, 61, -110,
            -124, 3, 0};

    private static final int PIXEL_IMAGE_DIM = 1;
    // Base64: "/woAELASCBAQABwASxLFgoUkDA=="
    private static final byte[] PIXEL_IMAGE_BYTES = {
            -1, 10, 0, 16, -80, 18, 8, 16, 16, 0, 28, 0, 75, 18, -59, -126, -123, 36, 12};

    static ByteBuffer makeByteBuffer(byte[] src, int length) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(length);
        buffer.put(src, 0, length);
        return buffer;
    }

    static ByteBuffer makeSimpleImage() {
        return makeByteBuffer(SIMPLE_IMAGE_BYTES, SIMPLE_IMAGE_BYTES.length);
    }

    static void checkSimpleImageData(ImageData imageData) {
        assertEquals(imageData.width, SIMPLE_IMAGE_DIM, "invalid width");
        assertEquals(imageData.height, SIMPLE_IMAGE_DIM, "invalid height");
        int iccSize = imageData.icc.capacity();
        // Do not expect ICC profile to be some exact size; currently it is 732
        assertTrue(iccSize >= 300 && iccSize <= 1000, "unexpected ICC profile size");
    }

    static void checkPixelFormat(PixelFormat pixelFormat, int bytesPerPixel) {
        ImageData imageData = Decoder.decode(makeSimpleImage(), pixelFormat);
        checkSimpleImageData(imageData);
        assertEquals(imageData.pixels.limit(), SIMPLE_IMAGE_DIM * SIMPLE_IMAGE_DIM * bytesPerPixel, "Unexpected pixels size");
    }

    static void testRgba() {
        checkPixelFormat(PixelFormat.RGBA_8888, 4);
    }

    static void testRgbaF16() {
        checkPixelFormat(PixelFormat.RGBA_F16, 8);
    }

    static void testRgb() {
        checkPixelFormat(PixelFormat.RGB_888, 3);
    }

    static void testRgbF16() {
        checkPixelFormat(PixelFormat.RGB_F16, 6);
    }

    static void checkGetInfo(ByteBuffer data, int dim, int alphaBits) {
        StreamInfo streamInfo = Decoder.decodeInfo(data);
        assertSame(streamInfo.status, Status.OK, "Unexpected decoding error");
        assertTrue(streamInfo.width == dim && streamInfo.height == dim, "Invalid width / height");
        assertEquals(streamInfo.alphaBits, alphaBits, "Invalid alphaBits");
    }

    static void testGetInfoNoAlpha() {
        checkGetInfo(makeSimpleImage(), SIMPLE_IMAGE_DIM, 0);
    }

    static void testGetInfoAlpha() {
        checkGetInfo(makeByteBuffer(PIXEL_IMAGE_BYTES, PIXEL_IMAGE_BYTES.length), PIXEL_IMAGE_DIM, 8);
    }

    static void testNotEnoughInput() {
        for (int i = 0; i < 6; ++i) {
            ByteBuffer jxlData = makeByteBuffer(SIMPLE_IMAGE_BYTES, i);
            StreamInfo streamInfo = Decoder.decodeInfo(jxlData);
            assertSame(streamInfo.status, Status.NOT_ENOUGH_INPUT,
                    "Expected 'not enough input', but got " + streamInfo.status + " " + i);
        }
    }

    // Simple executable to avoid extra dependencies.
    @Test
    void testBulk() {
        testRgba();
        testRgbaF16();
        testRgb();
        testRgbF16();
        testGetInfoNoAlpha();
        testGetInfoAlpha();
        testNotEnoughInput();
    }

    /** direct */
    private BufferedImage getImage() throws Exception {
        byte[] jxl = Files.readAllBytes(Paths.get(file));
        ByteBuffer bb = ByteBuffer.allocateDirect(jxl.length);
        bb.put(jxl);
        ImageData imageData = Decoder.decode(bb, PixelFormat.RGBA_8888);
Debug.println("image: " + imageData.width + "x" + imageData.height);
Debug.println("decoded: " + imageData.pixels.capacity());

        BufferedImage image = new BufferedImage(imageData.width, imageData.height, BufferedImage.TYPE_4BYTE_ABGR);
        ByteBuffer decoded = (ByteBuffer) imageData.pixels;
        byte[] raster = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
Debug.println("raster: " + raster.length);
//        decoded.get(raster);
        // TODO i don't wanna do this.
        //  but using JXL_BIG_ENDIAN at decoder_jni.cc#ToPixelFormat doesn't work (nothing changes)
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
    }

    /** direct gui */
    public static void main(String[] args) throws Exception {
        DecoderTest app = new DecoderTest();
        app.setup();
        app.show(app.getImage());
    }

    /** gui */
    private void show(BufferedImage image) {
        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            public void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, this);
            }
        };
        panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        frame.setContentPane(new JScrollPane(panel));
        frame.setTitle("JPEG XL (JNI)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        while (true) Thread.yield();
    }

    @Test
    @DisplayName("direct")
    void test1() throws Exception {
        BufferedImage image = getImage();
        assertNotNull(image);
    }

    @Test
    @Disabled("currently jna version is used")
    @DisplayName("spi specified")
    void test02() throws Exception {
        ImageReader ir = ImageIO.getImageReadersByFormatName("jpegxl").next();
        ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(Paths.get(file)));
        ir.setInput(iis);
        BufferedImage image = ir.read(0);
        assertNotNull(image);
    }

    @Test
    @Disabled("currently jna version is used")
    @DisplayName("spi auto")
    void test03() throws Exception {
        BufferedImage image = ImageIO.read(new File(file));
        assertNotNull(image);
    }

    @Test
    @Disabled("currently jna version is used")
    @DisplayName("spi specified gui")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        ImageReader ir = ImageIO.getImageReadersByFormatName("jpegxl").next();
        ImageInputStream iis = ImageIO.createImageInputStream(Files.newInputStream(Paths.get(file)));
        ir.setInput(iis);
        BufferedImage image = ir.read(0);

        show(image);
    }

    @Test
    @Disabled("currently jna version is used")
    @DisplayName("spi auto gui")
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test3() throws Exception {
        BufferedImage image = ImageIO.read(new File(file));

        show(image);
    }
}
