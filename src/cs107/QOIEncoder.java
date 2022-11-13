package cs107;

import java.util.ArrayList;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image){
        assert image != null;
        assert image.channels() == QOISpecification.RGB || image.channels() == QOISpecification.RGBA;
        assert image.color_space() == QOISpecification.sRGB || image.color_space() == QOISpecification.ALL;
        
        byte[][] header = new byte[5][];

        header[0] = QOISpecification.QOI_MAGIC;
        header[1] = ArrayUtils.fromInt(image.data()[0].length);
        header[2] = ArrayUtils.fromInt(image.data().length);
        header[3] = ArrayUtils.wrap(image.channels());
        header[4] = ArrayUtils.wrap(image.color_space());

        return(ArrayUtils.concat(header[0], header[1], header[2], header[3], header[4]));
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel){
        assert pixel.length ==4;

        byte[] encoding = new byte[4];
        
        encoding[0] = QOISpecification.QOI_OP_RGB_TAG;
        encoding[1] = pixel[QOISpecification.r];
        encoding[2] = pixel[QOISpecification.g];
        encoding[3] = pixel[QOISpecification.b];
        
        return encoding;
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel){
        assert pixel.length ==4;

        byte[] encoding = new byte[5];
        
        encoding[0] = QOISpecification.QOI_OP_RGBA_TAG;
        encoding[1] = pixel[QOISpecification.r];
        encoding[2] = pixel[QOISpecification.g];
        encoding[3] = pixel[QOISpecification.b];
        encoding[4] = pixel[QOISpecification.a];
        
        return encoding;
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){        
        assert index >= 0 && index <= 63;
        
        byte tag = (byte) (QOISpecification.QOI_OP_INDEX_TAG); 
        byte encoding = (byte) (index | tag);
        
        return ArrayUtils.wrap(encoding);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){
        assert diff != null && diff.length == 3;
        for (byte elem : diff){
            assert elem > -3 && elem < 2;
        }

        final byte decal = 0b00_00_00_10;
        byte tag = (byte) (QOISpecification.QOI_OP_DIFF_TAG);
        byte encoding = (byte) (((diff[2]+decal) | (diff[1]+decal << 2) | (diff[0]+decal << 4)) | (tag));

        return ArrayUtils.wrap(encoding);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){
        assert diff != null && diff.length == 3;
        assert diff[1] > -33 && diff[1] < 32; 
        assert (diff[0] - diff[1]) > -9 && (diff[0] - diff[1]) < 8;
        assert (diff[2] - diff[1]) > -9 && (diff[2] - diff[1]) < 8;

        final byte decal_g = 0b00_10_00_00;
        final byte decal = 0b00_00_10_00;

        byte tag = (byte) (QOISpecification.QOI_OP_LUMA_TAG);
        byte encoding[] = new byte[2];
        
        encoding[0] = (byte) ((diff[1]+decal_g) | (tag));
        encoding[1] = (byte) ((((diff[0] - diff[1]) + decal) << 4) | ((diff[2] - diff[1]) + decal));

        return encoding;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){
        assert count > 0 && count < 63;

        final byte decal = 0b00_00_00_01;

        byte tag = (byte) (QOISpecification.QOI_OP_RUN_TAG);
        byte encoding = (byte) ((count-decal) | tag);

        return ArrayUtils.wrap(encoding);
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image){
        assert image != null;
        for(byte[] pixel : image){
            assert pixel != null && pixel.length == 4;
        }

        byte[] prev_pixel = QOISpecification.START_PIXEL;
        byte[][] hash_table = new byte[64][4];
        int count = 0;

        final int a = QOISpecification.a;
        final int r = QOISpecification.r;
        final int g = QOISpecification.g;
        final int b = QOISpecification.b;

        ArrayList<Byte[]> encoding = new ArrayList<Byte[]>();
        
        for (int i = 0; i < image.length; i++){
            byte[] pixel = image[i];
            
            if(ArrayUtils.equals(pixel, prev_pixel)){ //*Etape 1
                count++;
                if(count >= 62 || i == image.length-1){ //<62 ?
                    encoding.add(ArrayUtils.cast(qoiOpRun((byte) count)));
                    prev_pixel = pixel;
                    count = 0;
                    continue;
                }
            }else{
                if(count != 0){ //<62 ?
                    encoding.add(ArrayUtils.cast(qoiOpRun((byte) count)));
                    count = 0;
                }
                if(ArrayUtils.equals(pixel, hash_table[QOISpecification.hash(pixel)])){ //*Etape 2
                    encoding.add(ArrayUtils.cast(qoiOpIndex(QOISpecification.hash(pixel))));
                    prev_pixel = pixel;
                    continue;
                }else{
                    hash_table[QOISpecification.hash(pixel)] = pixel;
                    if(pixel[a] == prev_pixel[a]){ //*Etape 3, 4, 5

                        byte[] diff = new byte[]{(byte) (pixel[r] - prev_pixel[r]), (byte) (pixel[g] - prev_pixel[g]), (byte) (pixel[b] - prev_pixel[b])};

                        if(diff[0] > -3 && diff[0] < 2 && diff[1] > -3 && diff[1] < 2 && diff[2] > -3 && diff[2] < 2){ //*Etape 3
                            encoding.add(ArrayUtils.cast(qoiOpDiff(diff)));
                            prev_pixel = pixel;
                            continue;
                        }else if((diff[1] > -33 && diff[1] < 32) && ((diff[0] - diff[1]) > -9 && (diff[0] - diff[1]) < 8) && ((diff[2] - diff[1]) > -9 && (diff[2] - diff[1]) < 8)){ //*Etape 4
                            encoding.add(ArrayUtils.cast(qoiOpLuma(diff)));
                            prev_pixel = pixel;
                            continue;
                        }else{ //*Etape 5
                            encoding.add(ArrayUtils.cast(qoiOpRGB(pixel)));
                            prev_pixel = pixel;
                            continue;
                        }
                    }else{ //*Etape 6
                        encoding.add(ArrayUtils.cast(qoiOpRGBA(pixel)));
                        prev_pixel = pixel;
                        continue;
                        }
                    }
                }
        }

        byte[][] result = new byte[encoding.size()][];
        for (int i = 0; i < encoding.size(); i++){
            result[i] = ArrayUtils.cast(encoding.get(i));
        }

        return ArrayUtils.concat(result);
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image){
        assert image != null;

        byte[] header = qoiHeader(image);

        byte[] data = encodeData(ArrayUtils.imageToChannels(image.data()));
        
        byte[] signature = QOISpecification.QOI_EOF;

        return ArrayUtils.concat(header,data,signature);
    }

}