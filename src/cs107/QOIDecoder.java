package cs107;

import static cs107.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.3
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder(){}

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header){
        assert header != null;
        assert header.length == QOISpecification.HEADER_SIZE;
        assert ArrayUtils.equals(ArrayUtils.extract(header, 0, 4), QOISpecification.QOI_MAGIC);
        assert header[12] == QOISpecification.RGB || header[12] == QOISpecification.RGBA;
        assert header[13] == QOISpecification.sRGB || header[13] == QOISpecification.ALL;

        int[] decoded = new int[]{
                ArrayUtils.toInt(ArrayUtils.extract(header, 4, 4)),
                ArrayUtils.toInt(ArrayUtils.extract(header, 8, 4)),
                header[12],
                header[13]
        };

        return decoded;
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param alpha (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx){
        assert buffer != null && input != null;
        assert position >= 0 && position < buffer.length;
        assert idx >= 0 && idx < input.length;
        assert ArrayUtils.extract(input, idx, input.length-idx).length >= 3;

        byte[] tab = ArrayUtils.extract(input, idx, 3);
        buffer[position] = ArrayUtils.concat(tab, ArrayUtils.wrap(alpha));

        return QOISpecification.RGB;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx){
        decodeQoiOpRGB(buffer, input, input[idx+3], position, idx);

        return QOISpecification.RGBA;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk){
        assert previousPixel != null && previousPixel.length == 4;
        assert (byte) (chunk >>> 6) == (byte) (QOISpecification.QOI_OP_DIFF_TAG >>> 6);

        final byte decal = 0b00_00_00_10;

        byte[] diff = new byte[4];
        diff[2] = (byte) ((chunk & 0b00_00_00_11) - decal);
        diff[1] = (byte) (byte) (((chunk & 0b00_00_11_00) >> 2) - decal);
        diff[0] = (byte) (((chunk & 0b00_11_00_00) >> 4) - decal);
        
        byte[] currentPixel = new byte[4];
        for(int i=0 ; i<previousPixel.length ; ++i){
            currentPixel[i] = (byte) (previousPixel[i] + diff[i]);
        }

        return currentPixel;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * @param previousPixel (byte[]) - The previous pixel
     * @param data (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data){
        assert previousPixel != null && data != null;
        assert previousPixel.length == 4;
        assert (byte) (data[0] >>> 6) == (byte) (QOISpecification.QOI_OP_LUMA_TAG >>> 6);

        final byte decal_g = 0b00_10_00_00;
        final byte decal = 0b00_00_10_00;

        byte[] diff = new byte[4];
        diff[1] = (byte) ((data[0] & 0b00_11_11_11) - decal_g);
        diff[0] = (byte) ((((data[1] & 0b11_11_00_00) >> 4) - decal) + diff[1]);
        diff[2] = (byte) (((data[1] & 0b00_00_11_11) - decal) + diff[1]);

        byte[] currentPixel = new byte[4];
        for(int i=0 ; i<previousPixel.length ; ++i){
            currentPixel[i] = (byte) (previousPixel[i] + diff[i]);
        }

        return currentPixel;
    }

    /**
     * Store the given pixel in the buffer multiple times
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param pixel (byte[]) - The pixel to store
     * @param chunk (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position){
        assert buffer != null && pixel != null;
        assert position >= 0 && position <= buffer.length;
        assert pixel.length == 4;
        assert buffer[0].length >= pixel.length;

        int rep = chunk & 0b00_11_11_11; 

        for(int i=0 ; i<=rep ; ++i){
            buffer[position + i] = pixel;
        }

        return rep;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     * @param data (byte[]) - Data to decode
     * @param width (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height){
        assert data != null;
        assert width > 0 && height > 0;
        //assert

        //* Etape 1

        byte[] previousPixel = QOISpecification.START_PIXEL;
        int position = 0;
        int positionHash;
        byte[][] hashTable = new byte[64][4];
        byte[][] buffer = new byte[width*height][4]; //output

        //* Etape 2

        for(int idx=0 ; idx<data.length ;){

            byte tag = (byte) (data[idx] & 0b11_00_00_00);

            switch(tag){

                case QOISpecification.QOI_OP_INDEX_TAG -> {
                    positionHash = data[idx] & 0b00_11_11_11;
                    byte[] pixel = hashTable[positionHash]; 

                    buffer[position] = pixel;
                    previousPixel = pixel;

                    idx++;
                    break;
                }

                case QOISpecification.QOI_OP_DIFF_TAG -> {
                    byte chunk = data[idx];
                    previousPixel = decodeQoiOpDiff(previousPixel, chunk);

                    buffer[position] = previousPixel;
                    hashTable[QOISpecification.hash(previousPixel)] = previousPixel;

                    idx++;
                    break;
                }

                case QOISpecification.QOI_OP_LUMA_TAG -> {
                    byte[] chunk = new byte[]{data[idx], data[idx+1]};
                    previousPixel = decodeQoiOpLuma(previousPixel, chunk);

                    buffer[position] = previousPixel;
                    hashTable[QOISpecification.hash(previousPixel)] = previousPixel;
        
                    idx += 2;
                    break;
                }

                default -> {

                    tag = (byte) (data[idx]);

                    switch(tag){

                        case QOISpecification.QOI_OP_RGB_TAG -> {
                            idx += decodeQoiOpRGB(buffer, data, (byte) previousPixel[QOISpecification.a], position, idx+1);

                            previousPixel = buffer[position];
                            hashTable[QOISpecification.hash(previousPixel)] = previousPixel;

                            idx++;
                            break;
                        }

                        case QOISpecification.QOI_OP_RGBA_TAG -> {
                            idx += decodeQoiOpRGBA(buffer, data, position, idx+1);

                            previousPixel = buffer[position];
                            hashTable[QOISpecification.hash(previousPixel)] = previousPixel;

                            idx++;
                            break;
                        }

                        default -> {
                            byte chunk = (byte) (data[idx] & 0b00_11_11_11);
                            int rep = decodeQoiOpRun(buffer, previousPixel, chunk, position);
        
                            position += rep;
                            previousPixel = buffer[position];
                        
                            idx++;
                            break;
                        }
                    }
                }
            }

            position++;
        }

        return buffer;
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content){
        assert content != null;
        assert ArrayUtils.equals(ArrayUtils.extract(content, content.length-QOISpecification.QOI_EOF.length, QOISpecification.QOI_EOF.length), QOISpecification.QOI_EOF);

        int[] header = decodeHeader(ArrayUtils.extract(content, 0, QOISpecification.HEADER_SIZE));
        int width = header[0];
        int height = header[1];

        byte[][] data = decodeData(ArrayUtils.extract(content, QOISpecification.HEADER_SIZE, content.length-QOISpecification.HEADER_SIZE-QOISpecification.QOI_EOF.length), width, height);
        int[][] image_data = ArrayUtils.channelsToImage(data, height, width);

        return Helper.generateImage(image_data, (byte) header[2], (byte) header[3]);
    }

}